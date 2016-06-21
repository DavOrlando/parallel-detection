package it.uniroma3.parallel.filter;

import java.util.Set;

import it.uniroma3.parallel.utils.FetchManager;

public class LinkValueFilter {

	public Set<String> getSetForFilter() {
		return FetchManager.getInstance().makeSetOfAllMultilingualProperties();
	}

	public boolean filter(String text) {
		return this.getSetForFilter().contains(text);
	}

}
