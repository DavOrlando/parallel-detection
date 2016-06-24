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
	
	public Page getStarterPage() {
		return starterPage;
	}

	public List<Page> getCandidateParallelHomepages() {
		return new ArrayList<>(candidateParallelHomepages.values());
	}

	public List<PairOfPages> getListOfPairs() {
		return new ArrayList<>(listOfPairs);
	}

	public void setListOfPair(List<PairOfPages> listOfPair) {
		this.listOfPairs = listOfPair;
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

	/**
	 * Aggiunge un URL e la pagina alla mappa di pagine candidate. Utile per la
	 * prima euristica.
	 * 
	 * @param url
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void addCandidateParallelHomepage(URL url) throws IOException, URISyntaxException {
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
