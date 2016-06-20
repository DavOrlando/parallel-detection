package it.uniroma3.parallel.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.filter.PreHomepageFilter;
import it.uniroma3.parallel.utils.FetchManager;

/**
 * Classe che rappresenta un gruppo di prababili homepage parallele. Conosce
 * l'homepage dal quale sono stati selezionati i probabili URL delle altre
 * homepage. Inoltre è in grado di ritornare tutte le possibili coppie
 * (homepagePrimitiva,homepageNuova).
 * 
 * @author davideorlando
 *
 */
public class ParallelPages {

	private static final int DEFAULT_ENTRY_POINT_NUMBER = 5;

	private Page starterPage;
	private Map<URL, Page> candidateParallelHomepages;
	private List<PairOfPages> listOfPair;

	/**
	 * Costruttore di ParallelPages data una Page. Inserisce la pagina passata
	 * per parametro nella mappa di possibili pagine parallele candidate.
	 * 
	 * @param starterPage
	 * @throws LangDetectException
	 * @throws MalformedURLException
	 */
	public ParallelPages(Page starterPage) throws LangDetectException, MalformedURLException {
		this.candidateParallelHomepages = new HashMap<>();
		this.starterPage = starterPage;
		this.candidateParallelHomepages.put(starterPage.getUrlRedirect(), starterPage);
	}

	/**
	 * Crea e inizializza lo stato dell'oggetto ParallelPages in base alle
	 * informazioni che trova nella homepage passata per parametro. Popolando le
	 * coppie in questo modo (HPPrimitiva,CandidatoPossibile).
	 * 
	 * @param primaryHomepage
	 * @throws LangDetectException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public ParallelPages(Homepage primaryHomepage) throws LangDetectException, MalformedURLException {
		this((Page) primaryHomepage);
		this.findCandidateParallelHomepagesFromHomepage();
		this.organizeInPairsFromHomepage();
	}

	/**
	 * Costruttore per ParallelPages data una PreHomepage. Popola le coppie con
	 * i link che ci sono all'interno della prehomepage.
	 * 
	 * @param preHomepage
	 * @throws MalformedURLException
	 * @throws LangDetectException
	 */
	public ParallelPages(PreHomepage preHomepage) throws MalformedURLException, LangDetectException {
		this((Page) preHomepage);
		this.organizeInPairsFromPreHomepage();
	}

	private void organizeInPairsFromPreHomepage() throws LangDetectException {
		PreHomepage preHomepage = (PreHomepage) this.starterPage;
		this.listOfPair = new ArrayList<>();
		int i = 1;
		for (Page firstPage : preHomepage.getPossibleHomepages()) {
			for (Page secondPage : preHomepage.getPossibleHomepages()) {
				if (!firstPage.equals(secondPage) && firstPage.getURLString().compareTo(secondPage.getURLString()) >= 0
						&& !firstPage.getLanguage().equals(secondPage.getLanguage())) {
					this.listOfPair.add(new PairOfPages(firstPage, secondPage, i));
					i++;
				}
			}
		}
	}

	public void setHomepage(Homepage homepage) {
		this.starterPage = homepage;
	}

	public void setListOfPair(List<PairOfPages> listOfPair) {
		this.listOfPair = listOfPair;
	}

	/**
	 * Trova le pagine candidate ad essere parallele. Lo fa attraverso la
	 * ricerca di URL di pagine multilingua all'interno della homepage. Il primo
	 * elemento della mappa sarà l'homepage primitiva.
	 */
	public void findCandidateParallelHomepagesFromHomepage() {
		List<Page> multilingualPages;
		multilingualPages = ((Homepage) this.starterPage).getMultilingualPage();
		for (Page page : multilingualPages) {
			candidateParallelHomepages.put(page.getUrlRedirect(), page);
		}
	}

	public List<PairOfPages> getListOfPairs() {
		return this.listOfPair;
	}

	public Page getStarterPage() {
		return starterPage;
	}

