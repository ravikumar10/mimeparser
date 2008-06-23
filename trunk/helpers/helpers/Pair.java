package helpers;

public class Pair<T,S> {
	
	private T firstObject;
	private S secondObject;
	
	public Pair(T firstObject,S secondObject) {
		this.firstObject=firstObject;
		this.secondObject=secondObject;
	}
	
	public T getFirstObject() {
		return firstObject;
	}
	public S getSecondObject() {
		return secondObject;
	}
	
	@Override
	public int hashCode() {
		int hashcode = this.firstObject.hashCode() + secondObject.hashCode();
//		System.out.println("Hashcode: " + hashcode);
		return hashcode;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Pair) {
			Pair<T,S> pair = (Pair<T, S>) obj;
			return this.getFirstObject().equals(pair.getFirstObject());
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "FO: " + firstObject.toString() + " SO: " + secondObject.toString();
	}
}
