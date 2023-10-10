import java.util.ArrayList;
import com.microsoft.z3.*;

public class SeqProver {
	
	public Token substitute(Token eq, Token pred) {
		
		if (eq.getName().equals(Parser.EQ)) {
			Token var = eq.getTokens().get(0);
			Token expr = eq.getTokens().get(1);
			if (pred.getName().equals(Parser.VAR)) {
				if (pred.equals(var)) {
					return expr.clone();
				}
				return pred.clone();
			} else if (pred.getName().equals(Parser.CONSTANT) || 
					pred.getName().equals(Parser.TRUE) || 
					pred.getName().equals(Parser.FALSE)) {
				return pred.clone();
			} else {
				Token result = new Token(pred.getType(),pred.getName());
				for (Token t: pred.getTokens()) {
					result.addToken(substitute(eq,t));
				}
				return result;
			}
		}
		System.out.println("ERROR in SeqProver.substitute");
		return new Token(Parser.ERROR);
	}
	
	public Token substituteProgPre(ArrayList<Token> eqList, Token prog) {
		Token result = prog.clone();
		for (Token eq : eqList) {
			if (eq.getName().startsWith(Parser.EQ)) {
				result = substitute(eq,result);
			}
		}
		
		return result;
	}
	
	public Token substituteProg(ArrayList<Token> eqList, Token prog) {
		
		Token result = prog.clone();
		String prefix = "fresh";
		Parser par = new Parser();
		int count = 0;
		
		ArrayList<Token> fstList = new ArrayList<Token>();
		ArrayList<Token> sndList = new ArrayList<Token>();
		
		for (Token eq: eqList) {
			if (eq.getName().startsWith(Parser.EQ)) {
				Token tempVar = par.parseExpression(prefix + count++);
				Token fstEq = new Token(Parser.PRED,Parser.EQ)
						.addToken(eq.getTokens().get(0).clone()).addToken(tempVar.clone());
				Token sndEq = new Token(Parser.PRED,Parser.EQ)
						.addToken(tempVar.clone()).addToken(eq.getTokens().get(1).clone());
				fstList.add(fstEq);
				sndList.add(sndEq);
			}
		}
		
		result = substituteProgPre(fstList,result);
		result = substituteProgPre(sndList,result);
		return result;
	}
	
	public boolean contains(Token big, Token small) {
		
		if (big.equals(small)) return true;
		for (Token t: big.getTokens()) {
			if (contains(t,small)) {
				return true;
			}
		}
		
		return false;
	}

