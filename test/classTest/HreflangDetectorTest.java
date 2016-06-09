package classTest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.detection.HreflangDetector;
import it.uniroma3.parallel.model.GroupOfHomepages;
import it.uniroma3.parallel.model.Homepage;

public class HreflangDetectorTest {

	private static final String URL_FOR_TEST = "http://localhost:8080/testForHreflang/";
	private static final String ABSOLUTE_URL = "http://localhost:8080/testForHreflang/homeFr.html";
	private HreflangDetector homepageDetector;
	private Homepage homepage;
	
	@Before
	public void setUp() throws Exception {
		this.homepageDetector = new HreflangDetector();
	}
	

	@Test
	public void pageWithNoHreflang() {
		try {
			homepage = new Homepage(URL_FOR_TEST +"noHreflang.html");
			assertNull(this.homepageDetector.detect(homepage));
		} catch (IOException | LangDetectException e) {
			fail();
			e.printStackTrace();
		}
	}
	

	@Test
	public void pageWithOneHreflang() {
		try {
			homepage = new Homepage(URL_FOR_TEST+"oneHreflang.html");
			GroupOfHomepages detectByHreflang = this.homepageDetector.detect(homepage);
			assertNotNull(detectByHreflang);
			//2 perchè anche la homepage viene aggiunta
			assertEquals(2,detectByHreflang.getParallelURLs().size());
		} catch (IOException | LangDetectException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	@Test
	public void pageWithOneHreflangRelative() {
		try {
			homepage = new Homepage(URL_FOR_TEST+"oneHreflangRelative.html");
			GroupOfHomepages detectByHreflang = this.homepageDetector.detect(homepage);
			assertTrue(detectByHreflang.getParallelURLs().contains(new URL(ABSOLUTE_URL)));
		} catch (IOException | LangDetectException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void pageWithTwoHreflang() {
		try {
			homepage = new Homepage(new URL(URL_FOR_TEST+"twoHreflang.html"));
			GroupOfHomepages detectByHreflang = this.homepageDetector.detect(homepage);
			//3 perchè anche la homepage viene aggiunta
			assertEquals(3,detectByHreflang.getParallelURLs().size());
		} catch (IOException | LangDetectException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	@Test
	public void pageWithTwoButOneIsRelativeHreflang() {
		try {
			homepage = new Homepage(new URL(URL_FOR_TEST+"twoHreflangButOneIsRelative.html"));
			GroupOfHomepages detectByHreflang = this.homepageDetector.detect(homepage);
			assertTrue(detectByHreflang.getParallelURLs().contains(new URL(ABSOLUTE_URL)));
		} catch (IOException | LangDetectException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	@Test
	public void homepageWithDuplicateHreflang() {
		try {
			homepage = new Homepage(new URL(URL_FOR_TEST+"duplicateHreflang.html"));
			GroupOfHomepages detectByHreflang = this.homepageDetector.detect(homepage);
			assertEquals(2,detectByHreflang.getParallelURLs().size());
		} catch (IOException | LangDetectException e) {
			fail();
			e.printStackTrace();
		}
	}

}
