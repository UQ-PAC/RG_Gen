int n = 1; 
int m = 1; 

void thr1() {
  n = n + m;
  n = n + m;
  n = n + m;
  n = n + m;
  n = n + m;
  assert(n <= 144 && m <= 144);
}

void thr2() {
  m = n + m;
  m = n + m;
  m = n + m;
  m = n + m;
  m = n + m;
  assert (n <= 144 && m <= 144);
}
