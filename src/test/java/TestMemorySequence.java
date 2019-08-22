

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import dna.DNA;
import sequence.MemorySequence;
import sequence.Sequence;

public class TestMemorySequence {
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
	public void testBuildIndex3() throws Exception {
		Sequence test = MemorySequence.fromString("AGCTGTGCTATAGAGATATATATAGGGCGCGATAGACA");
		Sequence kmer = MemorySequence.fromString("GCT");
		test.buildIndex(kmer.getSize());
		List<Long> actual = test.getMatchingPositions(kmer);
		List<Long> expected = List.of(1L, 6L);
		
		assertEquals(expected, actual);		
	}
}
