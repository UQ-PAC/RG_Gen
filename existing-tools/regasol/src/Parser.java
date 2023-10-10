
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.microsoft.z3.*;

public class Parser {
	
	final public static String SKIP = "skip";
	final public static String ASSIGN = ":=";
	final public static String IF = "if";
	final public static String ELSE = "else";
	final public static String WHILE = "while";
	final public static String ATOMIC = "atomic";
	final public static String AWAIT = "await";
	final public static String END = "end-";
	final public static String DO = "do";
	final public static String BOOL = "bool";
	final public static String BLOCK = "block";
	final public static String EXPR = "expr";
	final public static String ERROR = "error";
	
	final public static char OB = '(';
	final public static char CB = ')';
	final public static String CONSTANT = "constant";
	final public static String VAR = "var";
	final public static String TRUE = "true";
	final public static String FALSE = "false";
	final public static String COM = "stm"; 
	
	final public static String EQ = "=";
	final public static String LE = "<=";
	final public static String GE = ">=";
	final public static String LT = "<";
	final public static String GT = ">";
	final public static String NEG = "!";
	final public static String AND = "&&";
	final public static String OR = "||";
	final public static String IMP = "=>";
	
	final public static String EMP = "empty";
	final public static String VAL = "value";
	
	final public static String PRED = "predicate";
	final public static String FORALL = "forall";
	final public static String EXIST = "exists";
	
	final public static String PROOF = "proof";

	private String findEnd(Scanner sc, String keywordS, String keywordE) {
		
		String result = "";
		int count = 1;
		while (sc.hasNextLine() && count > 0) {
			String line = sc.nextLine().trim();
			if (line.startsWith(keywordS)) {
				count++;
			}
			if (line.startsWith(keywordE)) {
				count--;
			}
			if (count > 0) {
				result = result + line + "\n";
			}
		}
		if (count > 0) {
			System.out.println("Parsing error in findEnd: " + result);
			return ERROR + " " + result;
		}
		result = result.substring(0,result.length()-1);
		return result;
	}
	
	public boolean isWellTyped(ArrayList<Token> program) {
		for (Token t: program) {
			if (t.getType().equals(ERROR) || !isWellTyped(t.subtokens)) {
				return false;
			}
		}
		
		return true;
	}
	
