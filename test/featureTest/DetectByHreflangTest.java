package featureTest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.detection.MultilingualDetector;
import it.uniroma3.parallel.model.GroupOfParallelUrls;
import it.uniroma3.parallel.model.Page;

public class DetectByHreflangTest {

	private static final String ABSOLUTE_URL = "http://localhost:8080/testForHreflang/homeFr.html";
	private MultilingualDetector multilingualDetector;
	
	@Before
	public void setUp() throws Exception {
		this.multilingualDetector = new MultilingualDetector();
	}
	

	@Test
	public void siteNoHreflang() {
		try {
			Page notMultilingual = new Page("http://localhost:8080/testForHreflang/noHreflang.html");
			assertNull(this.multilingualDetector.detectByHreflang(notMultilingual));
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}
	

	@Test
	public void siteOneHreflang() {
		try {
			Page multilingual = new Page("http://localhost:8080/testForHreflang/oneHreflang.html");
			GroupOfParallelUrls detectByHreflang = this.multilingualDetector.detectByHreflang(multilingual);
			assertNotNull(detectByHreflang);
			//2 perchè anche la homepage viene aggiunta
			assertEquals(2,detectByHreflang.getParallelUrls().size());
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	@Test
	public void siteOneHreflangRelative() {
		try {
			Page multilingual = new Page("http://localhost:8080/testForHreflang/oneHreflangRelative.html");
			GroupOfParallelUrls detectByHreflang = this.multilingualDetector.detectByHreflang(multilingual);
			assertTrue(detectByHreflang.getParallelUrls().contains(new URL(ABSOLUTE_URL)));
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void siteTwoHreflang() {
		try {
			Page multilingual = new Page(new URL("http://localhost:8080/testForHreflang/twoHreflang.html"));
			GroupOfParallelUrls detectByHreflang = this.multilingualDetector.detectByHreflang(multilingual);
			//3 perchè anche la homepage viene aggiunta
			assertEquals(3,detectByHreflang.getParallelUrls().size());
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	@Test
	public void siteTwoButOneIsRelativeHreflang() {
		try {
			Page multilingual = new Page(new URL("http://localhost:8080/testForHreflang/twoHreflangButOneIsRelative.html"));
			GroupOfParallelUrls detectByHreflang = this.multilingualDetector.detectByHreflang(multilingual);
			assertTrue(detectByHreflang.getParallelUrls().contains(new URL(ABSOLUTE_URL)));
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	@Test
	public void homepageWithDuplicateHreflang() {
		try {
			Page multilingual = new Page(new URL("http://localhost:8080/testForHreflang/duplicateHreflang.html"));
			GroupOfParallelUrls detectByHreflang = this.multilingualDetector.detectByHreflang(multilingual);
			assertEquals(2,detectByHreflang.getParallelUrls().size());
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}

}
