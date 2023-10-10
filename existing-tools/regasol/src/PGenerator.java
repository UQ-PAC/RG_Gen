import java.util.ArrayList;
import java.util.HashMap;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;

public class PGenerator {
	
	
	public Relation getGuarantee(Token provedProgram) {
		
		Relation result = new Relation();
		Computation comp = new Computation();
		for (int i = 0 ; i < provedProgram.getTokens().size(); i++) {
			Token current = provedProgram.getTokens().get(i);
			if(comp.isAtomic(current) && (i-1 >= 0 && provedProgram.getTokens().get(i-1).getType().equals(Parser.PRED)) &&
					(i+1 < provedProgram.getTokens().size() && provedProgram.getTokens().get(i+1).getType().equals(Parser.PRED))) {
				result.addPair(provedProgram.getTokens().get(i-1).clone(), provedProgram.getTokens().get(i+1).clone());
			} else if (current.getType().equals(Parser.COM)) {
				for (Token t: current.getTokens()) {
					result.union(getGuarantee(t));
				}
			}
		}
		Simplifier simpl = new Simplifier();
		result = simpl.simplifyRelation(result);
		return result;
	}

	//This is for the first time: generate the sequential proof and 
	//compute the guarantee
	public ProofStruct generateSProof(Context ctx, Solver solver, Token precond, Token program) {
		
		SeqProver seqProver = new SeqProver();
		Simplifier simpl = new Simplifier();
		
		Token provedProgram = seqProver.proveProgram(ctx, solver, precond, program);
		ProofStruct ps = new ProofStruct(provedProgram);
		ps.setG(simpl.simplifyRelation(getGuarantee(provedProgram)));
		return ps;
	}
	

	//For subsequent iterations
	