	public ArrayList<Token> parseCommands(String program){
		//System.out.println("parsing: " + program);
		
		ArrayList<Token> components = new ArrayList<Token>();
		Scanner scanner = new Scanner(program);
		while (scanner.hasNextLine()) {
			
			String line = scanner.nextLine().trim();

			if(line.equals("")) {continue;}
			Token t = new Token(EMP);
			if (line.startsWith("[") && line.endsWith("]")) {
				line = line.substring(1,line.length()-1);
				t = parsePredicate(line);
			} else if (line.startsWith(SKIP)) {
				t = new Token(COM,SKIP);
			} else if (line.contains(ASSIGN)) {
				t = new Token(COM,ASSIGN);
				String left = line.substring(0,line.indexOf(ASSIGN));
				String right = line.substring(left.length() + ASSIGN.length());
				Token leftT = parseExpression(left);
				Token rightT = parseExpression(right);
				t.addToken(leftT).addToken(rightT);
			} else if (line.startsWith(ATOMIC)) {
				String atomBlock = findEnd(scanner,ATOMIC,END+ATOMIC);
				if (atomBlock.startsWith(ERROR)) {
					t = new Token(ERROR,ATOMIC);
					t.addToken(new Token(atomBlock.substring(ERROR.length()).trim()));
				} else {
					t = new Token(COM,ATOMIC);
					Token atomToken = new Token(COM,BLOCK);
					for (Token aT: parseCommands(atomBlock)) {
						atomToken.addToken(aT);
					}
					t.addToken(atomToken);	
				}
			} else if (line.startsWith(AWAIT)) {
				String awaitBlock = findEnd(scanner,AWAIT,END+AWAIT);
				if (awaitBlock.startsWith(ERROR)) {
					t = new Token(ERROR,AWAIT);
					t.addToken(new Token(awaitBlock.substring(ERROR.length()).trim()));
				} else {
					t = new Token(COM,AWAIT);
					Token b = parseBoolean(line.substring(AWAIT.length(), line.length()-DO.length()).trim());
					//b.addToken(new Token (line.substring(AWAIT.length(), line.length()-DO.length()).trim()));
					t.addToken(b);
					Token awaitToken = new Token(COM,BLOCK);
					for (Token aT: parseCommands(awaitBlock)) {
						awaitToken.addToken(aT);
					}
					t.addToken(awaitToken);
				}
			} else if (line.startsWith(WHILE)) {
				String whileBlock = findEnd(scanner,WHILE,END+WHILE);
				if (whileBlock.startsWith(ERROR)) {
					t = new Token(ERROR,WHILE);
					t.addToken(new Token(whileBlock.substring(ERROR.length()).trim()));
					System.out.println(t);
					return null;
				} else {
					t = new Token(COM,WHILE);
					Token b = parseBoolean(line.substring(WHILE.length(), line.length()-DO.length()).trim());
					t.addToken(b);
					Token whileToken = new Token(COM,BLOCK);
					for (Token wT: parseCommands(whileBlock)) {
						whileToken.addToken(wT);
					}
					t.addToken(whileToken);
				}
			} else if (line.startsWith(IF)) {
				String ifBlock = findEnd(scanner,IF,ELSE);
				if (ifBlock.startsWith(ERROR)) {
					t = new Token(ERROR,IF);
					t.addToken(new Token(ifBlock.substring(ERROR.length()).trim()));
				} else {
					t = new Token(COM,IF);
					Token b = parseBoolean(line.substring(IF.length(), line.length()-DO.length()).trim());
					t.addToken(b);

					Token ifToken = new Token(COM,BLOCK);
					for (Token iT: parseCommands(ifBlock)) {
						ifToken.addToken(iT);
					}
					t.addToken(ifToken);
				}
				
				String elseBlock = findEnd(scanner,ELSE,END+IF);
				Token elseToken;
				if (elseBlock.startsWith(ERROR)) {
					elseToken = new Token(ERROR,ELSE);
					elseToken.addToken(new Token(elseBlock.substring(ERROR.length()).trim()));
				} else {
					elseToken = new Token(COM,BLOCK);
					for (Token eT: parseCommands(elseBlock)) {
						elseToken.addToken(eT);
					}
				}
				t.addToken(elseToken);
			} else {
				System.out.println("\n+++++++++++++++++++++\n"
						+ "Cannot recognize commmand: " + line + ""
								+  "\n+++++++++++++++++++++\n");
				t = new Token(ERROR,COM);
				t.addToken(new Token(VAL,line));
			}
			
			components.add(t);
		}
		
		scanner.close();
		return components;
	}
	
	public Token parseProgram(String program) {
		Token result = new Token(COM,BLOCK);
		for (Token t: parseCommands(program)) {
			result.addToken(t);
		}
		return result;
	}
	
	public boolean isBalanced(String input) {
		int count = 0;
		for (int i=0;i < input.length(); i++) {
			if (input.charAt(i) == OB) {
				count++;
			}
			if (input.charAt(i) == CB) {
				count--;
			}
			if (count < 0) return false;
		}
		if (count == 0) return true;
		return false;
	}
	
	private String removeBrackets(String input) {
		input = input.trim();
		if (input.startsWith(OB+"") && input.endsWith(CB+"") && isBalanced(input.substring(1,input.length()-1))) {
			return removeBrackets(input.substring(1,input.length()-1));
		}
		
		return input;
	}
	
	public int findNextOpIndex(String exp) {
		int count = 0;
		for (int i = 0; i < exp.length(); i++) {
			if (exp.charAt(i) == OB) {
				count++;
			}
			if (exp.charAt(i) == CB) {
				count--;
			}
			if (count == 0 && i > 0 && (exp.charAt(i) == '+' || exp.charAt(i) == '-' || 
					exp.charAt(i) == '*' || exp.charAt(i) == '/') || exp.charAt(i) == '%') {
				return i;
			}
		}
		
		return -1;
	}
	
	public String findNextOp(String exp, int index) {
		
		if (index >= exp.length()) {
			System.out.println("Index out of range error: " + index + " " + exp);
			return "";
		}
		
		String result = exp.substring(index,index+1);
		return result;
	}
	
