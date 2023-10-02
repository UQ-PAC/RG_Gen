from parser import *
from proof import *
from lark import Lark
import sys
from colorama import Fore
import time
from simplifier import simplify_formula
from printer import to_str


def main():
    if len(sys.argv) != 2:
        print('Usage: main.py filename')
        exit(1)

    precompute_start = time.time()

    # Parse test file.
    program = parse_test_file(sys.argv[1])
    specified_pre = program[0]
    specified_post = program[1]
    global_variables = program[2]
    threads: list[Procedure] = program[3:]
    proof = Proof(specified_pre, specified_post, threads)

    # Pre-compute necessary CFG-node information.
    # Get the list of global assignments contained in each thread.
    global_assignments: dict[Procedure, list[Assignment]] = \
        init_global_assignments(proof, global_variables)
    # Allocate to each thread the list of environment global assignments.
    init_interfering_assignments(proof, global_assignments)
    # Allocate to each node the thread it belongs to.
    init_owner_thread(proof)
    # Initialise local variables.
    init_local_vars(proof, global_variables)
    # Check for local variables in the precondition.
    check_precondition(specified_pre, global_variables)
    # Provide each statement with the set of global assignments from which they
    # are unreachable in the CFG.
    init_unreachable_from(proof, global_variables)

    # Perform analysis.
    start = time.time()
    success = conduct_proof(proof)
    end = time.time()
    # Print results.
    print(f'Precompute time: {start - precompute_start:.2f}s')
    print(f'Analysis time: {end - start:.2f}s')
    for t in threads:
        print('\n' + t.get_proof_str())
    print('\nDerived Postcondition: ' +
          to_str(simplify_formula(proof.generated_post)))
    msg = f'\n{Fore.GREEN}Verification Successful{Fore.RESET}' if success else \
        f'\n{Fore.RED}Verification Unsuccessful{Fore.RESET}'
    print(msg)


def conduct_proof(proof):
    # max_iterations is the maximum number of times a proof can fail???
    while True:
        verified = fixpoint_proof(proof)
        if verified:
            return True
        # Fixpoint proof was cut short due to verification failure.
        problem_nodes = get_problem_post_nodes(proof)
        repeated_assign = None
        for node in problem_nodes:
            repeated_assign = node.find_repeated_assign([])
            if repeated_assign:
                proof.add_auxiliary_variable(repeated_assign)
                proof.clear_preconditions()
                break
        if not repeated_assign:
            return False


def fixpoint_proof(proof: Proof):
    """
    Repeatedly regenerates the proof outline for each thread, as per the
    strongest-proof approach. Stops when the combined postcondition ceases to
    satisfy the specified postcondition.
    Returns true iff a valid proof is found.
    """
    i = 1
    regenerate_proof(proof, i)
    fixpoint = False
    while not fixpoint:
        i += 1
        fixpoint = regenerate_proof(proof, i)
        if is_sat(And(proof.generated_post, Not(proof.specified_post))) and \
                is_valid(Implies(proof.specified_post, proof.generated_post)):
            return False
    return True


def get_problem_post_nodes(proof: Proof):

    problem_states = And(proof.generated_post, Not(proof.specified_post))

    def is_problem_node(node):
        return is_sat(And(node.pred, problem_states))

    problem_post_nodes = set()
    for t in proof.threads:
        for n in t.get_post_nodes():
            if is_problem_node(n):
                problem_post_nodes.add(n)
    return problem_post_nodes


def regenerate_proof(proof: Proof, iteration_number):
    start = time.time()
    print(f'Iteration {iteration_number}...', end='')
    fixpoint = True
    for t in proof.threads:
        t.regenerate_proof([Node(proof.specified_pre, None, None)])
        if not t.fixpoint_reached:
            fixpoint = False
    proof.generated_post = And([t.get_post() for t in proof.threads])
    end = time.time()
    print(f'({end - start:.2f}s)')
    return fixpoint


def get_global_assigns_in_block(block: list[Statement], global_vars):

    def add_if_global_assign(node):
        if isinstance(node, Assignment):
            for lhs_symb in [node.pairs[i][0] for i in range(len(node.pairs))]:
                if lhs_symb in global_vars:
                    global_assigns.add(node)
                    break

    global_assigns = set()
    for n in block:
        add_if_global_assign(n)
        if isinstance(n, Conditional):
            global_assigns |= get_global_assigns_in_block(n.true_block,
                                                          global_vars)
            global_assigns |= get_global_assigns_in_block(n.false_block,
                                                          global_vars)
    return global_assigns


