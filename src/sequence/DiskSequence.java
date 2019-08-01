package sequence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiskSequence implements Sequence {
	private File file;
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/test";  
	Connection conn;
	private List<Base> bases;
	
	public DiskSequence(File file) {
		this.file = file; 	
	}
	
	private DiskSequence(List<Base> bases) {
		this.bases = bases;
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
	
	public static DiskSequence fromString(String sequence) {
		List<Base> bases = new ArrayList<>();
		for (int i = 0; i < sequence.length(); i++) {
			// convert char character in string to base
			// then insert base into bases (List<Base>)
			char dnaCharacter = sequence.charAt(i);
			String dnaChartoString = String.valueOf(dnaCharacter);
			Base base = Base.valueOf(dnaChartoString);
			bases.add(base);
		}
		return new DiskSequence(bases);
	}

	
	@Override
	public void buildIndex(long k) throws Exception{
		// STEP 1: Register JDBC driver 
        Class.forName(JDBC_DRIVER); 
        String tableName = tableName(k);
 
        // STEP 2: Open a connection 
        conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(dna_start BIGINT NOT NULL, file_start BIGINT NOT NULL, hash BIGINT NOT NULL, PRIMARY KEY(dna_start));"; 
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
		char[] dnaArray = new char[(int) k];
		long dnaHashVal = 0;
		
		// insert initial hash value into the database
		for (int i = 0; i < k; i++) {
			character = (char) buffer.read();
			dnaArray[i] = (char) character;
			dnaHashVal ^= Long.rotateLeft(getValue((char) dnaArray[i]), (int) (k - i - 1));
		}
		Statement stmt2 = conn.createStatement();
		String sql2 = "INSERT INTO " + tableName + " VALUES("+ 0 + "," + 0 + "," + dnaHashVal + ");";
		stmt2.executeUpdate(sql2);
		stmt2.close();

		// now, insert next corresponding hash values to the db starting from index 1
		int ptr = 0;
		long dnaStart = k + 1; // set 4 because initial hashval is already in db at index 0
		long fileStart = k + 1;
		long skip = 0;
		while((character = buffer.read()) != -1) {
			if (!isGood((char) character)) {
				skip++;
				continue;
			}
			char temp = dnaArray[ptr];
			dnaArray[ptr] = (char) character;
			dnaHashVal = Long.rotateLeft(dnaHashVal, 1) ^ Long.rotateLeft(getValue(temp), (int) k) ^ getValue(dnaArray[ptr]);
			Statement stmt3 = conn.createStatement();
			String sql3 = "INSERT INTO " + tableName + " VALUES(" + (dnaStart - k) + "," + (fileStart - k) + "," + dnaHashVal + ");";
			System.out.println("sql3: " + sql3);
			stmt3.executeUpdate(sql3);
			stmt3.close();
			dnaStart++;
			fileStart++;
			ptr = (ptr + 1) % dnaArray.length;
		}
		buffer.close();
		conn.close();
	}
	
	private boolean isGood(char character) {
		if (character != 'A' && character != 'T' && character != 'G' && character != 'C' ) {
			return false;
		}
		return true;
	}
	
	/**
	 * Searches for the starting position of the sub-sequence that matches with k-mer.
	 * @param kmer The sequence of the k-mer
	 * @return the list with matching positions
	 * @throws Exception
	 */
	public List<Long> getIndexDB(Sequence kmer) throws Exception {
		RandomAccessFile randomFile = new RandomAccessFile(file, "r");
		List<Long> output = new ArrayList<>();
		long kmerHashVal = 0;
		for (int i = 0; i < kmer.getSize(); i++) { 
			kmerHashVal ^= Long.rotateLeft(kmer.getBase(i).getValue(), (int) (kmer.getSize() - i - 1));
		}
		char[] kmerArray = kmer.toString().toCharArray();
		// STEP 1: Register JDBC driver 
        Class.forName(JDBC_DRIVER); 
        
        // STEP 2: Open a connection  
        conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        String sql = "SELECT dna_start, file_start FROM " + tableName(kmer.getSize()) + " WHERE hash = " + kmerHashVal + ";";
        ResultSet result = stmt.executeQuery(sql); // obtains the sub-table from the query above.
        while (result.next()) {
        	char[] dnaArray = new char[(int) kmer.getSize()];
        	long dnaStart = result.getLong("dna_start");
        	long fileStart = result.getLong("file_start");
        	randomFile.seek(fileStart);
        	int i = 0;
        	while (i < kmer.getSize()) {
        		char character = (char) randomFile.read();
        		System.out.println("character: " + character);
        		if (!isGood(character)) {
        			continue;
        		}
        		dnaArray[i] = character;
        		i++;
        	}
        	if (Arrays.equals(kmerArray, dnaArray)) {
        		output.add(dnaStart);
        	}
        }
        result.close();
        stmt.close();
        conn.close();
        randomFile.close();
        return output;      
	}
	
	/**
	 * Generates a table name that represents the length of the k-mer.
	 * @param l length of the sequence
	 * @return
	 */
	private String tableName(long l) {
		return "kmer" + l;
	}
	
	/**
	 * Sets the corresponding magic number to each nucleotide.
	 * @param base each nucleotide A, G, T, C
	 * @return magic value of the nucleotide
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
	
	
	/**
	 * Clears the database.
	 * @throws Exception
	 */
	public void clearTable(int k) throws Exception {
		// STEP 1: Register JDBC driver 
        Class.forName(JDBC_DRIVER); 
        
        // STEP 2: Open a connection 
        conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        String sql = "DROP TABLE IF EXISTS " + tableName(k);
        stmt.executeUpdate(sql);
        stmt.close();
        conn.close();
	}
	
	@Override
	public long getSize() {
		return bases.size();
	}
	
	@Override
	public Base getBase(long position) {		
		return bases.get((int) position) ;
	}

	@Override
	public List<Long> getMatchingPositions(Sequence kmer) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
