from pysmt.shortcuts import *
from typing import List


# Indent for printing proof outlines.
INDENT = 4

class Node:
    def __init__(self, predicate, local_parent, environment_parent):
        self.pred = predicate
        self.local_parent = local_parent
        self.environment_parent = environment_parent

class Statement:
    """
    In this implementation, a procedure contains of a block of statements. All
    nodes in the CFG except Procedure nodes are statements. Statements can be:
    - Conditionals, which contain their own blocks of statements.
    - Assignment statements, which may be local or global.
    - Assume statements (Assumptions).
    - Assert statements (Assertions).
    - The special EOF 'E' statement that is appended to procedures.
    Each statement stores its precondition and a cached postcondition.
    Each statement also contains a function for recomputing its precondition as
    per the strongest-proof approach.

    Preconditions are disjunctions of nodes. Each node represents an assertion
    that is created via a local transition or environment interference. The
    parent of the former is the related node in the precondition of the
    preceding instruction. The latter has two parents: The related destabilised
    node, and the destabilising environment node. The resulting proof resembles
    a set of interlinked trees, with nodes grouped by instruction.
    """
    def __init__(self):
        # The precondition of this statement. Formally, a proof outline is a
        # list of <precondition, instruction> pairs. In this implementation,
        # each precondition is a disjunction of nodes.
        self.nodes = []
        # The thread this statement belongs to.
        self.thread = None

    def regenerate_proof(self, input_nodes):
        """
        Recomputes the precondition of this statement.

        The parameter 'pre' represents the postcondition of the previous
        statement, and represents the tentative precondition of this statement.
        This tentative precondition must be a simplified first-order predicate.

        If this precondition contains states not captured by the current
        precondition, the latter must be updated to include these states. Then,
        if the current precondition is unstable, it must be stabilised via a
        weakening. If either of these steps results in a change to the current
        precondition, the cached postcondition must be updated via a strongest-
        postcondition derivation before it is passed to the next statement.

        For conditionals (and later, loops), the proof for the contained blocks
        must be regenerated before a postcondition can be derived. For
        simplicity, the cached postconditions for these statements are always
        updated, regardless of whether the proof for the inner block was
        changed. This is OK, since the SP transformers for these statements are
        quite simple (e.g. they do not contain quantifiers).
        """
        # Add incoming nodes to the precondition.
        self.nodes += input_nodes
        # List of nodes that have been newly added in this call.
        new_nodes = []
        new_nodes += input_nodes
        # Check if any nodes are unstable under any environment nodes.
        precondition = Or([n.pred for n in self.nodes])
        for assign in self.thread.interfering_assignments:
            for env_node in assign.nodes:
                for local_node in self.nodes:
                    conjunction = And(local_node.pred, env_node.pred)
                    img = assign.sp(conjunction)
                    if is_sat(And(img, Not(precondition))):
                        # local_node is unstable under env_node
                        img_node = Node(img, local_node, env_node)
                        self.nodes.append(img_node)
                        new_nodes.append(img_node)
                        precondition = Or(precondition, img)
                        self.thread.fixpoint_reached = False
        # If the statement is a conditional, update the proofs of its blocks.
        if isinstance(self, Conditional):
            # Regenerate proof for the true-block.
            if_nodes = []
            else_nodes = []
            for n in new_nodes:
                if_nodes.append(Node(And(n.pred, self.cond), self, None))
                else_nodes.append(Node(And(n.pred, Not(self.cond)), self, None))
            for stmt in self.true_block:
                if_nodes = stmt.regenerate_proof(if_nodes)
            for stmt in self.false_block:
                else_nodes = stmt.regenerate_proof(else_nodes)
            output_nodes = if_nodes + else_nodes
        else:
            output_nodes = []
            for node in new_nodes:
                output_nodes.append(Node(self.sp(node.pred), node, None))
        return output_nodes


    def get_proof_str(self):
        proof_str = ''
        disjuncts = [n.pred for n in self.nodes]
        proof_str += '{' + str(Or(disjuncts)) + '}'
        if not isinstance(self, Eof):
            proof_str += '\n' + str(self)
        if isinstance(self, Conditional):
            proof_str += ' {'
            body = ''
            for n in self.true_block:
                body += '\n'
                body += n.get_proof_str()
            body = body.replace('\n', '\n' + ' ' * INDENT)
            proof_str += body
            proof_str += '\n} else {'
            body = ''
            for n in self.false_block:
                body += '\n'
                body += n.get_proof_str()
            body = body.replace('\n', '\n' + ' ' * INDENT)
            proof_str += body
            proof_str += '\n}' if body else '}'
        return proof_str

    def sp(self, pre):
        return pre


