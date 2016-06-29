package it.uniroma3.detection;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;

import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.filter.LinkTextFilter;
import it.uniroma3.parallel.model.Page;

public class OutlinkMultilingualDetectorTest {
	
	private static final String URL_TEST_GETMULTILINGUALPAGE = "http://localhost:8080/testForMultilingualFilter/";
	private static final String ALT_TEXT = "http://localhost:8080/test/multilingualEn1.html";
	private static final String NO_OUTLINK = "http://localhost:8080/test/poorMultilingualIt1.html";
	private static final String HTTP_LOCALHOST_8080_TEST_HOME_IT_HTML = "http://localhost:8080/test/homeIt.html";
	private static final String NO_ALT_TEXT = "http://localhost:8080/test/multilingualIt1.html";;
	private OutlinkDetectorImplForTest outlinkDetector;
	private Page page;

	@Before
	public void setUp() throws Exception {
		this.page = new Page(HTTP_LOCALHOST_8080_TEST_HOME_IT_HTML);
		this.outlinkDetector = new OutlinkDetectorImplForTest();
	}

	@Test
	public void getHtmlElementsFromPage_test() {
		assertEquals(1, this.outlinkDetector.getHtmlElements("body", page.getDocument()).size());
	}

	@Test
	public void getHtmlElementsFromPageWithAnchor_test() {
		assertFalse(this.outlinkDetector.getHtmlElements("a", page.getDocument()).isEmpty());
	}

	@Test
	public void getHtmlElementsFromPageWithTenAnchor_test() {
		assertEquals(10, this.outlinkDetector.getHtmlElements("a", page.getDocument()).size());
	}

	@Test
	public void getHtmlElementsFromPageWithThreeDiv_test() {
		assertEquals(3, this.outlinkDetector.getHtmlElements("div", page.getDocument()).size());
	}

	@Test
	public void getHtmlElementsFromPageWithoutParagraph_test() {
		assertTrue(this.outlinkDetector.getHtmlElements("p", page.getDocument()).isEmpty());
	}

	@Test
	public void getAllOutlinksPageWithoutOutlink_test() {
		try {
			assertTrue(this.outlinkDetector.getAllOutlinks(new Page(NO_OUTLINK)).isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getAllOutlinksPageWithTenOutlink_test() {
		assertFalse(this.outlinkDetector.getAllOutlinks(page).isEmpty());
		assertEquals(10, this.outlinkDetector.getAllOutlinks(page).size());
	}

	@Test
	public void checkAnchorTextFromSiteWithGoodTextInAnchor_test() {
		HashSet<Element> allOutlinks = this.outlinkDetector.getAllOutlinks(page);
		boolean verita = false;
		for (Element element : allOutlinks)
			verita = verita || this.outlinkDetector.checkAnchorText(element, page);
		assertTrue(verita);

	}

	@Test
	public void checkAnchorTextFromSiteWithoutTextInAnchor_test() {
		try {
			HashSet<Element> allOutlinks = this.outlinkDetector.getAllOutlinks(new Page(NO_OUTLINK));
			boolean verita = false;
			for (Element element : allOutlinks)
				verita = verita || this.outlinkDetector.checkAnchorText(element, page);
			assertFalse(verita);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void checkAltAttributesFromSiteWithoutImg_test() {
		try {
			HashSet<Element> allOutlinks = this.outlinkDetector.getAllOutlinks(new Page(NO_OUTLINK));
			boolean verita = false;
			for (Element element : allOutlinks)
				verita = verita | this.outlinkDetector.checkAltAttributes(element);
			assertFalse(verita);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void checkAltAttributesFromSiteWithImgAndGoodTextInAlt_test() {
		try {
			HashSet<Element> allOutlinks = this.outlinkDetector.getAllOutlinks(new Page(ALT_TEXT));
			boolean verita = false;
			for (Element element : allOutlinks)
				verita = verita || this.outlinkDetector.checkAltAttributes(element);
			assertTrue(verita);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void checkAltAttributesFromSiteWithImgButBadTextInAlt_test() {
		try {
			HashSet<Element> allOutlinks = this.outlinkDetector.getAllOutlinks(new Page(NO_ALT_TEXT));
			boolean verita = false;
			for (Element element : allOutlinks)
				verita = verita || this.outlinkDetector.checkAltAttributes(element);
			assertFalse(verita);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void getMultilingualPageFromPageWithoutMultilingualPage_test(){
		try {
			assertTrue(this.outlinkDetector.getMultilingualPage(new Page(NO_ALT_TEXT)).isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void getMultilingualPageFromPageWithTwoMultilingualPage_test(){
		assertFalse(this.outlinkDetector.getMultilingualPage(page).isEmpty());
		assertEquals(2,this.outlinkDetector.getMultilingualPage(page).size());
	}


	@Test
	public void getMultilingualPageNoOutlinkTest_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "noOutlink.html"));
			assertTrue(outlinkDetector.getMultilingualPage(page).isEmpty());
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void getMultilingualPageOneGoodOutlinkTest_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "oneOutlink.html"));
			assertEquals(1, outlinkDetector.getMultilingualPage(page).size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}


	@Test
	public void getMultilingualPageTwoEqualsOutlinkTest_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "twoEqualsOutlink.html"));
			assertEquals(1,outlinkDetector.getMultilingualPage(page).size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getMultilingualPageTwoGoodOutlink_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "twoGoodOutlink.html"));
			assertEquals(2, outlinkDetector.getMultilingualPage(page).size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getMultilingualPageOneGoodAndOneBadOutlinkTest_test() {
		try {
			this.page = new Page(new URL(URL_TEST_GETMULTILINGUALPAGE + "oneGoodAndOneBadOutlink.html"));
			assertEquals(1, this.outlinkDetector.getMultilingualPage(page).size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
