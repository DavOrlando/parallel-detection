package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.roadrunner.RoadRunnerInvocator;
import it.uniroma3.parallel.filter.LabelFilter;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.utils.FetchManager;

public class HomepageOutlinkDetector extends OutlinkDetector {

	public HomepageOutlinkDetector() {
		
	}

	/**
	 * Si cercano nella homepage del sito passato come parametro dei link
	 * uscenti che portano a pagine di linguaggio differente e stessa struttura
	 * del DOM, euristica che ci porta a dire che il sito è multilingua. Si
	 * collezionano tutti questi URL, che rappresentano gli URL verso le pagine
	 * nelle varie lingue.
	 * 
	 * @see GroupOfParallelUrls
	 * @param homepage
	 *            la pagina che rappresenta l'homepage del sito
	 * @return GroupOfParallelUrls
	 * @throws IOException
	 * @throws URISyntaxException 
	 */

	@Override
	public ParallelPages detect(Page page) throws IOException, InterruptedException, LangDetectException, URISyntaxException {
		// da ritornare alla fine
		ParallelPages parallelPage = new ParallelPages(page);
		findCandidatePage(parallelPage);
		organizeInPairs(parallelPage);
		FetchManager.getInstance().persistParallelPages(parallelPage);
		RoadRunnerInvocator.getInstance().runRoadRunner(parallelPage);
		LabelFilter labelFilter = new LabelFilter();
		parallelPage.lasciaSoloQuestiURL(labelFilter.filter(parallelPage));
		return parallelPage;
	}

	/**
	 * Trova le pagine candidate ad essere parallele. Lo fa attraverso la
	 * ricerca di URL di pagine multilingua all'interno della homepage. Il primo
	 * elemento della mappa sarà l'homepage primitiva.
	 * @param parallelPage 
	 * @throws URISyntaxException 
	 */
	public void findCandidatePage(ParallelPages parallelPage) throws URISyntaxException {
		 List<Page> multilingualPages = getMultilingualPage(parallelPage.getStarterPage());
		for (Page page : multilingualPages) {
			parallelPage.addCandidateParallelHomepage(page);
		}
	}
	
	/***
	 * Crea una lista con tutte le coppie generate da
	 * (firstHomepage,possibleParallelHomepages[i-esima]). E' possibile accedere
	 * alla lista, dopo averla creata con questo metodo, grazie a getPair().
	 * 
	 * @return
	 */
	public void organizeInPairs(ParallelPages parallelPage) {
		LinkedList<PairOfPages> listOfPairs = new LinkedList<>();
		int i = 1;
		for (Page page : parallelPage.getCandidateParallelHomepages()) {
			if (!page.equals(parallelPage.getStarterPage())) {
				PairOfPages pair = new PairOfPages(parallelPage.getStarterPage(), page, i);
				listOfPairs.add(pair);
				i++;
			}
		}
		parallelPage.setListOfPair(listOfPairs);
	}

}