	public Token parseExpression(String expr){
		
		Token result = new Token(EXPR);
		expr = removeBrackets(expr.trim()).trim();
		String constants = "-{0,1}[0-9]+";
		Pattern pat = Pattern.compile(constants);
		int index = findNextOpIndex(expr);
		
		if (index < 0) {
			if (expr.startsWith("-")) {
				result.setName("-").addToken(parseExpression(expr.substring("-".length())));
			}
			Matcher match = pat.matcher(expr);
			 if (match.matches()) {
				 result.setName(CONSTANT).addToken(new Token(VAL,expr));
			 } else {
				 result.setName(VAR).addToken(new Token(VAL,expr));
			 }
		} else {
			String op = findNextOp(expr,index);
			String left = expr.substring(0,index);
			String right = expr.substring(index + op.length());
			Token leftT = parseExpression(left);
			Token rightT = parseExpression(right);
			result.setName(op).addToken(leftT).addToken(rightT);
		}

		return result;
	}
	
	private int findNextBoolOp(String bexpr) {
		
		int count = 0;
		for (int i =0; i < bexpr.length(); i++) {
			if (bexpr.charAt(i) == OB) {
				count++;
			}
			if (bexpr.charAt(i) == CB) {
				count--;
			}
			if (count == 0 && (bexpr.charAt(i) == '&'||bexpr.charAt(i) == '|')) {
				return i;
			}
		}
		
		return -1;
	}
	
	public Token parsePredicate(String pexpr) {
		
		Token result = new Token(PRED);
		pexpr = removeBrackets(pexpr.trim()).trim();
		
		if (pexpr.startsWith(FORALL)||pexpr.startsWith(EXIST)) {
			if (pexpr.startsWith(FORALL)) {
				result.setName(FORALL);
			} else {
				result.setName(EXIST);
			}
			int subindex = pexpr.indexOf(":");
			if (subindex < 0) {
				return new Token(ERROR,PRED).addToken(new Token(VAL,pexpr));
			} else {
				String[] vars = pexpr.substring(0, subindex).split("\\s+");
				for (String s: vars) {
					if(!s.equals(FORALL) && !s.equals(EXIST)) {
						result.addToken(new Token(EXPR,VAR).addToken(new Token(VAL,s)));
					}
				}
				String subpexpr = pexpr.substring(subindex+1);
				result.addToken(parsePredicate(subpexpr));
				
			}
		} else if (!pexpr.contains(FORALL) && !pexpr.contains(EXIST)) {
			return bool2Pred(parseBoolean(pexpr));			
		} else if (pexpr.startsWith(NEG)) {
			result.setName(NEG).addToken(parsePredicate(pexpr.substring(NEG.length())));
		} else if (findNextBoolOp(pexpr) >=0) {
			int index = findNextBoolOp(pexpr);
			String left = pexpr.substring(0,index);
			String right = pexpr.substring(index + 1);
			Token leftT = parsePredicate(left);
			Token rightT = parsePredicate(right);
			result.setName(pexpr.substring(index,index+1)).addToken(leftT).addToken(rightT);
		}
			
		
		return result;
	}
	
	public Token bool2Pred(Token b) {
		Token result = new Token(PRED);
		if (b.getType().equals(BOOL)) {
			result.setName(b.getName());
			for (Token s: b.getTokens()) {
				result.addToken(bool2Pred(s));
			}
		} else {
			return b;
		}
		
		return result;
	}
	