	public Token getSubstitution(Token assignment, Token var) {

		Token expr = assignment.getTokens().get(1);
		if (contains(expr,var)) {
			if (expr.equals(var)) {
				Token eq = new Token(Parser.PRED,Parser.EQ);
				eq.addToken(var.clone());
				eq.addToken(assignment.getTokens().get(0));
				return eq;
			} else if (expr.getName().startsWith("+")) {
				Token fst = expr.getTokens().get(0);
				Token snd = expr.getTokens().get(1);
				if (!contains(fst,var)) {
					Token newAssign = new Token(Parser.COM,Parser.ASSIGN);
					Token left = new Token(Parser.EXPR,"-");
					left.addToken(assignment.getTokens().get(0).clone());
					left.addToken(fst.clone());
					newAssign.addToken(left);
					newAssign.addToken(snd.clone());
					return getSubstitution(newAssign,var);
				}
				if (!contains(snd,var)) {
					Token newAssign = new Token(Parser.COM,Parser.ASSIGN);
					Token left = new Token(Parser.EXPR,"-");
					left.addToken(assignment.getTokens().get(0).clone());
					left.addToken(snd.clone());
					newAssign.addToken(left);
					newAssign.addToken(fst.clone());
					return getSubstitution(newAssign,var);
				}
			} else if (expr.getName().startsWith("-") && expr.getTokens().size() == 2) {// Normal operations
				Token fst = expr.getTokens().get(0);
				Token snd = expr.getTokens().get(1);
				if (!contains(fst,var)) {
					Token newAssign = new Token(Parser.COM,Parser.ASSIGN);
					Token left = new Token(Parser.EXPR,"-");
					left.addToken(fst.clone());
					left.addToken(assignment.getTokens().get(0).clone());
					newAssign.addToken(left);
					newAssign.addToken(snd.clone());
					return getSubstitution(newAssign,var);
				}
				if (!contains(snd,var)) {
					Token newAssign = new Token(Parser.COM,Parser.ASSIGN);
					Token left = new Token(Parser.EXPR,"+");
					left.addToken(assignment.getTokens().get(0).clone());
					left.addToken(snd.clone());
					newAssign.addToken(left);
					newAssign.addToken(fst.clone());
					return getSubstitution(newAssign,var);
				}
			} else if (expr.getName().startsWith("-") && expr.getTokens().size() == 1) {// Negative expression
				Token newAssign = new Token(Parser.COM,Parser.ASSIGN);
				Token left = new Token(Parser.EXPR,"-").addToken(assignment.getTokens().get(0).clone());
				newAssign.addToken(left);
				newAssign.addToken(expr.getTokens().get(0));
				return getSubstitution(newAssign,var);
			} else if (expr.getName().startsWith("*")) {
				Token fst = expr.getTokens().get(0);
				Token snd = expr.getTokens().get(1);
				if (!contains(fst,var)) {
					Token newAssign = new Token(Parser.COM,Parser.ASSIGN);
					Token left = new Token(Parser.EXPR,"/");
					left.addToken(assignment.getTokens().get(0).clone());
					left.addToken(fst.clone());
					newAssign.addToken(left);
					newAssign.addToken(snd.clone());
					return getSubstitution(newAssign,var);
				}
				if (!contains(snd,var)) {
					Token newAssign = new Token(Parser.COM,Parser.ASSIGN);
					Token left = new Token(Parser.EXPR,"/");
					left.addToken(assignment.getTokens().get(0).clone());
					left.addToken(snd.clone());
					newAssign.addToken(left);
					newAssign.addToken(fst.clone());
					return getSubstitution(newAssign,var);
				}
			} else if (expr.getName().startsWith("/")) {
				Token fst = expr.getTokens().get(0);
				Token snd = expr.getTokens().get(1);
				if (!contains(fst,var)) {
					Token newAssign = new Token(Parser.COM,Parser.ASSIGN);
					Token left = new Token(Parser.EXPR,"/");
					left.addToken(fst.clone());
					left.addToken(assignment.getTokens().get(0).clone());
					newAssign.addToken(left);
					newAssign.addToken(snd.clone());
					return getSubstitution(newAssign,var);
				}
				if (!contains(snd,var)) {
					Token newAssign = new Token(Parser.COM,Parser.ASSIGN);
					Token left = new Token(Parser.EXPR,"*");
					left.addToken(assignment.getTokens().get(0).clone());
					left.addToken(snd.clone());
					newAssign.addToken(left);
					newAssign.addToken(fst.clone());
					return getSubstitution(newAssign,var);
				}
			} 
		} else {
			Token result = new Token(Parser.PRED,Parser.EQ);
			result.addToken(var.clone());
			result.addToken(expr.clone());
			return result;
		}
		
		return new Token(Parser.ERROR);
	}
	
	public boolean isBasicPred(Token pred) {
		
		if (pred.getName().startsWith(Parser.EQ) || pred.getName().startsWith(Parser.LT) ||
				pred.getName().startsWith(Parser.GT) || pred.getName().startsWith(Parser.GE) ||
				pred.getName().startsWith(Parser.LE)) {
			return true;
		}
		if (pred.getName().startsWith(Parser.NEG)){
			return isBasicPred(pred.getTokens().get(0));
		}
		
		return false;
		
	}
	//Remove all atomic formulas that contain var
	public Token remove(Token pred, Token var) {
		//System.out.println("VAR " + var + " PRED " + pred);
		if (!contains(pred,var)) {
			return pred.clone();
		} else {
			if (isBasicPred(pred)) {
				return new Token(Parser.PRED,Parser.TRUE);
			}

			Token newPred = new Token(pred.getType(),pred.getName());
			//System.out.println("BIG: " + printer.printPredicate(pred));
			for (Token t: pred.getTokens()) {
				Token temp = remove(t,var);
				if(!temp.getName().startsWith(Parser.TRUE)) {
					newPred.addToken(temp);
				}

			}
			
			if (newPred.getTokens().size() == 0) {
				newPred = new Token(Parser.PRED,Parser.TRUE);
			}
			if(newPred.getTokens().size() == 1) {
				newPred = newPred.getTokens().get(0);
			}
			
			return newPred;
			}
	}
	
