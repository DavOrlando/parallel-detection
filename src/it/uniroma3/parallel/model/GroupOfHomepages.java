package it.uniroma3.parallel.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.cybozu.labs.langdetect.LangDetectException;

//
public class GroupOfHomepages {

	private Homepage homepage;
	private List<Page> possibleParallelHomepages;

	/***
	 * Crea e inizializza lo stato dell'oggetto GroupOfHomepages in base alle
	 * informazioni che trova nella homepage passata per parametro. In
	 * particolare popola la lista di possibili pagine parallele alla homepage.
	 * 
	 * @param homepage
	 * @throws MalformedURLException
	 * @throws LangDetectException
	 */
	public GroupOfHomepages(Homepage homepage) throws MalformedURLException, LangDetectException {
		this.possibleParallelHomepages = new LinkedList<>();
		this.homepage = homepage;
		List<String> multilingualOutlinks = this.homepage.getMultilingualOutlinks();
		for (String outlink : multilingualOutlinks)
			possibleParallelHomepages.add(new Page(new URL(outlink)));
	}

	public Homepage getHomepage() {
		return homepage;
	}

	public List<Page> getPossibleParallelHomepages() {
		return possibleParallelHomepages;
	}

	/***
	 * Ritorna una lista con tutte le coppie generate da
	 * (firstHomepage,possibleParallelHomepages[i-esima]).
	 * 
	 * @return
	 */
	public List<PairOfHomepages> divideInPairs() {
		List<PairOfHomepages> listOfPairs = new LinkedList<>();
		int i = 1;
		for (Page page : possibleParallelHomepages) {
			PairOfHomepages pair = new PairOfHomepages(this.homepage, page, i);
			listOfPairs.add(pair);
			i++;
		}
		return listOfPairs;
	}

	/**
	 * Ritorna la mappa che serve per far funzionare ancora tutto. PROVVISORIO
	 * TODO
	 * 
	 * @return
	 */
	public Map<String, String> getLocalPath2Url() {
		Map<String, String> localPath2Url = new HashMap<>();
		for (Page page : this.possibleParallelHomepages) {
			localPath2Url.put(page.getLocalPath(), page.getUrlRedirect().toString());
		}
		return localPath2Url;
	}

	public List<String> getFileToVerify() {
		List<String> fileToVerify = new ArrayList<>();
		for (int i = 1; i <= this.possibleParallelHomepages.size(); i++) {
			fileToVerify.add(this.getHomepage().getNameFolder() + i);
		}
		return fileToVerify;
	}

}
