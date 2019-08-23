package sequence;

/**
 * These values represents DNA nucleotides and the corresponding hash values are derived from 
 * ntHash: recursive nucleotide hashing.
 * @author gerardlee
 */

public enum Base {
	A(0x3c8bfbb395c60474L), T(0x3193c18562a02b4cL), G(0x20323ed082572324L), C(0x295549f54be24456L);
	
	private final long value;
	
	Base(final long newValue) {
		value = newValue;
	}
	
	/**
	 * @return hash value of the base
	 */
	public long getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		
		// prints out enum values (A, T, G, C)
		return this.name();
	}
}
