package it.uniroma3.parallel.detection;

import java.io.IOException;

import java.net.URISyntaxException;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.filter.LanguageFilter;
import it.uniroma3.parallel.filter.PrehomepageLabelFilter;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.model.PreHomepage;
import it.uniroma3.parallel.roadrunner.RoadRunnerInvocator;
import it.uniroma3.parallel.utils.FetchManager;

/**
 * Classe che è rappresenta la terza euristica. Supponendo che abbiamo davanti
 * una prehomepage creiamo le coppie di homepage dai link uscenti che possiedono
 * come valori stringhe rilevanti per il cambiamento di lingua(come 'english'
 * oppure 'en') e che siano parallele fra di loro e con diverso linguaggio.
 * 
 * @author davideorlando
 *
 */
public class PreHomepageOutlinkMultilingualDetector extends OutlinkMultilingualDetector {
	private static final Logger logger = Logger.getLogger(PreHomepageOutlinkMultilingualDetector.class);

	@Override
	public ParallelPages detect(Page page)
			throws IOException, InterruptedException, LangDetectException, URISyntaxException {
		PreHomepage preHomepage = (PreHomepage) page;
		preHomepage.setPossibleHomepages(this.getMultilingualPage(preHomepage));
		ParallelPages parallelPages = new ParallelPages(preHomepage);
		organizeInPairs(parallelPages);
		for (PairOfPages pairOfPages : parallelPages.getListOfPairs())
			FetchManager.getInstance().persistPairOfHomepage(pairOfPages, preHomepage.getPageName());
		RoadRunnerInvocator.getInstance().runRoadRunner(parallelPages);
		parallelPages.setListOfPair(new PrehomepageLabelFilter().filter(parallelPages));
		return parallelPages;
	}

	/***
	 * Setta la lista di coppie con tutte le coppie generate da
	 * (possibleParallelHomepages[i-esima],possibleParallelHomepages[i-esima]).
	 * Con la condizione che le due pagine non siano uguali e abbiano un
	 * linguaggio differente.
	 * 
	 * @return
	 */
	@Override
	public void organizeInPairs(ParallelPages parallelPages) {
		LinkedList<PairOfPages> listOfPairs = new LinkedList<>();
		int i = 1;
		for (Page firstPage : ((PreHomepage) parallelPages.getStarterPage()).getPossibleHomepages()) {
			for (Page secondPage : ((PreHomepage) parallelPages.getStarterPage()).getPossibleHomepages()) {
				if (firstPage.getURLString().compareTo(secondPage.getURLString()) > 0
						&& new LanguageFilter().filter(firstPage, secondPage)) {
					listOfPairs.add(new PairOfPages(firstPage, secondPage, i));
					i++;
				}
			}
		}
		parallelPages.setListOfPair(listOfPairs);
	}

}