	public ProofStruct fixProof(Context ctx, Solver solver, ProofStruct ps) {
		//Printer p = new Printer();
		//Nothing to fix
		ArrayList<Integer> resIndex = ps.URIndex;
		ArrayList<Token> res = ps.currentUR;
		if (resIndex == null) {
			return ps;
		}
		
		Simplifier simpl = new Simplifier();
		Token proof = ps.provedProgram;
		Token unaffectedProof = getSubProof(proof,0,resIndex.get(0));
		Token affectedProof = getSubProof(proof,resIndex.get(0),proof.getTokens().size());
		//If it happens right in the loop then we move the invariant from
		//unaffected proof to affected proof
		if (proof.getTokens().get(resIndex.get(0)).getName().equals(Parser.WHILE) && resIndex.get(0) > 0) {
			Token inv = unaffectedProof.getTokens().get(unaffectedProof.getTokens().size()-1).clone();
			affectedProof.getTokens().add(0, inv);
			unaffectedProof.getTokens().remove(unaffectedProof.getTokens().size()-1);
		}
		ProofStruct result = ps.clone();
		Token mergedProof = affectedProof.clone();
		for (Token cond: res) {
			Token fixedP = fixProof(ctx,solver,affectedProof,resIndex,cond);
			mergedProof = mergeProof(ctx,solver,mergedProof,fixedP);
			Token finalP = combineSubProof(unaffectedProof,mergedProof);
			result.setProof(finalP);
			result.setG(simpl.simplifyRelation(result.guarantee.union(getGuarantee(fixedP))));
		}
		//Reset the unstable residues
		ps.setUR(null, null);
		
		return result;
	}

	
	//Replace all predicates in the proof with s, except invariant
	public Token replacePredicates(Token proof, String s) {
		if (proof.getName().equals(Parser.SKIP) || proof.getName().equals(Parser.ASSIGN) 
				|| proof.getName().equals(Parser.ATOMIC) || proof.getName().equals(Parser.AWAIT)) {
			return proof;
		}
		
		if (proof.getName().equals(Parser.PRED)) {
			return new Token(Parser.ERROR,s);
		}
		
		Token result = new Token(proof.getType(),proof.getName());
		for (Token t: proof.getTokens()) {
			if (t.getType().equals(Parser.PRED)) {
				result.addToken(new Token(Parser.ERROR,s));
			} else {
				if (t.getName().equals(Parser.SKIP) || t.getName().equals(Parser.ASSIGN) 
						|| t.getName().equals(Parser.ATOMIC) || t.getName().equals(Parser.AWAIT)) {
					
				}
			}
		}
		
		
		return result;
		
	}
	
public Token cutProof(Token proof, ArrayList<Integer> resIndex) {
		
		Token result = getSubProof(proof,resIndex.get(0),proof.getTokens().size());
		if(proof.getTokens().get(resIndex.get(0)).getName().equals(Parser.WHILE)) {
			if (resIndex.get(0) -1 >= 0 && 
					proof.getTokens().get(resIndex.get(0)-1).getType().equals(Parser.PRED)) {
				result.getTokens().add(0,proof.getTokens().get(resIndex.get(0)-1));
			}
		}
		return result;
	}
/*	
	public Token cutProof2(Token proof, ArrayList<Integer> resIndex) {
		
		Token result = new Token(Parser.COM, Parser.BLOCK);
		
		if (resIndex.size() == 1) {
			for (int i = resIndex.get(0); i < proof.getTokens().size(); i++) {
				result.addToken(proof.getTokens().get(i).clone());
			}
		} else {
			@SuppressWarnings("unchecked")
			ArrayList<Integer> innerIndex = (ArrayList<Integer>) resIndex.clone();
			innerIndex.remove(0);
			Token innerP = cutProof2(proof.getTokens().get(resIndex.get(0)), innerIndex);
			result.addToken(innerP);
			for (int i = resIndex.get(0) + 1; i < proof.getTokens().size(); i++) {
				result.addToken(proof.getTokens().get(i).clone());
			}
		}
		
		return result;
	}
*/	
	public Token stripWhile(Token whileProof) {
		Token result = whileProof.clone();
		Token sbody = stripProof(whileProof.getTokens().get(1));
		sbody.addToken(whileProof.getTokens().get(1).getTokens().get(whileProof.getTokens().get(1).getTokens().size()-1));
		result.getTokens().set(1, sbody);
		
		return result;
	}
	
	public Token replacePredicates(Token proof, Token mark) {

		Token result = proof.clone();
		
		if (proof.getType().equals(Parser.PRED)) {
			return mark.clone();
		}
		
		for (int i = 0 ; i < result.getTokens().size(); i++) {
			Token stmt = result.getTokens().get(i);
			Token reducedStmt = replacePredicates(stmt,mark);
			result.getTokens().set(i, reducedStmt);
		}
		
		return result;
	}
	
	public Token stripProof(Token proof) {
		
		Token result = proof.clone();
		
		if (proof.getType().equals(Parser.PRED)) {
			
			return null;
		}
		
		for (int i = 0 ; i < result.getTokens().size(); i++) {
			Token stmt = result.getTokens().get(i);
			Token reducedStmt = stripProof(stmt);
			
			//Preserve the invariant

			if (i+1 < result.getTokens().size() && result.getTokens().get(i+1).getName().equals(Parser.WHILE)) {
				reducedStmt = stmt;
			}
			if(reducedStmt != null) {
				result.getTokens().set(i, reducedStmt);
			} else {
				result.getTokens().remove(i);
				i--;
			}
		}
		
		return result;
	}
	//start: inclusive, end: exclusive
	public Token getSubProof(Token proof, int start, int end) {
		
		//System.out.println(start + " " + end + " " + proof.getTokens().size());
		
		if (end <= proof.getTokens().size() && end >= start && start >= 0) {
			Token result = new Token(Parser.COM,Parser.BLOCK);
			for (int i = start; i< end; i++) {
				result.addToken(proof.getTokens().get(i).clone());
			}
			return result;
		}
		
		return null;
	}
	
