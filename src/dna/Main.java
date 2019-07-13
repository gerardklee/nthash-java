package dna;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

	public static void main(String[] args) {
//		//warm up getIndex
//		for (int i = 0; i < 10000; i++) {
//			DNA randomStr2 = new DNA(100);
//			DNA kmerrandom = new DNA(100);
//			randomStr2.getIndex(kmerrandom);
//		}
//		DNA generateSeq = new DNA(10000000);
//		DNA generatekmer = new DNA(10000);
//		
//		// getIndexaverage
//		double cnt = 0;
//		for (int i = 0; i < 100; i++) {
//			long start = System.nanoTime();
//			generateSeq.getIndex(generatekmer);
//			long elapse = System.nanoTime() - start;
//			cnt += elapse;
//		}
//		double avg = cnt / 100 * 1e-6;
//		System.out.println("single core: " + avg);
//		
//		// warm up getIndexFast
//		for (int i = 0; i < 10000; i++) {
//			DNA randomStr2 = new DNA(100);
//			DNA kmerrandom = new DNA(100);
//			randomStr2.getIndexFast(kmerrandom);
//		}
//		DNA generateSeq1 = new DNA(10000000);
//		DNA generatekmer1 = new DNA(10000);
//		
//		// getIndexFast average
//		double cnt1 = 0;
//		for (int i = 0; i < 100; i++) {
//			long start = System.nanoTime();
//			generateSeq1.getIndexFast(generatekmer1);
//			long elapse = System.nanoTime() - start;
//			cnt1 += elapse;
//		}
//		double avg1 = cnt1 / 100 / 1e6;
//		System.out.println("double core: " + avg1);
//		
		
		// *** TEST FOR ROLLINGHASH BEGINS HERE ***
		
//		DNA seq = new DNA("AGTCAATTTAGATACAATATTTTAGAGAGAGATATAACAA");
//		DNA kmer = new DNA("CAA");
//		System.out.println(seq.getIndexHash(kmer));	
//		System.out.println(seq.getIndex(kmer));
//		
//		//warm up getIndex
//		for (int i = 0; i < 10000; i++) {
//			DNA randomStr2 = new DNA(100);
//			DNA kmerrandom = new DNA(100);
//			randomStr2.getIndex(kmerrandom);
//		}
//		DNA generateSeq = new DNA(10000000);
//		DNA generatekmer = new DNA(10000);
//		
//		// getIndexaverage
//		double cnt = 0;
//		for (int i = 0; i < 100; i++) {
//			long start = System.nanoTime();
//			generateSeq.getIndex(generatekmer);
//			long elapse = System.nanoTime() - start;
//			cnt += elapse;
//		}
//		double avg = cnt / 100 * 1e-6;
//		System.out.println("brute force: " + avg);
//		
//		//warm up getIndexHash
//		for (int i = 0; i < 10000; i++) {
//			DNA randomStr2 = new DNA(100);
//			DNA kmerrandom = new DNA(100);
//			randomStr2.getIndexHash(kmerrandom);
//		}
//		DNA generateSeq2 = new DNA(10000000);
//		DNA generatekmer2 = new DNA(10000);
//		
//		// getIndexaverage
//		double cnt2 = 0;
//		for (int i = 0; i < 100; i++) {
//			long start = System.nanoTime();
//			generateSeq2.getIndexHash(generatekmer2);
//			long elapse = System.nanoTime() - start;
//			cnt2 += elapse;
//		}
//		double avg2 = cnt2 / 100 * 1e-6;
//		System.out.println("hash: " + avg2);
		
		// *** TEST END HERE ***

		List<DNA> kmers = new ArrayList<>();
		DNA generateSeq = new DNA(10000000);
		
		for (int i = 0; i < 100; i++) {
			DNA kmerrandom = new DNA(100);
			kmers.add(kmerrandom);
		}
		
		
		Map<Long, List<Integer>> result = generateSeq.buildIndex(100);
		
		
		//warm up getMap
		for (int i = 0; i < 20000; i++) {
			DNA kmerrandom = new DNA(100);
			generateSeq.findIndex(result, kmerrandom);
		}
		
		// getIndexMap
		double cnt1 = 0;
		for (int i = 0; i < 100; i++) {
			long start = System.nanoTime();
			generateSeq.findIndex(result, kmers.get(i));
			long elapse = System.nanoTime() - start;
			cnt1 += elapse;
		}
		double avg1 = cnt1 / 100 * 1e-6;
		System.out.println("map find: " + avg1);
		
		//warm up getIndex
		for (int i = 0; i < 20000; i++) {
			DNA random = new DNA(100);
			DNA kmerrandom = new DNA(100);
			random.getIndex(kmerrandom);
		}
		
		// getIndexaverage
		double cnt = 0;
		for (int i = 0; i < 100; i++) {
			long start = System.nanoTime();
			generateSeq.getIndex(kmers.get(i));
			long elapse = System.nanoTime() - start;
			cnt += elapse;
		}
		double avg = cnt / 100 * 1e-6;
		System.out.println("brute force: " + avg);
		
	}
}
