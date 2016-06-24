package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.roadrunner.RoadRunnerInvocator;
import it.uniroma3.parallel.filter.LabelFilter;
import it.uniroma3.parallel.model.Homepage;
import it.uniroma3.parallel.model.Page;
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
		Homepage homepage = (Homepage) page;
		// da ritornare alla fine
		ParallelPages parallelPage = new ParallelPages(homepage);
		findCandidatePage(parallelPage);
		FetchManager.getInstance().persistParallelPages(parallelPage);;
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

}