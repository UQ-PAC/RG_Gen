from parser import *
from proof import *
from lark import Lark
import sys
from colorama import Fore


def main():
    if len(sys.argv) != 2:
        print('Usage: main.py filename')
        exit(1)

    # Parse test file.
    program = parse_test_file(sys.argv[1])
    specified_precondition = program[0]
    specified_postcondition = program[1]
    global_variables = program[2]
    threads: list[Procedure] = program[3:]

    # Pre-compute necessary CFG-node information.
    # Get the list of global assignments contained in each thread.
    global_assignments: dict[Procedure, list[Assignment]] = \
        init_global_assignments(threads, global_variables)
    # Allocate to each thread the list of environment global assignments.
    init_interfering_assignments(threads, global_assignments)
    # Allocate to each node the thread it belongs to.
    init_owner_thread(threads)
    # Check for duplicated local variables.
    verify_local_vars(threads, global_variables)

    # Perform analysis.
    for t in threads:
        t.regenerate_proof([Node(specified_precondition, None, None)])
    fixpoint_reached = False
    while not fixpoint_reached:
        fixpoint_reached = True
        for t in threads:
            t.regenerate_proof([])
            if not t.fixpoint_reached:
                fixpoint_reached = False

    # Compute program postcondition.
    local_posts = [t.get_post() for t in threads]
    program_post = And(local_posts)

    # Print results.
    for t in threads:
        print()
        print(t.get_proof_str())
    print()
    print('Derived Postcondition: ' + str(simplify(program_post).serialize()))
    print()
    if is_sat(And(program_post, Not(specified_postcondition))):
        print(f'{Fore.RED}Verification Unsuccessful.{Fore.RESET}')
    else:
        print(f'{Fore.GREEN}Verification Successful!{Fore.RESET}')


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


def init_global_assignments(threads: list[Procedure], global_vars):
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
    for t in threads:
        global_assigns = []
        recurse_cfg(t, global_assignments_initialiser)
        thread_to_global_assigns[t] = global_assigns

    return thread_to_global_assigns


def init_interfering_assignments(threads: list[Procedure], global_assigns):
    """
    Attaches to each thread a list of all environment instructions that may
    destabilise one of its assertions. This list happens to be the list of
    all global assignments in the environment.
    """
    for t1 in threads:
        interfering_assigns = []
        for t2, assigns in global_assigns.items():
            if t1 != t2:
                interfering_assigns.extend(assigns)
        t1.interfering_assignments = interfering_assigns


def init_owner_thread(threads: list[Procedure]):
    """
    Provides each statement with the thread it is in.
    """
    def owner_thread_initialiser(node):
        node.thread = t

    for t in threads:
        recurse_cfg(t, owner_thread_initialiser)


def verify_local_vars(threads: list[Procedure], global_vars):
    """
    Provides each thread with a list of its local variables.
    """
    def get_vars_used(node):
        if isinstance(node, Assignment):
            for p in node.pairs:
                vars_used.add(p[0])
                vars_used.update(get_free_variables(p[1]))
        elif not isinstance(node, Eof):
            vars_used.update(get_free_variables(node.cond))

    locals_by_thread = []
    for t in threads:
        vars_used = set()
        recurse_cfg(t, get_vars_used)
        local_vars = vars_used - global_vars
        locals_by_thread.append(local_vars)

    duplicates = set()
    for i in range(len(threads)):
        for j in range(i + 1, len(threads)):
            duplicates.update(locals_by_thread[i] & locals_by_thread[j])

    if duplicates:
        exit(f'Error: Duplicate local variables: {str(duplicates)}.\n'
             f'Local variables must be distinct across threads.')

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
