package it.uniroma3.parallel.model;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Classe che rappresenta una Prehomepage. Ovvero una pagina in cui si sceglie
 * la lingua per poi entrare nella homepage vera e propria.
 * 
 * @author davideorlando
 *
 */
public class PreHomepage extends Page {

	private List<Page> possibleHomepages;

	public PreHomepage(String homepageStringUrl) throws IOException {
		super(homepageStringUrl);
		this.possibleHomepages = new LinkedList<>();
	}

	public List<Page> getPossibleHomepages() {
		return possibleHomepages;
	}

	public void setPossibleHomepages(List<Page> possibleHomepages) {
		this.possibleHomepages = possibleHomepages;
	}

}