	public Token parseBoolean(String bexpr){
		
		Token result = new Token(BOOL);
		bexpr = removeBrackets(bexpr.trim()).trim();
		int index = findNextBoolOp(bexpr);
		
		if (index < 0) {
			if (bexpr.equals(TRUE) || bexpr.equals(FALSE)) {
				result.setName(bexpr);
			} else if (bexpr.startsWith(NEG)) {
				result.setName(NEG).addToken(parseBoolean(bexpr.substring(NEG.length())));
			} else if (bexpr.contains(LE)) {
				int eqIndex = bexpr.indexOf(LE);
				String left = bexpr.substring(0,eqIndex);
				String right = bexpr.substring(eqIndex + LE.length());
				Token leftT = parseExpression(left);
				Token rightT = parseExpression(right);
				result.setName(LE).addToken(leftT).addToken(rightT);
			} else if (bexpr.contains(GE)) {
				int eqIndex = bexpr.indexOf(GE);
				String left = bexpr.substring(0,eqIndex);
				String right = bexpr.substring(eqIndex + GE.length());
				Token leftT = parseExpression(left);
				Token rightT = parseExpression(right);
				result.setName(GE).addToken(leftT).addToken(rightT);
			} else if (bexpr.contains(EQ)) {
				int eqIndex = bexpr.indexOf(EQ);
				String left = bexpr.substring(0,eqIndex);
				String right = bexpr.substring(eqIndex + EQ.length());
				Token leftT = parseExpression(left);
				Token rightT = parseExpression(right);
				result.setName(EQ).addToken(leftT).addToken(rightT);
			} else if (bexpr.contains(LT)) {
				int eqIndex = bexpr.indexOf(LT);
				String left = bexpr.substring(0,eqIndex);
				String right = bexpr.substring(eqIndex + LT.length());
				Token leftT = parseExpression(left);
				Token rightT = parseExpression(right);
				result.setName(LT).addToken(leftT).addToken(rightT);
			} else if (bexpr.contains(GT)) {
				int eqIndex = bexpr.indexOf(GT);
				String left = bexpr.substring(0,eqIndex);
				String right = bexpr.substring(eqIndex + GT.length());
				Token leftT = parseExpression(left);
				Token rightT = parseExpression(right);
				result.setName(GT).addToken(leftT).addToken(rightT);
			}
		} else {
			String left = bexpr.substring(0,index);
			String right = bexpr.substring(index + 2);
			Token leftT = parseBoolean(left);
			Token rightT = parseBoolean(right);
			result.setName(bexpr.substring(index,index+2)).addToken(leftT).addToken(rightT);
		}

		return result;
	}
	
	public ArrayList<String> getTokens(String expr){
		
		ArrayList<String> result = new ArrayList<String>();
		expr = expr.trim();
		while (!expr.equals("")) {
			if (expr.startsWith("(")) {
				int count = 1;
				int i = 0;
				for (i = 1 ; i < expr.length() && count > 0; i++) {
					if (expr.charAt(i) == '(') {
						count++;
					}
					if (expr.charAt(i) == ')') {
						count--;
					}
				}
				if (count == 0) {
					String token = expr.substring(1,i-1);
					result.add(token);
					expr = expr.substring(i);
				} else {
					System.out.println("Error");
					return result;
				}
			} else {
				if (expr.indexOf(" ") >= 0) {
					String token = expr.substring(0,expr.indexOf(" "));
					result.add(token);
					expr = expr.substring(expr.indexOf(" "));
				} else {
					result.add(expr);
					expr = "";
				}
			}
			expr = expr.trim();
		}

		
		return result;
	}
	//TOBE DONE
	public Token parseSmtExpr(String expr) {

		
		Token result = new Token(EXPR);
		expr = removeBrackets(expr.trim()).trim();
		String constants = "-{0,1}[0-9]+";
		Pattern pat = Pattern.compile(constants);
		

		if (expr.startsWith("+") || expr.startsWith("-") || 
				expr.startsWith("*") || expr.startsWith("/") || expr.startsWith("mod")) {

			String op = "";
			
			if (expr.startsWith("mod")) {
				op = "%";
				expr = expr.substring(3);
			} else {
				op = expr.substring(0,1);
				expr = expr.substring(op.length());
			}
			result.setName(op);
			for (String token: getTokens(expr)) {
				result.addToken(parseSmtExpr(token));
			}
		}  else {
			Matcher match = pat.matcher(expr);
			 if (match.matches()) {
				 result.setName(CONSTANT).addToken(new Token(VAL,expr));
			 } else {
				 result.setName(VAR).addToken(new Token(VAL,expr));
			 }
		}

		return result;
	}
	