	public Token combineSubProof(Token firstP, Token secondP) {
		Token result = new Token(Parser.COM,Parser.BLOCK);
		result.addTokens(firstP.getTokens());
		result.addTokens(secondP.getTokens());
		return result;
	}
	
	public Token mergeProof(Context ctx, Solver solver, Token originalProof, Token fixedProof) {
		
		if (!originalProof.getType().equals(Parser.COM) && 
				!originalProof.getType().equals(Parser.PRED)) {
			return originalProof.clone();
		}

		if (originalProof.getTokens().size() != fixedProof.getTokens().size() &&
				originalProof.getType().equals(Parser.COM)) {
			System.out.println("Error in PGenerator.mergeProof when checking for compatible size");
			Printer p = new Printer();
			System.out.println("First:");
			System.out.println(p.printCommand(originalProof));
			System.out.println("Second:");
			System.out.println(p.printCommand(fixedProof));
			return new Token(Parser.COM,Parser.BLOCK);
		}
		
		if (originalProof.getType().equals(Parser.PRED) && fixedProof.getType().equals(Parser.PRED)) {
			//System.out.println("HERE");
			if (fixedProof.getName().equals(Parser.FALSE) || originalProof.equals(fixedProof)) {
				return originalProof.clone();
			}
				
			if(originalProof.getName().equals(Parser.FALSE)) {
				return fixedProof;
			}
			
			Computation comp = new Computation();
			
			if(comp.imply(ctx, solver, fixedProof, originalProof)) {
				fixedProof.setName(Parser.FALSE);
				return originalProof.clone();
			}
			
			/*
			if(comp.imply(ctx, solver, originalProof, fixedProof)) {
				originalProof.setName(Parser.FALSE);
				return fixedProof.clone();
			}
			*/
			
				
				Token disj = new Token(Parser.PRED,Parser.OR);
				disj.addToken(originalProof).addToken(fixedProof);
				return disj.clone();			
		}
		
		Token result =  originalProof.clone();
		//Printer printer = new Printer();
		//System.out.println("Original:\n" + printer.printCommand(originalProof));
		//System.out.println("Fixed: \n" + printer.printCommand(fixedProof));
		
		for (int i = 0; i < originalProof.getTokens().size(); i++) {
			Token stmt1 = originalProof.getTokens().get(i).clone();
			//System.out.println(originalProof.getTokens());
			//System.out.println("1: " + stmt1);
			//System.out.println(fixedProof.getTokens());
			Token stmt2 = fixedProof.getTokens().get(i);
			//System.out.println("2: " + stmt2);
			Token fixedStmt = mergeProof(ctx,solver,stmt1,stmt2);
			result.getTokens().set(i,fixedStmt);
		}
		
		return result;
	}
	
