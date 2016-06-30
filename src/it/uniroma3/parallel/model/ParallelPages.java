package it.uniroma3.parallel.model;

import java.io.IOException;
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
import org.apache.log4j.Logger;

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

	private static final Logger logger = Logger.getLogger(ParallelPages.class);
	private Page starterPage;
	private Map<URI, Page> candidateParallelHomepages;
	private List<PairOfPages> listOfPairs;

	/**
	 * Costruttore di ParallelPages data una Page. Inserisce la pagina passata
	 * per parametro nella mappa di possibili pagine parallele candidate.
	 * 
	 * @param starterPage
	 * @throws URISyntaxException
	 */
	public ParallelPages(Page starterPage) throws URISyntaxException  {
		this.candidateParallelHomepages = new HashMap<>();
		this.starterPage = starterPage;
		this.candidateParallelHomepages.put(starterPage.getUrlRedirect().toURI(), starterPage);
		this.listOfPairs = new LinkedList<>();
	}

	/**
	 * Ritorna la pagina da cui è stata creata questa struttura dati.
	 * 
	 * @return
	 */
	public Page getStarterPage() {
		return starterPage;
	}

	/**
	 * Ritorna la lista di pagine candidate ad essere homepage parallele
	 * multilingua.
	 * 
	 * @return
	 */
	public List<Page> getCandidateParallelHomepages() {
		return new ArrayList<>(candidateParallelHomepages.values());
	}

	public List<PairOfPages> getListOfPairs() {
		return new LinkedList<>(listOfPairs);
	}

	public void setListOfPair(List<PairOfPages> listOfPair) {
		this.listOfPairs = listOfPair;
	}

	/**
	 * Ritorna la collezione di URI composta dalla homepage primitiva e da URL
	 * provenienti dalle probabili homepage parallele.
	 * 
	 * @return
	 */
	public Set<URI> getParallelURIs() {
		return new HashSet<>(candidateParallelHomepages.keySet());
	}

	/***
	 * Ritorna true se non ci sono entry points, false altrimenti.
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		// perchè la homepage c'è sempre
		return this.candidateParallelHomepages.size() <= 1;
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
			parallelHomepages.put(url.toURI(), this.candidateParallelHomepages.get(url.toURI()));
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
	public void addCandidateParallelHomepage(Page page){
		try {
			this.candidateParallelHomepages.put(page.getUrl().toURI(), page);
		} catch (URISyntaxException e) {
			logger.error(e);
		}
	}

}
