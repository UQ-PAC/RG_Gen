function {:existential true} R1(x : int, x': int) : bool {
  (x <= 1 && x <= -1) || (x <= 1 && -1 < x && x <= 0 && -1 < x' && x' <= 0) || (x <= 1 && -1 < x && 0 < x) || 1 < x
}

function {:existential true} R2(x : int, x': int) : bool {
  (-1 < x - x' && x + x' <= -2) || (-1 < x - x' && -2 < x + x' && x - x' <= 0)
}

class C {
  var x: int

  method T1()
    requires x == 0
    modifies this
  {
    var r: int;
    Rely1();
    r := x;
    Rely1();
    assert r == 0;
  }

  method T2()
    modifies this
  {
    Rely2();
    var old_x: int;
    if (x != 0) {
      Rely2();
      old_x := x;
      x := 1;
      assert R1(old_x,x);
      Rely2();
    }
  }

  method Rely1()
    modifies this
    ensures R1(old(x),x)

  method ReflexiveR1()
    ensures R1(old(x),x)
  {}

  method TransitiveR1()
    modifies this
    ensures R1(old(x),x)
  {
    Rely1();
    Rely1();
  }

  method Rely2()
    modifies this
    ensures R2(old(x),x)

  method ReflexiveR2()
    ensures R2(old(x),x)
  {}

  method TransitiveR2()
    modifies this
    ensures R2(old(x),x)
  {
    Rely2();
    Rely2();
  }
}