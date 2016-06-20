package it.uniroma3.parallel.detection;

import java.io.IOException;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.filter.LabelFilter;
import it.uniroma3.parallel.filter.LabelFilterPrehomepage;
import it.uniroma3.parallel.filter.PreHomepageFilter;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.model.PreHomepage;
import it.uniroma3.parallel.roadrunner.RoadRunnerInvocator;
import it.uniroma3.parallel.utils.FetchManager;

public class PreHomepageOutlinkDetector extends OutlinkDetector {
	@Override
	public ParallelPages detect(Page page) throws IOException, InterruptedException, LangDetectException {
		PreHomepageFilter preHomepageFilter = new PreHomepageFilter();
		if(!preHomepageFilter.filter(page, null))
			return null;
		PreHomepage preHomepage = (PreHomepage) page;
		ParallelPages parallelPages = new ParallelPages(preHomepage);
		for(PairOfPages pairOfPages : parallelPages.getListOfPairs())
			FetchManager.getInstance().persistPairOfHomepage(pairOfPages, preHomepage.getPageName());
		this.runRoadRunner(parallelPages);
		LabelFilterPrehomepage labelFilterPrehomepage = new LabelFilterPrehomepage();
		parallelPages.setListOfPair(labelFilterPrehomepage.filter(parallelPages));
		return parallelPages;
	}
}
