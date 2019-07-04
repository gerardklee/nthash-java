package dna;

public enum Base {
	A, T, G, C;

	@Override
	public String toString() {
		
		// prints out enum values (A, T, G, C)
		return this.name();
	}
}
