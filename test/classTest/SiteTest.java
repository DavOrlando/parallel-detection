package classTest;

import static org.junit.Assert.*;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import it.model.Site;
import it.utils.UrlHelper;

public class SiteTest {
	private String urlSite;

	@Before
	public void setUp() throws Exception {
		this.urlSite = "http://localhost:8080/testMinimale/homeIt.html";
	}

	@Test
	public void siteTest() {
		try {
			Site site = new Site(this.urlSite);
			Document document = Jsoup.connect(urlSite).userAgent("Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1")
					.timeout(8000).get();
			assertEquals(document, site.getDocument());
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}

}
