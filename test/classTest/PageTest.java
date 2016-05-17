package classTest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.utils.UrlUtil;

public class PageTest {
	private static final String HTTP_LOCALHOST_8080 = "http://localhost:8080";
	private URL urlSite;
	private Page page;

	@Before
	public void setUp() throws Exception {
		this.urlSite = new URL("http://localhost:8080/testMinimale/homeIt.html");
		this.page = new Page(urlSite);
	}

	@Test
	public void siteTest() {
		try {
			Document document = Jsoup.connect(urlSite.toString())
					.userAgent("Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1").timeout(8000).get();
			assertEquals(document, page.getDocument());
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void getUrlRootTest_UrlWithPort() {
		assertEquals(HTTP_LOCALHOST_8080, page.getDomain());
	}

	@Test
	public void getUrlRootTest_SimpleUrl() {
		try {
			URL urlIncomplete = new URL("http://www.ferrari.com");
			Page page = new Page(urlIncomplete);
			assertEquals(urlIncomplete.toString(), page.getDomain());
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

	}

}
