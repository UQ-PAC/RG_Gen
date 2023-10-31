# 0 "../../test-programs/conditional_lock/conditional_lock_t.c"
# 0 "<built-in>"
# 0 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4
# 0 "<command-line>" 2
# 1 "../../test-programs/conditional_lock/conditional_lock_t.c"
int x, z;

thr1() {
    int r = 0;
    if (z == 0) {
        x = 0;
        r = x;
    }
    assert (r == 0);
}

thr2() {
    if (z == 1) {
        x = 1;
        z = 0;
    }
}
