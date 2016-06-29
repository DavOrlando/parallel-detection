package classTest;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.detection.HomepageOutlinkMultilingualDetector;
import it.uniroma3.parallel.model.Page;

public class HomepageOutlinkDetectorTest {


	private static final String USER_AGENT = "Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1";
	private static final String HTTP_LOCALHOST_8080 = "http://localhost:8080";
	private static final String URL_FOR_TEST = "http://localhost:8080/testForLevenshteinAndLanguageFilter/";

	private URL urlSite;
	private HomepageOutlinkMultilingualDetector homepageOutlinkDetector;
	private Page page;

	@Before
	public void setUp() throws Exception {
		this.urlSite = new URL("http://localhost:8080/testMinimale/homeIt.html");
		this.page = new Page(urlSite);
		homepageOutlinkDetector = new HomepageOutlinkMultilingualDetector();
	}

	@Test
	public void noOutlinkTest() {
		try {
			this.page = new Page(new URL(URL_FOR_TEST + "noOutlink.html"));
			assertTrue(homepageOutlinkDetector.getMultilingualPage(page).isEmpty());
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void oneGoodOutlinkTest() {
		try {
			this.page = new Page(new URL(URL_FOR_TEST + "oneOutlink.html"));
			assertEquals(1, homepageOutlinkDetector.getMultilingualPage(page).size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void oneBadOutlinkEditDistanceFailTest() {
		try {
			this.page = new Page(new URL(URL_FOR_TEST + "oneOutlinkButNoEditDistance.html"));
			assertTrue(homepageOutlinkDetector.getMultilingualPage(page).isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void oneBadOutlinkLanguageFailTest() {
		try {
			this.page = new Page(new URL(URL_FOR_TEST + "oneOutlinkButNoLanguage.html"));
			assertTrue(homepageOutlinkDetector.getMultilingualPage(page).isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void twoEqualsOutlinkTest() {
		try {
			this.page = new Page(new URL(URL_FOR_TEST + "twoEqualsOutlink.html"));
			assertEquals(1,homepageOutlinkDetector.getMultilingualPage(page).size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void twoGoodOutlink() {
		try {
			this.page = new Page(new URL(URL_FOR_TEST + "twoGoodOutlink.html"));
			assertEquals(2, homepageOutlinkDetector.getMultilingualPage(page).size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void oneGoodAndOneBadOutlinkTest() {
		try {
			this.page = new Page(new URL(URL_FOR_TEST + "oneGoodAndOneBadOutlink.html"));
			assertEquals(1, homepageOutlinkDetector.getMultilingualPage(page).size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}


}
