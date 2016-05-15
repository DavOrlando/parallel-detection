package it.model;

import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ParallelPages {

	private List<URL> parallelPageUrls;

	/**
	 * Crea un oggetto ParallelPages che mi rappresenta un gruppo di pagine
	 * parallele e multilingua.
	 */
	public ParallelPages() {
		this.parallelPageUrls = new LinkedList<URL>();
	}

	public void addURL(URL url) {
		this.parallelPageUrls.add(url);
	}

	public List<URL> getParallelPageUrls() {
		return this.parallelPageUrls;
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

	/*
	 * TODO non si è riuscito a capire se quando aggiungeva due volte la stessa
	 * lista lo faceva apposta o non. Per adesso non lo mettiamo, non ha senso.
	 */
	public Set<List<String>> getGroupOfEntryPoints(int numberMaxOfEntryPoints) {
		Set<List<String>> group = new HashSet<>();
		List<String> listOfFiveURL = new LinkedList<String>();
		for (URL url : this.parallelPageUrls) {
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
