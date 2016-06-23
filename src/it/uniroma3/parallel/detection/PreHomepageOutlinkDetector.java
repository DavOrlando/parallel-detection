package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.net.URISyntaxException;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.filter.LabelFilterPrehomepage;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.model.PreHomepage;
import it.uniroma3.parallel.utils.FetchManager;

public class PreHomepageOutlinkDetector extends OutlinkDetector {
	@Override
	public ParallelPages detect(Page page) throws IOException, InterruptedException, LangDetectException, URISyntaxException {
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
