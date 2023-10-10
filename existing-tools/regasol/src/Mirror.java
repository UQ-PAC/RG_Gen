import java.util.ArrayList;

public class Mirror {
	
	public Pair<Integer,Integer> pair;
	ArrayList<Token> subList;
	
	public Mirror(Pair<Integer,Integer>p, ArrayList<Token> list) {
		pair = p;
		subList = list;
	}
	
	public void setPair(int src, int dest) {
		pair = new Pair<Integer,Integer>(src,dest);
	}
	
	public void setList(ArrayList<Token> list) {
		subList = list;
	}
	
	public void addSub(Token sub) {
		subList.add(sub);
	}

}
