import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;

public class SCThread extends SChecker implements Runnable{
	
	Context ctx;
	Solver solver;
	ProofStruct ps;
	String option;
	
	public SCThread(Context nctx, Solver nsolver, ProofStruct nps, String opt) {
		ctx = nctx;
		solver = nsolver;
		ps = nps;
		option = opt;
	}
	
	public void run() {
		ps = findUnstableResidue(ctx,solver,ps,option,false);
	}
}