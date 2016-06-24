package it.uniroma3.parallel.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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

	private Page starterPage;
	private Map<URI, Page> candidateParallelHomepages;
	private List<PairOfPages> listOfPairs;

	/**
	 * Costruttore di ParallelPages data una Page. Inserisce la pagina passata
	 * per parametro nella mappa di possibili pagine parallele candidate.
	 * 
	 * @param starterPage
	 * @throws LangDetectException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public ParallelPages(Page starterPage) throws LangDetectException, MalformedURLException, URISyntaxException {
		this.candidateParallelHomepages = new HashMap<>();
		this.starterPage = starterPage;
		this.candidateParallelHomepages.put(starterPage.getUrlRedirect().toURI(), starterPage);
	}

	/**
	 * Crea e inizializza lo stato dell'oggetto ParallelPages in base alle
	 * informazioni che trova nella homepage passata per parametro. Popolando le
	 * coppie in questo modo (HPPrimitiva,CandidatoPossibile).
	 * 
	 * @param primaryHomepage
	 * @throws LangDetectException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public ParallelPages(Homepage primaryHomepage)
			throws LangDetectException, MalformedURLException, URISyntaxException {
		this((Page) primaryHomepage);
		this.organizeInPairsFromHomepage();
	}

	/**
	 * Costruttore per ParallelPages data una PreHomepage. Popola le coppie con
	 * i link che ci sono all'interno della prehomepage.
	 * 
	 * @param preHomepage
	 * @throws MalformedURLException
	 * @throws LangDetectException
	 * @throws URISyntaxException
	 */
	public ParallelPages(PreHomepage preHomepage)
			throws MalformedURLException, LangDetectException, URISyntaxException {
		this((Page) preHomepage);
		this.organizeInPairsFromPreHomepage();
	}

	private void organizeInPairsFromPreHomepage() throws LangDetectException {
		PreHomepage preHomepage = (PreHomepage) this.starterPage;
		this.listOfPairs = new ArrayList<>();
		int i = 1;
		for (Page firstPage : preHomepage.getPossibleHomepages()) {
			for (Page secondPage : preHomepage.getPossibleHomepages()) {
				if (firstPage.getURLString().compareTo(secondPage.getURLString()) >= 0 && !firstPage.equals(secondPage)
						&& !firstPage.getLanguage().equals(secondPage.getLanguage())) {
					this.listOfPairs.add(new PairOfPages(firstPage, secondPage, i));
					i++;
				}
			}
		}
	}

	public void setHomepage(Homepage homepage) {
		this.starterPage = homepage;
	}

	public void setListOfPair(List<PairOfPages> listOfPair) {
		this.listOfPairs = listOfPair;
	}

	public List<PairOfPages> getListOfPairs() {
		return this.listOfPairs;
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
		this.listOfPairs = new LinkedList<>();
		int i = 1;
		for (Page page : candidateParallelHomepages.values()) {
			if (!page.equals(starterPage)) {
				PairOfPages pair = new PairOfPages(this.starterPage, page, i);
				listOfPairs.add(pair);
				i++;
			}
		}
	}

	/**
	 * Rimuove tutte le pagine non multilingua dalla collezione di pagine
	 * candidate ad essere multilingua lasciando solo quelle che possiedono un
	 * URL fra quelli passati per parametro.
	 * 
	 * @param urls
	 * @throws URISyntaxException
	 */
	public void lasciaSoloQuestiURL(Collection<URL> urls) throws URISyntaxException {
		Map<URI, Page> parallelHomepages = new HashMap<>();
		for (URL url : urls)
			parallelHomepages.put(url.toURI(), this.candidateParallelHomepages.get(url));
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
	 * Ritorna la collezione di URL composta dalla homepage primitiva e da URL
	 * provenienti dalle probabili homepage parallele.
	 * 
	 * @return
	 */
	public Set<URI> getParallelURLs() {
		HashSet<URI> parallelURLs = new HashSet<>();
		parallelURLs.addAll(candidateParallelHomepages.keySet());
		return parallelURLs;
	}

	/**
	 * Aggiunge un URL e la pagina alla mappa di pagine candidate. Utile per la
	 * prima euristica.
	 * 
	 * @param url
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void addCandidateHomepage(URL url) throws IOException, URISyntaxException {
		this.candidateParallelHomepages.put(url.toURI(), new Page(url));
	}




	/**
	 * Aggiunge alla mappa una nuova pagina.
	 * 
	 * @param page
	 * @throws URISyntaxException
	 */
	public void addCandidateParallelHomepage(Page page) throws URISyntaxException {
		this.candidateParallelHomepages.put(page.getUrl().toURI(), page);
	}

}
