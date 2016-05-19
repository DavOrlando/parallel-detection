package it.uniroma3.parallel.model;

import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Classe che rappresenta un gruppo di URL paralleli provenienti da un homepage
 * di un sito. Necessaria per procurare l'input al crawler.
 * 
 * @author davideorlando
 *
 */
public class GroupOfParallelUrls {

	private static final int DEFAULT_ENTRY_POINT_NUMBER = 5;
	private Set<URL> parallelUrls;
	private URL homepageURL;

	/**
	 * Crea un oggetto ParallelUrls che mi rappresenta un gruppo di URL di
	 * pagine parallele e multilingua.
	 */
	public GroupOfParallelUrls() {
		this.parallelUrls = new HashSet<URL>();
	}

	/***
	 * Aggiungo l'URL alla collezione di URL paralleli.
	 * 
	 * @param url
	 */
	public void addURL(URL url) {
		this.parallelUrls.add(url);
	}

	public Set<URL> getParallelUrls() {
		return this.parallelUrls;
	}

	public URL getHomepageURL() {
		return homepageURL;
	}

	public void setHomepageURL(URL homepageURL) {
		this.homepageURL = homepageURL;
	}

	/***
	 * Ritorna un insieme di liste in cui ciascuna ha al massimo un numero di
	 * URL paralleli.
	 * 
	 * @return
	 */
	public Set<List<String>> getGroupOfEntryPoints() {
		return this.getGroupOfEntryPoints(DEFAULT_ENTRY_POINT_NUMBER);
	}

	/***
	 * Ritorna un insieme di liste in cui ciascuna ha al massimo un numero di
	 * URL paralleli.
	 * 
	 * @param numberMaxOfEntryPoints
	 *            il numero di entry points massimi che vogliamo in ciascuna
	 *            lista.
	 * @return
	 */
	public Set<List<String>> getGroupOfEntryPoints(int numberMaxOfEntryPoints) {
		Set<List<String>> group = new HashSet<>();
		List<String> listOfFiveURL = new LinkedList<String>();
		for (URL url : this.parallelUrls) {
			if (listOfFiveURL.size() == numberMaxOfEntryPoints) {
				group.add(listOfFiveURL);
				listOfFiveURL = new LinkedList<String>();
			}
			listOfFiveURL.add(url.toString());
		}
		if (listOfFiveURL.size() != 0)
			group.add(listOfFiveURL);
		return group;

	}

}
