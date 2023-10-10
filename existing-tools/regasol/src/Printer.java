import java.io.*;
import java.util.*;

public class Printer {
	
	public String convert2smt(Token pred) {
		
		String result = "";
		if(pred.getName().equals(Parser.FORALL)) {
			result = result + "(forall ";
			String vars = "";
			for (int i=0;i<pred.getTokens().size()-1;i++) {
				vars = vars + "(" + pred.getTokens().get(i).getTokens().get(0).getName() + " Int) "; 
			}
			vars = "(" + vars + ")";
			result = result + vars + convert2smt(pred.getTokens().get(pred.getTokens().size()-1)) + ")";
		} else if (pred.getName().equals(Parser.TRUE) || pred.getName().equals(Parser.FALSE)) {
			result = "(" + pred.getName() + ")";
		} else if (pred.getName().equals(Parser.AND)) {
			for (Token t: pred.getTokens()) {
				result = result + " " + convert2smt(t);
			}
			result = "(and" + result + ")";
		} else if (pred.getName().equals(Parser.OR)) {
			for (Token t: pred.getTokens()) {
				result = result + " " + convert2smt(t);
			}
			result = "(or" + result + ")";
		} else if (pred.getName().equals(Parser.NEG)) {
			for (Token t: pred.getTokens()) {
				result = result + " " + convert2smt(t);
			}
			result = "(not" + result + ")";
			
		} else if (pred.getName().equals(Parser.EQ) || 
				pred.getName().equals(Parser.LE) || pred.getName().equals(Parser.GE) || 
				pred.getName().equals(Parser.LT) || pred.getName().equals(Parser.GT)) {
			for (Token t: pred.getTokens()) {
				result = result + " " + convert2smt(t);
			}
			result = "(" + pred.getName() + result + ")";	
		} else if (pred.getName().equals("+") || pred.getName().equals("-") || 
				pred.getName().equals("*") || pred.getName().equals("/") || pred.getName().equals("%")) {
			for (Token t: pred.getTokens()) {
				result = result + " " + convert2smt(t);
			}
			
			String op = pred.getName();
			
			if (pred.getName().startsWith("%")) {
				op = "mod";
			}
			
			result = "(" + op + result + ")";	
			//System.out.println("Result:" + result);
		} else if (pred.getName().equals(Parser.VAR) || pred.getName().equals(Parser.CONSTANT)) {
			result = pred.getTokens().get(0).getName();
		}
		return result;
	}
	
	// Z3 cannot recognize (false) or (true) and it is too complicated to fix the parser now....
	public String modifiedConvert2smt(Token pred) {
		return convert2smt(pred).replaceAll("\\(true\\)",Parser.TRUE).replaceAll("\\(false\\)",Parser.FALSE);
	}
	
	//Only support Int type at the moment
	public String declareVars(ArrayList<String> vars) {
		
		String result = "";
		for (String s: vars) {
			result = result + "(declare-const " + s + " Int)\n";
		}
		
		return result;
	}
	
	public ArrayList<String> getFreeVars(ArrayList<Token> preds){
		ArrayList<String> result = new ArrayList<String>();
		
		for (Token pred: preds) {
			for (String candidate: getFreeVars(pred)) {
				if (!result.contains(candidate)) {
					result.add(candidate);
				}
			}
		}
		
		return result;
	}
	
	public ArrayList<String> getFreeVars(Token pred) {
		
		ArrayList<String> result = new ArrayList<String>();
		if (pred.getName().equals(Parser.FORALL) || pred.getName().equals(Parser.EXIST)) {
			ArrayList<String> bounded = new ArrayList<String>();
			for (int i=0;i<pred.getTokens().size()-1;i++) {// assume no aliasing with bounded variables
				bounded.add(pred.getTokens().get(i).getTokens().get(0).getName());
			}
			
			for (String s: getFreeVars(pred.getTokens().get(pred.getTokens().size()-1))) {
				if (!bounded.contains(s)) {
					result.add(s);
				}
			}
		} else if (pred.getName().equals(Parser.VAR)) {
			result.add(pred.getTokens().get(0).getName());
		} else if (pred.getName().equals(Parser.CONSTANT)) {
			return result;
		} else {
			for (Token t: pred.getTokens()) {
				for (String s: getFreeVars(t)) {
					if (!result.contains(s)) result.add(s);
				}
			}
		}
		return result;
	}
	
	public String generateSmtConstraint(Token pred) {
		String result = "";
		result = result + declareVars(getFreeVars(pred));
		result = result + "\n(assert " + modifiedConvert2smt(pred) + ")";
		result = result + "\n" + "(check-sat-using (then qe smt))\n(get-model)";
		return result;
	}
	
	public String generateSimplifiedConstraint(Token pred, String option) {
		String result = "";
		result = result + declareVars(getFreeVars(pred));
		result = result + "\n(simplify " + modifiedConvert2smt(pred) + " " + option + ")";
		return result;
	}
	
	public String generateSimplifiedConstrains(ArrayList<Token> preds, String option) {
		String result = "";
		ArrayList<String> vars = new ArrayList<String>();
		for (Token pred: preds) {
			for (String v: getFreeVars(pred)) {
				if (!vars.contains(v)) {
					vars.add(v);
				}
			}
			result = result + "\n(simplify " + modifiedConvert2smt(pred) + " " + option + ")";
		}
		
		result = declareVars(vars) + result;
		
		return result;
	}
	
