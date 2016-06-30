package it.uniroma3.filter;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.detection.PreHomepageOutlinkMultilingualDetector;
import it.uniroma3.parallel.filter.PrehomepageLabelFilter;
import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.model.PreHomepage;
import it.uniroma3.parallel.roadrunner.RoadRunnerInvocator;
import it.uniroma3.parallel.utils.FetchManager;

public class PrehomepageLabelFilterTest {

	private static final String URL_PREHOMEPAGE = "http://localhost:8080/testForPreHomepageMultilingualDetection/twoGoodOutlink.html";
	private PrehomepageLabelFilter labelFilter;
	private PreHomepage page;
	private ParallelPages parallelPages;

	@Before
	public void setUp() throws Exception {
		labelFilter = new PrehomepageLabelFilter();
		page = new PreHomepage(URL_PREHOMEPAGE);
		page.setPossibleHomepages(new PreHomepageOutlinkMultilingualDetector().getMultilingualPage(page));
		parallelPages = new ParallelPages(page);
		new PreHomepageOutlinkMultilingualDetector().organizeInPairs(parallelPages);
		FetchManager.getInstance().makeDirectories(FetchManager.getInstance().getBasePath(page.getPageName()), 1);
		FetchManager.getInstance().savePageInLocal(page, page.getPageName(), 1, true);
		for (PairOfPages pairOfPages : parallelPages.getListOfPairs())
			FetchManager.getInstance().persistPairOfHomepage(pairOfPages, page.getPageName());
		RoadRunnerInvocator.getInstance().runRoadRunner(parallelPages);
	}

	@Test
	public void filterParallelPagesFromGoodPreHomepageButNotLabel_test() {
		assertTrue(labelFilter.filter(parallelPages).isEmpty());
	}

	@After
	public void after(){
		FetchManager.getInstance().deleteOutput(page.getPageName());
	}
}