	public Token fixProof(Context ctx, Solver solver, Token affectedProof , ArrayList<Integer> resIndex, Token resCond) {
		
		SeqProver seq = new SeqProver();
		Computation comp = new Computation();
		
		if (resIndex.size() > 3) {
			System.out.println("No support of nested commands for now");
			return new Token(Parser.COM,Parser.BLOCK);
		}
		
		if (resIndex.size() == 1) {
			Token result = seq.proveProgram(ctx, solver, resCond, stripProof(affectedProof));
			return result;
		} else {// it is inside a while loop or if
			
			Token fixedProof = new Token(Parser.COM,Parser.BLOCK);
			Token newPreCond = new Token(Parser.PRED);
			Token remaining = new Token(Parser.COM,Parser.BLOCK);
			boolean isRedudant = false;
			
			if (affectedProof.getTokens().get(0).getType().equals(Parser.PRED)) {// while loop
				Token whileProof = affectedProof.getTokens().get(1);
				Token whileBool = whileProof.getTokens().get(0);
				Parser par = new Parser();
				Token whileCond = par.bool2Pred(whileBool);
				Token whileBody = whileProof.getTokens().get(1);
				Token unaffectedwhileProof = getSubProof(whileBody,0,resIndex.get(2));
				Token affectedwhileProof = getSubProof(whileBody,resIndex.get(2),whileBody.getTokens().size()).clone();
				affectedwhileProof = stripProof(affectedwhileProof);
				ArrayList<Integer> newIndex = (ArrayList<Integer>) resIndex.clone();
				newIndex.remove(0);
				newIndex.remove(0);
				Token fixedWhileProof = fixProof(ctx,solver,affectedwhileProof,newIndex,resCond);
				Token inv = affectedProof.getTokens().get(0);
				Token postCond = fixedWhileProof.getTokens().get(fixedWhileProof.getTokens().size()-1);
				if (comp.imply(ctx, solver, postCond, inv)) { // the current inv is good enough
					isRedudant = true;
					fixedWhileProof.addToken(inv.clone());
				} else {
					Token conj = new Token(Parser.PRED,Parser.AND).addToken(whileCond).addToken(postCond);
					if (comp.imply(ctx, solver, conj, inv)) {
						inv = postCond;
						fixedWhileProof.addToken(inv.clone());
					} else {
						System.out.println("Error in PGenerator.fixProof when generating new invariant");
						return new Token(Parser.COM,Parser.BLOCK);
					}
				}
				
				Token newWhile = new Token(Parser.COM,Parser.WHILE);
				newWhile.addToken(whileBool);
				unaffectedwhileProof = replacePredicates(unaffectedwhileProof,new Token(Parser.PRED,Parser.FALSE));
				Token newWhileBody = combineSubProof(unaffectedwhileProof,fixedWhileProof);
				newWhile.addToken(newWhileBody);
				fixedProof.addToken(inv);
				fixedProof.addToken(newWhile);
				newPreCond = new Token(Parser.PRED,Parser.AND);
				newPreCond.addToken(inv);
				newPreCond.addToken(new Token(Parser.PRED,Parser.NEG).addToken(whileCond));
				remaining = getSubProof(affectedProof,2,affectedProof.getTokens().size());
			}
			
			if (affectedProof.getTokens().get(0).getName().equals(Parser.IF)) {
				
				Token ifProof = affectedProof.getTokens().get(0);
				Token ifBody =  ifProof.getTokens().get(1);
				Token elseBody = ifProof.getTokens().get(2);
				fixedProof = ifProof.clone();
				if (resIndex.get(1) == 1) {// In the if-branch
					Token affectedIfProof = getSubProof(ifBody,resIndex.get(2),ifBody.getTokens().size());
					Token unaffectedIfProof = getSubProof(ifBody,0,resIndex.get(2));
					affectedIfProof = stripProof(affectedIfProof);
					ArrayList<Token> newIndex = (ArrayList<Token>) resIndex.clone();
					newIndex.remove(0);
					newIndex.remove(0);
					Token fixedIfProof = seq.proveProgram(ctx, solver, resCond, affectedIfProof);
					unaffectedIfProof = replacePredicates(unaffectedIfProof,new Token(Parser.PRED,Parser.FALSE));
					Token newIfBranch = combineSubProof(unaffectedIfProof,fixedIfProof);
					fixedProof.getTokens().set(1, newIfBranch);
					newPreCond = fixedIfProof.getTokens().get(fixedIfProof.getTokens().size()-1);
				} else if (resIndex.get(1) == 2) {// else-branch
					Token affectedElseProof = getSubProof(elseBody,resIndex.get(2),elseBody.getTokens().size());
					Token unaffectedElseProof = getSubProof(elseBody,0,resIndex.get(2));
					affectedElseProof = stripProof(affectedElseProof);
					ArrayList<Token> newIndex = (ArrayList<Token>) resIndex.clone();
					newIndex.remove(0);
					newIndex.remove(0);
					Token fixedElseProof = seq.proveProgram(ctx, solver, resCond, affectedElseProof);
					unaffectedElseProof = replacePredicates(unaffectedElseProof,new Token(Parser.PRED,Parser.FALSE));
					Token newElseBranch = combineSubProof(unaffectedElseProof,fixedElseProof);
					fixedProof.getTokens().set(2, newElseBranch);
					newPreCond = fixedElseProof.getTokens().get(fixedElseProof.getTokens().size()-1);
				} else {
					System.out.println("Error in PGenerator.fixProof while fixing if-else statement");
				}
				
				fixedProof = new Token(Parser.COM,Parser.BLOCK).addToken(fixedProof);
				
				remaining = getSubProof(affectedProof,1,affectedProof.getTokens().size());

			}
			
			
			Token result = new Token(Parser.COM,Parser.BLOCK);
			for (Token t: fixedProof.getTokens()) {
				result.addToken(t);
			}

			if (!isRedudant) {
				remaining = stripProof(remaining);
				Simplifier simpl = new Simplifier();
				newPreCond = simpl.simplifyPredicate(ctx,solver,newPreCond);
				if (remaining.getTokens().size() > 0) {
					for (Token t: seq.proveProgram(ctx, solver, newPreCond, remaining).getTokens()) {
						result.addToken(t);
					}
				} else {
					result.addToken(newPreCond);
				}
			} else {
				Token replacedProof = replacePredicates(remaining, new Token(Parser.PRED,Parser.FALSE));
				for (Token t: replacedProof.getTokens()) {
					result.addToken(t);
				}
			}
			return result;
			
		}
	}
	
