import java.util.ArrayList;
import java.util.HashMap;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;

public class SChecker {
	//Sequential version
	public ArrayList<ProofStruct> run(Context ctx, Solver solver, ArrayList<ProofStruct> proofs, String option){
		
		ArrayList<ProofStruct> result = new ArrayList<ProofStruct>();
		for (ProofStruct ps: proofs) {
			result.add(findUnstableResidue(ctx,solver,ps, option,false));
		}
		return result;
	}
	
	//Parallel version
	public ArrayList<ProofStruct> run(ArrayList<Context> ctx, ArrayList<Solver> solver, 
			ArrayList<ProofStruct> proofs, String option) throws InterruptedException{
		
		ArrayList<ProofStruct> result = new ArrayList<ProofStruct>();
		ArrayList<SCThread> scthreads = new ArrayList<SCThread>();
		ArrayList<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < proofs.size();i++) {
			scthreads.add(new SCThread(ctx.get(i),solver.get(i),proofs.get(i),option));
			threads.add(new Thread(scthreads.get(i)));
			threads.get(i).start();
		}
		for (Thread t : threads) {
			t.join();
		}
		for (int i = 0 ; i < proofs.size();i++) {
			result.add(scthreads.get(i).ps);
		}
		return result;
	}
	
	//With mirror
	public ArrayList<ProofStruct> run(Context ctx, Solver solver, ArrayList<ProofStruct> proofs, 
			HashMap<Integer, Pair<Integer,ArrayList<Token>>> mirrors, String option){
		
		ArrayList<ProofStruct> result = new ArrayList<ProofStruct>();
		for (ProofStruct ps: proofs) {
			result.add(new ProofStruct());
		}
		
		for (int i = 0; i < proofs.size(); i++) {
			if (!mirrors.containsKey(i)) {
				ProofStruct ps = proofs.get(i);
				result.set(i,findUnstableResidue(ctx,solver,ps,option,false));
			}
		}
		
		for (int i = 0; i < proofs.size(); i++) {
			if (mirrors.containsKey(i)) {
				ProofStruct target = result.get(mirrors.get(i).getFst());
				ArrayList<Token> subList = mirrors.get(i).getSnd();
				result.set(i, target.substitute(subList));
			}
		}
		
		return result;
	}
	
	
	public ProofStruct findUnstableResidue(Context ctx, Solver solver, ProofStruct ps, String option, boolean isInner){
		
		Computation comp = new Computation();
		Token program = ps.provedProgram;
		Relation rely = ps.rely;
		
		for (int i = 0; i < program.getTokens().size(); i++) {
			if (comp.isPrimitive(i,program, isInner)) {
				Token primitivePred = program.getTokens().get(i);
				if (!comp.isStable(ctx, solver, primitivePred, rely)) {
					Relation CRely;
					if (option.equals("--a") && rely.getRelation().size() > 70) {
						CRely = comp.computeClosureByExtension(ctx, solver, ps.relyClosure, rely);
					} else {
						CRely = comp.computeClosure(ctx, solver, rely);
					}
					ArrayList<Token> ur = comp.getUnstableResidue(ctx, solver, primitivePred, CRely);
					ArrayList<Integer> index = new ArrayList<Integer>();
					index.add(i);
					ps.setUR(index, ur);
					ps.setUP(primitivePred.clone());
					ps.setRelyClosure(CRely);
					return ps; 
					
				}
				
			} else if (program.getTokens().get(i).getName().startsWith(Parser.BLOCK) || 
					program.getTokens().get(i).getName().startsWith(Parser.WHILE) ||
					program.getTokens().get(i).getName().startsWith(Parser.IF)) {

				ProofStruct sub = new ProofStruct(program.getTokens().get(i).clone());
				sub.setR(ps.rely);
				sub.setRelyClosure(ps.relyClosure);
				sub = findUnstableResidue(ctx,solver,sub,option,true);
				if (sub.URIndex != null) {
					sub.URIndex.add(0,i);
					ps.setUR(sub.URIndex, sub.currentUR);
					ps.setUP(sub.uPrimitive);
					ps.setRelyClosure(sub.relyClosure);
					return ps;
				}
			}
		}
		
		ps.setUR(null, null);
		return ps;
	}
}
