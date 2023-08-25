/* Horn ICE output
function {:existential true} {:inline} R1(x: int, z: int, new_x: int, new_z: int) : bool
{
  (x - new_x <= 0 && z <= 0 && new_z <= 0 && -1 < x - new_x) || (x - new_x <= 0 && 0 < z) || (0 < x - new_x && 0 < z)
}

function {:existential true} {:inline} R2(x: int, z: int, new_x: int, new_z: int) : bool
{
  z <= -2 || (-2 < z && z - new_z <= 0) || (-2 < z && 0 < z - new_z && 0 < new_z)
}

Prover time = 10.82
Number of prover queries = 6567
Learner time = 123.24
Number of learner queries = 591
Total time: 154.82
Number of positive examples:13
Number of negative examples:31
Number of Horn clauses:554
*/

/* Code 
procedure T1()
	modifies x, z;
{
	var r: int;
T10: 	r := 0;
T11: 	if (z == 0) {
T12:		x := 0;
T13:		r := x;
	}
T14:z := 2;
	assert r == 0;
}

procedure T2()
	requires z == 1;
	modifies x, z;
{
T20: 	x := 1;
T21: 	z := 0;
}
*/

function {:existential true} {:inline} T10(x: int, r: int, z: int) : bool;
function {:existential true} {:inline} T11(x: int, r: int, z: int) : bool;
function {:existential true} {:inline} T12(x: int, r: int, z: int) : bool;
function {:existential true} {:inline} T13(x: int, r: int, z: int) : bool;
function {:existential true} {:inline} T14(x: int, r: int, z: int) : bool;
function {:existential true} {:inline} T20(x: int, z: int) : bool;
function {:existential true} {:inline} T21(x: int, z: int) : bool;
function {:existential true} {:inline} R1(x: int, z: int, new_x:int, new_z: int) : bool;
function {:existential true} {:inline} R2(x: int, z: int, new_x:int, new_z: int) : bool;

var x: int;
var r: int;
var z: int;
var new_x:int;
var new_z: int;

procedure pre_condition()
	requires z == 1;
{ 
	
	assert T10(x, r, z);
	assert T20(x, z);
}

procedure t1_transition_T10()
	modifies r;
	requires T10(x, r, z);
	ensures T11(x, r, z);
	ensures R2(old(x), old(z) ,x, z);
{ 
	r := 0;
}

procedure t1_transition_T11()
	requires T11(x, r, z);
{ 
	if (z == 0) {
		assert T12(x, r, z);
	} else {
		assert T14(x, r, z);
	} 
}

procedure t1_transition_T12()
	modifies x;
	requires T12(x, r, z);
	ensures T13(x, r, z);
	ensures R2(old(x), old(z) ,x, z);
{ 
	x := 0;
}

procedure t1_transition_T13()
	modifies r;
	requires T13(x, r, z);
	ensures T14(x, r, z);
	ensures R2(old(x), old(z) ,x, z);
{ 
	r := x;
}

procedure t1_transition_T14()
	modifies z;
	requires T14(x, r, z);
	ensures r == 0; // assertion
	ensures R2(old(x), old(z), x, z);
{
	z := 2;
}

procedure t2_transition_T20()
	modifies x;
	requires T20(x, z);
	ensures T21(x, z);
	ensures R1(old(x), old(z), x, z);
{ 
	x := 1;
}

procedure t2_transition_T21()
	modifies z;
	requires T21(x, z);
	ensures R1(old(x), old(z), x, z);
{ 
	z := 0;
}

procedure T10_Stable_R1()
	requires T10(x, r, z);
	requires R1(x, z, new_x, new_z);
{ 
	assert T10(new_x, r, new_z);
}

procedure T11_Stable_R1()
	requires T11(x, r, z);
	requires R1(x, z, new_x, new_z);
{ 
	assert T11(new_x, r, new_z);
}

procedure T12_Stable_R1()
	requires T12(x, r, z);
	requires R1(x, z, new_x, new_z);
{ 
	assert T12(new_x, r, new_z);
}

procedure T13_Stable_R1()
	requires T13(x, r, z);
	requires R1(x, z, new_x, new_z);
{ 
	assert T13(new_x, r, new_z);
}

procedure T14_Stable_R1()
	requires T14(x, r, z);
	requires R1(x, z, new_x, new_z);
{
	assert T14(new_x, r, new_z);
}

procedure T20_Stable_R2()
	requires T20(x, z);
	requires R2(x, z, new_x, new_z);
{ 
	assert T20(new_x, new_z);
}

procedure T21_Stable_R2()
	requires T21(x, z);
	requires R2(x, z, new_x, new_z);
{ 
	assert T21(new_x, new_z);
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
	modifies z, x;
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