def init_unreachable_from(proof, global_vars):

    def helper(block: list[Statement], reached):
        for node in block:
            node.unreachable_from |= global_assigns - reached
            reached.add(node)
            if isinstance(node, Conditional):
                reached_copy = reached.copy()
                helper(node.true_block, reached)
                helper(node.false_block, reached_copy)
                reached |= reached_copy

    for t in proof.threads:
        global_assigns = get_global_assigns_in_block(t.block, global_vars)
        helper(t.block, set())


def parse_test_file(filename):
    lark = Lark(grammar, parser='lalr', transformer=Transform())
    with open(filename, 'r') as reader:
        return lark.parse(reader.read()).children[0]


def recurse_cfg(node, function):
    """
    Applies the given function to all statements in this CFG, except EOFs.
    This function is used liberally throughout this file to apply particular
    initialisation procedures to all statements in the CFG in this order.
    """
    if isinstance(node, Procedure):
        for stmt in node.block:
            recurse_cfg(stmt, function)
    elif isinstance(node, Conditional):
        function(node)
        for stmt in node.true_block:
            recurse_cfg(stmt, function)
        for stmt in node.false_block:
            recurse_cfg(stmt, function)
    else:
        function(node)


def init_global_assignments(proof, global_vars):
    """
    Returns a dictionary of {thread -> list[Assignment]} that maps each thread
    to a list of its contained global assignments. This is necessary because we
    want to eventually provide each program statement with a list of statements
    which may destabilise their preconditions - that is, a list of environment
    assignments to global variables (see init_interfering_assignments).
    """
    def global_assignments_initialiser(node):
        if isinstance(node, Assignment):
            for lhs_symb in [node.pairs[i][0] for i in range(len(node.pairs))]:
                if lhs_symb in global_vars:
                    global_assigns.append(node)
                    break

    thread_to_global_assigns = {}
    for t in proof.threads:
        global_assigns = []
        recurse_cfg(t, global_assignments_initialiser)
        thread_to_global_assigns[t] = global_assigns

    return thread_to_global_assigns


def init_interfering_assignments(proof, global_assigns):
    """
    Attaches to each thread a list of all environment instructions that may
    destabilise one of its assertions. This list happens to be the list of
    all global assignments in the environment.
    """
    for t1 in proof.threads:
        interfering_assigns = []
        for t2, assigns in global_assigns.items():
            if t1 != t2:
                interfering_assigns.extend(assigns)
        t1.interfering_assignments = interfering_assigns


def init_owner_thread(proof):
    """
    Provides each statement with the thread it is in.
    """
    def owner_thread_initialiser(node):
        node.thread = t

    for t in proof.threads:
        recurse_cfg(t, owner_thread_initialiser)


def init_local_vars(proof, global_vars):
    """
    Provides each thread with a list of its local variables.
    Additionally, checks for duplicate local variables.
    """
    def get_vars_used(node):
        if isinstance(node, Assignment):
            for p in node.pairs:
                vars_used.add(p[0])
                vars_used.update(get_free_variables(p[1]))
        elif not isinstance(node, Eof):
            vars_used.update(get_free_variables(node.cond))

    threads = proof.threads
    for t in threads:
        vars_used = set()
        recurse_cfg(t, get_vars_used)
        local_vars = vars_used - global_vars
        t.local_vars.extend(local_vars)

    duplicates = []
    for i in range(len(threads)):
        for j in range(i + 1, len(threads)):
            duplicates.extend([v for v in threads[i].local_vars
                               if v in threads[j].local_vars])
    if duplicates:
        dupes_set = frozenset([v for v in duplicates])
        exit(f'Error: Duplicate local variables: {str(dupes_set)}.\n'
             f'Local variables must be distinct across threads.')


def check_precondition(specified_pre, global_vars):
    pre_vars = set(get_free_variables(specified_pre))
    err_vars = pre_vars.difference(global_vars)
    if err_vars:
        exit(f'Error: Local variables in precondition: {str(err_vars)}.\n'
             f'Local variables must not be constrained in the precondition.')

# =========================== Testing ============================

def print_info(threads: list[Procedure]):

    def print_node_info(node):
        if isinstance(node, Conditional):
            print('Conditional:')
        elif isinstance(node, Assertion):
            print('Assertion:')
        elif isinstance(node, Assumption):
            print('Assumption:')
        elif isinstance(node, Assignment):
            print('Assignment:')
        else:
            exit('Unknown Statements')
        print(node.pretty())
        print('PC = ' + str(node.pc))
        interfering_assigns = [i.pretty() for i in
                               node.interfering_assignments]
        print('Interfering Assignments = ' + str(interfering_assigns))
        print()

    for t in threads:
        recurse_cfg(t, print_node_info)


if __name__ == '__main__':
    main()
