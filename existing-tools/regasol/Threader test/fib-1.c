int n = 1; 
int m = 1; 

void thr1() {
  n = n + m;
  assert(n <= 3 && m <= 3);
}

void thr2() {
  m = n + m;
  assert (n <= 3 && m <= 3);
}
