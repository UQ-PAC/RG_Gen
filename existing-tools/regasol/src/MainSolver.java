import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.microsoft.z3.*;

public class MainSolver {
	
	String outputString = "";	

	public ArrayList<ProofStruct> runBasic(Context ctx, Solver solver, ArrayList<Token> programs, 
			Token precond, Token postcond, int iterations) {
		PGenerator pg = new PGenerator();
		SChecker sc = new SChecker();
		SatSolver.countSAT=0;
		String output = "";
		outputString = "";
		
		ArrayList<ProofStruct> proofs = init(programs,precond);
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			if (i == 0) {
				proofs = pg.run(ctx, solver, proofs,true);
			} else {
				proofs = pg.run(ctx, solver, proofs,false);
			}
			
			proofs = sc.run(ctx, solver, proofs, "");
			output = printEndIteration(i+1,proofs);
			outputString = outputString + output + "\n";
			System.out.println(output);
			
			if(isStableAll(proofs)) {
				long estimatedTime = System.currentTimeMillis() - startTime;
				output = printStable(estimatedTime,i+1,postcond,proofs);
				outputString = outputString + output + "\n";
				//System.out.println(output);
				Token combinedPost = new Token(Parser.PRED,Parser.AND);
				for (ProofStruct ps: proofs) {
					combinedPost.addToken(ps.provedProgram.getTokens().get(ps.provedProgram.getTokens().size()-1));
				}
				if (combinedPost.getTokens().size() == 1) {
					combinedPost = combinedPost.getTokens().get(0);
				}
				output = output + "\n" + printPost(ctx,solver,combinedPost,postcond);
				outputString = output + outputString;
				System.out.println(output);
				return proofs;
			}
		}
		long estimatedTime = System.currentTimeMillis() - startTime;
		output = printUnstable(estimatedTime,iterations);
		outputString = output + outputString;
		System.out.println(output);
		return proofs;
	}

	public ArrayList<ProofStruct> runAdv(Context ctx, Solver solver, ArrayList<Token> programs, 
			Token precond, Token postcond, int iterations, HashMap<Integer,Pair<Integer,ArrayList<Token>>> mirrors) {
		
		PGenerator pg = new PGenerator();
		SChecker sc = new SChecker();
		SatSolver.countSAT=0;
		String output = "";
		outputString = "";
		
		ArrayList<ProofStruct> proofs = init(programs,precond);
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			if (i == 0) {
				proofs = pg.run(ctx, solver, proofs, mirrors, true);
			} else {
				proofs = pg.run(ctx, solver, proofs, mirrors, false);
			}
			
			proofs = sc.run(ctx, solver, proofs, mirrors, "--a");
			output = printEndIteration(i+1,proofs);
			outputString = outputString + output + "\n";
			System.out.println(output);
			
			if(isStableAll(proofs)) {
				long estimatedTime = System.currentTimeMillis() - startTime;
				output = printStable(estimatedTime,i+1,postcond,proofs);
				outputString = outputString + output + "\n";
				//System.out.println(output);
				Token combinedPost = new Token(Parser.PRED,Parser.AND);
				for (ProofStruct ps: proofs) {
					combinedPost.addToken(ps.provedProgram.getTokens().get(ps.provedProgram.getTokens().size()-1));
				}
				if (combinedPost.getTokens().size() == 1) {
					combinedPost = combinedPost.getTokens().get(0);
				}
				output = output + "\n" + printPost(ctx,solver,combinedPost,postcond);
				outputString = output + outputString;
				System.out.println(output);
				return proofs;
			}
		}
		long estimatedTime = System.currentTimeMillis() - startTime;
		output = printUnstable(estimatedTime,iterations);
		outputString = output + outputString;
		System.out.println(output);
		return proofs;
	}
	
	public ArrayList<ProofStruct> runAdvParallel(ArrayList<Context> ctx, ArrayList<Solver> solver, ArrayList<Token> programs, 
			Token precond, Token postcond, int iterations, HashMap<Integer,Pair<Integer,ArrayList<Token>>> mirrors) throws InterruptedException {
		
		PGenerator pg = new PGenerator();
		SChecker sc = new SChecker();
		SatSolver.countSAT=0;
		String output = "";
		outputString = "";
		
		ArrayList<ProofStruct> proofs = init(programs,precond);
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			if (i == 0) {
				proofs = pg.run(ctx, solver, proofs, true);
			} else {
				proofs = pg.run(ctx, solver, proofs, false);
			}
			
			proofs = sc.run(ctx, solver, proofs, "--a");
			output = printEndIteration(i+1,proofs);
			outputString = outputString + output + "\n";
			System.out.println(output);
			
			if(isStableAll(proofs)) {
				long estimatedTime = System.currentTimeMillis() - startTime;
				output = printStable(estimatedTime,i+1,postcond,proofs);
				outputString = outputString + output + "\n";
				//System.out.println(output);
				Token combinedPost = new Token(Parser.PRED,Parser.AND);
				for (ProofStruct ps: proofs) {
					combinedPost.addToken(ps.provedProgram.getTokens().get(ps.provedProgram.getTokens().size()-1));
				}
				if (combinedPost.getTokens().size() == 1) {
					combinedPost = combinedPost.getTokens().get(0);
				}
				output = output + "\n" + printPost(ctx.get(0),solver.get(0),combinedPost,postcond);
				outputString = output + outputString;
				System.out.println(output);
				return proofs;
			}
		}
		long estimatedTime = System.currentTimeMillis() - startTime;
		output = printUnstable(estimatedTime,iterations);
		outputString = output + outputString;
		System.out.println(output);
		return proofs;
	}
	
	public ArrayList<ProofStruct> init(ArrayList<Token> programs, Token precond){
		ArrayList<ProofStruct> result = new ArrayList<ProofStruct>();
		for (Token prog: programs) {
			ProofStruct ps = new ProofStruct(prog);
			ArrayList<Token> pre = new ArrayList<Token>();
			pre.add(precond);
			ps.setUR(null, pre);
			result.add(ps);
		}
		
		return result;
	}
	
	public boolean isStableAll(ArrayList<ProofStruct> proofs) {
		for (ProofStruct ps: proofs) {
			if (ps.currentUR != null) {
				return false;
			}
		}
		
		return true;
	}
	
	
	public String printEndIteration(int iterations, ArrayList<ProofStruct> proofs) {
		String result = "";
		result = result + "--------------------------\n";
		result = result + "| End of iteration " + iterations + "     |\n";
		result = result + "--------------------------\n";
		for (ProofStruct ps: proofs) {
			result = result + ps + "\n";
			result = result + "-------------------------------\n";
		}
		
		result = result + "\n++++++++++++++++++++++++++++++++++++\n";
		return result;
	}
	
	public String printUnstable(long runningTime, int iterations) {
		String result = "";
		result = result + "Proofs are unstable after " + iterations + " iterations" + "\n";
		result = result + "Running time (in milliseconds): " + runningTime + "\n";
		result = result + "SAT calls: " + SatSolver.countSAT + "\n";
		return result;
	}
	
	public String printStable(long runningTime, int iterations, Token postcond, ArrayList<ProofStruct> proofs) {
		String result = "";
		result = result + "Number of iterations to be stable: " + iterations + "\n";
		result = result + "Running time (in milliseconds): " + runningTime + "\n";
		result = result + "SAT calls: " + SatSolver.countSAT;
		return result;
	}
	
	
	public String printPost(Context ctx, Solver solver, Token combinedPost, Token postcond) {
		String result = "";
		Printer printer = new Printer();
		result = result + "Combined postcondition: " + "\n";
		result = result + "   " + printer.printPredicate(combinedPost) + "\n";
		result = result + "Asserted Postcondition: " + "\n";
		result = result + "   " + printer.printPredicate(postcond) + "\n";
		Computation comp = new Computation();
		if (comp.imply(ctx, solver, combinedPost, postcond)) {
			result = result + "Combined postcondition implies the asserted postcondition\n";
		} else {
			result = result + "Combined postcondition fails to imply the asserted postcondition\n";
		}
		
		return result;
	}
	
	public ArrayList<ProofStruct> solveFromFile(String inputFile) throws InterruptedException {
		
		File input = new File(inputFile);
		Parser par = new Parser();
		int iterations = 5;//default
		Scanner sc;
		
		HashMap<Integer,Pair<Integer,ArrayList<Token>>> mirrors = new HashMap<Integer,Pair<Integer,ArrayList<Token>>>();
		ArrayList<Token> programs = new ArrayList<Token>();
		Token precond = new Token(Parser.PRED);
		Token postcond = new Token(Parser.PRED);
		ArrayList<String> vars = new ArrayList<String>();
		ArrayList<String> programIDs = new ArrayList<String>();
		ArrayList<String> runPrograms = new ArrayList<String>();
		boolean parallel = false;
		boolean advance = false;
		try {
			sc = new Scanner(input);
			while(sc.hasNextLine()) {
				String line = sc.nextLine().trim();
				if (!line.equals("")) {
					
					if (line.startsWith("iterations:")) {
						line = line.substring("iterations:".length()).trim();
						try {
							int iter = Integer.parseInt(line);
							iterations = iter;
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
					
					if (line.startsWith("variable")) {
						line = line.substring("variable".length());
						line = line.replaceAll("\\s*,\\s*", " ");
						String[] tokens = line.split(" ");
						for (String t: tokens) {
							if (!vars.contains(t)) {
								vars.add(t);
							}
						}
					}
					
					if (line.startsWith("program")) {
						line = line.substring("program".length()).trim();
						programIDs.add(line);
						line = sc.nextLine();
						String prog = "";
						while (!line.startsWith("end-program")) {
							prog = prog +line + "\n"; 
							line = sc.nextLine();
						}
						Token progToken = par.parseProgram(prog);
						programs.add(progToken);
					}
					
					if (line.startsWith("precondition:")) {
						line = line.substring("precondition:".length()).trim();
						line = line.replaceAll("\\[", "").replaceAll("\\]", "").trim();
						precond = par.parsePredicate(line);
					}
					
					if (line.startsWith("postcondition:")) {
						line = line.substring("postcondition:".length()).trim();
						line = line.replaceAll("\\[", "").replaceAll("\\]", "").trim();
						postcond = par.parsePredicate(line);
					}
					
					if (line.startsWith("run:")) {
						line = line.substring("run:".length()).trim();
						line = line.replaceAll("\\s*,\\s*", " ");
						String[] tokens = line.split(" ");
						for (String t: tokens) {
							runPrograms.add(t);
						}
					}
					
					if (line.startsWith("mode:")) {
						line = line.substring("mode:".length()).trim();
						line = line.replaceAll("\\s*,\\s*", " ");
						String[] tokens = line.split(" ");
						for (String t: tokens) {
							if (t.equals("parallel")) {
								parallel = true;
							}
							
							if (t.equals("advance")) {
								advance = true;
							}
						}
					}
					
					if (line.startsWith("mirror:")) {
						line = line.substring("mirror:".length()).trim();
						String[] components = line.split("\\s*;\\s*");
						for(String s: components) {
							String mirrorProg = s.substring(0,s.indexOf(":")).trim();
							s = s.substring(s.indexOf(":")+1).trim();
							String imageProg = s.substring(0,s.indexOf("[")).trim();
							s = s.substring(s.indexOf("[") + 1);
							s = s.substring(0,s.indexOf("]"));
							String[] eqs = s.split(",");
							ArrayList<Token> eqList = new ArrayList<Token>();
							for (String eq: eqs) {
								eqList.add(par.parsePredicate(eq));
							}
							
							if (programIDs.indexOf(mirrorProg) != -1 && programIDs.indexOf(imageProg) != -1) {
								mirrors.put(programIDs.indexOf(mirrorProg), 
										new Pair<Integer,ArrayList<Token>>(programIDs.indexOf(imageProg),eqList));
							}
						}
					}
				}
			}
			sc.close();
			
			ArrayList<ProofStruct> result = new ArrayList<ProofStruct>();
			
			if(parallel) {
				ArrayList<Context> ctxList = new ArrayList<Context>();
				ArrayList<Solver> solverList = new ArrayList<Solver>();
				for (int i=0;i<programs.size();i++) {
					ctxList.add(new Context());
					solverList.add(ctxList.get(i).mkSolver());
				}
				
				result = runAdvParallel(ctxList,solverList,programs,precond,postcond,iterations, mirrors);
			} else if (advance){
				Context ctx = new Context();
				Solver solver = ctx.mkSolver();
				result =  runAdv(ctx,solver,programs,precond,postcond,iterations, mirrors);
			} else {
				Context ctx = new Context();
				Solver solver = ctx.mkSolver();
				result = runBasic(ctx,solver,programs,precond,postcond,iterations);
			}
			
			Printer printer = new Printer();
			printer.writeToFile(outputString, inputFile + ".result");
			
			return result;

			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws InterruptedException {
		MainSolver mainS = new MainSolver();
		if (args.length != 1) {
			System.out.println("Need 1 arguement for file name!");
		} else {
			mainS.solveFromFile(args[0]);
		}
	}
}
