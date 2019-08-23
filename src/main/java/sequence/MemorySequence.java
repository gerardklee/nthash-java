package sequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dna.DNA;

/**
 * An immutable sequence that is stored completely in memory.
 * @author gerardlee
 */
public class MemorySequence implements Sequence {
	private List<Base> bases;
	
	// Long is the length of the k-mer
	// built a map based on the length of the k-mer
	private Map<Long, Map<Long, List<Integer>>> indices;
	
	/**
	 * Constructs a new MemorySequence from the input sequence string that contains A, G, T, and C.
	 * Note that there should be no white space between any of the bases.
	 * @param sequence input sequence
	 * @return new MemorySequence
	 */
	public static MemorySequence fromString(String sequence) { // static constructor/initializer
		List<Base> bases = new ArrayList<>();
		for (int i = 0; i < sequence.length(); i++) {
			// convert char character in string to base
			// then insert base into bases (List<Base>)
			char dnaCharacter = sequence.charAt(i);
			String dnaChartoString = String.valueOf(dnaCharacter);
			Base base = Base.valueOf(dnaChartoString);
			bases.add(base);
		}
		return new MemorySequence(bases);
	}
	
	/**
	 * Generates a random MemorySequence of length k.
	 * @param k length of the MemorySequence
	 * @return new MemorySequence
	 */
	public static MemorySequence random(int k) {
		List<Base> bases = new ArrayList<>();
		Random r = new Random();
		for (int i = 0; i < k; i++) { 
			int randomValue = r.nextInt(4); // 0 - 3
			if (randomValue == 0) { 
				bases.add(Base.A);
			} else if (randomValue == 1) {
				bases.add(Base.T);
			} else if (randomValue == 2) {
				bases.add(Base.G);
			} else {
				bases.add(Base.C);
			} 			
		}
		return new MemorySequence(bases);
	}
	
	/**
	 * Creates a new memory sequence from a list of bases.
	 * @param bases list of bases
	 */
	private MemorySequence(List<Base> bases) {
		this.indices = new HashMap<>();
		this.bases = new ArrayList<>(bases);
	}
	
	@Override
	public void buildIndex(long k) { // long vs. Long (autoboxing, autounboxing)
		Map<Long, List<Integer>> result = new HashMap<>();
		int dnaSize = bases.size();
		long dnaHashVal = 0;
		// compare each k-mer in the entire sequence to the target k-mer
		for (int i = 0; i < dnaSize - k + 1; i++) {
			
			// initializing k-mer at dna[0]
			if (i == 0) {
				for (int j = 0; j < k; j++) {
					dnaHashVal ^= Long.rotateLeft(bases.get(j).getValue(), (int) (k - j - 1));
					
				}
			}
			
			// calculate k-mer at dna[i]
			else {
				dnaHashVal = Long.rotateLeft(dnaHashVal, 1) ^ Long.rotateLeft(bases.get(i - 1).getValue(), (int) k) ^ bases.get((int) (i + k - 1)).getValue();
			}
			
			if (result.containsKey(dnaHashVal)) {
				result.get(dnaHashVal).add(i);				
			} else {
				List<Integer> indices = new ArrayList<>();
				indices.add(i);
				result.put(dnaHashVal, indices);
			}
		}
		indices.put(k, result);
	}

	@Override
	public List<Long> getMatchingPositions(Sequence kmer) throws Exception{
		if (indices.containsKey(kmer.getSize())) {
			return findPositionFast(indices.get(kmer.getSize()), kmer);
		}
		return findPositionSlow(kmer);
	}
	
	private List<Long> findPositionFast(Map<Long, List<Integer>> map, Sequence kmer) {
		long k = kmer.getSize();
		long kmerHashVal = 0;
		
		// get hashVal
		for (long i = 0; i < k; i++) {
			kmerHashVal ^= Long.rotateLeft(kmer.getBase(i).getValue(), (int) (k - i - 1));
		}
		
		if (!map.containsKey(kmerHashVal)) {
			return List.of();
		}
		
		List<Long> result = new ArrayList<>();
		for (long index : map.get(kmerHashVal)) {
			if (isSame(kmer, index)) {
				result.add(index);
			}
		}
		return result;
	}
	
	private List<Long> findPositionSlow(Sequence kmer) {
		List<Long> indices = new ArrayList<>();
		for (long i = 0; i < bases.size() - kmer.getSize() + 1; i++) {
			if (isSame(kmer, i)) {
				indices.add(i);
			}
		}
		return indices;
	}
	
	private boolean isSame(Sequence kmer, long index) {
		for (long i = 0; i < kmer.getSize(); i++) {
			if (kmer.getBase(i) != this.bases.get((int) (index + i))) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public long getSize() {		
		return bases.size();
	}

	@Override
	public Base getBase(long position) {		
		return bases.get((int) position) ;
	}

}
