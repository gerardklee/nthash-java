package test;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.util.List;
import org.junit.Test;
import sequence.DiskSequence;
import sequence.Sequence;

public class TestDiskSequence {
	@Test
	public void biuldIndexFile3() throws Exception {	
		File file = new File("/Users/gerardlee/Desktop/file.txt");
		DiskSequence test = new DiskSequence(file);
		test.clearTable(3);
		test.buildIndex(3);
		Sequence kmer = DiskSequence.fromString("ATG");
		List<Long> expected = List.of(0L, 16L);
		List<Long> actual = test.getIndexDB(kmer);
		test.viewDB(3);
		assertEquals(expected, actual);
	}
}
