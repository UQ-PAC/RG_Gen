import java.util.ArrayList;

public class Relation {
	
	private ArrayList<Pair<Token,Token>> pairList = new ArrayList<Pair<Token,Token>>();
	
	public Relation() {}
	
	public Relation(ArrayList<Pair<Token,Token>> list) {
		pairList = list;
	}
	
	public Relation(Token t1, Token t2) {
		pairList.add(new Pair<Token,Token>(t1,t2));
	}
	
	public Relation(Pair<Token,Token> pair) {
		pairList.add(pair);
	}
	
	public ArrayList<Pair<Token,Token>> getRelation(){
		return pairList;
	}
	
	public Relation setRelation(ArrayList<Pair<Token,Token>> list) {
		pairList = list;
		return this;
	}
	
	public Relation addPair(Pair<Token,Token> pair) {
		pairList.add(pair);
		return this;
	}
	
	public Relation addPair(Token fst, Token snd) {
		pairList.add(new Pair<Token,Token>(fst,snd));
		return this;
	}
	
	public Relation addPairs(ArrayList<Pair<Token,Token>> list) {
		for (Pair<Token,Token> p : list) {
			if(!pairList.contains(p) && !p.getFst().getName().equals(Parser.FALSE) 
					&& !p.getSnd().getName().equals(Parser.FALSE) && !p.getFst().equals(p.getSnd())) {
				pairList.add(p);
			}
		}
		return this;
	}
	
	public Relation union(Relation r) {
		return addPairs(r.getRelation());
	}
	
	public String toString() {
		String result = "";
		Printer printer = new Printer();
		for (Pair<Token,Token> t : getRelation()) {
			result = result + printer.printPredicate(t.getFst()) + " ---> " + printer.printPredicate(t.getSnd()) + "\n";
		}
		return result;
	}
	
	public Relation substitutePre(ArrayList<Token> subList) {
		Relation result = clone();
		SeqProver seqProver = new SeqProver();

		for (Token eq: subList) {
			if (eq.getName().startsWith(Parser.EQ)) {
				Relation temp = new Relation();
				for (Pair<Token,Token> p : result.getRelation()) {
					Token fst = seqProver.substitute(eq, p.getFst().clone());
					//System.out.println(">>" + fst);
					Token snd = seqProver.substitute(eq, p.getSnd().clone());
					temp.addPair(fst, snd);
				}
				result = temp;
			}
		}
		return result;
	}
	
	public Relation substitute(ArrayList<Token> subList) {
		String prefix = "fresh";
		int count = 0;
		Parser par = new Parser();
		Relation result = new Relation();
		ArrayList<Token> fstList = new ArrayList<Token>();
		ArrayList<Token> sndList = new ArrayList<Token>();
		for (Token eq: subList) {
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
		
		result = substitutePre(fstList);
		result = result.substitutePre(sndList);
		return result;
	}
	
	public Relation clone() {
		Relation cloneRel = new Relation();
		for (Pair<Token,Token> p: pairList) {
			cloneRel.getRelation().add(new Pair<Token,Token>(p.getFst().clone(),p.getSnd().clone()));
		}	
		return cloneRel;
	}
}
