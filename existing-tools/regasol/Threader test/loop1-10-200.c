int n = 0;
 
void thr1() {
  while (n < 100){n++;}
  assert(n <=100);
}

void thr2() {
  n=20;
  assert(n <=100);
}

