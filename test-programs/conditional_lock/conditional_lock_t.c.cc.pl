:- multifile r/5,implicit_updates/0,var2names/2,preds/2,preds/3,trans_preds/4,
   cube_size/1,start/2,error/2,error/3,refinement/1,cutpoint/1,invgen_template/2,
   invgen_template/1,cfg_exit_relation/1,stmtsrc/2,strengthening/2,trans_preds/3,
   globals/2,bound_var/2,bounding_vars/2,int2pc/1,dec_data/3,transition_access/2,
   singleton/2.
refinement(inter).
cube_size(1).
var2names(p(_,data(B,C,D)), [(B,x),(C,z),(D,r1)]).
dec_data(p(_,data(B,C,D)), [[B,C],[D],[]], 2).
start(p(pc([[[1|1]],[[1|1]]]),data(_,_,_)), []).
error(p(pc([[[6|6]],_]),data(_,_,_)), [], 1).
preds(_, p(_,data(_,_,_)), []).
trans_preds(_, p(_,data(_,_,_)), p(_,data(_,_,_)), []).

r(p(pc([[[1|1]],A]),data(B,C,_)), p(pc([[[2|2]],A]),data(B,C,H)), [], [H=0], (1,1)).
stmtsrc((1,1), 'r=0;').
transition_access((1,1), [_,_,w]).
r(p(pc([[[2|2]],A]),data(B,C,D)), p(pc([[[4|4]],A]),data(B,C,D)), [C=0], [], (1,2)).
stmtsrc((1,2), 'assume(z == 0);').
transition_access((1,2), [_,r,_]).
r(p(pc([[[4|4]],A]),data(_,C,D)), p(pc([[[5|5]],A]),data(F,C,D)), [], [F=0], (1,3)).
stmtsrc((1,3), 'x=0;').
transition_access((1,3), [w,_,_]).
r(p(pc([[[5|5]],A]),data(B,C,_)), p(pc([[[3|3]],A]),data(B,C,H)), [], [H=B], (1,4)).
stmtsrc((1,4), 'r=x;').
transition_access((1,4), [r,_,w]).
r(p(pc([[[2|2]],A]),data(B,C,D)), p(pc([[[3|3]],A]),data(B,C,D)), [C+1=<0], [], (1,5)).
stmtsrc((1,5), 'assume(z < 0);').
transition_access((1,5), [_,r,_]).
r(p(pc([[[2|2]],A]),data(B,C,D)), p(pc([[[3|3]],A]),data(B,C,D)), [C>=1+0], [], (1,6)).
stmtsrc((1,6), 'assume(z > 0);').
transition_access((1,6), [_,r,_]).
r(p(pc([[[3|3]],A]),data(B,C,D)), p(pc([[[6|6]],A]),data(B,C,D)), [D+1=<0], [], (1,7)).
stmtsrc((1,7), 'assume(r < 0);').
transition_access((1,7), [_,_,r]).
r(p(pc([[[3|3]],A]),data(B,C,D)), p(pc([[[6|6]],A]),data(B,C,D)), [D>=1+0], [], (1,8)).
stmtsrc((1,8), 'assume(r > 0);').
transition_access((1,8), [_,_,r]).
r(p(pc([[[3|3]],A]),data(B,C,D)), p(pc([[[7|7]],A]),data(B,C,D)), [D=0], [], (1,9)).
stmtsrc((1,9), 'assume(r == 0);').
transition_access((1,9), [_,_,r]).
r(p(pc([A,[[1|1]]]),data(B,C,D)), p(pc([A,[[2|2]]]),data(B,C,D)), [C=1], [], (2,1)).
stmtsrc((2,1), 'assume(z == 1);').
transition_access((2,1), [_,r,_]).
r(p(pc([A,[[2|2]]]),data(_,C,D)), p(pc([A,[[3|3]]]),data(F,C,D)), [], [F=1], (2,2)).
stmtsrc((2,2), 'x=1;').
transition_access((2,2), [w,_,_]).
r(p(pc([A,[[3|3]]]),data(B,_,D)), p(pc([A,[[4|4]]]),data(B,G,D)), [], [G=0], (2,3)).
stmtsrc((2,3), 'z=0;').
transition_access((2,3), [_,w,_]).
r(p(pc([A,[[1|1]]]),data(B,C,D)), p(pc([A,[[4|4]]]),data(B,C,D)), [C+1=<1], [], (2,4)).
stmtsrc((2,4), 'assume(z < 1);').
transition_access((2,4), [_,r,_]).
r(p(pc([A,[[1|1]]]),data(B,C,D)), p(pc([A,[[4|4]]]),data(B,C,D)), [C>=1+1], [], (2,5)).
stmtsrc((2,5), 'assume(z > 1);').
transition_access((2,5), [_,r,_]).
