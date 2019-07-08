package test;
import static org.junit.Assert.assertEquals;

import java.util.List;

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
}
