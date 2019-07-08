package test;
import org.junit.Test;
import dna.DNA;

import static org.junit.Assert.assertEquals;

public class TestString {
	@Test
	public void toStringTest() {
		DNA str = new DNA("AGTC");
		String actual = str.toString();
		String expected = "AGTC";
		assertEquals(expected, actual);
		
	}
}
