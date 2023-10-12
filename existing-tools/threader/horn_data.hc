false :-
        [s_221(A,B,C,D),s_111(A,B,C,D)].
s_221(A, B, C, D) :-
        [s_211(A,B,C,D),D>=1+0].
s_211(A, B, C, D) :-
        [s_180(E,F,G,H),t_1_43(E,F,G,H,A,B,C,D),{1=1}].
t_1_43(A, B, C, D, E, F, G, H) :-
        [s_110(A,B,C,D),H=1,G=C,F=B,E=A].
s_110(A, B, C, D) :-
        [s_65(E,F,G,H),t_2_33(E,F,G,H,A,B,C,D),{1=1}].
t_2_33(A, B, C, D, E, F, G, H) :-
        [s_56(A,B,C,D),E=0,H=D,G=C,F=B].
s_56(A, B, C, D) :-
        [s_38(E,F,G,H),t_1_8(E,F,G,H,A,B,C,D),{1=1}].
t_1_8(A, B, C, D, E, F, G, H) :-
        [s_9(A,B,C,D),E>=1,H=D,G=C,F=B,E=A].
s_9(A, B, C, D) :-
        [s_4(A,_,C,D),B=1].
s_4(A, B, C, D) :-
        [s_2(E,F,G,H),t_2_1(E,F,G,H,A,B,C,D),{1=1}].
t_2_1(A, B, C, D, E, F, G, H) :-
        [s_1(A,B,C,D),E=1,H=D,G=C,F=B].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_38(A, B, C, D) :-
        [s_23(A,B,C,D),C+1=<0].
s_23(A, B, C, D) :-
        [s_10(A,B,C,D),B>=1].
s_10(A, B, C, D) :-
        [s_3(E,F,G,H),t_1_4(E,F,G,H,A,B,C,D),{1=1}].
t_1_4(A, B, C, D, E, F, G, H) :-
        [s_4(A,B,C,D),F=1,H=D,G=C,E=A].
s_4(A, B, C, D) :-
        [s_2(E,F,G,H),t_2_1(E,F,G,H,A,B,C,D),{1=1}].
t_2_1(A, B, C, D, E, F, G, H) :-
        [s_1(A,B,C,D),E=1,H=D,G=C,F=B].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_3(A, B, C, D) :-
        [s_1(_,B,C,D),A=1].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_65(A, B, C, D) :-
        [s_26(E,F,G,H),t_2_24(E,F,G,H,A,B,C,D),{1=1}].
t_2_24(A, B, C, D, E, F, G, H) :-
        [s_42(A,B,C,D),G=1,H=D,F=B,E=A].
s_42(A, B, C, D) :-
        [s_28(A,B,C,D),D=<0].
s_28(A, B, C, D) :-
        [s_11(A,B,C,_),D=0].
s_11(A, B, C, D) :-
        [s_7(E,F,G,H),t_1_4(E,F,G,H,A,B,C,D),{1=1}].
t_1_4(A, B, C, D, E, F, G, H) :-
        [s_4(A,B,C,D),F=1,H=D,G=C,E=A].
s_4(A, B, C, D) :-
        [s_2(E,F,G,H),t_2_1(E,F,G,H,A,B,C,D),{1=1}].
t_2_1(A, B, C, D, E, F, G, H) :-
        [s_1(A,B,C,D),E=1,H=D,G=C,F=B].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_7(A, B, C, D) :-
        [s_3(A,B,C,D),B+1=<1].
s_3(A, B, C, D) :-
        [s_1(_,B,C,D),A=1].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_26(A, B, C, D) :-
        [s_15(E,F,G,H),t_2_9(E,F,G,H,A,B,C,D),{1=1}].
t_2_9(A, B, C, D, E, F, G, H) :-
        [s_10(A,B,C,D),F>=1,H=D,G=C,F=B,E=A].
s_10(A, B, C, D) :-
        [s_3(E,F,G,H),t_1_4(E,F,G,H,A,B,C,D),{1=1}].
t_1_4(A, B, C, D, E, F, G, H) :-
        [s_4(A,B,C,D),F=1,H=D,G=C,E=A].
s_4(A, B, C, D) :-
        [s_2(E,F,G,H),t_2_1(E,F,G,H,A,B,C,D),{1=1}].
t_2_1(A, B, C, D, E, F, G, H) :-
        [s_1(A,B,C,D),E=1,H=D,G=C,F=B].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_3(A, B, C, D) :-
        [s_1(_,B,C,D),A=1].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_15(A, B, C, D) :-
        [s_12(E,F,G,H),t_2_6(E,F,G,H,A,B,C,D),{1=1}].
t_2_6(A, B, C, D, E, F, G, H) :-
        [s_6(A,B,C,D),E=1,H=D,G=C,F=B].
s_6(A, B, C, D) :-
        [s_1(E,F,G,H),t_1_2(E,F,G,H,A,B,C,D),{1=1}].
t_1_2(A, B, C, D, E, F, G, H) :-
        [s_2(A,B,C,D),F=1,H=D,G=C,E=A].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_12(A, B, C, D) :-
        [s_5(A,B,C,D),A+1=<1].
