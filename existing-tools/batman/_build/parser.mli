
(* The type of tokens. *)

type token = 
  | T_int_litt of (int)
  | T_id of (string)
  | T_WHILE
  | T_VAR
  | T_TIMES
  | T_THREAD
  | T_THEN
  | T_SKIP
  | T_SEMICOLON
  | T_RPAR
  | T_RBRACKET
  | T_RAND
  | T_PLUS
  | T_PERCENT
  | T_NOT
  | T_NEQUAL
  | T_MINUS
  | T_LPAR
  | T_LBRACKET
  | T_LABEL
  | T_INIT
  | T_IF
  | T_GREATERE
  | T_GREATER
  | T_EQUAL
  | T_EOF
  | T_ENDIF
  | T_END
  | T_ELSE
  | T_DONE
  | T_DO
  | T_DIVIDED
  | T_COMMA
  | T_COLON
  | T_BOR
  | T_BOOL of (bool)
  | T_BLESSE
  | T_BLESS
  | T_BEQUAL
  | T_BEGIN
  | T_BAND
  | T_ASSUME

(* This exception is raised by the monolithic API functions. *)

exception Error

(* The monolithic API. *)

val prog: (Lexing.lexbuf -> token) -> Lexing.lexbuf -> (Abs_init.ithread list * Abs_init.ivardecl list * Abs_init.iexpr)
