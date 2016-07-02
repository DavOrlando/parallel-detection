package it.uniroma3.filter;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallelcorpora.filter.LanguageFilter;
import it.uniroma3.parallelcorpora.model.Page;

public class LanguageFilterTest {

	private static final String HTTP_LOCALHOST_8080_TEST_HOME_EN_HTML = "http://localhost:8080/test/homeEn.html";
	private static final String HTTP_LOCALHOST_8080_TEST_HOME_IT_HTML = "http://localhost:8080/test/homeIt.html";
	private LanguageFilter languageFilter;
	private Page page;

	@Before
	public void setUp() throws Exception {
		languageFilter = new LanguageFilter();
		page = new Page(HTTP_LOCALHOST_8080_TEST_HOME_IT_HTML);
	}

	@Test
	public void filterStessoLinguaggio_test() {
		assertFalse(languageFilter.filter(page, page));
	}

	@Test
	public void filterLinguaggioDifferente_test() {
		try {
			Page page2 = new Page(HTTP_LOCALHOST_8080_TEST_HOME_EN_HTML);
			assertTrue(languageFilter.filter(page, page2));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
}
