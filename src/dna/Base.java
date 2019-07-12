package dna;

public enum Base {
	A(1), T(2), G(3), C(4);
	
	private final long value;
	
	Base(final long newValue) {
		value = newValue;
	}
	
	public long getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		
		// prints out enum values (A, T, G, C)
		return this.name();
	}
}
