package dna;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

public class DNA {
	private List<Base> bases;
	
	/**
	 * Constructor that takes DNA string, convert them into DNA enum, and insert into list.
	 * @param dnaString
	 */
	public DNA(String dnaString) {
		bases = new ArrayList<>();
		for (int i = 0; i < dnaString.length(); i++) {
			// convert char character in string to base
			// then insert base into bases (List<Base>)
			char dnaCharacter = dnaString.charAt(i);
			String dnaChartoString = String.valueOf(dnaCharacter);
			Base base = Base.valueOf(dnaChartoString);
			bases.add(base);
		}
	}
	
	/**
	 * Constructor for random generation of DNA sequence.
	 * @param length of random sequence to be generated.
	 */
	public DNA(int n) {
		bases = new ArrayList<>();
		Random r = new Random();
		for (int i = 0; i < n; i++) { 
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
	}
	
	@Override
	public String toString() {
		// convert base back into String
		StringBuilder builder = new StringBuilder();
		for (Base base: bases) {
			builder.append(base.toString());
		}
		return builder.toString();
	}
	
	/**
	 * Brute force search.
	 * @param kmer
	 * @return list of first index of the sequence when the k-mer finds its match in the sequence.
	 */
	public List<Integer> getIndex(DNA kmer) {
		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < bases.size() - kmer.bases.size() + 1; i++) {
			if (isSame(kmer, i)) {
				indices.add(i);
			}
		}
		return indices;
	}
	
	/**
	 * Brute force search for each core of CPU.
	 * @param kmer
	 * @return
	 */
	public List<Integer> getIndexRange(DNA kmer, int start, int end) {
		List<Integer> indices = new ArrayList<>();
		for (int i = start; i < end; i++) {
			if (isSame(kmer, i)) {
				indices.add(i);
			}
		}
		return indices;
	}
	
	/**
	 * Compare each character of k-mer to each character of the entire sequence.
	 * @param kmer
	 * @param index
	 * @return true if the k-mer finds its match in the sequence. Otherwise, false.
	 */
	public boolean isSame(DNA kmer, int index) {
		for (int i = 0; i < kmer.bases.size(); i++) {
			if (kmer.bases.get(i) != bases.get(index + i)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Hashing implemented search.
	 * @param kmer
	 * @return
	 */
	public boolean isSameFast(DNA kmer) {
		int dnaSize = bases.size();
		int kmerSize = kmer.bases.size();
		int dnaHashVal = 0, kmerHashVal = 0;
		
		// get hash value of k-mer (conversion of character to ASCII involved)
		String kmerString = kmer.toString();
		for (int i = 0; i < kmerSize; i++) {
			char kmerChar = kmerString.charAt(i);
			int charAsc = (int) kmerChar;
			kmerHashVal += charAsc * Math.pow(2, kmerSize - i - 1);
		}
		
		// get hash value of substring of k-mer length
		// compare the hash value of substring to that of k-mer's
		String dnaString = bases.toString();
		for (int i = 0; i < dnaSize - kmerSize; i++) {
			// initializing hash value of substring
			if (i == 0) {
				for (int j = 0; j < kmerString.length(); j++) {
					char dnaChar = dnaString.charAt(i);
					int dnaAscVal = (int) dnaChar;
					dnaHashVal += dnaAscVal * Math.pow(2, kmerSize - i - 1);		
					System.out.println("first substring val: " + dnaHashVal);
				}
			}
			
			// as i moves along the sequence, calculate corresponding substring hash value
			// dnaSizeHash = 2 * (dnaHashVal - frontValue) + newComingValue
			else {
				char dnaChar = dnaString.charAt(i);
				dnaHashVal = 2 * (dnaHashVal - (int) dnaString.charAt(i-1)  * 3) + (int) dnaString.charAt(i + kmerSize - 1);
			}
			System.out.println("dnaHashVal: " + dnaHashVal + ", " + "kmerHashVal: " + kmerHashVal);
			// if hash value of substring is equal to hash value of k-mer,
			// compare each character in both strings
			if (dnaHashVal == kmerHashVal) {
				for (int j = 0; j < kmerString.length(); j++) {
					if (dnaString.charAt(i + j) != kmerString.charAt(j)) {
						return false;
					}
				}
				System.out.println("i value: " + i);
			}
		}
		return true;	
	}
	
	private class GetIndex implements Runnable {
		public volatile List<Integer> returnValue;
		private DNA kmer;
		private int startingPoint;
		private int endPoint;
		
		public GetIndex(DNA kmer, int startingPoint, int endPoint) {
			this.kmer = kmer;
			this.startingPoint = startingPoint;
			this.endPoint = endPoint;
		}
		
		@Override
		public void run() {
			returnValue = getIndexRange(kmer, startingPoint, endPoint);
		}
		
	}
	
	/**
	 * 
	 * @param kmer
	 * @return
	 */
	public List<Integer> getIndexFast(DNA kmer) {
		// how many cores you have
		List<Integer> combinedOutput = new ArrayList<>();
		int cores = 2;
		int totalSize = bases.size() - kmer.bases.size() + 1;
		int divSize = totalSize / cores;
		List<GetIndex> outputs = new ArrayList<>();
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < cores; i++) {
			int startingPoint = i * divSize;
			int endPoint = startingPoint + divSize;
			if (i == cores - 1) {
				endPoint = totalSize;
			}
			GetIndex output = new GetIndex(kmer, startingPoint, endPoint);
			outputs.add(output);
			Thread thread = new Thread(output);
			threads.add(thread);
			//thread.start(); // starts a new thread and calls run in runnable interface in that new thread
		}
		
		for (Thread thread : threads) {
			thread.start();
		}
		
		for (Thread thread : threads) {
			try {
				thread.join(); // one thread is waiting until another thread completes its execution
			} catch (InterruptedException e) {
				e.printStackTrace(); 
			}
		}
		for (GetIndex output : outputs) { 
			combinedOutput.addAll(output.returnValue);
		}
		Collections.sort(combinedOutput);
		
		return combinedOutput;
	}
}