	//Sequential version
	public ArrayList<ProofStruct> run(Context ctx, Solver solver, ArrayList<ProofStruct> proofs, boolean fstTime) {
		
		if (fstTime) {
			return runFstTime(ctx,solver,proofs);
		} else {
			return runLater(ctx,solver,proofs);
		}
	}
	
	
	//Parallel version
	public ArrayList<ProofStruct> run(ArrayList<Context> ctx, ArrayList<Solver> solver, 
			ArrayList<ProofStruct> proofs, boolean fstTime) throws InterruptedException {
		
		ArrayList<ProofStruct> result = new ArrayList<ProofStruct>();
		ArrayList<PGThread> pgthreads = new ArrayList<PGThread> ();
		ArrayList<Thread> threads = new ArrayList<Thread>();
		for (int i = 0 ; i < proofs.size();i++) {
			ProofStruct ps = proofs.get(i);
			pgthreads.add(new PGThread(ctx.get(i),solver.get(i),ps,fstTime));
			threads.add(new Thread(pgthreads.get(i)));
			threads.get(i).start();
			
		}
		for (int i = 0; i < proofs.size();i++) {
			threads.get(i).join();
		}
		
		for (int i = 0; i < proofs.size();i++) {
			result.add(pgthreads.get(i).ps);
		}
		
		//propose the relies
		for (int i = 0; i < result.size(); i++) {
			Relation rely = new Relation();
			for (int j = 0; j < result.size(); j++) {
				if (i != j) {
					rely = rely.union(result.get(j).guarantee);
				}
			}
			result.get(i).setR(rely);
		}
		
		return result;
	}
	
	//With mirrors 
	public ArrayList<ProofStruct> run(Context ctx, Solver solver, ArrayList<ProofStruct> proofs, 
			HashMap<Integer, Pair<Integer,ArrayList<Token>>> mirrors, boolean fstTime) {
		
		if (fstTime) {
			return runFstTime(ctx,solver,proofs,mirrors);
		} else {
			return runLater(ctx,solver,proofs,mirrors);
		}
	}
	
	
	public ArrayList<ProofStruct> runFstTime(Context ctx, Solver solver, ArrayList<ProofStruct> proofs) {
		ArrayList<ProofStruct> result = new ArrayList<ProofStruct>();
		
		for (ProofStruct ps: proofs) {
			Token precond = ps.currentUR.get(0);//Store the precondition in currentUR
			result.add(generateSProof(ctx,solver,precond,ps.provedProgram));
		}
		
		//propose the relies
		for (int i = 0; i < result.size(); i++) {
			Relation rely = new Relation();
			for (int j = 0; j < result.size(); j++) {
				if (i != j) {
					rely = rely.union(result.get(j).guarantee);
				}
			}
			result.get(i).setR(rely);
		}
		
		return result;
	}
	
