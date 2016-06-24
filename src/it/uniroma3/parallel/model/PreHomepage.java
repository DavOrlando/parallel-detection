package it.uniroma3.parallel.model;

import java.io.IOException;
import java.util.List;

public class PreHomepage extends Page {

	private List<Page> possibleHomepages;

	public PreHomepage(String homepageStringUrl) throws IOException {
		super(homepageStringUrl);
	}

	public List<Page> getPossibleHomepages() {
		return possibleHomepages;
	}

	public void setPossibleHomepages(List<Page> possibleHomepages) {
		this.possibleHomepages = possibleHomepages;
	}


}