	public Token proveAssign(Context ctx, Solver solver, Token precond, Token assignStmt) {
		
		if(!precond.getType().equals(Parser.PRED)||!assignStmt.getName().equals(Parser.ASSIGN)) {
			System.out.println("ERROR in SeqProver.proveAsign where \n precond: " + precond + "\n assignStmt: " + assignStmt);
			return new Token(Parser.ERROR);
		}
		
		Token result = new Token(Parser.COM,Parser.BLOCK);
		
		result.addToken(precond.clone());
		result.addToken(assignStmt.clone());
		
		if(precond.getName().startsWith(Parser.FALSE)) {
			result.addToken(precond.clone());
			return result;
		}
		
		Token var = assignStmt.getTokens().get(0); // The left assigned variable
		Token expr = assignStmt.getTokens().get(1);// The right hand side expression
		Token eq = getSubstitution(assignStmt,var);
		
		Token postCond;

		if (contains(expr,var)) {
			postCond = substitute(eq,precond).clone();

		} else {
			postCond = remove(precond,var);
			if (postCond.getName().startsWith(Parser.TRUE)) {
				postCond = eq.clone();
			} else {
				if (postCond.getName().startsWith(Parser.OR)) {
					for (int i = 0; i < postCond.getTokens().size(); i++) {
						Token newAtom = new Token(Parser.PRED,Parser.AND).
								addToken(postCond.getTokens().get(i).clone()).addToken(eq);
						postCond.getTokens().set(i, newAtom);
					}
				} else {
					postCond = new Token(Parser.PRED,Parser.AND).addToken(postCond.clone()).addToken(eq.clone());
				}
				
			}
		}
		
		Simplifier simpl = new Simplifier();
		postCond = simpl.simplifyBySub(postCond);
		postCond = simpl.simplifyPredicate(ctx,solver,postCond);
		
		return result.addToken(postCond);
	}
	
	public Token proveSkip(Token precond, Token skipStmt) {
		
		if(!precond.getType().equals(Parser.PRED)||!skipStmt.getName().equals(Parser.SKIP)) {
			return new Token(Parser.ERROR);
		}
		
		Token result = new Token(Parser.COM,Parser.BLOCK)
				.addToken(precond.clone()).addToken(skipStmt.clone())
				.addToken(precond.clone());
		return result;
	}
	
	public Token proveIf(Context ctx, Solver solver, Token precond, Token ifStmt) {
		
		Simplifier simpl = new Simplifier();
		Parser parser = new Parser();
		
		Token cond = parser.bool2Pred(ifStmt.getTokens().get(0));
		Token ifBlock = ifStmt.getTokens().get(1);
		Token elseBlock = ifStmt.getTokens().get(2);
		Token ifCond,elseCond;
		
		if (precond.getName().startsWith(Parser.FALSE)) {
			ifCond = precond.clone();
			elseCond = precond.clone();
		} else {
			ifCond = new Token(Parser.PRED,Parser.AND).addToken(precond.clone()).addToken(cond);
			elseCond = new Token(Parser.PRED,Parser.AND).addToken(precond.clone())
					             .addToken(new Token(Parser.PRED,Parser.NEG).addToken(cond));
		}
		
		ifCond = simpl.simplifyPredicate(ctx,solver,ifCond);
		elseCond = simpl.simplifyPredicate(ctx,solver,elseCond);
		
		Token body = new Token(Parser.COM,Parser.IF).addToken(ifStmt.getTokens().get(0));
		//Computation comp = new Computation();
		Token ifProof,elseProof,postCond;

		ifProof = proveBlock(ctx,solver,ifCond,ifBlock);
		elseProof = proveBlock(ctx,solver,elseCond,elseBlock);
		postCond = new Token(Parser.PRED,Parser.OR);
		postCond.addToken(ifProof.getTokens().get(ifProof.getTokens().size()-1));
		postCond.addToken(elseProof.getTokens().get(elseProof.getTokens().size()-1));
		postCond = simpl.simplifyPredicate(ctx,solver,postCond);
		
		body.addToken(ifProof);
		body.addToken(elseProof);
		
		Token result = new Token(Parser.COM,Parser.BLOCK);
		result.addToken(precond.clone());
		result.addToken(body);
		result.addToken(postCond);
		
		return result;
	}
	