	public ArrayList<ProofStruct> runLater(Context ctx, Solver solver, ArrayList<ProofStruct> proofs) {
		
		ArrayList<ProofStruct> result = new ArrayList<ProofStruct>();
		
		for (ProofStruct ps: proofs) {
			result.add(fixProof(ctx,solver,ps));
		}
		
		//propose the relies
		for (int i = 0; i < result.size(); i++) {
			Relation rely = new Relation();
			for (int j = 0; j < result.size(); j++) {
				if (i != j) {
					rely = rely.union(result.get(j).guarantee);
				}
			}
			result.get(i).setR(rely);
			result.get(i).setRelyClosure(proofs.get(i).relyClosure);
		}
		
		return result;
	}
	
	public ArrayList<ProofStruct> runFstTime(Context ctx, Solver solver, ArrayList<ProofStruct> proofs,
			HashMap<Integer, Pair<Integer,ArrayList<Token>>> mirrors) {
		
		ArrayList<ProofStruct> result = new ArrayList<ProofStruct>(proofs.size());
		for (int i = 0 ; i< proofs.size(); i++) {
			result.add(new ProofStruct());
		}
		
		for (int i = 0; i < proofs.size(); i++) {
			if (!mirrors.containsKey(i)) {
				ProofStruct ps = proofs.get(i);
				Token precond = ps.currentUR.get(0);//Store the precondition in currentUR
				result.set(i,generateSProof(ctx,solver,precond,ps.provedProgram));
			}
			

		}
		
		for (int i = 0; i < proofs.size(); i++) {
			if (mirrors.containsKey(i)) {
				ProofStruct target = result.get(mirrors.get(i).getFst());
				ArrayList<Token> subList = mirrors.get(i).getSnd();
				result.set(i, target.substitute(subList));
			}
		}
		
		//propose the relies
		for (int i = 0; i < result.size(); i++) {
			Relation rely = new Relation();
			for (int j = 0; j < result.size(); j++) {
				if (i != j) {
					rely = rely.union(result.get(j).guarantee);
				}
			}
			result.get(i).setR(rely);
		}
		
		return result;
	}
	
	public ArrayList<ProofStruct> runLater(Context ctx, Solver solver, ArrayList<ProofStruct> proofs, 
			HashMap<Integer, Pair<Integer,ArrayList<Token>>> mirrors) {

		
		ArrayList<ProofStruct> result = new ArrayList<ProofStruct>(proofs.size());
		for (int i = 0 ; i< proofs.size(); i++) {
			result.add(new ProofStruct());
		}
		
		for (int i = 0; i < proofs.size(); i++) {
			if (!mirrors.containsKey(i)) {
				ProofStruct ps = proofs.get(i);
				result.set(i,fixProof(ctx,solver,ps));
			}
		}
		
		for (int i = 0; i < proofs.size(); i++) {
			if (mirrors.containsKey(i)) {
				ProofStruct target = result.get(mirrors.get(i).getFst());
				ArrayList<Token> subList = mirrors.get(i).getSnd();
				result.set(i, target.substitute(subList));
			}
		}
		
		//propose the relies
		for (int i = 0; i < result.size(); i++) {
			Relation rely = new Relation();
			for (int j = 0; j < result.size(); j++) {
				if (i != j) {
					rely = rely.union(result.get(j).guarantee);
				}
			}
			result.get(i).setR(rely);
			result.get(i).setRelyClosure(proofs.get(i).relyClosure);
		}
		
		return result;
	}
}
