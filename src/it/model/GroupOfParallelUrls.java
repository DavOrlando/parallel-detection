package it.model;

import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GroupOfParallelUrls {

	private static final int DEFAULT_ENTRY_POINT_NUMBER = 5;
	private List<URL> parallelUrls;
	private URL homepageURL;

	/**
	 * Crea un oggetto ParallelUrls che mi rappresenta un gruppo di URL
	 * di pagine parallele e multilingua.
	 */
	public GroupOfParallelUrls() {
		this.parallelUrls = new LinkedList<URL>();
	}

	public void addURL(URL url) {
		this.parallelUrls.add(url);
	}

	public List<URL> getParallelUrls() {
		return this.parallelUrls;
	}
	
	public URL getHomepageURL() {
		return homepageURL;
	}

	public void setHomepageURL(URL homepageURL) {
		this.homepageURL = homepageURL;
	}

	/***
	 * Ritorna un insieme di liste, in cui ciascuna ha al massimo un numero di
	 * entrypoints.
	 * 
	 * @param numberMaxOfEntryPoints
	 *            il numero di entry points massimi che vogliamo in ciascuna
	 *            lista.
	 * @return
	 */
	
	public Set<List<String>> getGroupOfEntryPoints() {
		return this.getGroupOfEntryPoints(DEFAULT_ENTRY_POINT_NUMBER);
	}
	/*
	 * TODO non si è riuscito a capire se quando aggiungeva due volte la stessa
	 * lista lo faceva apposta o non. Per adesso non lo mettiamo, non ha senso.
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
