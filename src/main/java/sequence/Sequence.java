package sequence;

import java.util.List;

public interface Sequence {
	/**
	 * Builds the index based on the length of the k-mer.
	 * @param k length of the k-mer
	 * @throws Exception 
	 */
	public void buildIndex(long k) throws Exception;
	
	/**
	 * Gets positions of the sequence that matches with k-mer. If an index has been built, then this method
	 * tries to use that index to perform faster look-up.
	 * @param kmer sub-sequence to search for
	 * @return list of matching starting positions in this sequence
	 */
	public List<Long> getMatchingPositions(Sequence kmer) throws Exception;
	
	/**
	 * @return size of the sequence
	 */
	public long getSize();
	
	/**
	 * @param position the position of the sequence
	 * @return the base at the position provided
	 */
	public Base getBase(long position);
	
}