s_5(A, B, C, D) :-
        [s_2(A,_,C,D),B=1].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_180(A, B, C, D) :-
        [s_121(E,F,G,H),t_1_5(E,F,G,H,A,B,C,D),{1=1}].
t_1_5(A, B, C, D, E, F, G, H) :-
        [s_5(A,B,C,D),E+1=<1,H=D,G=C,F=B,E=A].
s_5(A, B, C, D) :-
        [s_2(A,_,C,D),B=1].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_121(A, B, C, D) :-
        [s_54(E,F,G,H),t_1_35(E,F,G,H,A,B,C,D),{1=1}].
t_1_35(A, B, C, D, E, F, G, H) :-
        [s_76(A,B,C,D),F=1,H=D,G=C,E=A].
s_76(A, B, C, D) :-
        [s_47(E,F,G,H),t_2_25(E,F,G,H,A,B,C,D),{1=1}].
t_2_25(A, B, C, D, E, F, G, H) :-
        [s_46(A,B,C,D),E=0,H=D,G=C,F=B].
s_46(A, B, C, D) :-
        [s_33(A,B,_,D),C=1].
s_33(A, B, C, D) :-
        [s_16(A,B,C,D),D=<0].
s_16(A, B, C, D) :-
        [s_7(A,B,C,_),D=0].
s_7(A, B, C, D) :-
        [s_3(A,B,C,D),B+1=<1].
s_3(A, B, C, D) :-
        [s_1(_,B,C,D),A=1].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_47(A, B, C, D) :-
        [s_17(E,F,G,H),t_2_20(E,F,G,H,A,B,C,D),{1=1}].
t_2_20(A, B, C, D, E, F, G, H) :-
        [s_33(A,B,C,D),G=1,H=D,F=B,E=A].
s_33(A, B, C, D) :-
        [s_16(A,B,C,D),D=<0].
s_16(A, B, C, D) :-
        [s_7(A,B,C,_),D=0].
s_7(A, B, C, D) :-
        [s_3(A,B,C,D),B+1=<1].
s_3(A, B, C, D) :-
        [s_1(_,B,C,D),A=1].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_17(A, B, C, D) :-
        [s_8(E,F,G,H),t_2_7(E,F,G,H,A,B,C,D),{1=1}].
t_2_7(A, B, C, D, E, F, G, H) :-
        [s_7(A,B,C,D),H=0,G=C,F=B,E=A].
s_7(A, B, C, D) :-
        [s_3(A,B,C,D),B+1=<1].
s_3(A, B, C, D) :-
        [s_1(_,B,C,D),A=1].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_8(A, B, C, D) :-
        [s_4(E,F,G,H),t_2_3(E,F,G,H,A,B,C,D),{1=1}].
t_2_3(A, B, C, D, E, F, G, H) :-
        [s_3(A,B,C,D),F+1=<1,H=D,G=C,F=B,E=A].
s_3(A, B, C, D) :-
        [s_1(_,B,C,D),A=1].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_4(A, B, C, D) :-
        [s_2(E,F,G,H),t_2_1(E,F,G,H,A,B,C,D),{1=1}].
t_2_1(A, B, C, D, E, F, G, H) :-
        [s_1(A,B,C,D),E=1,H=D,G=C,F=B].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_54(A, B, C, D) :-
        [s_37(E,F,G,H),t_1_21(E,F,G,H,A,B,C,D),{1=1}].
t_1_21(A, B, C, D, E, F, G, H) :-
        [s_35(A,B,C,D),F=0,H=D,G=C,E=A].
s_35(A, B, C, D) :-
        [s_19(A,B,C,D),C+1=<1].
s_19(A, B, C, D) :-
        [s_9(A,B,C,D),A>=1].
s_9(A, B, C, D) :-
        [s_4(A,_,C,D),B=1].
s_4(A, B, C, D) :-
        [s_2(E,F,G,H),t_2_1(E,F,G,H,A,B,C,D),{1=1}].
t_2_1(A, B, C, D, E, F, G, H) :-
        [s_1(A,B,C,D),E=1,H=D,G=C,F=B].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_37(A, B, C, D) :-
        [s_22(A,B,C,_),D=0].
s_22(A, B, C, D) :-
        [s_11(E,F,G,H),t_1_8(E,F,G,H,A,B,C,D),{1=1}].
t_1_8(A, B, C, D, E, F, G, H) :-
        [s_9(A,B,C,D),E>=1,H=D,G=C,F=B,E=A].
s_9(A, B, C, D) :-
        [s_4(A,_,C,D),B=1].
s_4(A, B, C, D) :-
        [s_2(E,F,G,H),t_2_1(E,F,G,H,A,B,C,D),{1=1}].
t_2_1(A, B, C, D, E, F, G, H) :-
        [s_1(A,B,C,D),E=1,H=D,G=C,F=B].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_11(A, B, C, D) :-
        [s_7(E,F,G,H),t_1_4(E,F,G,H,A,B,C,D),{1=1}].
