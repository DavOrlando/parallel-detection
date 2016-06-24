package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.filter.LabelFilterPrehomepage;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.model.PreHomepage;
import it.uniroma3.parallel.roadrunner.RoadRunnerInvocator;
import it.uniroma3.parallel.utils.FetchManager;

public class PreHomepageOutlinkDetector extends OutlinkDetector {
	@Override
	public ParallelPages detect(Page page) throws IOException, InterruptedException, LangDetectException, URISyntaxException {
		PreHomepage preHomepage = (PreHomepage) page;
		preHomepage.setPossibleHomepages(this.getMultilingualPage(preHomepage));
		ParallelPages parallelPages = new ParallelPages(preHomepage);
		organizeInPairs(parallelPages);
		for(PairOfPages pairOfPages : parallelPages.getListOfPairs())
			FetchManager.getInstance().persistPairOfHomepage(pairOfPages, preHomepage.getPageName());
		RoadRunnerInvocator.getInstance().runRoadRunner(parallelPages);
		LabelFilterPrehomepage labelFilterPrehomepage = new LabelFilterPrehomepage();
		parallelPages.setListOfPair(labelFilterPrehomepage.filter(parallelPages));
		return parallelPages;
	}
	
	public void organizeInPairs(ParallelPages parallelPages) throws LangDetectException {
		ArrayList<PairOfPages> listOfPairs = new ArrayList<>();
		int i = 1;
		for (Page firstPage : ((PreHomepage) parallelPages.getStarterPage()).getPossibleHomepages()) {
			for (Page secondPage : ((PreHomepage) parallelPages.getStarterPage()).getPossibleHomepages()) {
				if (firstPage.getURLString().compareTo(secondPage.getURLString()) >= 0 && !firstPage.equals(secondPage)
						&& !firstPage.getLanguage().equals(secondPage.getLanguage())) {
					listOfPairs.add(new PairOfPages(firstPage, secondPage, i));
					i++;
				}
			}
		}
		parallelPages.setListOfPair(listOfPairs);
	}

}
