function {:existential true} {:inline} R1(x: int, z: int, x': int, z': int) : bool
{
  (z <= 0 && z - z' <= 0 && z' <= 0 && z <= -1) || (z <= 0 && z - z' <= 0 && z' <= 0 && -1 < z && x <= 0 && x <= -1)
  || (z <= 0 && z - z' <= 0 && z' <= 0 && -1 < z && x <= 0 && -1 < x && -1 < x' && x' <= 0)
  || (z <= 0 && z - z' <= 0 && z' <= 0 && -1 < z && 0 < x) || 0 < z
}

function {:existential true} {:inline} R2(x: int, z: int, x': int, z': int) : bool
{
  (x + z' <= 1 && z - z' <= 0) || (1 < x + z' && z - z' <= 0)
}

class C {
  var x: int
  var z: int

  method T1()
    modifies this
  {
    var old_x: int;
    var r: int;
    Rely1();
    r := 0;
    Rely1();
    if (z == 0) {
      Rely1();
      old_x := x;
      assert R2(old_x, z, x, z);
      x := 0;
      Rely1();
      r := x;
    }
    Rely1();
    assert r == 0;
  }

  method T2()
    requires z == 1
    modifies this
  {
    var old_x: int;
    var old_z: int;
    Rely2();
    old_x := x;
    x := 1;
    assert R1(old_x, z, x, z);
    Rely2();
    old_z := z;
    z := 0;
    assert R1(x, old_z, x, z);
  }

  method Rely1()
    modifies this
    ensures R1(old(x),old(z),x,z)

  method ReflexiveR1()
    ensures R1(old(x),old(z),x,z)
  {}

  method TransitiveR1()
    modifies this
    ensures R1(old(x),old(z),x,z)
  {
    Rely1();
    Rely1();
  }

  method Rely2()
    modifies this
    ensures R2(old(x),old(z),x,z)

  method ReflexiveR2()
    ensures R2(old(x),old(z),x,z)
  {}

  method TransitiveR2()
    modifies this
    ensures R2(old(x),old(z),x,z)
  {
    Rely2();
    Rely2();
  }

}