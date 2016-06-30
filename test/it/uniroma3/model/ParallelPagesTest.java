package it.uniroma3.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.ParallelPages;

public class ParallelPagesTest {

	private static final String TEST_PAGE_2 = "http://localhost:8080/test/homeEn.html";
	private static final String TEST_PAGE = "http://localhost:8080/test/homeIt.html";
	private ParallelPages parallelPage;
	private Page starterPage;
	private Page otherPage;

	@Before
	public void setUp() throws Exception {
		starterPage = new Page(TEST_PAGE);
		otherPage = new Page(TEST_PAGE_2);
		parallelPage = new ParallelPages(starterPage);
	}

	@Test
	public void addCandidateParallelHomepageURL_test() {
		try {
			assertTrue(parallelPage.isEmpty());
			parallelPage.addCandidateParallelHomepage(new URL(TEST_PAGE_2));
			assertFalse(parallelPage.isEmpty());
			assertEquals(2, parallelPage.getCandidateParallelHomepages().size());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void addCandidateParallelHomepagePage_test() {
		try {
			assertTrue(parallelPage.isEmpty());
			parallelPage.addCandidateParallelHomepage(new Page(TEST_PAGE_2));
			assertFalse(parallelPage.isEmpty());
			assertEquals(2, parallelPage.getCandidateParallelHomepages().size());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void isEmptyParallelPageEmpty_test() {
		assertTrue(parallelPage.isEmpty());
	}

	@Test
	public void isEmptyParallelPageNotEmpty_test() {
		parallelPage.addCandidateParallelHomepage(otherPage);
		assertFalse(parallelPage.isEmpty());
	}

	@Test
	public void lasciaSoloQuestiURLCollezioneVuota_test() {
		parallelPage.addCandidateParallelHomepage(otherPage);
		parallelPage.lasciaSoloQuestiURL(new ArrayList<>());
		assertTrue(parallelPage.isEmpty());
	}

	@Test
	public void lasciaSoloQuestiURLCollezioneConUnURL_test() {
		try {
			parallelPage.addCandidateParallelHomepage(otherPage);
			ArrayList<URL> urls = new ArrayList<>();
			urls.add(new URL(TEST_PAGE));
			parallelPage.lasciaSoloQuestiURL(urls);
			assertTrue(parallelPage.isEmpty());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void lasciaSoloQuestiURLCollezioneConDueURL_test() {
		try {
			parallelPage.addCandidateParallelHomepage(otherPage);
			ArrayList<URL> urls = new ArrayList<>();
			urls.add(new URL(TEST_PAGE));
			urls.add(new URL(TEST_PAGE_2));
			parallelPage.lasciaSoloQuestiURL(urls);
			assertFalse(parallelPage.isEmpty());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail();
		}
	}
}
