package it.uniroma3.detection;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallelcorpora.detection.HomepageOutlinkMultilingualDetector;
import it.uniroma3.parallelcorpora.model.Page;
import it.uniroma3.parallelcorpora.model.ParallelPages;

public class HomepageOutlinkMultilingualDetectorTest {

	private static final String URL_TEST_GETMULTILINGUALPAGE = "http://localhost:8080/testForHomepageMultilingualDetection/";
	private HomepageOutlinkMultilingualDetector homepageOutlinkDetector;
	private Page page;
	private ParallelPages parallelPages;

	@Before
	public void setUp() throws Exception {
		homepageOutlinkDetector = new HomepageOutlinkMultilingualDetector();
	}

	@Test
	public void findCandidatePages_NoOutlink_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "noOutlink.html"));
			this.parallelPages = new ParallelPages(page);
			homepageOutlinkDetector.findCandidatePages(parallelPages);
			assertTrue(parallelPages.isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void findCandidatePages_OneGoodOutlink_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "oneOutlink.html"));
			this.parallelPages = new ParallelPages(page);
			homepageOutlinkDetector.findCandidatePages(parallelPages);
			assertEquals(2,this.parallelPages.getCandidateParallelHomepages().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void findCandidatePages_TwoEqualsOutlink_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "twoEqualsOutlink.html"));
			this.parallelPages = new ParallelPages(page);
			homepageOutlinkDetector.findCandidatePages(parallelPages);
			assertEquals(2,this.parallelPages.getCandidateParallelHomepages().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void findCandidatePages_TwoGoodOutlink_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "twoGoodOutlink.html"));
			this.parallelPages = new ParallelPages(page);
			homepageOutlinkDetector.findCandidatePages(parallelPages);
			assertEquals(3,this.parallelPages.getCandidateParallelHomepages().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void findCandidatePages_OneGoodAndOneBadOutlink_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "oneOutlink.html"));
			this.parallelPages = new ParallelPages(page);
			homepageOutlinkDetector.findCandidatePages(parallelPages);
			assertEquals(2,this.parallelPages.getCandidateParallelHomepages().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	
	@Test
	public void organizeInPairs_NoOutlink_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "noOutlink.html"));
			this.parallelPages = new ParallelPages(page);
			homepageOutlinkDetector.findCandidatePages(parallelPages);
			homepageOutlinkDetector.organizeInPairs(parallelPages);
			assertTrue(parallelPages.getListOfPairs().isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void organizeInPairs_OneGoodOutlink_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "oneOutlink.html"));
			this.parallelPages = new ParallelPages(page);
			homepageOutlinkDetector.findCandidatePages(parallelPages);
			homepageOutlinkDetector.organizeInPairs(parallelPages);
			assertEquals(1,this.parallelPages.getListOfPairs().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void organizeInPairs_TwoEqualsOutlink_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "twoEqualsOutlink.html"));
			this.parallelPages = new ParallelPages(page);
			homepageOutlinkDetector.findCandidatePages(parallelPages);
			homepageOutlinkDetector.organizeInPairs(parallelPages);
			assertEquals(1,this.parallelPages.getListOfPairs().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void organizeInPairs_TwoGoodOutlink_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "twoGoodOutlink.html"));
			this.parallelPages = new ParallelPages(page);
			homepageOutlinkDetector.findCandidatePages(parallelPages);
			homepageOutlinkDetector.organizeInPairs(parallelPages);
			assertEquals(2,this.parallelPages.getListOfPairs().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void organizeInPairs_OneGoodAndOneBadOutlink_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "oneOutlink.html"));
			this.parallelPages = new ParallelPages(page);
			homepageOutlinkDetector.findCandidatePages(parallelPages);
			homepageOutlinkDetector.organizeInPairs(parallelPages);
			assertEquals(1,this.parallelPages.getListOfPairs().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