	public Token parseSmtPred(String pred) {
		
		Token result = new Token(PRED);
		
		pred = removeBrackets(pred).trim();

		
		if (pred.startsWith("forall") || pred.startsWith("exists")) {
			ArrayList<String> tokens = getTokens(pred);
			String op = tokens.get(0);
			tokens.remove(0);
			result.setName(op);
			ArrayList<String> vars = getTokens(tokens.get(0));
			for (String v: vars) {
				result.addToken(parseSmtExpr(v.substring(0,v.indexOf(" "))));
			}
			result.addToken(parseSmtPred(tokens.get(1)));
		} else if (pred.startsWith(TRUE) || pred.startsWith(FALSE)) {
			result.setName(pred);
		} else {
			ArrayList<String> tokens = getTokens(pred);
			String op = tokens.get(0);
			tokens.remove(0);
			if (op.startsWith(EQ) || op.startsWith(LT) || op.startsWith(GT) ||
					op.startsWith(LE) || op.startsWith(GE)) {
				result.setName(op);
				for (String t: tokens) {
					result.addToken(parseSmtExpr(t));
				}
			} else if (op.startsWith("not")) {
				result.setName(NEG);
				for (String t: tokens) {
					result.addToken(parseSmtPred(t));
				}
			} else if (op.startsWith("and") || op.startsWith("or") || op.startsWith("=>")) {
				if (op.startsWith("and")) {
					result.setName(AND);
				}
				if (op.startsWith("or")) {
					result.setName(OR);
				}
				
				if (op.startsWith("=>")) {
					result.setName(IMP);
				}

				for (String t: tokens) {
					result.addToken(parseSmtPred(t));
				}
			} else {
				result = parseSmtExpr(pred);
			}
		}
		
		return result;
		
	}

	//Extract a list of equalities from the result
	public ArrayList<Token> parseSmtResult(String satResult){
		
		ArrayList<Token> result = new ArrayList<Token>();
		Scanner sc = new Scanner(satResult);
		String line = sc.nextLine();
		
		if(line.startsWith("unsat")) {
			result.add(new Token("result","unsat"));
		} else {
			result.add(new Token("result","sat"));
			//sc.nextLine();//Remove the "(model" line
			while (sc.hasNextLine()) {
				String current = sc.nextLine().trim();
				if (current.startsWith("(model") || current.equals(")")) {
					continue;
				}
				String[] words = current.split("\\s+");

				if(words[0].length() > 0 && words[0].charAt(0) != CB && words.length > 1) {
					String value = sc.nextLine();
					value = value.substring(0,value.length()-1).trim();
					Token t = new Token(BOOL,EQ);
					t.addToken(new Token(EXPR,VAR).addToken(new Token(VAL,words[1])));
					t.addToken(new Token(EXPR,CONSTANT).addToken(new Token(VAL,value)));
					result.add(t);
				}
			}
		}
		sc.close();
		return result;
	}
	
