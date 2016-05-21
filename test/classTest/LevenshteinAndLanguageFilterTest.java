package classTest;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.detection.M2ltilingualSite;
import it.uniroma3.parallel.detection.LevenshteinAndLanguageFilter;
import it.uniroma3.parallel.model.Page;

public class LevenshteinAndLanguageFilterTest {
	private static final String URL_FOR_TEST = "http://localhost:8080/testForLevenshteinAndLanguageFilter/";
	private Page page;
	private LevenshteinAndLanguageFilter filter;
	
	@Before
	public void setUp() throws Exception {
		this.filter = new LevenshteinAndLanguageFilter();
	}

	@Test
	public void noOutlinkTest() {
		try {
			this.page = new Page(new URL(URL_FOR_TEST+"noOutlink.html"));
			assertTrue(this.filter.doFilter(page).isEmpty());
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}
	
	@Test
	public void oneGoodOutlinkTest() {
		try {
			this.page = new Page(new URL(URL_FOR_TEST+"oneOutlink.html"));
			assertEquals(1,this.filter.doFilter(page).size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void oneBadOutlinkEditDistanceFailTest() {
		try {
			this.page = new Page(new URL(URL_FOR_TEST +"oneOutlinkButNoEditDistance.html"));
			assertTrue(this.filter.doFilter(page).isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void oneBadOutlinkLanguageFailTest() {
		try {
			this.page = new Page(new URL(URL_FOR_TEST +"oneOutlinkButNoLanguage.html"));
			assertTrue(this.filter.doFilter(page).isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void twoEqualsOutlinkTest() {
		try {
			this.page = new Page(new URL(URL_FOR_TEST +"twoEqualsOutlink.html"));
			assertEquals(1,this.filter.doFilter(page).size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void twoGoodOutlink() {
		try {
			this.page = new Page(new URL(URL_FOR_TEST +"twoGoodOutlink.html"));
			assertEquals(2,this.filter.doFilter(page).size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void oneGoodAndOneBadOutlink() {
		try {
			this.page = new Page(new URL(URL_FOR_TEST +"oneGoodAndOneBadOutlink.html"));
			assertEquals(1,this.filter.doFilter(page).size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
