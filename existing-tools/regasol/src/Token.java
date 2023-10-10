import java.util.ArrayList;

public class Token {
	
	ArrayList<Token> subtokens;
	String name;
	String type;
	
	public Token(String type) {
		subtokens = new ArrayList<Token>();
		this.type = type;
		name = "";
	}
	
	public Token(String type, String name) {
		subtokens = new ArrayList<Token>();
		this.type = type;
		this.name = name;
	}
	
	public Token(String type, String name, ArrayList<Token> tokens) {
		subtokens = tokens;
		this.name = name;
		this.type = type;
	}
	
	public Token setName(String newName) {
		name = newName;
		return this;
	}
	
	public Token setTokens(ArrayList<Token> tokens) {
		subtokens = tokens;
		return this;
	}
	
	public Token setToken(Token token) {
		name = token.getName();
		type = token.getType();
		subtokens = token.getTokens();
		return this;
	}
	
	public Token addToken(Token t) {
		subtokens.add(t);
		return this;
	}
	
	public Token addTokens(ArrayList<Token> tokens) {
		for (Token t: tokens) {
			subtokens.add(t);
		}
		return this;
	}
	
	public String getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<Token> getTokens(){
		return subtokens;
	}
	
	public String toString() {
		String result = "[" + name + ": " + type;
		for (Token t: subtokens) {
			result = result + "," + t.toString(); 
		}
		result = result + "]";
		return result;
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof Token)) {
			return false;
		}
		Token t = (Token) obj;
		if (!name.equals(t.getName()) || !type.equals(t.getType()) || subtokens.size() != t.getTokens().size()) {
			return false;
		}
		for (int i=0;i<subtokens.size();i++) {
			if (!subtokens.get(i).equals(t.getTokens().get(i))) {
				return false;
			}
		}
		
		return true;
	}
/*	
	public boolean equals(Token t) {
		if (!name.equals(t.getName()) || !type.equals(t.getType()) || subtokens.size() != t.getTokens().size()) {
			System.out.println("DD");
			return false;
		}
		for (int i=0;i<subtokens.size();i++) {
			if (!subtokens.get(i).equals(t.getTokens().get(i))) {
				return false;
			}
		}
		
		return true;
	}
*/	
	public Token clone(){  
		Token cloneToken = new Token(type,name);
		for (Token t: subtokens) {
			cloneToken.addToken(t.clone());
		}
		return cloneToken;
	}  
	
	public boolean isPredicate() {
		return this.getType().startsWith(Parser.PRED);
	}
	
	public Token getError() {
		return new Token(Parser.PRED,Parser.ERROR);
	}
	
	public Token combine(String op, Token first, Token second) {
		if (!first.isPredicate() || !second.isPredicate()) {
			System.out.println("Failure in Token.conj");
			return getError();
		}
		
		Token result = new Token(Parser.PRED,op);
		return result.addToken(first).addToken(second).clone();
	}
	
	public Token combineL(String op, ArrayList<Token> tokens) {
		for (Token t: tokens) {
			if (!t.isPredicate()) {
				System.out.println("Failure in Token.conj");
				return getError();
			}
		}	
		if (tokens.size() == 0) {
			return new Token(Parser.PRED,Parser.TRUE);
		}
		if (tokens.size() == 1) {
			return tokens.get(0).clone();
		}
		Token result = new Token(Parser.PRED,op);

		for (Token t: tokens) {
			result.addToken(t);
		}
		return result.clone();
	}
	
	public Token negate() {
		if (!this.isPredicate()) {
			System.out.println("Error in Token.negate");
			return getError();
		}
		
		Token result = new Token(Parser.PRED,Parser.NEG);
		result.addToken(this);
		return result.clone();
	}

}
