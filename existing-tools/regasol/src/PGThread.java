import com.microsoft.z3.*;

public class PGThread extends PGenerator implements Runnable{
	
	Context ctx;
	Solver solver;
	boolean isFstTime;
	ProofStruct ps;
	
	public PGThread(Context nctx, Solver nsolver, ProofStruct nps, boolean fstTime) {
		ctx = nctx;
		solver = nsolver;
		ps = nps;
		isFstTime = fstTime;
	}
	
	public void run() {
		if(isFstTime) {
			Token precond = ps.currentUR.get(0);
			ps = generateSProof(ctx,solver,precond,ps.provedProgram);
		} else {
			ps = fixProof(ctx,solver,ps);
		}
	}

}
