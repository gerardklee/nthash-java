package dna;

public class Main {

	public static void main(String[] args) {
//		DNA generateSeq = new DNA(10000000);
//
//		//warm up getIndex
//		for (int i = 0; i < 20000; i++) {
//			DNA random = new DNA(100);
//			random.buildIndex(4);
//		}
//		
//		// getIndexaverage
//		double cnt = 0;
//		for (int i = 0; i < 2; i++) {
//			long start = System.nanoTime();
//			generateSeq.buildIndex(10000);
//			long elapse = System.nanoTime() - start;
//			cnt += elapse;
//		}
//		double avg = cnt / 2 * 1e-9;
//		System.out.println("buildIndex: " + avg);
//		
//		//warm up buildIndex
//		for (int i = 0; i < 20000; i++) {
//			DNA random = new DNA(100);
//			random.buildIndexFast(4);
//		}
//		
//		// getIndexaverage
//		double cnt2 = 0;
//		for (int i = 0; i < 2; i++) {
//			long start = System.nanoTime();
//			generateSeq.buildIndexFast(10000);
//			long elapse = System.nanoTime() - start;
//			cnt2 += elapse;
//		}
//		double avg2 = cnt2 / 2 * 1e-9;
//		System.out.println("buildIndexFast: " + avg2);
		
		//DNA mine = new DNA();
		DNA mine = new DNA();
		System.out.println(mine);
	}
}
