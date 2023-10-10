int n = 0;
int m = 0;
 
void thr1() {
  while (n < 100){n++;}
  assert(n = 100);
}

void thr2() {
  while (m < 200){m++;}
  assert(m = 200);
}

