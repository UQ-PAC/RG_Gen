public class Pair<T,G> {
	
	private T first;
	private G second;
	
	public Pair(T fst, G snd) {
		first = fst;
		second = snd;
	}

	public T getFst() {
		return first;
	}
	
	public G getSnd() {
		return second;
	}
	
	public Pair<T,G> setFst(T fst){
		first = fst;
		return this;
	}
	
	public Pair<T,G> setSnd(G snd){
		second = snd;
		return this;
	}
	
	public String toString() {
		return "(" + first.toString() + "," + second.toString() + ")";
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		if (! (obj instanceof Pair<?,?>)) {
			return false;
		}
		Pair<T,G> p = (Pair<T,G>) obj;
		if (first.equals(p.getFst()) && second.equals(p.getSnd())) {
			return true;
		}
		
		return false;
	}
}