t_1_4(A, B, C, D, E, F, G, H) :-
        [s_4(A,B,C,D),F=1,H=D,G=C,E=A].
s_4(A, B, C, D) :-
        [s_2(E,F,G,H),t_2_1(E,F,G,H,A,B,C,D),{1=1}].
t_2_1(A, B, C, D, E, F, G, H) :-
        [s_1(A,B,C,D),E=1,H=D,G=C,F=B].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_7(A, B, C, D) :-
        [s_3(A,B,C,D),B+1=<1].
s_3(A, B, C, D) :-
        [s_1(_,B,C,D),A=1].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_111(A, B, C, D) :-
        [s_66(E,F,G,H),t_2_33(E,F,G,H,A,B,C,D),{1=1}].
t_2_33(A, B, C, D, E, F, G, H) :-
        [s_56(A,B,C,D),E=0,H=D,G=C,F=B].
s_56(A, B, C, D) :-
        [s_38(E,F,G,H),t_1_8(E,F,G,H,A,B,C,D),{1=1}].
t_1_8(A, B, C, D, E, F, G, H) :-
        [s_9(A,B,C,D),E>=1,H=D,G=C,F=B,E=A].
s_9(A, B, C, D) :-
        [s_4(A,_,C,D),B=1].
s_4(A, B, C, D) :-
        [s_2(E,F,G,H),t_2_1(E,F,G,H,A,B,C,D),{1=1}].
t_2_1(A, B, C, D, E, F, G, H) :-
        [s_1(A,B,C,D),E=1,H=D,G=C,F=B].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_38(A, B, C, D) :-
        [s_23(A,B,C,D),C+1=<0].
s_23(A, B, C, D) :-
        [s_10(A,B,C,D),B>=1].
s_10(A, B, C, D) :-
        [s_3(E,F,G,H),t_1_4(E,F,G,H,A,B,C,D),{1=1}].
t_1_4(A, B, C, D, E, F, G, H) :-
        [s_4(A,B,C,D),F=1,H=D,G=C,E=A].
s_4(A, B, C, D) :-
        [s_2(E,F,G,H),t_2_1(E,F,G,H,A,B,C,D),{1=1}].
t_2_1(A, B, C, D, E, F, G, H) :-
        [s_1(A,B,C,D),E=1,H=D,G=C,F=B].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_3(A, B, C, D) :-
        [s_1(_,B,C,D),A=1].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_66(A, B, C, D) :-
        [s_27(E,F,G,H),t_2_24(E,F,G,H,A,B,C,D),{1=1}].
t_2_24(A, B, C, D, E, F, G, H) :-
        [s_42(A,B,C,D),G=1,H=D,F=B,E=A].
s_42(A, B, C, D) :-
        [s_28(A,B,C,D),D=<0].
s_28(A, B, C, D) :-
        [s_11(A,B,C,_),D=0].
s_11(A, B, C, D) :-
        [s_7(E,F,G,H),t_1_4(E,F,G,H,A,B,C,D),{1=1}].
t_1_4(A, B, C, D, E, F, G, H) :-
        [s_4(A,B,C,D),F=1,H=D,G=C,E=A].
s_4(A, B, C, D) :-
        [s_2(E,F,G,H),t_2_1(E,F,G,H,A,B,C,D),{1=1}].
t_2_1(A, B, C, D, E, F, G, H) :-
        [s_1(A,B,C,D),E=1,H=D,G=C,F=B].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_7(A, B, C, D) :-
        [s_3(A,B,C,D),B+1=<1].
s_3(A, B, C, D) :-
        [s_1(_,B,C,D),A=1].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_27(A, B, C, D) :-
        [s_19(E,F,G,H),t_2_9(E,F,G,H,A,B,C,D),{1=1}].
t_2_9(A, B, C, D, E, F, G, H) :-
        [s_10(A,B,C,D),F>=1,H=D,G=C,F=B,E=A].
s_10(A, B, C, D) :-
        [s_3(E,F,G,H),t_1_4(E,F,G,H,A,B,C,D),{1=1}].
t_1_4(A, B, C, D, E, F, G, H) :-
        [s_4(A,B,C,D),F=1,H=D,G=C,E=A].
s_4(A, B, C, D) :-
        [s_2(E,F,G,H),t_2_1(E,F,G,H,A,B,C,D),{1=1}].
t_2_1(A, B, C, D, E, F, G, H) :-
        [s_1(A,B,C,D),E=1,H=D,G=C,F=B].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_3(A, B, C, D) :-
        [s_1(_,B,C,D),A=1].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_19(A, B, C, D) :-
        [s_9(A,B,C,D),A>=1].
s_9(A, B, C, D) :-
        [s_4(A,_,C,D),B=1].
s_4(A, B, C, D) :-
        [s_2(E,F,G,H),t_2_1(E,F,G,H,A,B,C,D),{1=1}].
t_2_1(A, B, C, D, E, F, G, H) :-
        [s_1(A,B,C,D),E=1,H=D,G=C,F=B].
s_1(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
s_2(A, B, C, _) :-
        [A=0,B=0,C=0,A=0,B=0,C=0].
