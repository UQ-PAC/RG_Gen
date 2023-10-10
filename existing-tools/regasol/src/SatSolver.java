import java.io.*;
import com.microsoft.z3.*;

public class SatSolver {
	public static int countSAT = 0;

	public static String solver = "z3";
	public static String z3Dir = "";
	
	
	public void setSolver(String newSolver) {
		solver = newSolver;
	}
	
    public String runSolver(File inputFile) {
    	SatSolver.countSAT++;
        Runtime instance = Runtime.getRuntime();
        String[] commandAndArguments = {solver,inputFile.getAbsolutePath()};

        try {
            Process p = instance.exec(commandAndArguments);
            String response = readProcessOutput(p);
            //System.out.println(response);
            return response;
        }catch(Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
   
    public String runSolver(Context ctx, Solver solver, Token pred, String option){
    	SatSolver.countSAT++;
    	String result = "";
    	Parser p = new Parser();
    	BoolExpr expr = p.parseBoolExpr(ctx, pred);
    	solver.add(expr);
        if (solver.check().equals(Status.SATISFIABLE)) {
            result = "sat";
            if (option.equals("model")) {
                result = result + "\n" + solver.getModel();
            }
        } else {
        	result = "unsat";
        }
    	return result;
    }
    
    public String runSolver(Solver solver, BoolExpr expr, String option){
    	SatSolver.countSAT++;
    	String result = "";
    	solver.push();
    	solver.add(expr);
        if (solver.check().equals(Status.SATISFIABLE)) {
            result = "sat";
            if (option.equals("model")) {
                result = result + "\n" + solver.getModel();
            }
        } else {
        	result = "unsat";
        }
        solver.pop();
    	return result;
    }
    
    public String readProcessOutput(Process p) throws Exception{
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String response = "";
        String line;
        while ((line = reader.readLine()) != null) {
            response += line+"\r\n";
        }
        reader.close();
        return response;
    }
}
