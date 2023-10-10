from pysmt.fnode import *

"""
Custom string conversion for pySMT formulae.
Features minimal parentheses and conventional boolean operators (&&, ||, ==>).
"""

order = {NOT: 0, AND: 1, OR: 2, IMPLIES: 3}
symbols = {NOT: '!', AND: ' && ', OR: ' || ', IMPLIES: ' ==> '}

def to_str(formula: FNode):
    if formula.is_equals():
        return str(formula.args()[0]) + ' == ' + str(formula.args()[1])
    elif formula.is_not():
        arg: FNode = formula.args()[0]
        arg_type = arg.node_type()
        if arg_type in order.keys():
            return symbols[NOT] + '(' + to_str(arg) + ')'
        return symbols[NOT] + to_str(arg)
    elif formula.is_and() or formula.is_or() or formula.is_implies():
        node_type = formula.node_type()
        args = []
        for i in range(len(formula.args())):
            arg: FNode = formula.args()[i]
            arg_type = arg.node_type()
            arg_str = to_str(arg)
            if arg_type in order.keys() and order[arg_type] > order[node_type] \
                    or formula.is_implies() and i == 0 and arg.is_implies():
                arg_str = '(' + arg_str + ')'
            args.append(arg_str)
        return symbols[node_type].join(args)
    else:
        string = str(formula)
        if string[0] == '(' and string[-1] == ')':
            string = string[1:-1]
        return string