	public void writeToFile(String content, String fileName){
		try {
		    FileWriter fileWriter = new FileWriter(fileName);
		    PrintWriter printWriter = new PrintWriter(fileWriter);
		    printWriter.print(content);
		    printWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public String printPredicate(Token pred) {
		
		String result = "";
		
		if (pred.getName().startsWith(Parser.CONSTANT) || pred.getName().startsWith(Parser.VAR)) {
			return pred.getTokens().get(0).getName();
		}
		
		if (pred.getName().startsWith("+") || pred.getName().startsWith("-") ||
				pred.getName().startsWith("*") || pred.getName().startsWith("/") || 
				pred.getName().startsWith("%")) {
			for (Token t: pred.getTokens()) {
				result = result + pred.getName() + printPredicate(t);
			}
			result = result.substring(pred.getName().length());
			result = "(" + result + ")";
			return result;
		}
		
		if (pred.getName().startsWith(Parser.EQ) || pred.getName().startsWith(Parser.LT) ||
				pred.getName().startsWith(Parser.GT) || pred.getName().startsWith(Parser.LE) ||
				pred.getName().startsWith(Parser.GE)) {
			for (Token t: pred.getTokens()) {
				String s = printPredicate(t);
				if (s.startsWith("(")&& s.endsWith(")")){
					s = s.substring(1,s.length()-1);
				}
				result = result + pred.getName() + s;
			}
			result = result.substring(pred.getName().length());
			//result = "(" + result + ")";
			return result;
		}
		
		if (pred.getName().startsWith(Parser.TRUE) || pred.getName().startsWith(Parser.FALSE)) {
			result = pred.getName();
			return result;
		}
		
		if (pred.getName().startsWith(Parser.AND) || pred.getName().startsWith(Parser.OR)) {
			for (Token t: pred.getTokens()) {
				result = result + pred.getName() + printPredicate(t);
			}
			result = result.substring(pred.getName().length());
			result = "(" + result + ")";
			return result;
		}
		
		if (pred.getName().startsWith(Parser.NEG)) {
			result = pred.getName() + "(" + printPredicate(pred.getTokens().get(0)) + ")";
			return result;
		}
		
		if (pred.getName().startsWith(Parser.FORALL) || pred.getName().startsWith(Parser.EXIST)) {
			result = result + pred.getName();
			for (int i = 0 ; i < pred.getTokens().size()-1; i++) {
				result = result + " " + printPredicate(pred.getTokens().get(i));
			}
			result = result + ":" + printPredicate(pred.getTokens().get(pred.getTokens().size()-1));
			return result;
		}
		
		return result;

	}
	
	public String printCommand(Token stmt) {
		return printCommandWithIndent(stmt,0);
	}
	
	public String printCommandWithIndent(Token stmt,int spaces) {
		
		String result = "";
		String indent = "";
		for (int i = 0 ; i < spaces ; i ++) {
			indent = indent + " ";
		}
		
		if (stmt.getType().startsWith(Parser.PRED)) {
			result = indent + "[" + printPredicate(stmt) + "]";
			return result;
		}
		
		if (stmt.getName().startsWith(Parser.SKIP)) {
			result = indent + stmt.getName();
			return result;
		}
		
		if (stmt.getName().startsWith(Parser.ASSIGN)) {
			String left = printPredicate(stmt.getTokens().get(0));
			String right = printPredicate(stmt.getTokens().get(1));
			if (left.startsWith("(") && left.endsWith(")")) {
				left = left.substring(1,left.length()-1);
			}
			if (right.startsWith("(") && right.endsWith(")")) {
				right = right.substring(1,right.length()-1);
			}
			result = indent + left + stmt.getName() + right;
			return result;
		}
		
		if (stmt.getName().startsWith(Parser.BLOCK)) {
			for (Token t: stmt.getTokens()) {
				result = result + "\n" + printCommandWithIndent(t,spaces);
			}
			result = result.substring(1);
			return result;
		}
		
		if (stmt.getName().startsWith(Parser.ATOMIC)) {
			result = indent + stmt.getName();
			result = result + "\n" + printCommandWithIndent(stmt.getTokens().get(0),spaces+1) + "\n";
			result = result + indent + Parser.END + stmt.getName();
			return result;
		}
		
		if (stmt.getName().startsWith(Parser.AWAIT)) {
			result = indent + stmt.getName() + " " + printPredicate(stmt.getTokens().get(0)) + " " + Parser.DO;
			result = result + "\n" + printCommandWithIndent(stmt.getTokens().get(1),spaces+1) + "\n";
			result = result + indent + Parser.END + stmt.getName();
			return result;
		}
		
		if (stmt.getName().startsWith(Parser.WHILE)) {
			result = indent + stmt.getName() + " " + printPredicate(stmt.getTokens().get(0)) + " " + Parser.DO;
			result = result + "\n" + printCommandWithIndent(stmt.getTokens().get(1),spaces+1) + "\n";
			result = result + indent + Parser.END + stmt.getName();
			return result;
		}
		
		if (stmt.getName().startsWith(Parser.IF)) {
			result = indent + stmt.getName() + " " + printPredicate(stmt.getTokens().get(0)) + " " + Parser.DO;
			result = result + "\n" + printCommandWithIndent(stmt.getTokens().get(1),spaces+1) + "\n";
			result = result + indent + Parser.ELSE;
			result = result + "\n" + printCommandWithIndent(stmt.getTokens().get(2),spaces+1) + "\n";
			result = result + indent + Parser.END + stmt.getName();
			return result;
		}
		
		return result;
	}
}
