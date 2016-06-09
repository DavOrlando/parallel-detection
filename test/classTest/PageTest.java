package classTest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.model.Page;

public class PageTest {
	private static final String USER_AGENT = "Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1";
	private static final String HTTP_LOCALHOST_8080 = "http://localhost:8080";
	private static final String URL_FOR_TEST = "http://localhost:8080/testForLevenshteinAndLanguageFilter/";

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
					.userAgent(USER_AGENT).timeout(8000).get();
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
			URL urlIncomplete = new URL("http://localhost:8080");
			Page page = new Page(urlIncomplete);
			assertEquals(urlIncomplete.toString(), page.getDomain());
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void getNameFolder_removeSlash() {
		String folderNameSite = "localhost8080testMinimalehomeIt.html";
		assertEquals(folderNameSite, page.getName());

	}
	
}
