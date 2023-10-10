# About This Directory

This directory contains the source code and executables of existing tools for
verifying concurrent programs. This file contains some maybe-helpful usage
information.

## ReGaSol

### Usage and Project Structure

To run on Mac, I've included the extra file libz3java.dylib. If you experience
an UnsatisfiedLinkError on Windows, you may need to install the associated .dll
file. To run a test, you can execute the .jar file, e.g. `java -jar regasol.jar
test/fib1.test`. However, the authors include the script `regasol.sh` if you
need to add the current directory to `LD_LIBRARY_PATH` first. The tests in
`test/` are from the original implementation, and this list is likely to be
modified in the near future. The tests in `Threader test` are also part of the
original implementation and are likely to be removed when the Threader tool is
added to the existing-tools directory.

### Notes on the Implementation

I'm currently investigating some *interesting* aspects of ReGaSol's
implementation:
- The variable list `variables` that appears at the top of test files seems to
be redundant. These variables are added to an array upon parsing, but so far
I've been unable to find where they are used. Adding or removing variables from
this list does not appear to have any effect on the tool's output.
- There are tokens in the parser that aren't used in test files, and it is
unclear what they do. For example, `"var"`, `"constant"`, `"stm"`, `"error"`.
- It seems from the implementation that local variables are not distinguished
from global variables, however the paper is not clear about this point.
- I've added a test called `failure.test` demonstrating that ReGaSol has very
poor completeness when the precondition does not constrain all program
variables. I believe this is an unmentioned quirk of the algorithm itself, but
it may also be an implementation error.