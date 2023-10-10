import java.io.File;
import java.util.*;
import com.microsoft.z3.*;

public class Computation {
	
	public static final String tempDir = "";
	public static final String tempFile = "temp.smt";
	public static int count = 0;
	
	public boolean isIn(Context ctx, Solver solver, ArrayList<Token> evalList, Token pred) {
		
		for (Token t: evalList) {
			pred = new Token(Parser.BOOL,Parser.AND).addToken(pred).addToken(t);
		}

		Token result = checkSat(ctx,solver,pred).get(0);
		if (result.getName().equals("unsat")) {
			return false;
		}
		
		return true;
	}
	
	public boolean[][] FWClosure(boolean[][] adjmatrix){
		
		int size = adjmatrix.length;
		boolean[][] closure = adjmatrix.clone();
		
		for (int k = 0 ; k < size ; k++) {
			for (int j = 0 ; j < size ; j++) {
				for (int i = 0 ; i < size ; i++) {
					if (closure[i][k]&&closure[k][j]) {
						closure[i][j] = true;
					}
				}
			}
		}
		
		return closure;
	}
	
	public boolean isSat(Context ctx, Solver solver, Token pred) {
		ArrayList<Token> constraints = new ArrayList<Token>();
		constraints.add(pred);
		return isSat(ctx,solver,constraints).get(0);
	}
	
	public ArrayList<Token> checkSat(Context ctx, Solver solver, Token pred){
		return checkSatFromSolver(ctx,solver,pred,"model");
	}
	
	public ArrayList<Boolean> isSat(Context ctx, Solver solver, ArrayList<Token> constraints){
		ArrayList<Boolean> results = isSatFromSolver(ctx, solver,constraints);
		return results;
		//return isSatFromFile(constraints);
	}
	
	public ArrayList<Token> checkSatFromSolver(Context ctx, Solver solver, Token pred, String option){
		Parser p = new Parser();
		SatSolver s = new SatSolver();
		String result = s.runSolver(ctx, solver, pred, option);
		return p.parseSmtResult(result);
	}
	
	public ArrayList<Boolean> isSatFromSolver(Context ctx, Solver solver, ArrayList<Token> constraints){
		SatSolver.countSAT++;
		ArrayList<Boolean> result = new ArrayList<Boolean>();
		Parser p = new Parser();
		for (Token t : constraints) {
			solver.push();
			solver.add(p.parseBoolExpr(ctx, t));
			if (solver.check().equals(Status.SATISFIABLE)) {
				result.add(true);
			} else {
				result.add(false);
			}
			solver.pop();
		}
		return result;
	}
	
	public ArrayList<Token> checkSatFromFile(Token pred){

		Printer printer = new Printer();
		String constraint = printer.generateSmtConstraint(pred);
		printer.writeToFile(constraint, tempDir + tempFile);
		
		SatSolver solver = new SatSolver();
		String result = solver.runSolver(new File (tempDir + tempFile));
		
		Parser parser = new Parser();

		return parser.parseSmtResult(result);
		
	}

	public ArrayList<Boolean> isSatFromFile(ArrayList<Token> constraints){
		
		Printer printer = new Printer();
		
		ArrayList<String> vars = printer.getFreeVars(constraints);
		
		String constraint = printer.declareVars(vars) + "\n";
		for (Token pred: constraints) {
			constraint = constraint + "(push)\n";
			constraint = constraint + "(assert " + printer.modifiedConvert2smt(pred) + ")\n";
			constraint = constraint + "(check-sat)\n(pop)\n";
		}

		printer.writeToFile(constraint, tempDir + tempFile);
		Parser par = new Parser();
		SatSolver satSolver = new SatSolver();
		
		return par.parseSmtResults(satSolver.runSolver(new File(tempDir + tempFile)));
	}
	
