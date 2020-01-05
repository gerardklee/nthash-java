package sequence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiskSequence implements Sequence {
	private File file;
	private RandomAccessFile randomFile;
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/test";  
	Connection conn;
	
	public DiskSequence(File file) throws FileNotFoundException, SQLException, ClassNotFoundException {
        Class.forName(JDBC_DRIVER); 
		this.file = file; 	
		this.randomFile = new RandomAccessFile(this.file, "r");
		this.conn = DriverManager.getConnection(DB_URL);
	}

	
	@Override
	public String toString() {
		return "DiskSequence of " + this.file.getAbsolutePath();
	}
	
	@Override
	public void buildIndex(long k) throws Exception{
        String tableName = tableName(k);
 
        // STEP 2: Open a connection 

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
		long dnaStart = k + 1;
		long fileStart = k + 1;
		while((character = buffer.read()) != -1) {
			if (!isGood((char) character)) {
				continue;
			}
			char temp = dnaArray[ptr];
			dnaArray[ptr] = (char) character;
			dnaHashVal = Long.rotateLeft(dnaHashVal, 1) ^ Long.rotateLeft(getValue(temp), (int) k) ^ getValue(dnaArray[ptr]);
			Statement stmt3 = conn.createStatement();
			String sql3 = "INSERT INTO " + tableName + " VALUES(" + (dnaStart - k) + "," + (fileStart - k) + "," + dnaHashVal + ");";
			stmt3.executeUpdate(sql3);
			stmt3.close();
			dnaStart++;
			fileStart++;
			ptr = (ptr + 1) % dnaArray.length;
		}
		buffer.close();
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
	private List<Long> getIndexDB(Sequence kmer) throws Exception {
		List<Long> output = new ArrayList<>();
		long kmerHashVal = 0;
		for (int i = 0; i < kmer.getSize(); i++) { 
			kmerHashVal ^= Long.rotateLeft(kmer.getBase(i).getValue(), (int) (kmer.getSize() - i - 1));
		}
		char[] kmerArray = new char[(int) kmer.getSize()];
		for (int i = 0; i < kmer.getSize(); i++) {
			kmerArray[i] = kmer.getBase(i).toString().charAt(0);
		}
        
        // STEP 2: Open a connection  
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
        return output;      
	}
	
	private boolean canTakeFastPath(long k) throws SQLException {
		String tableName = tableName(k);
        Statement stmt = conn.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(dna_start BIGINT NOT NULL, file_start BIGINT NOT NULL, hash BIGINT NOT NULL, PRIMARY KEY(dna_start));"; 
        stmt.executeUpdate(sql);
        stmt.close();
        
        Statement stmt1 = conn.createStatement();
        String sql1 = "SELECT COUNT(*) FROM " + tableName; 
            
        ResultSet result = stmt1.executeQuery(sql1);
        result.next();
        long a = result.getLong(1);
        stmt1.close();
        return a != 0;
	}
	
	/**
	 * Generates a table name that represents the length of the k-mer.
	 * @param l length of the sequence
	 * @return table name
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
	 * Clears table corresponding to the length of the k-mer.
	 * @param k length of the k-mer
	 * @throws Exception when there is no such table exists
	 */
	public void clearTable(int k) throws Exception {
      
        // STEP 2: Open a connection 
        Statement stmt = conn.createStatement();
        String sql = "DROP TABLE IF EXISTS " + tableName(k);
        stmt.executeUpdate(sql);
        stmt.close();
	}
	
	@Override
	public long getSize() {
		return file.length();
	}
	
	public void close() throws Exception {
		this.randomFile.close();
		this.conn.close();
	}
	
	@Override
	public Base getBase(long position) {	
		try {
			randomFile.seek(position);
			char c = (char)randomFile.read();
			String s = String.valueOf(c);
			Base b = Base.valueOf(s);
			return b;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public List<Long> getMatchingPositions(Sequence kmer) throws Exception{
		// TODO Auto-generated method stub
		if (canTakeFastPath(kmer.getSize())) {
			return getIndexDB(kmer);
		} else {
			return findPositionSlow(kmer);
		}
	}
	
	private List<Long> findPositionSlow(Sequence kmer) {
		List<Long> indices = new ArrayList<>();
		for (long i = 0; i < this.getSize() - kmer.getSize() + 1; i++) {
			if (isSame(kmer, i)) {
				indices.add(i);
			}
		}
		return indices;
	}
	
	private boolean isSame(Sequence kmer, long index) {
		for (long i = 0; i < kmer.getSize(); i++) {
			if (kmer.getBase(i) != this.getBase(index + i)) {
				return false;
			}
		}
		return true;
	}
	
}