	public Token proveWhile(Context ctx, Solver solver, Token inv, Token whileStmt) {
		
		Printer printer = new Printer();
		Computation comp = new Computation();
		
		Token precond = new Token(Parser.PRED,Parser.AND);
		Parser p = new Parser();
		Token loopCond = p.bool2Pred(whileStmt.getTokens().get(0));
		precond.addToken(inv.clone()).addToken(loopCond.clone());
		Token proof;
		if (comp.imply(ctx, solver, inv, loopCond)) {
			proof = proveBlock(ctx,solver,inv,whileStmt.getTokens().get(1));
		} else if (comp.imply(ctx, solver, loopCond, inv)){
			proof = proveBlock(ctx,solver,loopCond, whileStmt.getTokens().get(1));
		} else {
			proof = proveBlock(ctx,solver,precond,whileStmt.getTokens().get(1));
		}
		//System.out.println("PRECOND " + precond);
		//System.out.println("PROOF " + proof);
		Token lastPred = proof.getTokens().get(proof.getTokens().size()-1);
		if (!comp.imply(ctx,solver,lastPred, inv)) {
			System.out.println("Fail to prove invariant: " + printer.printPredicate(inv) + "\n using " + printer.printPredicate(lastPred));
			return new Token(Parser.ERROR);	

		}
		proof.getTokens().add(inv.clone());
		
		Token body = new Token(Parser.COM,Parser.WHILE).addToken(whileStmt.getTokens().get(0));
		body.addToken(proof);
		Token postCond = new Token(Parser.PRED,Parser.AND);
		postCond.addToken(inv.clone());
		postCond.addToken(new Token(Parser.PRED,Parser.NEG).addToken(loopCond));
		
		
		if (comp.isSat(ctx, solver, postCond)) {
			Simplifier simpl = new Simplifier();
			postCond = simpl.simplifyPredicate(ctx,solver,postCond);
		} else {
			postCond = new Token(Parser.PRED,Parser.FALSE);
		}
		

		Token result = new Token(Parser.COM, Parser.BLOCK);
		result.addToken(inv.clone()).addToken(body).addToken(postCond);
		//System.out.println("POST: " + postCond);
		
		return result;
	}
	
	public Token proveAtomic(Context ctx, Solver solver, Token precond, Token atomicStmt) {
		
		Token proof = proveBlock(ctx,solver,precond,atomicStmt.getTokens().get(0));
		if (proof.getType().equals(Parser.ERROR)) {
			return new Token(Parser.ERROR);
		}
		proof.getTokens().remove(0);
		Token postCond = proof.getTokens().get(proof.getTokens().size()-1).clone();
		proof.getTokens().remove(proof.getTokens().size()-1);
		Token body = new Token(Parser.COM,Parser.ATOMIC).addToken(proof);
		
		Token result = new Token(Parser.COM, Parser.BLOCK);
		result.addToken(precond.clone()).addToken(body).addToken(postCond);
		return result;
	}
	
	public Token proveAwait(Context ctx, Solver solver, Token precond, Token awaitStmt) {
		
		Token awaitCond = awaitStmt.getTokens().get(0);
		Parser par = new Parser();
		Token newPrecond = new Token(Parser.PRED,Parser.AND)
				           .addToken(precond.clone()).addToken(par.bool2Pred(awaitCond));
		Simplifier simpl = new Simplifier();
		newPrecond = simpl.simplifyByReduce(ctx, solver, newPrecond.clone());
		Token proof = proveBlock(ctx,solver,newPrecond, awaitStmt.getTokens().get(1));

		Token postCond = proof.getTokens().get(proof.getTokens().size()-1).clone();
		//postCond = simpl.simplifyByReduce(ctx, solver, postCond);
		proof.getTokens().remove(proof.getTokens().size()-1);
		Token body = new Token(Parser.COM,Parser.AWAIT)
				         .addToken(awaitCond.clone()).addToken(proof);
		
		Token result = new Token(Parser.COM, Parser.BLOCK);
		result.addToken(precond.clone()).addToken(body).addToken(postCond);
		return result;
	}
	