	public Token appendSuffix(Token pred, String suffix) {
		if (pred.getName().equals(Parser.VAR)) {
			pred.getTokens().get(0).setName(pred.getTokens().get(0).getName() + suffix); 
		} else if (pred.getName().equals(Parser.CONSTANT)) {
			return pred;
		} else {
			for (int i=0;i<pred.getTokens().size();i++) {
				appendSuffix(pred.getTokens().get(i), suffix);
			}
		}
		
		return pred;
	}
	
	public boolean isStable(Context ctx, Solver solver, Token pred, Relation rel) {
		
		ArrayList<Token> constraints = new ArrayList<Token>();
		
		for (Pair<Token,Token> pair: rel.getRelation()) {
			Token conjFst = new Token(Parser.PRED,Parser.AND)
					.addToken(pred).addToken(pair.getFst());
			Token conjSnd =  new Token(Parser.PRED,Parser.AND)
					.addToken(pair.getSnd()).addToken(pred.negate());
			
			Printer printer = new Printer();
			Parser parser = new Parser();
			ArrayList<String> varsInPred = printer.getFreeVars(pred);
			ArrayList<String> varsInPost = printer.getFreeVars(pair.getSnd());
			Token eqs = new Token(Parser.PRED,Parser.AND);
			for (String v : varsInPred) {
				if (!varsInPost.contains(v)) {
					eqs.addToken(parser.parsePredicate(v + "1" + "=" + v));
				}
			}
			if (eqs.getTokens().size() == 0) {
				eqs = new Token(Parser.PRED,Parser.TRUE);
			}
			if (eqs.getTokens().size() == 1) {
				eqs = eqs.getTokens().get(0);
			}
			conjSnd = appendSuffix(conjSnd.clone(),"1");
			conjFst.addToken(conjSnd).addToken(eqs);

			constraints.add(conjFst.clone());
		}
		
		ArrayList<Boolean> smtResult = isSat(ctx,solver,constraints);
		for (int i = 0 ; i < smtResult.size(); i++) {
			if (smtResult.get(i)) {
				return false;
			}
		}
		
		return true;
	}
	//Slower....
	public boolean isStableNew(Context ctx, Solver solver, Token pred, Relation rel) {
		
		Token constraint = new Token(Parser.PRED,Parser.OR);
		int count = 0 ;
		for (Pair<Token,Token> pair: rel.getRelation()) {
			Token conjFst = new Token(Parser.PRED,Parser.AND)
					.addToken(pred).addToken(pair.getFst());
			Token conjSnd =  new Token(Parser.PRED,Parser.AND)
					.addToken(pair.getSnd()).addToken(pred.negate());
			conjSnd = appendSuffix(conjSnd.clone(),"1");
			conjFst.addToken(conjSnd);
			conjFst = appendSuffix(conjFst.clone(),"" + count++);
			constraint.addToken(conjFst);
		}
		
		if(constraint.getTokens().size() == 0) {
			return true;
		}
		
		if (constraint.getTokens().size() == 1) {
			constraint = constraint.getTokens().get(0);
		}
		
		return !isSat(ctx,solver,constraint);
	}

	public Relation getCompose(Context ctx, Solver solver, Relation r1, Relation r2) {
		Relation result = new Relation();
		Printer printer = new Printer();
		ArrayList<Token> constraints = new ArrayList<Token>();
		ArrayList<Pair<Token,Token>> candidates = new ArrayList<Pair<Token,Token>>();
		ArrayList<String> vars = new ArrayList<String>();
		for (Pair<Token,Token> p1: r1.getRelation()) {
			for (Pair<Token,Token> p2: r2.getRelation()) {
				if(p1.getSnd().equals(p2.getFst())) {
					result.addPair(new Pair<Token,Token>(p1.getFst().clone(),p2.getSnd().clone()));
				} else {
					Token pred = new Token(Parser.PRED,Parser.AND)
							.addToken(p1.getSnd().clone())
							.addToken(p2.getFst().clone());
					constraints.add(pred);
					candidates.add(new Pair<Token,Token>(p1.getFst().clone(),p2.getSnd().clone()));
					for (String v: printer.getFreeVars(pred)) {
						if (!vars.contains(v)) {
							vars.add(v);
						}
					}
				}
			}
		}
		ArrayList<Boolean> smtResults = isSat(ctx, solver, constraints);
		for (int i = 0; i < smtResults.size(); i++) {
			if (smtResults.get(i)) {
				result.addPair(candidates.get(i));
			}
		}
		return result;
	}
	
