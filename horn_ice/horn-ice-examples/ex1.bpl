/* Horn ICE output
function {:existential true} {:inline} R1(x: int, x': int) : bool
{
  (x <= 1 && x <= -1) || (x <= 1 && -1 < x && x <= 0 && -1 < x' && x' <= 0) || (x <= 1 && -1 < x && 0 < x) || 1 < x
}

function {:existential true} {:inline} R2(x: int, x': int) : bool
{
  (-1 < x - x' && x + x' <= -2) || (-1 < x - x' && -2 < x + x' && x - x' <= 0)
}
*/

function {:existential true} R1(x : int, x': int) : bool;
function {:existential true} R2(x : int, x': int) : bool;

var x: int;
var r: int;

procedure T1()
	requires x == 0;
	modifies r, x;
	ensures r == 0;
{
	call Rely1();
	r := x;
	call Rely1();
}

procedure T2()
	modifies x;
{
	var old_x: int;
	call Rely2();
	if (x != 0) {
		call Rely2();
		old_x := x;
		x := 1;
		assert R1(old_x,x);
	}
} 

procedure Rely1();
	modifies x;
	ensures R1(old(x),x);
	
procedure ReflexiveR1()
	ensures R1(old(x),x);
{}

procedure TransitiveR1()
 	modifies x;
    	ensures R1(old(x),x);
{
	call Rely1();
	call Rely1();
}
  
procedure Rely2();
	modifies x;
	ensures R2(old(x),x);
	
procedure ReflexiveR2()
	ensures R2(old(x),x);
{}

procedure TransitiveR2()
	modifies x;
	ensures R2(old(x),x);
{
	call Rely2();
	call Rely2();
}
