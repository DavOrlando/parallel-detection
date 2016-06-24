package classTest;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.model.PreHomepage;

public class PairOfHomepagesTest {

	private PairOfPages pair;
	private Page hPage1;
	private Page hPage2;
	
	@Before
	public void setUp() throws Exception {
		hPage1 = new Page("http://localhost:8080/testSenzaHreflang/homeIt.html");
		hPage2 = new Page("http://localhost:8080/testSenzaHreflang/homeIt.html");
		pair = new PairOfPages(hPage1, hPage2, 0);
	}

	@Test
	public void CoppieUgualiEqualstest() {
		assertEquals(pair,new PairOfPages(hPage1,hPage2,0));
	}
	
	@Test
	public void CoppieDiversePerSecondaPaginaEqualstest() throws IOException {
		assertNotEquals(pair,new PairOfPages(hPage1,new Page("http://localhost:8080/testSenzaHreflang/homeEn.html"),0));
	}

}
