
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import dna.DNA;

public class TestDNA {
	@Test
	public void testDNA1() {
		DNA test = new DNA("ATCGCCCGGTCGGCGCGTCGCT");
		DNA kmer = new DNA("TCG");
		
		List<Integer> expected = List.of(1, 9, 17);
		List<Integer> actual = test.getIndex(kmer);
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void testDNA2() {
		DNA test = new DNA("TCT");
		DNA kmer = new DNA("TCG");
		
		List<Integer> expected = List.of();
		List<Integer> actual = test.getIndex(kmer);
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void testDNA3() {
		DNA test = new DNA("TCT");
		DNA kmer = new DNA("TCT");
		
		List<Integer> expected = List.of(0);
		List<Integer> actual = test.getIndex(kmer);
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void testDNA4() {
		DNA test = new DNA("AGTCGTG");
		DNA kmer = new DNA("GTG");
		
		List<Integer> expected = List.of(4);
		List<Integer> actual = test.getIndexFast(kmer);
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void testBuildIndex() {
		DNA test = new DNA("AGTCGTG");
		DNA kmer = new DNA("GTG");
		Map <Long, List<Integer>> result = test.buildIndex(kmer.getSize());
		List<Integer> actual = test.findIndex(result, kmer);
		List<Integer> expected = List.of(4);
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void testBuildIndex1() {
		DNA test = new DNA("AGTCGTG");
		DNA kmer = new DNA("GTAT");
		Map <Long, List<Integer>> result = test.buildIndex(kmer.getSize());
		List<Integer> actual = test.findIndex(result, kmer);
		List<Integer> expected = List.of();
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void testBuildIndex2() {
		DNA test = new DNA("AGCTGTGCT");
		DNA kmer = new DNA("GCT");
		Map <Long, List<Integer>> result = test.buildIndex(kmer.getSize());
		List<Integer> actual = test.findIndex(result, kmer);
		List<Integer> expected = List.of(1, 6);
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void testBuildIndex3() {
		DNA test = new DNA("AGCTGTGCTATAGAGATATATATAGGGCGCGATAGACA");
		DNA kmer = new DNA("GCT");
		Map <Long, List<Integer>> result = test.buildIndexFast(kmer.getSize());
		List<Integer> actual = test.findIndex(result, kmer);
		List<Integer> expected = List.of(1, 6);
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void testBuildIndex4() {
		DNA test = new DNA("AGCTGTGCTATAGAGATATATATAGGGCGCGATAGACA");
		DNA kmer = new DNA("GCTTAGACTATAGACGACAT");
		Map <Long, List<Integer>> result = test.buildIndexFast(kmer.getSize());
		List<Integer> actual = test.findIndex(result, kmer);
		List<Integer> expected = List.of();
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void testBuildIndex5() {
		DNA test = new DNA("GCTAGCTGGG");
		DNA kmer = new DNA("GCT");
		Map <Long, List<Integer>> result = test.buildIndex(kmer.getSize());
		List<Integer> actual = test.findIndex(result, kmer);
		List<Integer> expected = List.of(0, 4);
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void testFindIndexFast1() {
		DNA test = new DNA("AGCTGTGCTATGCT");
		DNA kmer = new DNA("GCT");
		Map <Long, List<Integer>> result = test.buildIndexFast(kmer.getSize());
		List<Integer> actual = test.findIndex(result, kmer);
		List<Integer> expected = List.of(1, 6, 11);
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void testFindIndexFast2() {
		DNA test = new DNA("GCTAGCTGGG");
		DNA kmer = new DNA("GCT");
		System.out.println("kmerSize: " + kmer.getSize());
		Map <Long, List<Integer>> result1 = test.buildIndex(kmer.getSize());
		Map <Long, List<Integer>> result2 = test.buildIndexFast(kmer.getSize());
		System.out.println("buildIndex: " + result1);
		System.out.println("buildIndexFast: " + result2);
		List<Integer> actual = test.findIndex(result2, kmer);
		List<Integer> expected = List.of(0, 4);
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void testgetIndexFile() {
		File file = new File("/Users/gerardlee/Desktop/file.txt");
		DNA test = new DNA(file);
		DNA kmer = new DNA("ATGT");
		List<Long> expected = List.of(0L);
		List<Long> actual = test.getIndexFile(kmer);
		
		assertEquals(expected, actual);
		
	}
	
	@Test
	public void biuldIndexFile() throws Exception {
		File file = new File("/Users/gerardlee/Desktop/file.txt");
		DNA test = new DNA(file);
		test.clearTable(3);
		test.buildIndexFile(3);
	}
	
	@Test
	public void biuldIndexFile2() throws Exception {	
		File file = new File("/Users/gerardlee/Desktop/file.txt");
		DNA test = new DNA(file);
		test.clearTable(3);
		test.buildIndexFile(3);
		test.viewDB(3);
		DNA test2 = new DNA("ATGTCGATATAGCGCATGC");
		System.out.println(test2.buildIndex(3));
	}	
	
	@Test
	public void biuldIndexFile3() throws Exception {	
		File file = new File("/Users/gerardlee/Desktop/file.txt");
		DNA test = new DNA(file);
		test.clearTable(3);
		test.buildIndexFile(3);
		DNA kmer = new DNA("GTC");

		List<Long> expected = List.of(2L);
		List<Long> actual = test.getIndexDB(kmer);
		
		assertEquals(expected, actual);
	}
}
