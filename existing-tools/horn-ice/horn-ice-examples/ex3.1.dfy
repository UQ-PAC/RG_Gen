function {:existential true} {:inline} R1(x: int, z: int, new_x: int, new_z: int) : bool
{
  (x - new_x <= 0 && z <= 0 && new_z <= 0 && -1 < x - new_x) || (x - new_x <= 0 && 0 < z) || (0 < x - new_x && 0 < z)
}

function {:existential true} {:inline} R2(x: int, z: int, new_x: int, new_z: int) : bool
{
  z <= -2 || (-2 < z && z - new_z <= 0) || (-2 < z && 0 < z - new_z && 0 < new_z)
}

class C {
  var x: int
  var z: int

  method T1()
    requires z == 1
    modifies this
  {
    var r: int;
    var old_x: int;
    var old_z: int;
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
    old_z := z;
    z := 2;
    assert R2(x, old_z, x, z);
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