import java.io.File;
import java.util.*;
import com.microsoft.z3.*;

public class Simplifier {
	
	public String tempFile = "temp.smt";
	
	//TOBE DONE
	public Token simplify(Token pred, String option) {
		
		Printer printer = new Printer();
		String input = printer.generateSimplifiedConstraint(pred, option);
		Parser par = new Parser();
		printer.writeToFile(input, tempFile);
		SatSolver solver = new SatSolver();
		String result = solver.runSolver(new File (tempFile));
		return par.parseSmtPred(result);
	}
	
	public boolean isPure(Token expr) {
		
		if (expr.getName().startsWith(Parser.CONSTANT)) {
			return true;
		}
		
		if (expr.getName().startsWith("+") || expr.getName().startsWith("-") ||
				expr.getName().startsWith("*") || expr.getName().startsWith("/")) {
			for (Token t : expr.getTokens()) {
				if (!isPure(t)) {
					return false;
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	//Assume to contain no variable
	public int evalExpr(Token expr) {
		
		if (expr.getName().startsWith("+")) {
			int result = 0;
			for (Token sub: expr.getTokens()) {
				result = result + evalExpr(sub);
			}
			return result;
		}
		
		if (expr.getName().startsWith("*")) {
			int result = 1;
			for (Token sub: expr.getTokens()) {
				result = result * evalExpr(sub);
			}
			return result;
		}
		
		if (expr.getName().startsWith("-")) {
			if (expr.getTokens().size() == 1) {
				int result = -evalExpr(expr.getTokens().get(0));
				return result;
			} else {
				int result = evalExpr(expr.getTokens().get(0));
				result = result - evalExpr(expr.getTokens().get(1));
				return result;
			}
		}
		
		if (expr.getName().startsWith("/")) {
			int result = evalExpr(expr.getTokens().get(0));
			result = result - evalExpr(expr.getTokens().get(1));
			return result;
		}
		
		if (expr.getName().startsWith(Parser.CONSTANT)) {
			int result = Integer.parseInt(expr.getTokens().get(0).getName());
			return result;
		}
		
		return -1;
	}
	
	public Token simplifyByRewrite(Token pred) {
		
		if (pred.getName().startsWith(Parser.VAR)) {
			return pred;
		}
		
		if ((pred.getName().startsWith(Parser.EQ) || pred.getName().startsWith(Parser.LT) || 
				pred.getName().startsWith(Parser.GT) || pred.getName().startsWith(Parser.LE) ||
				pred.getName().startsWith(Parser.GE)) &&
				(pred.getTokens().get(0).getName().startsWith("+") || pred.getTokens().get(0).getName().startsWith("-"))) {
			Token leftExp = pred.getTokens().get(0);
			Token rightExp = pred.getTokens().get(1);
			
			if ( leftExp.getTokens().size() == 2 && 
					leftExp.getTokens().get(0).getName().startsWith(Parser.VAR) && 
					leftExp.getTokens().get(1).getName().startsWith(Parser.CONSTANT)) {

				Token newLeft = leftExp.getTokens().get(0);
				Token newRight;
				if (leftExp.getName().startsWith("+")) {
					newRight = new Token(Parser.EXPR,"-").addToken(rightExp).addToken(leftExp.getTokens().get(1));
				} else {
					newRight = new Token(Parser.EXPR,"+").addToken(rightExp).addToken(leftExp.getTokens().get(1));
				}
				Printer p = new Printer();
				
				return simplifyByRewrite(new Token(Parser.PRED,pred.getName()).addToken(newLeft).addToken(newRight));
			}
		}
		
		if (pred.getType().startsWith(Parser.EXPR) && isPure(pred)) {
			return new Token(Parser.EXPR, Parser.CONSTANT).addToken(new Token(Parser.VAL, evalExpr(pred) + ""));
		} 
		
		Token result = new Token(pred.getType(),pred.getName());
		
		for (Token t : pred.getTokens()) {
			result.addToken(simplifyByRewrite(t));
		}
		return result;
	}
	
	public ArrayList<Token> simplifyByRewrite(ArrayList<Token> preds) {
		
		ArrayList<Token> result = new ArrayList<Token>();
		for (Token pred: preds) {
			result.add(simplifyByRewrite(pred));
		}
		
		return result;
	}
/*	
	public ArrayList<Token> simplifyByRewrite(ArrayList<Token> preds, String option) {
		
		
		ArrayList<Token> result = new ArrayList<Token>();
		Printer printer = new Printer();
		String input = printer.generateSimplifiedConstrains(preds, option);
		Parser par = new Parser();
		printer.writeToFile(input, Base.homeDir + tempFile);
		SatSolver solver = new SatSolver();
		String smtResult = solver.runSolver(new File (Base.homeDir + tempFile));
		Scanner sc = new Scanner(smtResult);
		int i = 0;
		String line = "";
		while(sc.hasNextLine()) {
			
			
			line = line + sc.nextLine();

			if (!par.isBalanced(line)) {
				continue;
			}
			if (line.contains("let")) {
				//Out parser cannot read constraints that contain the keyword let...
				result.add(preds.get(i));
			} else {
				//System.out.println("LINE:" + line);
				result.add(par.parseSmtPred(line));	
			}
			line = "";
			i++;
		}
		sc.close();
		return result;
	}
*/	
	public Token simplifyProvedProgram(Token provedProgram) {

		ArrayList<Token> candidates = new ArrayList<Token>();
		for (int i = 0; i < provedProgram.getTokens().size(); i++) {
			Token t = provedProgram.getTokens().get(i);
			if (t.getType().startsWith(Parser.PRED)) {
				candidates.add(t.clone());
			}
			
			if (t.getType().startsWith(Parser.COM)) {
				Token simplifiedProof = simplifyProvedProgram(t);
				provedProgram.getTokens().set(i, simplifiedProof);
			}
		}
		
		candidates = simplifyByRewrite(candidates);
		//candidates = simplifyByReduce(candidates);

		int count = 0;
		
		for (int i = 0 ; i < provedProgram.getTokens().size(); i++) {
			if (provedProgram.getTokens().get(i).getType().startsWith(Parser.PRED)) {
				provedProgram.getTokens().set(i,candidates.get(count++));
			}
		}
		
		
		return provedProgram;
		
	}
	
	public Relation removeIdens(Relation rel) {
		Relation result = new Relation();
		for (Pair<Token,Token> p: rel.getRelation()) {
			if (!p.getFst().equals(p.getSnd())) {
				result.addPair(p);
			}
		}
		return result;
	}
	
	public Token simplifyPredicate(Context ctx, Solver solver, Token pred) {
		Computation comp = new Computation();
		if (!comp.isSat(ctx, solver, pred)) {
			return new Token(Parser.PRED,Parser.FALSE);
		} else {
			Token result = pred.clone();
			result = simplifyByReduce(ctx,solver,result);
			result = simplifyByRewrite(result);
			//result = simplifyByReduceSimple(result);
			return result;
		}
	}
	
/*	
	public Relation simplifyRelation(Relation rel) {
		
		Relation result = new Relation();
		ArrayList<Token> candidates = new ArrayList<Token>();
		for (Pair<Token,Token> pair: rel.getRelation()) {
			candidates.add(pair.getFst().clone());
			candidates.add(pair.getSnd().clone());
		}
		
		//candidates = simplifyByRewrite(candidates);
		//candidates = simplifyByReduce(candidates);
		
		for (int i = 0 ; i < candidates.size(); i=i+2) {
			result.addPair(candidates.get(i), candidates.get(i+1));
		}
		result = removeIdens(result);
		return result;
	}
*/	
	
	public Relation simplifyRelation(Relation rel) {
		
		Relation result = new Relation();
		
		for (Pair<Token,Token> p : rel.getRelation()) {
			if (!result.getRelation().contains(p) && !p.getFst().getName().equals(Parser.FALSE) &&
					!p.getSnd().getName().equals(Parser.FALSE) && !p.getFst().equals(p.getSnd())) {
				result.addPair(p);
			}
		}
		return result;
	}
	
	public boolean isSub(Token pred) {
		return pred.getName().startsWith(Parser.EQ) && 
				pred.getTokens().get(0).getName().startsWith(Parser.VAR) &&
				pred.getTokens().get(1).getName().startsWith(Parser.CONSTANT);
	}
	
	public Token flatten(Token pred, String name) {
		if (pred.getName().startsWith(name)) {
			Token result = new Token(Parser.PRED,name);
			for (Token t: pred.getTokens()) {
				if (t.getName().startsWith(name)) {
					for (Token sub: flatten(t,name).getTokens()) {
						result.addToken(sub);
					}
				} else {
					result.addToken(t.clone());
				}
			}
			return result;
		} else {
			return pred.clone();
		}
	}
	
	public Token simplifyBySub(Token pred) {
		
		SeqProver seqProver = new SeqProver();
		if (pred.getName().startsWith(Parser.OR) || pred.getName().startsWith("=>")) {
			Token result = new Token(Parser.PRED,pred.getName());
			for (Token sub : pred.getTokens()) {
				result.addToken(simplifyBySub(sub));
			}
			return result;
		} 
		
		if (pred.getName().startsWith(Parser.AND)) {
			Token result = flatten(pred.clone(),Parser.AND);
			for (int i = 0 ; i < pred.getTokens().size(); i++) {
				Token sub = result.getTokens().get(i).clone();
				if (isSub(sub)) {
					result.getTokens().remove(i);
					result = seqProver.substitute(sub, result);
					result =simplifyBySub(result);
					result.addToken(sub);
					return result;
				}
				if (sub.getName().startsWith(Parser.AND)) {
					result.getTokens().set(i, simplifyBySub(sub));
				}
			}
			
			return result;
		}
		
		return pred.clone();
	}
	
	public Token simplifyByReduceSimple(Token pred) {// no need to call smt solver
		Token result = new Token(Parser.PRED,pred.getName());
		if (pred.getName().equals(Parser.AND)) {
			for (Token t: pred.getTokens()) {
				if (!t.getName().equals(Parser.TRUE)) {
					result.addToken(t);
				}
			}
			if (result.getTokens().size() == 0) {
				return new Token(Parser.PRED,Parser.TRUE);
			}else if (result.getTokens().size() == 1) {
				return result.getTokens().get(0);
			} else {
				return result;
			}
		} else if (pred.getName().equals(Parser.OR)) {
			for (Token t: pred.getTokens()) {
				if (!t.getName().equals(Parser.FALSE)) {
					result.addToken(t);
				}
			}
			if (result.getTokens().size() == 0) {
				return new Token(Parser.PRED,Parser.FALSE);
			}else if (result.getTokens().size() == 1) {
				return result.getTokens().get(0);
			} else {
				return result;
			}
		} else {
			return pred;
		}
	}
	
	public Token simplifyByReduce(Context ctx, Solver solver, Token pred) {
		
		Computation comp = new Computation();
		
		if (pred.getName().startsWith(Parser.AND) && pred.getTokens().size() == 2) {
			if (comp.imply(ctx, solver, pred.getTokens().get(0), pred.getTokens().get(1))) {
				return pred.getTokens().get(0);
			} else if (comp.imply(ctx, solver, pred.getTokens().get(1), pred.getTokens().get(0))) {
				return pred.getTokens().get(1);
			}	
		} else if (pred.getName().startsWith(Parser.OR) && pred.getTokens().size() == 2){
			if (comp.imply(ctx, solver, pred.getTokens().get(0), pred.getTokens().get(1))) {
				return pred.getTokens().get(1);
			} else if (comp.imply(ctx, solver, pred.getTokens().get(1), pred.getTokens().get(0))) {
				return pred.getTokens().get(0);
			}
		} 
		
		return pred;
	}
	
	public ArrayList<Token> removeRedundantsInOR(Context ctx, Solver solver, ArrayList<Token> disj){
		Computation comp = new Computation();
		for (int i = 0 ; i < disj.size(); i++) {
			for (int j = i +1; j < disj.size(); j++) {
				if(comp.imply(ctx, solver, disj.get(i), disj.get(j))) {
					disj.remove(j);
					return removeRedundantsInOR( ctx, solver, disj);
				}
			}
		}
		
		return disj;
		
	}
	
	/* Too costly, let's do something cheaper
	public Token simplifyByReduce(Context ctx, Solver solver, Token pred) {
		
		Computation comp = new Computation();
		Token result = pred;
		
		if (pred.getName().startsWith(Parser.AND)) {
			for (int j = 0 ; j < pred.getTokens().size(); j++) {
				Token temp = pred.clone();
				Token single = temp.getTokens().get(j).clone();
				temp.getTokens().remove(j);
				if (comp.imply(ctx, solver, temp, single)) {
					if (temp.getTokens().size() == 1) {
						temp = temp.getTokens().get(0);
					}
					return simplifyByReduce(ctx,solver,temp);
				}
			}	
		} else if (pred.getName().startsWith(Parser.OR)){
			for (int j = 0 ; j < pred.getTokens().size(); j++) {
				Token temp = pred.clone();
				Token single = temp.getTokens().get(j).clone();
				temp.getTokens().remove(j);
				if (comp.imply(ctx, solver, single, temp)) {
					if (temp.getTokens().size() == 1) {
						temp = temp.getTokens().get(0);
					}
					return simplifyByReduce(ctx,solver,temp);
				}
			}
		} 
		
		return result;
	}
	*/
	public ArrayList<Token> simplifyByReduce(Context ctx, Solver solver, ArrayList<Token> preds) {
		
		ArrayList<Token> result = new ArrayList<Token>();
		
		for (Token pred: preds) {
			result.add(simplifyByReduce(ctx,solver,pred));
		}
		return result;
	}
}