	public Token proveBlock(Context ctx, Solver solver, Token precond, Token blockStmt){
		
		Token result = new Token(Parser.COM,Parser.BLOCK);
		//System.out.println("BLOCK " + blockStmt);
		
		if(!precond.getType().equals(Parser.PRED)||!blockStmt.getName().equals(Parser.BLOCK)) {
			return new Token(Parser.ERROR);
		}
		Computation comp = new Computation();
		if(!precond.getName().startsWith(Parser.FALSE) && !comp.isSat(ctx,solver,precond)) {
			return proveBlock(ctx,solver,new Token(Parser.PRED,Parser.FALSE),blockStmt);
		}
		
		result.addToken(precond.clone());
		Token currentPred = precond;
		for (int i = 0; i < blockStmt.getTokens().size(); i++) {
			Token t= blockStmt.getTokens().get(i);
			//System.out.println("Current: " + t);
			Token subproof = new Token(Parser.COM,Parser.BLOCK);
			//System.out.println("prove " + t);
			if (t.getType().startsWith(Parser.PRED)) {
				if (comp.imply(ctx,solver,currentPred, t)) {
					subproof.addToken(currentPred.clone());
					subproof.addToken(t.clone());
				} else if (i+1 < blockStmt.getTokens().size() && 
						blockStmt.getTokens().get(i+1).getName().startsWith(Parser.WHILE)){
					//This case currentPred fails to satisfy the invariant t
					//we use currentPred as the invariant
					subproof.addToken(currentPred.clone());
					subproof.addToken(currentPred.clone());
					// old subproof.addToken(new Token(Parser.PRED,Parser.OR).addToken(t.clone()).addToken(currentPred.clone()));
				} else {
					System.out.println("Error in proving consequence rule:\n Ante: " + currentPred + "\nCon: " + t);
					return result.addToken(new Token(Parser.ERROR));
				}
			} else if (t.getName().startsWith(Parser.ASSIGN)) {
				subproof = proveAssign(ctx,solver,currentPred,t);
			} else if (t.getName().startsWith(Parser.SKIP)) {
				subproof = proveSkip(currentPred,t);
			} else if (t.getName().startsWith(Parser.IF)) {
				subproof = proveIf(ctx,solver,currentPred,t);
			} else if (t.getName().startsWith(Parser.WHILE)) {
				if (i-1>=0 && !blockStmt.getTokens().get(i-1).getType().startsWith(Parser.PRED)) {//insert the inv to make it standard
					result.addToken(currentPred.clone());
				}
					subproof = proveWhile(ctx,solver,currentPred,t);
			} else if (t.getName().startsWith(Parser.ATOMIC)) {
				subproof = proveAtomic(ctx,solver,currentPred,t);
			} else if (t.getName().startsWith(Parser.AWAIT)) {
				subproof = proveAwait(ctx,solver,currentPred,t);
			} else if (t.getName().startsWith(Parser.BLOCK)) {
				subproof = proveBlock(ctx,solver,currentPred,t);
			}
			
			if (subproof.getType().equals(Parser.ERROR)) {
				System.out.println("ERROR in Seq.Prover.proveBlock: " + t);
				return result.addToken(new Token(Parser.ERROR));
			}
			subproof.getTokens().remove(0);
			for (Token sub: subproof.getTokens()) {
				result.addToken(sub);
				//System.out.println(sub);
			}
			currentPred = subproof.getTokens().get(subproof.getTokens().size()-1);
		}
		
		return result;
		
	}
	
	
	public Token proveProgram(Context ctx, Solver solver, Token precond, Token program) {
		
		if(!precond.getType().equals(Parser.PRED)||!program.getType().equals(Parser.COM)) {
			return new Token(Parser.ERROR);
		}
		Simplifier simpl = new Simplifier();
		
		if (precond.getName().equals(Parser.AND) || precond.getName().equals(Parser.OR) ) {
			precond = simpl.flatten(precond, precond.getName());
		}
		
		if (program.getName().startsWith(Parser.BLOCK)) {
			return proveBlock(ctx,solver,precond,program);
		} else {
			Token newProgram = new Token(Parser.COM,Parser.BLOCK).addToken(program);
			return proveBlock(ctx,solver,precond,newProgram);
		}
	}

}
