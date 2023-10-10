int n = 0;
int m = 0;
int k = 0;
 
void thr1() {
  while (n < 100){n++;}
  assert(n = 100);
}

void thr2() {
  while (m < 200){m++;}
  assert(m = 200);
}

void thr3(){
  while (n <= 100 && m <=200){}
  k = m+n;
  assert(k=300);
}

