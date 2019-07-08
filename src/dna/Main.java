package dna;

import java.util.List;

public class Main {

	public static void main(String[] args) {
		DNA str = new DNA("AGTC");
		System.out.println(str);
		
		DNA randomStr = new DNA(1000);
		System.out.println(randomStr);
		DNA randomkmer = new DNA(4);
		System.out.println(randomkmer);
		List<Integer> result = randomStr.getIndexFast(randomkmer);
		System.out.println(result);		
	}
	

}