	public Relation computeBasicGraph(Context ctx, Solver solver, Relation rel) {
		
		ArrayList<Token> constraints = new ArrayList<Token>();
		ArrayList<Pair<Token,Token>> candidates = new ArrayList<Pair<Token,Token>>();
		Relation result = rel.clone();
		
		for (int i=0;i<result.getRelation().size();i++) {
			for (int j=0;j<result.getRelation().size();j++) {
				if (i!=j) {
					Pair<Token,Token> first = result.getRelation().get(i);
					Pair<Token,Token> second = result.getRelation().get(j);
					Pair<Token,Token> candidate = new Pair<Token,Token>(first.getFst(),second.getSnd());
					candidates.add(candidate);
					Token constraint = new Token(Parser.PRED,Parser.AND)
							.addToken(first.getSnd()).addToken(second.getFst());
					constraints.add(constraint);
				}
			}
		}
		
		ArrayList<Boolean> smtResults = isSat(ctx,solver,constraints);
		for (int i = 0; i < smtResults.size(); i++) {
			if (smtResults.get(i)) {
				result.addPair(candidates.get(i));
			}
		}
		return result;
	}
	
	public Relation computeClosureByFW(Context ctx, Solver solver, Relation rel) {
		
		//final long start = System.currentTimeMillis();
		Relation result = computeBasicGraph(ctx,solver,rel);
		//final long end = System.currentTimeMillis();
		//System.out.println("computeBasicGraph: " + (end-start));
		
		ArrayList<Token> vertices = new ArrayList<Token>();
		
		for (Pair<Token,Token> p : result.getRelation()) {
			if (!vertices.contains(p.getFst())) {
				vertices.add(p.getFst());
			}
			if (!vertices.contains(p.getSnd())) {
				vertices.add(p.getSnd());
			}
		}
		boolean[][] adjmatrix = new boolean[vertices.size()][vertices.size()];
		
		for (int i = 0 ; i < vertices.size(); i++) {
			for (int j = 0 ; j < vertices.size(); j++) {
				adjmatrix[i][j] = false;
			}
		}
		for (Pair<Token,Token> p : result.getRelation()) {
			adjmatrix[vertices.indexOf(p.getFst())][vertices.indexOf(p.getSnd())] = true;
		}
		
		boolean[][] closurematrix = FWClosure(adjmatrix);
		result = new Relation();
		for (int i=0; i<vertices.size(); i++) {
			for (int j=0; j<vertices.size(); j++) {
				if (closurematrix[i][j]) {
					result.addPair(vertices.get(i), vertices.get(j));
				}
			}
		}
		
		return result;
	}
	
	public Relation getComposeFromBasicGraphs(Relation r1, Relation r2) {
		
		Relation result = new Relation();
		for (Pair<Token,Token> p1 : r1.getRelation()) {
			for (Pair<Token,Token> p2 : r2.getRelation()) {
				if (p1.getSnd().equals(p2.getFst())) {
					Pair<Token,Token> p = new Pair<Token,Token>(p1.getFst().clone(),p2.getSnd().clone());
					result.addPair(p);
				}
			}
		}
		
		return result;
	}
	
