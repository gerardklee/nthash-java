package dna;

import java.util.ArrayList;
import java.util.List;

public class DNA {
	private List<Base> bases;
	
	public DNA(String dnaString) {

		bases = new ArrayList<>();
		for(int i = 0; i < dnaString.length(); i++) {
			char dnaCharacter = dnaString.charAt(i);
			String dnaCharString = String.valueOf(dnaCharacter);
			Base base = Base.valueOf(dnaCharString);
			bases.add(base);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Base base: bases) {
			builder.append(base.toString());
		}
		return builder.toString();
	}
}