	public ArrayList<Boolean> parseSmtResults(String satResults){
		ArrayList<Boolean> result = new ArrayList<Boolean>();
		Scanner sc = new Scanner(satResults);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.startsWith("sat")) {
				result.add(true);
			} else {
				result.add(false);
			}
		}
		
		sc.close();
		
		return result;
	}
	
	public ArithExpr parseArithExpr(Context ctx,Token pred) {
		
		ArithExpr result;
	
		if (pred.getName().equals(CONSTANT)) {
			result = ctx.mkInt(pred.getTokens().get(0).getName());
			return result;
		}
		
		if (pred.getName().equals(VAR)) {
			result = (ArithExpr) ctx.mkConst(pred.getTokens().get(0).getName(),
	                ctx.mkIntSort());
			return result;
		}
		
		if (pred.getName().equals("+")) {
			ArithExpr[] components = new ArithExpr[pred.getTokens().size()];
			int i=0;
			for (Token t : pred.getTokens()) {
				components[i++] = parseArithExpr(ctx,t);
			}
			result = ctx.mkAdd(components);
			return result;
		}
		
		if (pred.getName().equals("-")) {
			ArithExpr[] components = new ArithExpr[pred.getTokens().size()];
			int i=0;
			for (Token t : pred.getTokens()) {
				components[i++] = parseArithExpr(ctx,t);
			}
			result = ctx.mkSub(components);
			return result;
		}
		
		if (pred.getName().equals("*")) {
			ArithExpr[] components = new ArithExpr[pred.getTokens().size()];
			int i=0;
			for (Token t : pred.getTokens()) {
				components[i++] = parseArithExpr(ctx,t);
			}
			result = ctx.mkMul(components);
			return result;
		}
		
		if (pred.getName().equals("/")) {
			ArithExpr[] components = new ArithExpr[pred.getTokens().size()];
			int i=0;
			for (Token t : pred.getTokens()) {
				components[i++] = parseArithExpr(ctx,t);
			}
			result = ctx.mkDiv(components[0], components[1]);
			return result;
		}
		
		if (pred.getName().equals("mod")) {
			IntExpr[] components = new IntExpr[pred.getTokens().size()];
			int i=0;
			for (Token t : pred.getTokens()) {
				components[i++] = (IntExpr)parseArithExpr(ctx,t);
			}
			result = ctx.mkMod(components[0], components[1]);
			return result;
		}
		
		System.out.println("Error in parseArithExpr with input " + pred.toString());
		return null;
	}
	
	public BoolExpr parseBoolExpr(Context ctx, Token pred) {
		
		BoolExpr result;
		Printer printer = new Printer();
		//System.out.println(printer.printPredicate(pred));
		
		if (pred.getName().equals(TRUE)) {
			result = ctx.mkTrue();
			return result;
		}
		
		if (pred.getName().equals(FALSE)) {
			result = ctx.mkFalse();
			return result;
		}
		
		if(pred.getName().equals(NEG)) {
			result = ctx.mkNot(parseBoolExpr(ctx,pred.getTokens().get(0)));
			return result;
		}
		
		if (pred.getName().equals(AND)) {
			BoolExpr[] components = new BoolExpr[pred.getTokens().size()];
			int i=0;
			for (Token t : pred.getTokens()) {
				components[i++] = parseBoolExpr(ctx,t);
			}
			result = ctx.mkAnd(components);
			return result;
		}
		
		if (pred.getName().equals(OR)) {
			BoolExpr[] components = new BoolExpr[pred.getTokens().size()];
			int i=0;
			for (Token t : pred.getTokens()) {
				components[i++] = parseBoolExpr(ctx,t);
			}
			result = ctx.mkOr(components);
			return result;
		}
		
		if (pred.getName().equals(IMP)) {
			BoolExpr[] components = new BoolExpr[pred.getTokens().size()];
			int i=0;
			for (Token t : pred.getTokens()) {
				components[i++] = parseBoolExpr(ctx,t);
			}
			result = ctx.mkImplies(components[0],components[1]);
			return result;
		}
		
		if (pred.getName().equals(EQ)) {
			result = ctx.mkEq(parseArithExpr(ctx, pred.getTokens().get(0)), 
					          parseArithExpr(ctx, pred.getTokens().get(1)));
			return result;
		}
		
		if (pred.getName().equals(LT)) {
			result = ctx.mkLt(parseArithExpr(ctx, pred.getTokens().get(0)), 
					          parseArithExpr(ctx, pred.getTokens().get(1)));
			return result;
		}
		
		if (pred.getName().equals(GT)) {
			result = ctx.mkGt(parseArithExpr(ctx, pred.getTokens().get(0)), 
					          parseArithExpr(ctx, pred.getTokens().get(1)));
			return result;
		}
		
		if (pred.getName().equals(LE)) {
			result = ctx.mkLe(parseArithExpr(ctx, pred.getTokens().get(0)), 
					          parseArithExpr(ctx, pred.getTokens().get(1)));
			return result;
		}
		
		if (pred.getName().equals(GE)) {
			result = ctx.mkGe(parseArithExpr(ctx, pred.getTokens().get(0)), 
					          parseArithExpr(ctx, pred.getTokens().get(1)));
			return result;
		}
		
		System.out.println("Error in parseBoolExpr with input " + pred.toString());
		return null;
	}
	
	public static void main(String[] args) {

		Parser p = new Parser();
		Printer pr = new Printer();
		
		Token t = p.parseSmtPred("(exists ((x Type) (y Int)) (&& (= x x) (<= 1 y)))");
		System.out.println(t);
		System.out.println(pr.printPredicate(t));
	
	}

}
