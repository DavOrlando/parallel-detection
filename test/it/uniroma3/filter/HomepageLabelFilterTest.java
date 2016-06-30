package it.uniroma3.filter;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.detection.HomepageOutlinkMultilingualDetector;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.roadrunner.RoadRunnerInvocator;
import it.uniroma3.parallel.utils.FetchManager;

public class HomepageLabelFilterTest {

	private static final String HTTP_LOCALHOST_8080_TEST_HOME_IT_HTML = "http://localhost:8080/test/homeIt.html";
	private HomepageLabelFilterForTest filter;
	private List<String> vuoto;
	private ParallelPages parallelPage;

	@Before
	public void setUp() throws Exception {
		vuoto = new ArrayList<>();
		filter = new HomepageLabelFilterForTest();
		parallelPage = new ParallelPages(new Page(HTTP_LOCALHOST_8080_TEST_HOME_IT_HTML));
	}

	@Test
	public void isDifferentLanguageNessunLinguaggio_test() {
		assertFalse(filter.isDifferentLanguage(vuoto));
	}

	@Test
	public void isDifferentLanguageLinguaSconosciuta_test() {
		vuoto.add("0000");
		assertFalse(filter.isDifferentLanguage(vuoto));
	}

	@Test
	public void isDifferentLanguageUnicaLingua_test() {
		List<String> vuoto = new ArrayList<>();
		vuoto.add("ciao");
		assertTrue(filter.isDifferentLanguage(vuoto));
	}

	@Test
	public void isDifferentLanguageDueLingueUnaSconosciuta_test() {
		vuoto.add("ciao");
		vuoto.add("===");
		assertFalse(filter.isDifferentLanguage(vuoto));
	}

	@Test
	public void isDifferentLanguageDueLingueEntrambeDiverse_test() {
		vuoto.add("ciao");
		vuoto.add("hello");
		assertTrue(filter.isDifferentLanguage(vuoto));
	}

	@Test
	public void isEnoughTextNessunTesto_test() {
		assertFalse(filter.isEnoughText(vuoto));
	}

	@Test
	public void isEnoughTextPocoTesto_test() {
		vuoto.add("poco testo");
		assertFalse(filter.isEnoughText(vuoto));
	}

	@Test
	public void isEnoughTextAbbastanzaTesto_test() {
		vuoto.add("una sola riga ma abbastanza lunga per fare lang detection");
		assertTrue(filter.isEnoughText(vuoto));
	}

	@Test
	public void isEnoughTextAbbastanzaTestoNellaPrimaRigaMaNonNellaSeconda_test() {
		vuoto.add("una sola riga ma abbastanza lunga per fare lang detection");
		vuoto.add("poco testo");
		assertFalse(filter.isEnoughText(vuoto));
	}

	@Test
	public void isEnoughTextAbbastanzaTestoNellaPrimaRigaENellaSeconda_test() {
		vuoto.add("una sola riga ma abbastanza lunga per fare lang detection");
		vuoto.add("molto testo anche qui per fare lang detection");
		assertTrue(filter.isEnoughText(vuoto));
	}

	@Test
	public void filterEmptyParallelPage_test() {
		assertEquals(1, filter.filter(parallelPage).size());
	}

	@Test
	public void filterParallelPageWithThreeEntries_test() {
		HomepageOutlinkMultilingualDetector homepageOutlinkMultilingualDetector = new HomepageOutlinkMultilingualDetector();
		homepageOutlinkMultilingualDetector.findCandidatePages(parallelPage);
		homepageOutlinkMultilingualDetector.organizeInPairs(parallelPage);
		FetchManager.getInstance().persistParallelPages(parallelPage);
		try {
			RoadRunnerInvocator.getInstance().runRoadRunner(parallelPage);
			Collection<URL> filter2 = filter.filter(parallelPage);
			assertEquals(3, filter2.size());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			fail();
		}
	}

}
