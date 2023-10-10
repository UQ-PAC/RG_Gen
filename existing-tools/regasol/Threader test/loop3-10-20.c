int n = 0;
int m = 0;
int k = 0;
 
void thr1() {
  while (n < 10){n++;}
  assert(n = 10);
}

void thr2() {
  while (m < 20){m++;}
  assert(m = 20);
}

void thr3(){
  while (n <= 10 && m <=20){}
  k = m+n;
  assert(k=30);
}

