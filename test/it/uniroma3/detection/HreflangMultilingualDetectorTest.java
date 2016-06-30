package it.uniroma3.detection;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.detection.HreflangMultilingualDetector;
import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.model.Page;

public class HreflangMultilingualDetectorTest {

	private static final String URL_FOR_TEST = "http://localhost:8080/testForHreflang/";
	private static final String ABSOLUTE_URL = "http://localhost:8080/testForHreflang/homeFr.html";
	private HreflangMultilingualDetector homepageDetector;
	private Page page;

	@Before
	public void setUp() throws Exception {
		this.homepageDetector = new HreflangMultilingualDetector();
	}

	@Test
	public void detectPageWithoutHreflang_test() {
		try {
			page = new Page(URL_FOR_TEST + "noHreflang.html");
			assertNull(this.homepageDetector.detect(page));
		} catch (IOException | LangDetectException | URISyntaxException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void detectPageWithOneHreflang_test() {
		try {
			page = new Page(URL_FOR_TEST + "oneHreflang.html");
			ParallelPages detectByHreflang = this.homepageDetector.detect(page);
			assertNotNull(detectByHreflang);
			// 2 perchè anche la pageviene aggiunta
			assertEquals(2, detectByHreflang.getParallelURIs().size());
		} catch (IOException | LangDetectException | URISyntaxException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void detectPageWithOneHreflangRelative_test() {
		try {
			page = new Page(URL_FOR_TEST + "oneHreflangRelative.html");
			ParallelPages detectByHreflang = this.homepageDetector.detect(page);
			assertTrue(detectByHreflang.getParallelURIs().contains(new URL(ABSOLUTE_URL).toURI()));
		} catch (IOException | LangDetectException | URISyntaxException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void detectPageWithTwoHreflang_test() {
		try {
			page = new Page(new URL(URL_FOR_TEST + "twoHreflang.html"));
			ParallelPages detectByHreflang = this.homepageDetector.detect(page);
			// 3 perchè anche la pageviene aggiunta
			assertEquals(3, detectByHreflang.getParallelURIs().size());
		} catch (IOException | LangDetectException | URISyntaxException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void detectPageWithTwoButOneIsRelativeHreflang_test() {
		try {
			page = new Page(new URL(URL_FOR_TEST + "twoHreflangButOneIsRelative.html"));
			ParallelPages detectByHreflang = this.homepageDetector.detect(page);
			assertTrue(detectByHreflang.getParallelURIs().contains(new URL(ABSOLUTE_URL).toURI()));
		} catch (IOException | LangDetectException | URISyntaxException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void detectHomepageWithDuplicateHreflang_test() {
		try {
			page = new Page(new URL(URL_FOR_TEST + "duplicateHreflang.html"));
			ParallelPages detectByHreflang = this.homepageDetector.detect(page);
			// 2 perchè cè la homepage
			assertEquals(2, detectByHreflang.getParallelURIs().size());
		} catch (IOException | LangDetectException | URISyntaxException e) {
			fail();
			e.printStackTrace();
		}
	}

}
