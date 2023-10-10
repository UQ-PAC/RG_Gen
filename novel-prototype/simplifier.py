from pysmt.fnode import *
from pysmt.shortcuts import *

"""
Simplifier for pySMT formulae.
"""


def simplify_formula(formula: FNode):
    if is_valid(formula):
        return TRUE()
    dnf = to_dnf(formula)
    disjuncts = split_disjuncts(dnf)
    i = 0
    while i < len(disjuncts):
        if not is_sat(disjuncts[i]):
            disjuncts.pop(i)
            continue
        conjuncts = split_conjuncts(disjuncts[i])
        simplify_conjuncts(conjuncts)
        disjuncts[i] = And(conjuncts)
        i += 1
    simplify_disjuncts(disjuncts)
    return Or(disjuncts)


def simplify_conjuncts(conjuncts: list[FNode]):
    i = 0
    while i < len(conjuncts):
        remainder = conjuncts[0:i] + conjuncts[i+1:]
        if is_valid(Implies(And(remainder), conjuncts[i])):
            conjuncts.pop(i)
            continue
        i += 1


def simplify_disjuncts(disjuncts: list[FNode]):
    i = 0
    while i < len(disjuncts):
        remainder = disjuncts[0:i] + disjuncts[i+1:]
        if is_valid(Implies(disjuncts[i], Or(remainder))):
            disjuncts.pop(i)
            continue
        i += 1


def split_disjuncts(formula):
    if formula.is_or():
        disjuncts = []
        for arg in formula.args():
            disjuncts += split_disjuncts(arg)
        return disjuncts
    if formula.is_not() or formula.is_and() or not formula.is_bool_op():
        return [formula]
    exit(f"Error: Unexpected operator '{type(formula)}' in predicate.")


def split_conjuncts(formula):
    if formula.is_and():
        conjuncts = []
        for arg in formula.args():
            conjuncts += split_conjuncts(arg)
        return conjuncts
    if formula.is_not() or not formula.is_bool_op():
        return [formula]
    exit(f"Error: Unexpected operator '{type(formula)}' in predicate.")


def to_dnf(formula: FNode):
    f = simplify_out_implication(formula)
    f = simplify_negation(f)
    fixpoint = False
    while not fixpoint:
        f, fixpoint = distribute_over_conjunction(f)
    return f


def simplify_out_implication(formula):
    if not formula.is_bool_op():
        return formula
    if formula.is_implies():
        return Or(Not(simplify_out_implication(formula.args()[0])),
                  simplify_out_implication(formula.args()[1]))
    if formula.is_or():
        return Or([simplify_out_implication(arg) for arg in formula.args()])
    if formula.is_and():
        return And([simplify_out_implication(arg) for arg in formula.args()])
    if formula.is_not():
        return Not(simplify_out_implication(formula.args()[0]))
    exit(f"Error: Unexpected operator '{type(formula)}' in predicate.")


def simplify_negation(formula):
    """
    Requires that 'formula' does not contain implications.
    """
    if not formula.is_bool_op():
        return formula
    if formula.is_not():
        body = formula.args()[0]
        if body.is_equals():
            return formula  # pySMT does not support a 'not equals' operator
        if body.is_le():
            return LT(body.args()[1], body.args()[0])
        if body.is_lt():
            return LE(body.args()[1], body.args()[0])
        if body.is_not():
            return simplify_negation(body.args()[0])
        if body.is_or():
            return And([simplify_negation(Not(arg)) for arg in body.args()])
        if body.is_and():
            return Or([simplify_negation(Not(arg)) for arg in body.args()])
        exit(f"Error: Unexpected operator '{type(formula)}' in predicate.")
    if formula.is_or():
        return Or([simplify_negation(arg) for arg in formula.args()])
    if formula.is_and():
        return And([simplify_negation(arg) for arg in formula.args()])
    exit(f"Error: Unexpected operator '{type(formula)}' in predicate.")


def distribute_over_conjunction(formula) -> (FNode, bool):
    """
    Requires that 'formula' is in negation normal form.
    """
    if formula.is_not() or not formula.is_bool_op():
        return formula, True
    if formula.is_or():
        new_args = []
        fixpoint_reached = True
        for arg in formula.args():
            new_arg, fixpoint = distribute_over_conjunction(arg)
            new_args.append(new_arg)
            fixpoint_reached = fixpoint_reached and fixpoint
        return Or(new_args), fixpoint_reached
    if formula.is_and():
        args = formula.args()
        for i in range(len(args)):
            arg = args[i]
            if arg.is_or():
                conjuncts = list(args[0:i] + args[i+1:])
                distributed = Or([And(conjuncts + [a]) for a in arg.args()])
                return distribute_over_conjunction(distributed)[0], False
        new_args = []
        fixpoint_reached = True
        for arg in args:
            new_arg, fixpoint = distribute_over_conjunction(arg)
            new_args.append(new_arg)
            fixpoint_reached = fixpoint_reached and fixpoint
        return And(new_args), fixpoint_reached
    exit(f"Error: Unexpected operator '{type(formula)}' in predicate.")
