int x=1, m=0;

void thr() {
  int l=1;
  acquire(m); // m=0 /\ m'=1
  if (l>=2) { x=0; }
  else { x=1; }
  assert(x>=1);
  release(m);
}
