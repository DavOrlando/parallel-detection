package featureTest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import it.model.Page;
import it.multilingualDetection.MultilingualDetector;

public class DetectByHreflangTest {

	private URL url;
	private Page site;
	private MultilingualDetector multilingualDetector;
	
	@Before
	public void setUp() throws Exception {
		this.multilingualDetector = new MultilingualDetector();
		this.url = new URL("http://localhost:8080/testMinimale/homeIt.html");
		this.site = new Page(url);
	}
	



	@Test
	public void testDetectByHreflang_SiteThatHasHrefLangLink() {
		try {
			Set<List<String>> detectedByHreflang = this.multilingualDetector.detectByHreflang(site).getGroupOfEntryPoints(5);
			assertEquals(1,detectedByHreflang.size());
			List<String> lista = new ArrayList<>();
			lista.add("http://localhost:8080/testMinimale/homeEn.html");
			lista.add("http://localhost:8080/testMinimale/homeFr.html");
			lista.add("http://localhost:8080/testMinimale/homeIt.html");
			assertTrue(detectedByHreflang.contains(lista));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDetectByHreflang_SiteThatDoesntHaveHrefLangLink() {
		try {
			Page siteNotMultilingual = new Page(new URL("http://localhost:8080/testNonMultilingua/homeIt.html"));
			assertNull(this.multilingualDetector.detectByHreflang(siteNotMultilingual));
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}
}
