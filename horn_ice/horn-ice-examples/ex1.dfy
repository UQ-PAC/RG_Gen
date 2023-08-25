class C {
  function {:existential true} R1(x : int, r: int, x': int, r': int) : bool {
    //x <= -1 || (-1 < x && x <= 0 && x - x' <= 0 && x' <= 0 && -1 < r - r' && r - r' <= 0) || (-1 < x && 0 < x)  // generated rely
    x <= -1 || (x == 0 && x'==x && r'==r) || 0 < x  // manual simplification of generated rely
    //x == 0 ==> x' == 0 && r' == r  // ideal rely
  }

  var x: int
  var r: int

  method T1()
    requires x == 0
    modifies this
    ensures r == 0
  {
    Rely1();
    r := x;
    Rely1();
  }

  method T2()
    modifies this
  {
    var old_x:int;
    if (x != 0) {
      old_x := x;
      x := 1;
      assert R1(old_x,r,x,r);
    }
  }

  method Rely1()
    modifies this
    ensures R1(old(x),old(r),x,r)

  method ReflexiveR1()
    ensures R1(old(x),old(r),x,r)
  {}

  method TransitiveR1()
    modifies this 
    ensures R1(old(x),old(r),x,r)
  {
    Rely1();
    Rely1();
  }
  
}