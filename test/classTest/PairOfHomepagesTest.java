package classTest;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.model.Homepage;
import it.uniroma3.parallel.model.PairOfHomepages;

public class PairOfHomepagesTest {

	private PairOfHomepages pair;
	private Homepage hPage1;
	private Homepage hPage2;
	
	@Before
	public void setUp() throws Exception {
		hPage1 = new Homepage("http://localhost:8080/testSenzaHreflang/homeIt.html");
		hPage2 = new Homepage("http://localhost:8080/testSenzaHreflang/homeIt.html");
		pair = new PairOfHomepages(hPage1, hPage2, 0);
	}

	@Test
	public void CoppieUgualiEqualstest() {
		assertEquals(pair,new PairOfHomepages(hPage1,hPage2,0));
	}
	
	@Test
	public void CoppieDiversePerSecondaPaginaEqualstest() throws IOException {
		assertNotEquals(pair,new PairOfHomepages(hPage1,new Homepage("http://localhost:8080/testSenzaHreflang/homeEn.html"),0));
	}

}
