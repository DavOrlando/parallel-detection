package classTest;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.model.GroupOfHomepages;
import it.uniroma3.parallel.model.Homepage;

public class GroupOfHomepagesTest {

	private static final String URL_FOR_TEST = "http://localhost:8080/testForLevenshteinAndLanguageFilter/";
	private GroupOfHomepages groupOfHomepages;
	private Homepage homepage;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void makeAGroupOfHomepagesNoOutlinkTest() {
		try {
			this.homepage = new Homepage(new URL(URL_FOR_TEST + "noOutlink.html"));
			this.groupOfHomepages = new GroupOfHomepages(homepage);
			groupOfHomepages.findCandidateParallelHomepages();
			assertTrue(this.groupOfHomepages.isEmpty());
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void makeAGroupOfHomepagesWithOneGoodOutlinkTest() {
		try {
			this.homepage = new Homepage(new URL(URL_FOR_TEST + "oneOutlink.html"));
			this.groupOfHomepages = new GroupOfHomepages(homepage);
			groupOfHomepages.findCandidateParallelHomepages();
			assertEquals(2, groupOfHomepages.getCandidateParallelHomepages().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void makeAGroupOfHomepagesWithoneBadOutlinkEditDistanceFailTest() {
		try {
			this.homepage = new Homepage(new URL(URL_FOR_TEST + "oneOutlinkButNoEditDistance.html"));
			this.groupOfHomepages = new GroupOfHomepages(homepage);
			this.groupOfHomepages.findCandidateParallelHomepages();
			assertTrue(this.groupOfHomepages.isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void makeAGroupOfHomepagesWithoneBadOutlinkLanguageFailTest() {
		try {
			this.homepage = new Homepage(new URL(URL_FOR_TEST + "oneOutlinkButNoLanguage.html"));
			this.groupOfHomepages = new GroupOfHomepages(homepage);
			this.groupOfHomepages.findCandidateParallelHomepages();
			assertTrue(this.groupOfHomepages.isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void makeAGroupOfHomepagesWithTwoEqualsOutlinkTest() {
		try {
			this.homepage = new Homepage(new URL(URL_FOR_TEST + "twoEqualsOutlink.html"));
			this.groupOfHomepages = new GroupOfHomepages(homepage);
			this.groupOfHomepages.findCandidateParallelHomepages();
			assertEquals(2, groupOfHomepages.getCandidateParallelHomepages().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void makeAGroupOfHomepagesWithTwoGoodOutlinkTest() {
		try {
			this.homepage = new Homepage(new URL(URL_FOR_TEST + "twoGoodOutlink.html"));
			this.groupOfHomepages = new GroupOfHomepages(homepage);
			this.groupOfHomepages.findCandidateParallelHomepages();
			assertEquals(3, groupOfHomepages.getCandidateParallelHomepages().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void makeAGroupOfHomepagesWithOneGoodAndOneBadOutlinkTest() {
		try {
			this.homepage = new Homepage(new URL(URL_FOR_TEST + "oneGoodAndOneBadOutlink.html"));
			this.groupOfHomepages = new GroupOfHomepages(homepage);
			this.groupOfHomepages.findCandidateParallelHomepages();
			assertEquals(2, groupOfHomepages.getCandidateParallelHomepages().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void noGoodOutlinkDivideInPairsTest() {
		try {
			this.homepage = new Homepage(new URL(URL_FOR_TEST + "noOutlink.html"));
			this.groupOfHomepages = new GroupOfHomepages(homepage);
			this.groupOfHomepages.organizeInPairs();
			assertEquals(0, this.groupOfHomepages.getListOfPairs().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void oneGoodOutlinkDivideInPairsTest() {
		try {
			this.homepage = new Homepage(new URL(URL_FOR_TEST + "oneOutlink.html"));
			this.groupOfHomepages = new GroupOfHomepages(homepage);
			this.groupOfHomepages.findCandidateParallelHomepages();
			this.groupOfHomepages.organizeInPairs();
			assertEquals(1, this.groupOfHomepages.getListOfPairs().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void twoGoodOutlinkDivideInPairsTest() {
		try {
			this.homepage = new Homepage(new URL(URL_FOR_TEST + "twoGoodOutlink.html"));
			this.groupOfHomepages = new GroupOfHomepages(homepage);
			this.groupOfHomepages.findCandidateParallelHomepages();
			this.groupOfHomepages.organizeInPairs();
			assertEquals(2,this.groupOfHomepages.getListOfPairs().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void oneOutlinkGetParallelURL() {
		try {
			this.homepage = new Homepage(new URL(URL_FOR_TEST + "oneOutlink.html"));
			this.groupOfHomepages = new GroupOfHomepages(homepage);
			this.groupOfHomepages.findCandidateParallelHomepages();
			assertEquals(2,this.groupOfHomepages.getParallelURLs().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void oneOutlinkRemoveOther() {
		try {
			//popolato il gruppo di pagine
			this.homepage = new Homepage(new URL(URL_FOR_TEST + "twoGoodOutlink.html"));
			this.groupOfHomepages = new GroupOfHomepages(homepage);
			this.groupOfHomepages.findCandidateParallelHomepages();
			//creo insieme di url da lasciare
			URL url = new URL(homepage.getMultilingualLinks().get(0));
			HashSet<URL> urls = new HashSet<>();
			urls.add(url);
			urls.add(homepage.getUrlRedirect());
			//verifico prima di rimuoverli
			assertEquals(3, this.groupOfHomepages.getCandidateParallelHomepages().size());
			this.groupOfHomepages.lasciaSoloQuestiURL(urls);
			//verifico che siano rimasti solo quelli dell'insieme urls
			assertEquals(2,this.groupOfHomepages.getCandidateParallelHomepages().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