	public Relation computeClosureByBasicGraph(Context ctx, Solver solver, Relation rel) {

		Relation basicGraph = computeBasicGraph(ctx,solver,rel);
		Relation result = basicGraph.clone();
		Relation currentClosure = basicGraph.clone();
		boolean check = false;
		
		for (int i=2; i <= basicGraph.getRelation().size();i++) {
			if (check) {
				//result = compressRelation(result);
				//result = removeDuplicates(result);
				return result;
			} 
			check = true;
			currentClosure = getComposeFromBasicGraphs(currentClosure,basicGraph);
			for (Pair<Token,Token> p: currentClosure.getRelation()) {
				if (!result.getRelation().contains(p)) {
					result.addPair(p);
					check = false;
				}
			}
		}		
		//result = compressRelation(result);
		//result = removeDuplicates(result);

		return result;
	}
	
	public Relation computeClosureByIterations(Context ctx, Solver solver, Relation rel) {
		Relation result = rel.clone();
		Relation currentClosure = rel.clone();
		boolean check = false;
		for (int i=2; i <= rel.getRelation().size();i++) {
			if (check) {
				result = compressRelation(result);
				result = removeDuplicates(result);
				return result;
			} 
			check = true;
			currentClosure = getCompose(ctx,solver,currentClosure,rel);
			for (Pair<Token,Token> p: currentClosure.getRelation()) {
				if (!result.getRelation().contains(p)) {
					result.addPair(p);
					check = false;
				}
			}
		}		
		//result = compressRelation(result);
		result = removeDuplicates(result);
		//System.out.println("\n\n" + result + "\n\n");
		return result;
	}
	
	public Relation compressRelation(Relation rel) {
		Relation result = new Relation();
		ArrayList<Boolean> marked = new ArrayList<Boolean>();
		for (Pair<Token,Token> p : rel.getRelation()) {
			marked.add(false);
		}
		for (int i = 0 ; i < rel.getRelation().size();i++) {
			if (!marked.get(i)) {
				Pair<Token,Token> p = rel.getRelation().get(i);
				Token img = p.getSnd();
				ArrayList<Token> temp = new ArrayList<Token>();
				
				for (int j = i ; j < rel.getRelation().size();j++) {
					Pair<Token,Token> candidate = rel.getRelation().get(j);
					if (candidate.getSnd().equals(img) && !marked.get(j)) {
						temp.add(candidate.getFst().clone());
						marked.set(j,true);
					}
				}
				
				marked.set(i, true);
				
				if (temp.size() <= 1) {
					result.addPair(temp.get(0), img.clone());
				} else {
					Token disj = new Token(Parser.PRED,Parser.OR);
					for (Token t: temp) {
						disj.addToken(t.clone());
					}
					result.addPair(disj, img.clone());
				}
			}
			

		}
		
		return result;
	}
	
	public Relation removeDuplicates(Relation rel) {
		for (int i = 0 ; i < rel.getRelation().size(); i++) {
			Pair<Token, Token> current = rel.getRelation().get(i);
			for (int j = i + 1; j < rel.getRelation().size(); j++) {
				if (current.equals(rel.getRelation().get(j))) {
					rel.getRelation().remove(j);
					i--;
					j--;
				}
			}
		}
		return rel;
	}
	
	public Relation computeClosure(Context ctx, Solver solver, Relation rel) {
		//final long start = System.currentTimeMillis();
		Relation result = computeClosureByHeuristics(ctx,solver,rel);
		//Relation result = computeClosureByIterations(ctx,solver,rel);
		//final long end = System.currentTimeMillis();
		//System.out.println("Closure: " + (end -start) + ", Size: " + result.getRelation().size() + ", Initial size: " + rel.getRelation().size());
		return result;
	}
	
