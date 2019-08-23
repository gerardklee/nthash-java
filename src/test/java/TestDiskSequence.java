

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.util.List;
import org.junit.Test;
import sequence.DiskSequence;
import sequence.MemorySequence;
import sequence.Sequence;

public class TestDiskSequence {
	@Test
	public void testSimpleIndexed() throws Exception {	
		File file = new File("src/test/resources/short_sequence_1.txt");
		DiskSequence test = new DiskSequence(file);
		test.clearTable(4);
		test.buildIndex(4);
		Sequence kmer = MemorySequence.fromString("ATGG");
		List<Long> expected = List.of(0L, 5L);
		List<Long> actual = test.getMatchingPositions(kmer);
		assertEquals(expected, actual);
	}
	
	@Test
	public void testSimpleNotIndexed() throws Exception {	
		File file = new File("src/test/resources/short_sequence_1.txt");
		DiskSequence test = new DiskSequence(file);
		test.clearTable(4);
		Sequence kmer = MemorySequence.fromString("ATGG");
		List<Long> expected = List.of(0L, 5L);
		List<Long> actual = test.getMatchingPositions(kmer);
		assertEquals(expected, actual);
	}
}