class Procedure:
    def __init__(self, name: str, t_id, block: List[Statement]):
        # Human-readable name of this procedure.
        self.name = name
        # This procedure's list of statements.
        self.block = block
        # The special end-of-file 'E' statement, appended for analysis purposes.
        self.block.append(Eof())
        # The program counter symbol of the thread. E.g. 'pc_3'.
        self.pc_symb = Symbol('pc_' + str(t_id), INT)
        # True iff the thread has reached a fixpoint.
        self.fixpoint_reached = False
        # The environment instructions that may interfere with this thread.
        self.interfering_assignments = []

    def get_post(self):
        eof_stmt = self.block[-1]
        return Or([n.pred for n in eof_stmt.nodes])

    def regenerate_proof(self, nodes):
        self.fixpoint_reached = True
        for stmt in self.block:
            nodes = stmt.regenerate_proof(nodes)

    def __str__(self):
        return "procedure " + self.name + "()"

    def get_proof_str(self,):
        body = ''
        for n in self.block:
            body += '\n'
            body += n.get_proof_str()
        body = body.replace('\n', '\n' + ' ' * INDENT)
        proof_str = str(self) + ' {' + body + '\n}'
        return proof_str


class Assignment(Statement):
    def __init__(self, pairs):
        super().__init__()
        self.pairs = pairs  # tuples of (LHS, RHS) values

    def __str__(self):
        lhs = ', '.join([str(p[0]) for p in self.pairs])
        rhs = ', '.join([str(p[1]) for p in self.pairs])
        return lhs + " := " + rhs + ";"

    def sp(self, pre):
        """
        sp(x1..xn := E1..En, P) = exists y1..yn ::
            P[x1 <- y1..xn <- yn] && x1 == E[x1 <- y1] &&..&& xn == E[xn <- yn]
        :param pre: The precondition P.
        :return: The strongest postcondition with respect to P.
        """
        ys = []
        subs = {}
        equalities = []
        for i in range(len(self.pairs)):
            x = self.pairs[i][0]
            y = FreshSymbol(INT)
            ys.append(y)
            subs[x] = y
            equalities.append(Equals(x, self.pairs[i][1].substitute({x: y})))
        sp = Exists(ys, And(pre.substitute(subs), And(equalities)))
        return qelim(sp, 'z3')


class Assumption(Statement):
    def __init__(self, cond):
        super().__init__()
        self.cond = cond

    def __str__(self):
        return "assume " + str(self.cond) + ";"

    def sp(self, pre):
        """
        sp(assume E, P) = P && E
        :param pre: The precondition P.
        :return: The strongest postcondition with respect to P.
        """
        return And(pre, self.cond)


class Assertion(Statement):
    def __init__(self, cond):
        super().__init__()
        self.cond = cond

    def pretty(self):
        return "assert " + str(self.cond) + ";"

    def sp(self, pre):
        """
        sp(assert E, P) = E ==> P
        :param pre: The precondition P.
        :return: The strongest postcondition with respect to P.
        """
        return Implies(self.cond, pre)


class Conditional(Statement):
    def __init__(self, cond, true_block: List[Statement],
                 false_block: List[Statement]):
        super().__init__()
        self.cond = cond
        self.true_block = true_block
        self.false_block = false_block

    def __str__(self):
        return "if (" + str(self.cond) + ")"


class Eof(Statement):
    def __init__(self):
        super().__init__()