	public Relation computeClosureByHeuristics(Context ctx, Solver solver, Relation rel) {
		if (rel.getRelation().size() <= 70) {
			return computeClosureByBasicGraph(ctx,solver,rel);
		} else {
			int mid = rel.getRelation().size()/2;
			Relation fstRel = new Relation();
			for (int i=0; i < mid; i++) {
				fstRel.addPair(rel.getRelation().get(i).getFst().clone(),rel.getRelation().get(i).getSnd().clone());
			}
			Relation sndRel = new Relation();
			for (int i=mid; i < rel.getRelation().size(); i++) {
				sndRel.addPair(rel.getRelation().get(i).getFst().clone(),rel.getRelation().get(i).getSnd().clone());
			}
			
			return computeClosureByExtension(ctx,solver,computeClosureByHeuristics(ctx,solver,fstRel),sndRel);
		}
	}
	
	public Relation extendClosureSingleton(Context ctx, Solver solver, Relation currentClosure, Pair<Token,Token> newPair) {
		
		if (currentClosure.getRelation().contains(newPair)) {
			return currentClosure;
		}
		
		Relation result = currentClosure.clone();
		ArrayList<Pair<Token,Token>> p2Clo = new ArrayList<Pair<Token,Token>>();
		ArrayList<Pair<Token,Token>> candidates = new ArrayList<Pair<Token,Token>>();
		ArrayList<Token> constraints = new ArrayList<Token>();
		
		p2Clo.add(new Pair<Token,Token>(newPair.getFst().clone(),newPair.getSnd().clone()));
		
		for (Pair<Token,Token> p : currentClosure.getRelation()) {
			constraints.add(new Token(Parser.PRED,Parser.AND)
					.addToken(newPair.getSnd()).addToken(p.getFst()));
			candidates.add(new Pair<Token,Token>(newPair.getFst().clone(),p.getSnd().clone()));
		}
		
		ArrayList<Boolean> smtResults = isSat(ctx, solver, constraints);
		for (int i = 0; i < smtResults.size(); i++) {
			if (smtResults.get(i) && !p2Clo.contains(candidates.get(i))) {
				p2Clo.add(candidates.get(i));
			}
		}
		
		for (Pair<Token,Token> p : p2Clo) {
			if (!result.getRelation().contains(p)) {
				result.addPair(p);
			}
		}
		
		constraints = new ArrayList<Token>();
		ArrayList<Token> candidates2 = new ArrayList<Token>();
		
		for (Pair<Token,Token> p : currentClosure.getRelation()) {
			constraints.add(new Token(Parser.PRED,Parser.AND)
					.addToken(p.getSnd()).addToken(newPair.getFst()));
			candidates2.add(p.getFst().clone());
		}
		
		smtResults = isSat(ctx, solver, constraints);
		for (int i = 0; i < smtResults.size(); i++) {
			if (smtResults.get(i)) {
				for (Pair<Token,Token> p : p2Clo) {
					Pair<Token,Token> candidate = new Pair<Token,Token>
					(candidates2.get(i).clone(),p.getSnd().clone());
					if(!result.getRelation().contains(candidate)) {
						result.addPair(candidate);
					}
				}
			}
		}
		
		return result;
	}
	
	public Relation computeClosureByExtension(Context ctx, Solver solver, Relation currentClosure, Relation rel) {
		Relation result = currentClosure.clone();
		for (Pair<Token,Token> p : rel.getRelation()) {
			result = extendClosureSingleton(ctx,solver,result,p);
		}
		return result;
	}
	
