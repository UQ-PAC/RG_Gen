int n = 0;
 
void thr1() {
  while (n < 10){n++;}
  assert(n <=21);
}

void thr2() {
  n=20;
  assert(n <=21);
}