	public List<Page> getCandidateParallelHomepages() {
		return new ArrayList<>(candidateParallelHomepages.values());
	}

	/***
	 * Crea una lista con tutte le coppie generate da
	 * (firstHomepage,possibleParallelHomepages[i-esima]). E' possibile accedere
	 * alla lista, dopo averla creata con questo metodo, grazie a getPair().
	 * 
	 * @return
	 */
	public void organizeInPairsFromHomepage() {
		List<PairOfPages> listOfPairs = new LinkedList<>();
		int i = 1;
		for (Page page : candidateParallelHomepages.values()) {
			if (!page.equals(starterPage)) {
				PairOfPages pair = new PairOfPages(this.starterPage, page, i);
				listOfPairs.add(pair);
				i++;
			}
		}
		this.listOfPair = listOfPairs;
	}

	/**
	 * Rimuove tutte le pagine non multilingua dalla collezione di pagine
	 * candidate ad essere multilingua lasciando solo quelle che possiedono un
	 * URL fra quelli passati per parametro.
	 * 
	 * @param urls
	 */
	public void lasciaSoloQuestiURL(Collection<URL> urls) {
		HashMap<URL, Page> parallelHomepages = new HashMap<>();
		for (URL url : urls)
			parallelHomepages.put(url, this.candidateParallelHomepages.get(url));
		this.candidateParallelHomepages = parallelHomepages;
	}

	/***
	 * Ritorna true se non ci sono entry points, false altrimenti.
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		// perchè la homepage c'è sempre
		return this.candidateParallelHomepages.size() == 1;
	}

	/**
	 * Ritorna la mappa che serve per far funzionare ancora tutto. PROVVISORIO
	 * TODO
	 * 
	 * @return
	 */
	public Map<String, String> getLocalPath2Url() {
		Map<String, String> localPath2Url = new HashMap<>();
		for (Page page : this.candidateParallelHomepages.values()) {
			localPath2Url.put(FetchManager.getInstance().findPageByURL(page.getUrlRedirect()),
					page.getUrlRedirect().toString());
		}
		return localPath2Url;
	}

	public List<String> getFileToVerify() {
		List<String> fileToVerify = new ArrayList<>();
		for (int i = 1; i <= this.candidateParallelHomepages.size(); i++) {
			fileToVerify.add(this.getStarterPage().getPageName() + i);
		}
		return fileToVerify;
	}

	/**
	 * Ritorna la collezione di URL composta dalla homepage primitiva e da URL
	 * provenienti dalle probabili homepage parallele.
	 * 
	 * @return
	 */
	public Set<URL> getParallelURLs() {
		HashSet<URL> parallelURLs = new HashSet<>();
		parallelURLs.addAll(candidateParallelHomepages.keySet());
		return parallelURLs;
	}

	/**
	 * Aggiunge un URL e la pagina alla mappa di pagine candidate. Utile per la
	 * prima euristica.
	 * 
	 * @param url
	 * @throws IOException
	 */
	public void addCandidateHomepage(URL url) throws IOException {
		this.candidateParallelHomepages.put(url, new Page(url));
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
		List<String> listOfNumberedURL = new LinkedList<String>();
		for (URL url : this.getParallelURLs()) {
			if (listOfNumberedURL.size() == numberMaxOfEntryPoints) {
				group.add(listOfNumberedURL);
				listOfNumberedURL = new LinkedList<String>();
			}
			listOfNumberedURL.add(url.toString());
		}
		if (listOfNumberedURL.size() != 0)
			group.add(listOfNumberedURL);
		return group;

	}

	/***
	 * Ritorna un insieme di coppie.
	 * 
	 * @return
	 */
	public Set<Set<String>> getPairOfStringEntryPoint() {
		Set<Set<String>> group = new HashSet<>();
		for (URL url : this.getParallelURLs()) {
			Set<String> pairOfURL = new HashSet<String>();
			pairOfURL.add(starterPage.toString());
			pairOfURL.add(url.toString());
			group.add(pairOfURL);
		}
		return group;
	}

}
