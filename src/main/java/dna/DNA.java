package dna;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import sequence.Base;

public class DNA {
	private List<Base> bases;
	private File file;
	Connection conn;
	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/test";  
	
	/**
	 * Constructor for FASTA file process.
	 */
	public DNA() {
		try {
			File file = new File("/Users/gerardlee/Desktop/file.txt");
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			String dnaString = "";
			boolean first = true;
			while((line = br.readLine()) != null) {
				if (line.startsWith(">")) {
					if (first) { first = false; }
				} else {
					dnaString += line;
				}		
			}
			bases = new ArrayList<>();
			for (int i = 0; i < dnaString.length(); i++) {
				char dnaCharacter = dnaString.charAt(i);
				String dnaChartoString = String.valueOf(dnaCharacter);
				Base base = Base.valueOf(dnaChartoString);
				bases.add(base);
			}
			br.close();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	
	/**
	 * Constructor that takes DNA string, convert them into Base, and insert into list.
	 * @param dnaString DNA sequence
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
	 * @param n of random sequence to be generated
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
	
	/**
	 * Constructor that takes file as an argument and saves it in class.
	 * @param file FASTA file
	 */
	public DNA(File file) {
		this.file = file; 		
	}
	
	/**
	 * This method calculates k-mer at every index from a DNA sequence and insert them into database.
	 * @param k length of the k-mer
	 * @throws Exception
	 */
	public void buildIndexFile(int k) throws Exception{
		// STEP 1: Register JDBC driver 
        Class.forName(JDBC_DRIVER); 
        String tableName = tableName(k);
 
        // STEP 2: Open a connection 
        System.out.println("Connecting to database..."); 
        conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(start BIGINT NOT NULL, hash BIGINT NOT NULL, PRIMARY KEY(start));"; 
        stmt.executeUpdate(sql);
        stmt.close();
        Statement stmt1 = conn.createStatement();
        String sql1 = "CREATE INDEX IF NOT EXISTS hash_index ON " + tableName + "(hash);";
        stmt1.executeUpdate(sql1);
        stmt1.close();
        
        // STEP 3: Inserting data     
		InputStream stream = new FileInputStream(this.file);
		BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
		int character;
		char[] dnaArray = new char[k];
		long dnaHashVal = 0;
		
		// insert initial hash value into the database (need to clear the table in lujing's lesson)
		for (int i = 0; i < k; i++) {
			character = (char) buffer.read();
			System.out.println("in dnaArray: " + (char) character);
			dnaArray[i] = (char) character;
			dnaHashVal ^= Long.rotateLeft(getValue((char) dnaArray[i]), (int) (k - i - 1));
		}
		Statement stmt2 = conn.createStatement();
		String sql2 = "INSERT INTO " + tableName + " VALUES("+ 0 + "," + dnaHashVal + ");";
		stmt2.executeUpdate(sql2);
		stmt2.close();

		// now, insert next corresponding hash values to the db starting from index 1
		int ptr = 0;
		long chr = 4; // set 4 because initial hashval is already in db at index 0
		while((character = buffer.read()) != -1) {
			System.out.println((char) character);
			char temp = dnaArray[ptr];
			dnaArray[ptr] = (char) character;
			dnaHashVal = Long.rotateLeft(dnaHashVal, 1) ^ Long.rotateLeft(getValue(temp), k) ^ getValue(dnaArray[ptr]);
			Statement stmt3 = conn.createStatement();
			String sql3 = "INSERT INTO " + tableName + " VALUES(" + (chr - k) + "," + dnaHashVal + ");";
			stmt3.executeUpdate(sql3);
			stmt3.close();
			chr++;
			ptr = (ptr + 1) % dnaArray.length;
		}
		buffer.close();
		conn.close();
	}		
	
	private String tableName(int k) {
		return "kmer" + k;
	}
	
	/**
	 * Clears the table.
	 * @param k length of the k-mer
	 * @throws Exception if the table cannot be cleared
	 */
	public void clearTable(int k) throws Exception {
		// STEP 1: Register JDBC driver 
        Class.forName(JDBC_DRIVER); 
        
        // STEP 2: Open a connection 
        System.out.println("Connecting to database..."); 
        conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        String sql = "DROP TABLE IF EXISTS " + tableName(k);
        stmt.executeUpdate(sql);
        stmt.close();
        conn.close();
	}

	
	/**
	 * Views the database.
	 * @param k length of the k-mer
	 * @throws Exception when there is no database
	 */
	public void viewDB(int k) throws Exception {
		// STEP 1: Register JDBC driver 
        Class.forName(JDBC_DRIVER); 
        
        // STEP 2: Open a connection 
        System.out.println("Connecting to database..."); 
        conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM " + tableName(k) ;
        ResultSet result = stmt.executeQuery(sql);
        while (result.next()) {
        	long start = result.getLong("start");
        	long hash = result.getLong("hash");
        	System.out.println("start: " + start + ", " + "hash: " + hash);
        }
        result.close();
        stmt.close();
        conn.close();
	}
	
	/**
	 * This method finds the index of the DNA hash value that matches that of the k-mer.
	 * @param kmer The sequence of the k-mer
	 * @return the list with target indices
	 * @throws Exception when an error occurs during reading the input file or the database.
	 */
	public List<Long> getIndexDB(DNA kmer) throws Exception {
		RandomAccessFile randomFile = new RandomAccessFile(file, "r");
		List<Long> output = new ArrayList<>();
		long kmerHashVal = 0;
		for (int i = 0; i < kmer.getSize(); i++) { 
			kmerHashVal ^= Long.rotateLeft(kmer.bases.get(i).getValue(), kmer.getSize() - i - 1);
		}
		char[] kmerArray = kmer.toString().toCharArray();
		// STEP 1: Register JDBC driver 
        Class.forName(JDBC_DRIVER); 
        
        // STEP 2: Open a connection 
        System.out.println("Connecting to database..."); 
        conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        String sql = "SELECT start FROM " + tableName(kmer.getSize()) + " WHERE hash = " + kmerHashVal + ";";
        ResultSet result = stmt.executeQuery(sql);
        while (result.next()) {
        	char[] dnaArray = new char[kmer.getSize()];
        	long start = result.getLong("start");
        	System.out.println(start);
        	randomFile.seek(start);
        	for (int i = 0; i < kmer.getSize(); i++) {
        		dnaArray[i] = (char) randomFile.read();
        		System.out.println(dnaArray[i]);
        	}
        	if (Arrays.equals(kmerArray, dnaArray)) {
        		output.add(start);
        	}
        }
        result.close();
        stmt.close();
        conn.close();
        randomFile.close();
        return output;      
	}
	
	/**
	 * This method sets the magic value to each nucleotide.
	 * @param base each nucleotide (A, G, T, C)
	 * @return the magic value that corresponds to each nucleotide
	 */
	private long getValue(char base) {
		if (base == 'A') {
			return 0x3c8bfbb395c60474L;
		} else if (base == 'T') {
			return 0x3193c18562a02b4cL;
		} else if (base == 'G') {
			return 0x20323ed082572324L;
		} else { // base == 'C'
			return 0x295549f54be24456L;
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
	 * @param kmer sequence of the k-mer
	 * @return list of first index of the sequence when the k-mer finds its match in the sequence
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
	 * @param kmer sequence of the k-mer
	 * @return the list with indices of k-mer from the sequence that matches the actual k-mer
	 */
	public List<Long> getIndexFile(DNA kmer) {
		List<Long> result = new ArrayList<>();
		char[] dnaArray = new char[kmer.getSize()];
		char[] kmerArray = kmer.toString().toCharArray();
		int ptr = 0;
		long chr = 0;

		try {
			InputStream stream = new FileInputStream(this.file);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
			int character;
			
			// read some number of bytes
			// if there is no data, return -1
			while ((character = buffer.read()) != -1) { 
				dnaArray[ptr] = (char) character;
				ptr = (ptr + 1) % dnaArray.length;
				chr++;
				if (chr >= kmerArray.length) {
					boolean match = true;
					for (int i = 0; i < kmerArray.length; i++) {
						if (dnaArray[(ptr + i) % dnaArray.length] != kmerArray[i]) {
							match = false;
							break;
						}
					}
					if (match) {
						result.add(chr - kmer.getSize());
					}
				}
			}
			buffer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * This method uses brute force to find indices of k-mer from the sequence that matches the actual k-mer.
	 * @param kmer sequence of the k-mer
	 * @param start start index of the sequence
	 * @param end end index of the sequence
	 * @return the list with target indices
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
	 * @param kmer the sequence of the actual k-mer
	 * @param index index from the DNA sequence
	 * @return true if the k-mer finds its match in the sequence
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
	 * This method calculates hash value of k-mer from the sequence using Rabin-Karp algorithm.
	 * @param kmer sequence of the actual k-mer
	 * @return list with indices of k-mer from the sequence that matches the hash value of the actual k-mer
	 */
	public List<Integer> getIndexHash(DNA kmer) {
		List<Integer> result = new ArrayList<>();
		int dnaHashVal = 0, kmerHashVal = 0, power = 1;	
		String kmerString = kmer.toString();
		
		// get hash value of substring of k-mer length
		// compare the hash value of substring to that of k-mer's
		String dnaString = bases.toString();
		dnaString = dnaString.replace(" ", "").replace(",", "").replace("[", "").replace("]", "");
		for (int i = 0; i < dnaString.length() - kmerString.length() + 1; i++) {
			// initializing hash value of substring at i == 0
			if (i == 0) {
				for (int j = 0; j < kmerString.length(); j++) {
					dnaHashVal += (int) dnaString.charAt(kmerString.length() - j - 1) * power;
					kmerHashVal += (int) kmerString.charAt(kmerString.length() - j - 1) * power;
					if (j < kmerString.length()) { power *= 2;}
				}
			}
			
			// as i moves along the sequence, calculate corresponding substring hash value
			// dnaSizeHash = 2 * (dnaHashVal - dnaString[i-1] * 2^(kmerLength - 1) + newComingValue 
			// where newComingValue = dnaString[i+m-1]
			else {
				dnaHashVal = 2 * (dnaHashVal - (int) dnaString.charAt(i-1) * 
						         (int) Math.pow(2, kmerString.length() - 1)) + 
						         (int) dnaString.charAt(i+kmerString.length()-1); 
			}
			
			// if hash value of substring is equal to hash value of k-mer,
			// compare each character in both strings
			if (dnaHashVal == kmerHashVal) {
				for (int j = 0; j < kmerString.length(); j++) {
					if (dnaString.charAt(i + j) != kmerString.charAt(j)) {
						return List.of();
					}
				}
				result.add(i);
			}
		}
		return result;	
	}
	
	/**
	 * Calculate hash values of sub-sequences using Rabin-Karp algorithm and bit operation. 
	 * @param kmer sequence of the actual k-mer
	 * @return list of indices of matching sub k-mer from the DNA sequence
	 */
	public List<Integer> getIndexBit(DNA kmer) {
		List<Integer> result = new ArrayList<>();
		int dnaSize = bases.size();
		int k = kmer.bases.size();
		long kmerHashVal = 0;
		long dnaHashVal = 0;
		
		// get hash code of k-mer
		for (int i = 0; i < k; i++) { 
			kmerHashVal ^= Long.rotateLeft(kmer.bases.get(i).getValue(), k - i - 1);
		}
		
		// compare each k-mer in the entire sequence to the target k-mer
		for (int i = 0; i < dnaSize - k + 1; i++) {
			
			// initializing k-mer at dna[0]
			if (i == 0) {
				for (int j = 0; j < k; j++) {
					dnaHashVal ^= Long.rotateLeft(bases.get(j).getValue(), k - j - 1);
				}
			}
			
			// calculate k-mer at dna[i]
			else {
				dnaHashVal = Long.rotateLeft(dnaHashVal, 1) ^ Long.rotateLeft(bases.get(i - 1).getValue(), k) ^ bases.get(i + k - 1).getValue();
			}
			
			if (dnaHashVal == kmerHashVal && isSame(kmer, i)) {
				result.add(i);
			}
		}		
		return result;
	}
	
	/**
	 * Stores DNA hash values and its corresponding indices in hash table.
	 * @param k length of the k-mer
	 * @return hash table with DNA hash values and list of target indices
	 */
	public Map<Long, List<Integer>> buildIndex(int k) {
		Map<Long, List<Integer>> result = new HashMap<>();
		int dnaSize = bases.size();
		long dnaHashVal = 0;
		// compare each k-mer in the entire sequence to the target k-mer
		for (int i = 0; i < dnaSize - k + 1; i++) {
			
			// initializing k-mer at dna[0]
			if (i == 0) {
				for (int j = 0; j < k; j++) {
					dnaHashVal ^= Long.rotateLeft(bases.get(j).getValue(), k - j - 1);
					
				}
			}
			
			// calculate k-mer at dna[i]
			else {
				dnaHashVal = Long.rotateLeft(dnaHashVal, 1) ^ Long.rotateLeft(bases.get(i - 1).getValue(), k) ^ bases.get(i + k - 1).getValue();
			}
			
			if (result.containsKey(dnaHashVal)) {
				result.get(dnaHashVal).add(i);				
			} else {
				List<Integer> indices = new ArrayList<>();
				indices.add(i);
				result.put(dnaHashVal, indices);
			}
		}
		return result;
	}
	
	/**
	 * This method builds hash table using DNA hash values and its indices. If there are duplicate DNA hash values in the map, this method simply adds the corresponding index to the existing list. Otherwise, it inserts the DNA hash value into the map and a new list with the current index.
	 * @param start start index
	 * @param end end index
	 * @param k length of the k-mer
	 * @param map hash table with DNA hash values and its corresponding indices
	 */
	public void buildIndex(int start, int end, int k, Map<Long, List<Integer>> map) {
		long dnaHashVal = 0;
		// compare each k-mer in the entire sequence to the target k-mer
		for (int i = start; i < end; i++) {
			
			// initializing k-mer at dna[start]
			if (i == start) {
				for (int j = start; j < k + start; j++) {
					dnaHashVal ^= Long.rotateLeft(bases.get(j).getValue(), k + start - j - 1);					
				}
			}
			
			// calculate k-mer at DNA[i]
			else {
				dnaHashVal = Long.rotateLeft(dnaHashVal, 1) ^ Long.rotateLeft(bases.get(i - 1).getValue(), k) ^ bases.get(i + k - 1).getValue();
			}
			List<Integer> value =  map.computeIfAbsent(dnaHashVal, key -> { return Collections.synchronizedList(new ArrayList<>()); });
			value.add(i);
		}
	}
	
	/**
	 * This method uses multi-threading to build the hash table.
	 * @param k length of the k-mer
	 * @return
	 */
	public Map<Long, List<Integer>> buildIndexFast(int k) {
		Map<Long, List<Integer>> map = new ConcurrentHashMap<>();
		int cores = Runtime.getRuntime().availableProcessors();
		int totalSize = bases.size() - k + 1;
		int divSize = totalSize / cores;
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < cores; i++) { // 2
			final int startingPoint = i * divSize;
			final int endPoint = (i == cores - 1) ? totalSize : startingPoint + divSize;

			Thread thread = new Thread(() -> buildIndex(startingPoint, endPoint, k, map));
			threads.add(thread);
		}
		
		// start threads simultaneously
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
		return map;
		
	}
	
	/**
	 * This method utilizes stream to compare k-mer from the DNA sequence and the actual k-mer and find the indices that matches.
	 * @param map the hash table with DNA hash values and its corresponding list of indices
	 * @param kmer the sequence of the actual k-mer
	 * @return the list of indices that matches between k-mer from the DNA sequence and the actual k-mer 
	 */
	public List<Integer> findIndexFast(Map<Long, List<Integer>> map, DNA kmer) {
		int k = kmer.bases.size();
		long kmerHashVal = 0;
		
		// get hashVal
		for (int i = 0; i < k; i++) {
			kmerHashVal ^= Long.rotateLeft(kmer.bases.get(i).getValue(), k - i - 1);
		}
		
		if (!map.containsKey(kmerHashVal)) {
			return List.of();
		}
		
		// code below maximizes CPU usage to speed up the operation
		return map.get(kmerHashVal) // list of indices
			      .parallelStream() // break the list into 4 (in my case) and take that list automatically into multiple threads
			      .filter(index -> isSame(kmer, index)) // "filters" each item based on the condition
			      .collect(Collectors.toList());
		
	}
	
	/**
	 * 
	 * @return size of the base
	 */
	public int getSize() {
		return bases.size();
	}
	
	/**
	 * 
	 * @param map
	 * @param kmer
	 * @return
	 */
	public List<Integer> findIndex(Map<Long, List<Integer>> map, DNA kmer) {
		int k = kmer.bases.size();
		long kmerHashVal = 0;
		
		// get hashVal
		for (int i = 0; i < k; i++) {
			kmerHashVal ^= Long.rotateLeft(kmer.bases.get(i).getValue(), k - i - 1);
		}
		
		if (!map.containsKey(kmerHashVal)) {
			return List.of();
		}
		
		List<Integer> result = new ArrayList<>();
		for (int index : map.get(kmerHashVal)) {
			if (isSame(kmer, index)) {
				result.add(index);
			}
		}
		
		return result;
	}
	
	/**
	 * This class creates a thread that operates on sub-sequences of the DNA sequence.
	 * 
	 *
	 */
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
	 * This method utilizes multi-threading to find the matching indices of the DNA sequence to that of the actual k-mer.
	 * @param kmer the sequence of the k-mer
	 * @return the list with matching indices
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
		
		// start threads simultaneously
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
