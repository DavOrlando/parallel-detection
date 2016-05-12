package featureTest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import it.model.Site;
import it.multilingualDetection.MultilingualDetector;

public class DetectByHreflangTest {

	private Site site;
	private MultilingualDetector multilingualDetector;
	
	@Before
	public void setUp() throws Exception {
		this.multilingualDetector = new MultilingualDetector();
		this.site = new Site("http://localhost:8080/testMinimale/homeIt.html");
	}
	



	@Test
	public void testDetectByHreflang_SiteThatHasHrefLangLink() {
		try {
			Set<List<String>> detectedByHreflang = this.multilingualDetector.detectByHreflang(site);
			assertTrue(detectedByHreflang.size()==1);
			List<String> lista = new ArrayList<>();
			lista.add("http://localhost:8080/testMinimale/homeIt.html");
			lista.add("http://localhost:8080/testMinimale/homeEn.html");
			lista.add("http://localhost:8080/testMinimale/homeFr.html");
			assertTrue(detectedByHreflang.contains(lista));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDetectByHreflang_SiteThatDoesntHasHrefLangLink() {
		try {
			Site siteNotMultilingual = new Site("http://localhost:8080/testNonMultilingua/homeIt.html");
			Set<List<String>> detectByHreflang = this.multilingualDetector.detectByHreflang(siteNotMultilingual);
			assertTrue(detectByHreflang.isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
