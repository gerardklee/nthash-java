package dna;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

public class DNA {
	private List<Base> bases;
	private Hashtable<Integer, String> rollhash = new Hashtable<Integer, String>();
	
	/**
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
	 * @param kmer
	 * @return
	 */
	public boolean isSameFast(DNA kmer) {
		int dnaSize = bases.size();
		int kmerSize = kmer.bases.size();
		int dnaSizeHash = 0, kmerSizeHash = 0, power = 1;
		
		//hash value of kmer
		String kmerString = kmer.toString();
		for (int i = 0; i < kmerSize; i++) {
			char kmerChar = kmerString.charAt(i);
			int charAsc = (int) kmerChar;
			kmerSizeHash += charAsc * Math.pow(2, kmerSize - i - 1);
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
