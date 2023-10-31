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