	public ArrayList<Token> getUnstableResidue(Context ctx, Solver solver, Token pred, Relation closure){

		ArrayList<Token> result = new ArrayList<Token>();
		ArrayList<Token> candidates = new ArrayList<Token>();
		ArrayList<Token> constraints = new ArrayList<Token>();
		Simplifier simpl = new Simplifier();
		//Printer printer = new Printer();
		for (Pair<Token,Token> p: closure.getRelation()) {
			
			Token flatten = simpl.flatten(p.getFst(),Parser.OR);
			if(flatten.getTokens().contains(pred)) {
				result.add(p.getSnd().clone());
			} else {
				Token conj = new Token(Parser.PRED,Parser.AND)
						.addToken(pred.clone()).addToken(p.getFst());
				Token conj2 = new Token(Parser.PRED,Parser.AND)
						.addToken(pred.negate().clone()).addToken(p.getSnd());
				Printer printer = new Printer();
				Parser parser = new Parser();
				ArrayList<String> varsInPred = printer.getFreeVars(pred);
				ArrayList<String> varsInPost = printer.getFreeVars(p.getSnd());
				Token eqs = new Token(Parser.PRED,Parser.AND);
				for (String v : varsInPred) {
					if (!varsInPost.contains(v)) {
						eqs.addToken(parser.parsePredicate(v + "1=" + v));
					}
				}
				if (eqs.getTokens().size() == 0) {
					eqs = new Token(Parser.PRED,Parser.TRUE);
				}
				if (eqs.getTokens().size() == 1) {
					eqs = eqs.getTokens().get(0);
				}
				conj2 = appendSuffix(conj2.clone(),"1");
				conj.addToken(conj2).addToken(eqs);
					//System.out.println(printer.printPredicate(conj));
				constraints.add(conj);
					//constraints.add(conj2);
				Token currentCandidate = p.getSnd().clone();
				candidates.add(currentCandidate);
			}

		}
		
		ArrayList<Boolean> smtResult = isSat(ctx,solver,constraints);
		for (int i = 0 ; i < smtResult.size(); i=i+1) {
			if (smtResult.get(i)) {
				if (pred.getName().equals(Parser.AND)) {
					Token newCandidate = new Token(Parser.PRED,Parser.AND);
					newCandidate.addToken(candidates.get(i));
					Printer p = new Printer();
					for (Token t: pred.getTokens()) {
						if (hasZeroIntersection(p.getFreeVars(t),p.getFreeVars(candidates.get(i)))){
							newCandidate.addToken(t.clone());
						}
					}
					if (newCandidate.getTokens().size() == 1) {
						newCandidate = newCandidate.getTokens().get(0);
					}
					if (!result.contains(newCandidate)) {
						result.add(newCandidate);
					}
				} else {
					if (!result.contains(candidates.get(i))){
						result.add(candidates.get(i));
					}
				}
			} 
		}
		
		if (result.size() > 1) {
			result = simpl.removeRedundantsInOR(ctx, solver, result);
		}
		
		return result;
	}
	
	public boolean hasZeroIntersection(ArrayList<String> fst, ArrayList<String> snd) {
		for (String s: fst) {
			if (snd.contains(s)) {
				return false;
			}
		}
		
		return true;
	}

	/*
	public ArrayList<Token> getStableResidueGen(Token pred, Relation closure){
		System.out.println(closure);
		ArrayList<Token> result = new ArrayList<Token>();
		ArrayList<Token> candidates = new ArrayList<Token>();
		ArrayList<Token> constraints = new ArrayList<Token>();
		//Printer printer = new Printer();
		for (Pair<Token,Token> p: closure.getRelation()) {
			Token conj = new Token(Parser.PRED,Parser.AND).addToken(pred.clone()).addToken(p.getFst());
			if (!constraints.contains(conj)) {
				constraints.add(conj);
				Token currentCandidate = p.getSnd().clone();
				candidates.add(currentCandidate);
			}
		}
		
		ArrayList<Boolean> smtResult = isSat(constraints);
		for (int i = 0 ; i < smtResult.size(); i++) {
			if (smtResult.get(i)) {
				result.add(candidates.get(i));
			}
		}
		
		return result;
	}
	*/
	public Token stablize(Context ctx, Solver solver, Token pred, Relation closure) {
		Token result = pred.clone();
		for (Pair<Token,Token> p: closure.getRelation()) {
			Token conj = new Token(Parser.PRED,Parser.AND).addToken(pred).addToken(p.getFst());
			if (isSat(ctx,solver,conj)) {

				Token temp = new Token(Parser.PRED,Parser.AND).addToken(p.getSnd().clone());
				temp.addToken(new Token(Parser.PRED,Parser.NEG).addToken(pred.clone()));
				result = new Token(Parser.PRED,Parser.OR).addToken(result).addToken(temp);
				
			}
		}
		
		return result;
	}
	
