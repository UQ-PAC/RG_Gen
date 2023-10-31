:- multifile r/5,implicit_updates/0,var2names/2,preds/2,trans_preds/3,
cube_size/1,start/1,error/1,refinement/1,cutpoint/1,invgen_template/2,
 invgen_template/1,cfg_exit_relation/1,stmtsrc/2,strengthening/2,globals/3,
 bound_var/2,bounding_vars/2,transition_access/2,atomic/3.
refinement(inter).
cube_size(1).
preds(p(_, data(V1, V2)), []).
trans_preds(p(_, data(V1, V2)), p(_, data(V1p, V2p)), []).
var2names(p(pc(_), data(V1, V2)), [(V1, 'z'), (V2, 'x')]).
globals(p(pc(_), data(V1, V2)), [(V1, 'z'), (V2, 'x')], []).
% start location
start(pc(thr2-0-13)).
% error location
error(pc(thr2-err-0)).
% cutpoint locations
% invariant templates
% bound var 
% bounding vars 
bounding_vars(data(V1, V2), []).
% cfg loop exit relation
cfg_exit_relation([]).
% 6 transitions
r(p(pc(thr2-0-13), data(V1, V2)), p(pc(thr2-2-15), data(V1, V2)), [(V1 = 1)], [], 1). % z == 1
stmtsrc(1, 'assume(z == 1);').
transition_access( 1, [r, _] ).
r(p(pc(thr2-2-15), data(V1, V2)), p(pc(thr2-3-0), data(V1, V2p)), [], [V2p = 1], 2). % x := 1
stmtsrc(2, 'x=1;').
transition_access( 2, [_, w] ).
r(p(pc(thr2-3-0), data(V1, V2)), p(pc(thr2-1-17), data(V1p, V2)), [], [V1p = 0], 3). % z := 0
stmtsrc(3, 'z=0;').
transition_access( 3, [w, _] ).
r(p(pc(thr2-0-13), data(V1, V2)), p(pc(thr2-1-17), data(V1, V2)), [(V1 +1 =< 1)], [], 4). % z < 1
stmtsrc(4, 'assume(z < 1);').
transition_access( 4, [r, _] ).
r(p(pc(thr2-0-13), data(V1, V2)), p(pc(thr2-1-17), data(V1, V2)), [(V1 >= 1+ 1)], [], 5). % z > 1
stmtsrc(5, 'assume(z > 1);').
transition_access( 5, [r, _] ).
r(p(pc(thr2-1-17), data(V1, V2)), p(pc(thr2-ret-0), data(V1, V2)), [], [], 6). % 
stmtsrc(6, 'skip;').
transition_access( 6, [_, _] ).
% Atomic blocks
% Strengthenings
