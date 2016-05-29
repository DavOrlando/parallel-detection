package featureTest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.detection.HreflangDetector;
import it.uniroma3.parallel.model.GroupOfParallelUrls;
import it.uniroma3.parallel.model.Homepage;

public class DetectByHreflangTest {

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
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}
	

	@Test
	public void pageWithOneHreflang() {
		try {
			homepage = new Homepage(URL_FOR_TEST+"oneHreflang.html");
			GroupOfParallelUrls detectByHreflang = this.homepageDetector.detect(homepage);
			assertNotNull(detectByHreflang);
			//2 perchè anche la homepage viene aggiunta
			assertEquals(2,detectByHreflang.getParallelUrls().size());
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	@Test
	public void pageWithOneHreflangRelative() {
		try {
			homepage = new Homepage(URL_FOR_TEST+"oneHreflangRelative.html");
			GroupOfParallelUrls detectByHreflang = this.homepageDetector.detect(homepage);
			assertTrue(detectByHreflang.getParallelUrls().contains(new URL(ABSOLUTE_URL)));
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void pageWithTwoHreflang() {
		try {
			homepage = new Homepage(new URL(URL_FOR_TEST+"twoHreflang.html"));
			GroupOfParallelUrls detectByHreflang = this.homepageDetector.detect(homepage);
			//3 perchè anche la homepage viene aggiunta
			assertEquals(3,detectByHreflang.getParallelUrls().size());
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	@Test
	public void pageWithTwoButOneIsRelativeHreflang() {
		try {
			homepage = new Homepage(new URL(URL_FOR_TEST+"twoHreflangButOneIsRelative.html"));
			GroupOfParallelUrls detectByHreflang = this.homepageDetector.detect(homepage);
			assertTrue(detectByHreflang.getParallelUrls().contains(new URL(ABSOLUTE_URL)));
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	@Test
	public void homepageWithDuplicateHreflang() {
		try {
			homepage = new Homepage(new URL(URL_FOR_TEST+"duplicateHreflang.html"));
			GroupOfParallelUrls detectByHreflang = this.homepageDetector.detect(homepage);
			assertEquals(2,detectByHreflang.getParallelUrls().size());
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}

}
