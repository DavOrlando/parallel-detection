package it.uniroma3.adapter;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallelcorpora.adapter.CrawlingFrancElefanteAdapter;
import it.uniroma3.parallelcorpora.model.Page;
import it.uniroma3.parallelcorpora.model.ParallelPages;

public class CrawlingFrancElefanteAdapterTest {

	private static final String TEST = "http://localhost:8080/test/homeIt.html";
	private static final String TEST2 = "http://localhost:8080/test/homeEn.html";
	private static final String TEST3 = "http://localhost:8080/test/homeFr.html";
	private ParallelPages emptyParallelPage;
	private CrawlingFrancElefanteAdapter adapter;

	@Before
	public void setUp() throws Exception {
		this.emptyParallelPage = new ParallelPages(new Page(TEST));
		this.adapter = new CrawlingFrancElefanteAdapter();
	}

	@Test
	public void getGroupOfEntryPoints_OneForGroup_EmptyParallelPage_test() {
		assertTrue(emptyParallelPage.isEmpty());
		// 1 perchè la homepage c'è sempre
		assertEquals(1, this.adapter.getGroupOfEntryPoints(1, emptyParallelPage).size());
	}

	@Test
	public void getGroupOfEntryPoints_OneForGroup_ParallelPageWithOneCandidate_test() {
		try {
			emptyParallelPage.addCandidateParallelHomepage(new URL(TEST2));
			assertEquals(2, this.adapter.getGroupOfEntryPoints(1, emptyParallelPage).size());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getGroupOfEntryPoints_OneForGroup_ParallelPageWithTwoCandidate_test() {
		try {
			emptyParallelPage.addCandidateParallelHomepage(new URL(TEST2));
			emptyParallelPage.addCandidateParallelHomepage(new URL(TEST3));
			assertEquals(3, this.adapter.getGroupOfEntryPoints(1, emptyParallelPage).size());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getGroupOfEntryPoints_TwoForGroup_EmptyParallelPage_test() {
		assertTrue(emptyParallelPage.isEmpty());
		// 1 perchè la homepage c'è sempre
		assertEquals(1, this.adapter.getGroupOfEntryPoints(2, emptyParallelPage).size());
	}

	@Test
	public void getGroupOfEntryPoints_TowForGroup_ParallelPageWithOneCandidate_test() {
		try {
			emptyParallelPage.addCandidateParallelHomepage(new URL(TEST2));
			assertEquals(1, this.adapter.getGroupOfEntryPoints(2, emptyParallelPage).size());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getGroupOfEntryPoints_TwoForGroup_ParallelPageWithTwoCandidate_test() {
		try {
			emptyParallelPage.addCandidateParallelHomepage(new URL(TEST2));
			emptyParallelPage.addCandidateParallelHomepage(new URL(TEST3));
			assertEquals(2, this.adapter.getGroupOfEntryPoints(2, emptyParallelPage).size());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

}
