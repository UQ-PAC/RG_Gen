var x: int;
var z: int;
var r: int;
var new_x: int;
var new_z: int;

function {:existential true} {:inline} P0(x: int, z: int, r: int) : bool;
function {:existential true} {:inline} P1(x: int, z: int, r: int) : bool;
function {:existential true} {:inline} P2(x: int, z: int, r: int) : bool;
function {:existential true} {:inline} P3(x: int, z: int, r: int) : bool;
function {:existential true} {:inline} P4(x: int, z: int, r: int) : bool;

function {:existential true} {:inline} Q0(x: int, z: int) : bool;
function {:existential true} {:inline} Q1(x: int, z: int) : bool;
function {:existential true} {:inline} Q2(x: int, z: int) : bool;
function {:existential true} {:inline} Q3(x: int, z: int) : bool;

function {:existential true} {:inline} G_t1(x: int, z: int, new_x: int, new_z: int) : bool;
function {:existential true} {:inline} G_t2(x: int, z: int, new_x: int, new_z: int) : bool;

procedure pre_condition()
requires true;
{ 
	assert P0(x, z, r);
  	assert Q0(x, z);
}

procedure post_condition()
requires P4(x, z, r);
requires Q3(x, z);
{ 
  	assert (r == 0);
}

procedure t1_transition_s4()
modifies r;
requires P3(x, z, r);
ensures P4(x, z, r);
ensures G_t1(old(x), old(z), x, z);
{ 
	r := x;
}

procedure t1_transition_s3()
modifies x;
requires P2(x, z, r);
ensures P3(x, z, r);
ensures G_t1(old(x), old(z), x, z);
{ 
	x := 0;
}

procedure t1_transition_s2()
requires P1(x, z, r);
{ 
	if(z == 0) {
		assert P2(x, z, r);
	} else {
		assert P4(x, z, r);
	}
}


procedure t1_transition_s1() 
modifies r;
requires P0(x, z, r);
ensures P1(x, z, r);
ensures G_t1(old(x), old(z), x, z);
{ 
	r := 0;
}

procedure t2_transition_s3()
modifies z;
requires Q2(x, z);
ensures Q3(x, z);
ensures G_t2(old(x), old(z), x, z);
{ 
	z := 0;
}

procedure t2_transition_s2()
modifies x;
requires Q1(x, z);
ensures Q2(x, z);
ensures G_t2(old(x), old(z), x, z);
{ 
	x := 1;
}


procedure t2_transition_s1() 
requires Q0(x, z);
{ 
	if(z == 1) {
		assert Q1(x, z);
	} else {
		assert Q3(x, z);
	} 
}

procedure P0_Stable_G_t2()
requires P0(x, z, r);
requires G_t2(x, z, new_x, new_z);
{ 
	assert P0(new_x, new_z, r);
}

procedure P1_Stable_G_t2()
requires P1(x, z, r);
requires G_t2(x, z, new_x, new_z);
{ 
	assert P1(new_x, new_z, r);
}

procedure P2_Stable_G_t2()
requires P2(x, z, r);
requires G_t2(x, z, new_x, new_z);
{ 
	assert P2(new_x, new_z, r);
}

procedure P3_Stable_G_t2()
requires P3(x, z, r);
requires G_t2(x, z, new_x, new_z);
{ 
	assert P3(new_x, new_z, r);
}

procedure P4_Stable_G_t2()
requires P4(x, z, r);
requires G_t2(x, z, new_x, new_z);
{ 
	assert P4(new_x, new_z, r);
}

procedure Q0_Stable_G_t1()
requires Q0(x, z);
requires G_t1(x, z, new_x, new_z);
{ 
	assert Q0(new_x, new_z);
}

procedure Q1_Stable_G_t1()
requires Q1(x, z);
requires G_t1(x, z, new_x, new_z);
{ 
	assert Q1(new_x, new_z);
}

procedure Q2_Stable_G_t1()
requires Q2(x, z);
requires G_t1(x, z, new_x, new_z);
{ 
	assert Q2(new_x, new_z);
}

procedure Q3_Stable_G_t1()
requires Q3(x, z);
requires G_t1(x, z, new_x, new_z);
{ 
	assert Q3(new_x, new_z);
}
