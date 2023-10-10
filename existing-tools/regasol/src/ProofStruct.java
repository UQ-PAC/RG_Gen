import java.util.ArrayList;

public class ProofStruct {

	public ArrayList<Integer> URIndex;
	public ArrayList<Token> currentUR;
	public Token provedProgram;
	Relation guarantee;
	Relation rely;
	Relation relyClosure;
	//to record the primitive assertion that violates the stability
	public Token uPrimitive = new Token(Parser.PRED,Parser.FALSE);
	
	public ProofStruct() {
		URIndex =  null;
		provedProgram = new Token(Parser.COM);
		guarantee = new Relation();
		rely = new Relation();
		relyClosure = new Relation();
	}
	
	public ProofStruct(Token program) {
		URIndex =  null;
		provedProgram = program;
		guarantee = new Relation();
		rely = new Relation();
		relyClosure = new Relation();
	}
	
	public ProofStruct(Token provedP, Relation g, Relation r, ArrayList<Integer> index, ArrayList<Token> ur) {
		provedProgram = provedP;
		guarantee = g;
		rely = r;
		URIndex = index;
		currentUR = ur;
		relyClosure = new Relation();
	}
	
	public void setUR(ArrayList<Integer> index, ArrayList<Token> newUR) {
		URIndex = index;
		currentUR = newUR;
	}
	
	public void setUP(Token assertion) {
		uPrimitive = assertion;
	}
	
	public void setRelyClosure(Relation closure) {
		relyClosure = closure;
	}
	
	public void setG(Relation newG) {
		guarantee = newG;
	}
	
	public void setR(Relation newR) {
		rely = newR;
	}
	
	public void setProof(Token newProof) {
		provedProgram = newProof;
	}
	
	public ProofStruct clone() {
		@SuppressWarnings("unchecked")
		ProofStruct cl = new ProofStruct(provedProgram.clone(),guarantee.clone(),rely.clone(),
				(ArrayList<Integer>) URIndex.clone(), (ArrayList<Token>) currentUR.clone());
		return cl;
	}
	
	public ProofStruct substitute(ArrayList<Token> subList) {

		SeqProver sprover = new SeqProver();
		ProofStruct result = new ProofStruct(sprover.substituteProg(subList, provedProgram));
		result.setG(guarantee.substitute(subList));
		result.setR(rely.substitute(subList));
		result.setRelyClosure(relyClosure.substitute(subList));
		
		if (currentUR != null) {
			result.currentUR = new ArrayList<Token>();
			for (int i = 0 ; i<currentUR.size();i++) {
				result.currentUR.add(sprover.substituteProg(subList, currentUR.get(i)));
			}
		}
		
		if (URIndex != null) {
			result.URIndex = new ArrayList<Integer>();
			for (int i = 0 ; i<URIndex.size();i++) {
				result.URIndex.add(URIndex.get(i));
			}
		}
		
		return result;
	}
	
	public String toString() {
		String result = "";
		Printer printer = new Printer();
		result = result + "==============================\n";
		result = result + "Proof: \n";
		result = result + printer.printCommand(provedProgram);
		result = result + "\n==============================\n";
		result = result + "Unstable primitive assertion: ";
		if(!uPrimitive.getName().equals(Parser.FALSE)) {
			result = result + printer.printPredicate(uPrimitive);
		} else {
			result = result + "None";
		}
		result = result + "\nLocation: ";
		if(URIndex != null) {
			result = result + URIndex;
		} else {
			result = result + "None";
		}
		result = result + "\nUnstable residue: ";
		if (currentUR != null) {
			for (Token t : currentUR) {
				result = result + printer.printPredicate(t) + ",";
			}
			result = result.substring(0,result.length()-1);
		} else {
			result = result + "None";
		}
		result = result + "\n==============================\n";
		result = result + "Guarantee: \n" + guarantee;
		result = result + "==============================\n";
		result = result + "Rely: \n" + rely;
		result = result + "==============================";
		
		return result;
		
	}
	
}
