:- multifile r/5,implicit_updates/0,var2names/2,preds/2,trans_preds/3,
cube_size/1,start/1,error/1,refinement/1,cutpoint/1,invgen_template/2,
 invgen_template/1,cfg_exit_relation/1,stmtsrc/2,strengthening/2,globals/3,
 bound_var/2,bounding_vars/2,transition_access/2,atomic/3.
refinement(inter).
cube_size(1).
preds(p(_, data(V2, V3, V1)), []).
trans_preds(p(_, data(V2, V3, V1)), p(_, data(V2p, V3p, V1p)), []).
var2names(p(pc(_), data(V2, V3, V1)), [(V2, 'z'), (V3, 'x'), (V1, 'r')]).
globals(p(pc(_), data(V2, V3, V1)), [(V2, 'z'), (V3, 'x')], []).
% start location
start(pc(thr1-0-4)).
% error location
error(pc(thr1-err-0)).
% cutpoint locations
% invariant templates
% bound var 
% bounding vars 
bounding_vars(data(V2, V3, V1), []).
% cfg loop exit relation
cfg_exit_relation([]).
% 10 transitions
r(p(pc(thr1-0-4), data(V2, V3, V1)), p(pc(thr1-1-5), data(V2, V3, V1p)), [], [V1p = 0], 1). % r := 0
stmtsrc(1, 'r=0;').
transition_access( 1, [_, _, w] ).
r(p(pc(thr1-1-5), data(V2, V3, V1)), p(pc(thr1-3-7), data(V2, V3, V1)), [(V2 = 0)], [], 2). % z == 0
stmtsrc(2, 'assume(z == 0);').
transition_access( 2, [r, _, _] ).
r(p(pc(thr1-3-7), data(V2, V3, V1)), p(pc(thr1-4-0), data(V2, V3p, V1)), [], [V3p = 0], 3). % x := 0
stmtsrc(3, 'x=0;').
transition_access( 3, [_, w, _] ).
r(p(pc(thr1-4-0), data(V2, V3, V1)), p(pc(thr1-2-9), data(V2, V3, V1p)), [], [V1p = V3], 4). % r := x
stmtsrc(4, 'r=x;').
transition_access( 4, [_, r, w] ).
r(p(pc(thr1-1-5), data(V2, V3, V1)), p(pc(thr1-2-9), data(V2, V3, V1)), [(V2 +1 =< 0)], [], 5). % z < 0
stmtsrc(5, 'assume(z < 0);').
transition_access( 5, [r, _, _] ).
r(p(pc(thr1-1-5), data(V2, V3, V1)), p(pc(thr1-2-9), data(V2, V3, V1)), [(V2 >= 1+ 0)], [], 6). % z > 0
stmtsrc(6, 'assume(z > 0);').
transition_access( 6, [r, _, _] ).
r(p(pc(thr1-2-9), data(V2, V3, V1)), p(pc(thr1-err-0), data(V2, V3, V1)), [(V1 +1 =< 0)], [], 7). % r < 0
stmtsrc(7, 'assume(r < 0);').
transition_access( 7, [_, _, r] ).
r(p(pc(thr1-2-9), data(V2, V3, V1)), p(pc(thr1-err-0), data(V2, V3, V1)), [(V1 >= 1+ 0)], [], 8). % r > 0
stmtsrc(8, 'assume(r > 0);').
transition_access( 8, [_, _, r] ).
r(p(pc(thr1-2-9), data(V2, V3, V1)), p(pc(thr1-5-10), data(V2, V3, V1)), [(V1 = 0)], [], 9). % r == 0
stmtsrc(9, 'assume(r == 0);').
transition_access( 9, [_, _, r] ).
r(p(pc(thr1-5-10), data(V2, V3, V1)), p(pc(thr1-ret-0), data(V2, V3, V1)), [], [], 10). % 
stmtsrc(10, 'skip;').
transition_access( 10, [_, _, _] ).
% Atomic blocks
% Strengthenings
