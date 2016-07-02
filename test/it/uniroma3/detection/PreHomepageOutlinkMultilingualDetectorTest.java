package it.uniroma3.detection;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallelcorpora.detection.PreHomepageOutlinkMultilingualDetector;
import it.uniroma3.parallelcorpora.model.ParallelPages;
import it.uniroma3.parallelcorpora.model.PreHomepage;

public class PreHomepageOutlinkMultilingualDetectorTest {

	private static final String URL_TEST_GETMULTILINGUALPAGE = "http://localhost:8080/testForPreHomepageMultilingualDetection/";
	private PreHomepageOutlinkMultilingualDetector prehomepageOutlinkDetector;
	private PreHomepage page;
	private ParallelPages parallelPages;

	@Before
	public void setUp() throws Exception {
		prehomepageOutlinkDetector = new PreHomepageOutlinkMultilingualDetector();
	}

	@Test
	public void organizeInPairs_NoOutlink_test() {
		try {
			this.page = new PreHomepage(URL_TEST_GETMULTILINGUALPAGE + "noOutlink.html");
			this.page.setPossibleHomepages(prehomepageOutlinkDetector.getMultilingualPage(page));
			this.parallelPages = new ParallelPages(page);
			prehomepageOutlinkDetector.organizeInPairs(parallelPages);
			assertTrue(parallelPages.getListOfPairs().isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void organizeInPairs_OneGoodOutlink_test() {
		try {
			this.page = new PreHomepage(URL_TEST_GETMULTILINGUALPAGE + "oneOutlink.html");
			this.page.setPossibleHomepages(prehomepageOutlinkDetector.getMultilingualPage(page));
			this.parallelPages = new ParallelPages(page);
			prehomepageOutlinkDetector.organizeInPairs(parallelPages);
			assertEquals(0, this.parallelPages.getListOfPairs().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void organizeInPairs_TwoEqualsOutlink_test() {
		try {
			this.page = new PreHomepage(URL_TEST_GETMULTILINGUALPAGE + "twoEqualsOutlink.html");
			this.page.setPossibleHomepages(prehomepageOutlinkDetector.getMultilingualPage(page));
			this.parallelPages = new ParallelPages(page);
			prehomepageOutlinkDetector.organizeInPairs(parallelPages);
			assertEquals(0, this.parallelPages.getListOfPairs().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void organizeInPairs_TwoGoodOutlink_test() {
		try {
			this.page = new PreHomepage(URL_TEST_GETMULTILINGUALPAGE + "twoGoodOutlink.html");
			this.page.setPossibleHomepages(prehomepageOutlinkDetector.getMultilingualPage(page));
			this.parallelPages = new ParallelPages(page);
			prehomepageOutlinkDetector.organizeInPairs(parallelPages);
			assertEquals(1, this.parallelPages.getListOfPairs().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void organizeInPairs_OneGoodAndOneBadOutlink_test() {
		try {
			this.page = new PreHomepage(URL_TEST_GETMULTILINGUALPAGE + "oneOutlink.html");
			this.page.setPossibleHomepages(prehomepageOutlinkDetector.getMultilingualPage(page));
			this.parallelPages = new ParallelPages(page);
			prehomepageOutlinkDetector.organizeInPairs(parallelPages);
			assertEquals(0, this.parallelPages.getListOfPairs().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	public void organizeInPairs_TwoOutlinkButSameLanguage_test() {
		try {
			this.page = new PreHomepage(URL_TEST_GETMULTILINGUALPAGE + "oneOutlinkButNoLanguage.html");
			this.page.setPossibleHomepages(prehomepageOutlinkDetector.getMultilingualPage(page));
			this.parallelPages = new ParallelPages(page);
			prehomepageOutlinkDetector.organizeInPairs(parallelPages);
			assertEquals(0, this.parallelPages.getListOfPairs().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	public void organizeInPairs_ThreeOutlinkButOneLangDetectionError_test() {
		try {
			this.page = new PreHomepage(URL_TEST_GETMULTILINGUALPAGE + "langDetectionError.html");
			this.page.setPossibleHomepages(prehomepageOutlinkDetector.getMultilingualPage(page));
			this.parallelPages = new ParallelPages(page);
			prehomepageOutlinkDetector.organizeInPairs(parallelPages);
			assertEquals(1, this.parallelPages.getListOfPairs().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
