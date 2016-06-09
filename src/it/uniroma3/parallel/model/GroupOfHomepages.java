package it.uniroma3.parallel.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.cybozu.labs.langdetect.LangDetectException;

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
public class GroupOfHomepages {

	private Homepage homepage;
	private Map<URL, Page> candidateParallelHomepages;
	private List<PairOfHomepages> listOfPair;
	private String localPath;

	/***
	 * Crea e inizializza lo stato dell'oggetto GroupOfHomepages in base alle
	 * informazioni che trova nella homepage passata per parametro. In
	 * particolare popola la lista di possibili pagine parallele alla homepage.
	 * 
	 * @param homepage
	 * @throws LangDetectException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public GroupOfHomepages(Homepage homepage) throws LangDetectException, MalformedURLException {
		this.candidateParallelHomepages = new HashMap<>();
		this.homepage = homepage;
		List<String> multilingualOutlinks = this.homepage.getMultilingualLinks();
		for (String outlink : multilingualOutlinks) {
			try {
				URL url = new URL(outlink);
				candidateParallelHomepages.put(url, new Page(url));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		divideInPairs();
	}

	public List<PairOfHomepages> getListOfPairs() {
		return this.listOfPair;
	}

	public Homepage getHomepage() {
		return homepage;
	}

	public List<Page> getCandidateParallelHomepages() {
		return new ArrayList<>(candidateParallelHomepages.values());
	}

	public String getLocalPath() {
		return this.localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	/***
	 * Crea una lista con tutte le coppie generate da
	 * (firstHomepage,possibleParallelHomepages[i-esima]). E' possibile accedere
	 * alla lista, dopo averla creata con questo metodo, grazie a getPair().
	 * 
	 * @return
	 */
	public void divideInPairs() {
		List<PairOfHomepages> listOfPairs = new LinkedList<>();
		int i = 1;
		for (Page page : candidateParallelHomepages.values()) {
			PairOfHomepages pair = new PairOfHomepages(this.homepage, page, i);
			listOfPairs.add(pair);
			i++;
		}
		this.listOfPair = listOfPairs;
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
			fileToVerify.add(this.getHomepage().getName() + i);
		}
		return fileToVerify;
	}

	/**
	 * Rimuove tutte le pagine non multilingua dalla collezione di pagine
	 * candidate ad essere multilingua lasciando solo quelle che possiedono un
	 * URL fra quelli passati per parametro.
	 * 
	 * @param urls
	 */
	public void setParallelHomepagesByURL(Collection<URL> urls) {
		HashMap<URL, Page> parallelHomepages = new HashMap<>();
		for (URL url : urls)
			parallelHomepages.put(url, this.candidateParallelHomepages.get(url));
		this.candidateParallelHomepages = parallelHomepages;
	}

	/**
	 * Ritorna la collezione di URL provenienti dalle probabili homepage
	 * parallele.
	 * 
	 * @return
	 */
	public Collection<URL> getParallelURLs() {
		return this.candidateParallelHomepages.keySet();
	}

}
