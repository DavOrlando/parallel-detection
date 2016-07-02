package it.uniroma3.model;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallelcorpora.model.Page;
import it.uniroma3.parallelcorpora.model.PairOfPages;

public class PairOfPagesTest {

	private static final String HTTP_LOCALHOST_8080_TEST_SENZA_HREFLANG_HOME_IT_HTML = "http://localhost:8080/testSenzaHreflang/homeIt.html";
	private PairOfPages pair;
	private Page hPage1;
	private Page hPage2;
	
	@Before
	public void setUp() throws Exception {
		hPage1 = new Page(HTTP_LOCALHOST_8080_TEST_SENZA_HREFLANG_HOME_IT_HTML);
		hPage2 = new Page(HTTP_LOCALHOST_8080_TEST_SENZA_HREFLANG_HOME_IT_HTML);
		pair = new PairOfPages(hPage1, hPage2, 0);
	}

	@Test
	public void equalsCoppieUguali_test() {
		assertEquals(pair,new PairOfPages(hPage1,hPage2,0));
	}
	
	@Test
	public void equalsCoppieDiversePerSecondaPagina_test() {
		try {
			assertNotEquals(pair,new PairOfPages(hPage1,new Page("http://localhost:8080/testSenzaHreflang/homeEn.html"),0));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

}