	public Pair<Token,Token> abstractSingletonRel(Pair<Token,Token> rel){
		Token first = rel.getFst().clone();
		Token second = rel.getSnd().clone();
		Simplifier simpl = new Simplifier();
		if (first.getType().startsWith(Parser.PRED) && second.getType().startsWith(Parser.PRED)) {
			
			first = simpl.flatten(first, Parser.AND);
			second = simpl.flatten(second, Parser.AND);
			
			for (int i = 0 ; i < second.getTokens().size(); i++) {
				Token t = second.getTokens().get(i);
				if (first.getTokens().contains(t)) {
					second.getTokens().remove(i);
					i--;
				}
			}
			
			if (first.getTokens().size() == 0) {
				first = new Token(Parser.PRED,Parser.TRUE);
			}
			
			if (second.getTokens().size() == 0) {
				second = new Token(Parser.PRED,Parser.TRUE);
			}
			
			if (first.getTokens().size() == 1) {
				first = first.getTokens().get(0);
			}
			
			if (second.getTokens().size() == 1) {
				second = second.getTokens().get(0);
			}
			
			return new Pair<Token,Token>(first,second);
		}
		
		return null;
	}
	
	public Relation abstractRel(Relation rel) {
		Relation result = new Relation();
		for (Pair<Token,Token> p : rel.getRelation()) {
			Pair<Token,Token> refined = abstractSingletonRel(p);
			if (!result.getRelation().contains(refined)) {
				result.addPair(refined);
			}
		}
		
		Relation finalResult = new Relation();
		
		for (Pair<Token,Token> p : result.getRelation()) {
			Token img = p.getSnd();
			ArrayList<Token> temp = new ArrayList<Token>();
			for (Pair<Token, Token> candidate: result.getRelation()) {
				if (candidate.getSnd().equals(img)) {
					temp.add(candidate.getFst().clone());
				}
			}
			
			if (temp.size() <= 1) {
				finalResult.addPair(temp.get(0), img.clone());
			} else {
				Token conj = new Token(Parser.PRED,Parser.AND);
				for (Token t: temp) {
					conj.addToken(t.clone());
				}
				finalResult.addPair(conj, img.clone());
			}
		}
		return finalResult;
	}

	
	public boolean imply(Context ctx, Solver solver, Token fstPred, Token sndPred) {
	
		Token constraint = new Token(Parser.PRED,Parser.AND);
		constraint.addToken(new Token(Parser.PRED,Parser.NEG).addToken(sndPred)).addToken(fstPred);
		return !isSat(ctx,solver,constraint);
	}
	
	
	
	public boolean isAtomic(Token program) {
		
		if (program.getName().startsWith(Parser.ASSIGN) ||
				program.getName().startsWith(Parser.ATOMIC) || program.getName().startsWith(Parser.AWAIT)) {
			return true;
		}
		
		if (program.getName().startsWith(Parser.BLOCK) && program.getTokens().size() == 1 && isAtomic(program.getTokens().get(0))) {
			return true;
		}
		
		return false;
	}
	
	public boolean isPrimitive(int index, Token program, boolean isInner) {
		
		if (!program.getTokens().get(index).getType().equals(Parser.PRED)) {
			return false;
		}
		
		if ((index == 0 || index == program.getTokens().size()-1) && !isInner) {
			return true;
		}
		
		if (index - 1 >= 0 && isAtomic(program.getTokens().get(index-1))) {
			return true;
		}
		
		if (index + 1 < program.getTokens().size() && isAtomic(program.getTokens().get(index+1))) {
			return true;
		}
		
		return false;
	}

}
