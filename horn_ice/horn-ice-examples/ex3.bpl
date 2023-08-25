/* Horn ICE output
function {:existential true} {:inline} R1(x: int, z: int, x': int, z': int) : bool
{
  (z <= 1 && x + z <= 1 && -1 < z - z' && x - z <= -1) || (z <= 1 && x + z <= 1 && -1 < z - z' && -1 < x - z && x <= -1) 
  || (z <= 1 && x + z <= 1 && -1 < z - z' && -1 < x - z && -1 < x && x <= 0 && x - x' <= 0 && x' <= 0) 
  || (z <= 1 && x + z <= 1 && -1 < z - z' && -1 < x - z && -1 < x && 0 < x) || (z <= 1 && 1 < x + z && -1 < z - z') || 1 < z
}

function {:existential true} {:inline} R2(x: int, z: int, x': int, z': int) : bool
{
  z - z' <= 0
}

Prover time = 33.80
Number of prover queries = 4358
Learner time = 1976.34
Number of learner queries = 1997
Total time: 2085.58
Number of positive examples:20
Number of negative examples:0
Number of Horn clauses:2007
*/

function {:existential true} R1(x: int, z:int, x': int, z': int) : bool;
function {:existential true} R2(x: int, z: int, x': int, z': int) : bool;

var x: int;
var z: int;

procedure T1()
	requires z == 1;
	modifies x, z;
{
	var old_x: int;
	var r: int; 
	call Rely1();
	r := 0;
	call Rely1();
	if (z == 0) {
		call Rely1();
		old_x := x;
		x := 0;
		assert R2(old_x, z, x, z);
		call Rely1();
		r := x;
	}
	call Rely1();
	old_z := z;
	z := 2;
	assert R2(x, old_z, x, z);
	call Rely1();
	assert r == 0;
}

procedure T2()
	requires z == 1;
	modifies x, z;
{
	var old_x: int;
	var old_z: int;
	call Rely2();
	old_x := x;
	x := 1;
	assert R1(old_x, z, x, z);
	call Rely2();
	old_z := z;
	z := 0;
	assert R1(x, old_z, x, z);
} 

procedure Rely1();
	modifies x, z;
	ensures R1(old(x),old(z),x,z);
	
procedure ReflexiveR1()
	ensures R1(old(x),old(z),x,z);
{}

procedure TransitiveR1()
	modifies x, z;
	ensures R1(old(x),old(z),x,z);
{
	call Rely1();
	call Rely1();
}
  
procedure Rely2();
	modifies x, z;
	ensures R2(old(x),old(z),x,z);
	
procedure ReflexiveR2()
	ensures R2(old(x),old(z),x,z);
{}

procedure TransitiveR2()
	modifies x, z;
	ensures R2(old(x),old(z),x,z);
{
	call Rely2();
	call Rely2();
}