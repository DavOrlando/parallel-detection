package it.uniroma3.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallelcorpora.configuration.ConfigurationProperties;
import it.uniroma3.parallelcorpora.model.Page;

public class PageTest {
	private static final String HTTP_LOCALHOST_80802 = "http://localhost:8080";
	private static final String USER_AGENT = ConfigurationProperties.getInstance().getStringOfUserAgent();
	private static final String HTTP_LOCALHOST_8080 = HTTP_LOCALHOST_80802;

	private URL urlSite;
	private Page page;

	@Before
	public void setUp() throws Exception {
		this.urlSite = new URL("http://localhost:8080/testMinimale/homeIt.html");
		this.page = new Page(urlSite);
	}

	@Test
	public void pageCreation_test() {
		try {
			Document document = Jsoup.connect(urlSite.toString())
					.userAgent(USER_AGENT).timeout(8000).get();
			assertEquals(document, page.getDocument());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getDomainUrlWithPort_test() {
		assertEquals(HTTP_LOCALHOST_8080, page.getDomain());
	}

	@Test
	public void getDomainSimpleURL_test() {
		try {
			URL urlIncomplete = new URL(HTTP_LOCALHOST_80802);
			Page page = new Page(urlIncomplete);
			assertEquals(urlIncomplete.toString(), page.getDomain());
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void getPageName_test() {
		String folderNameSite = "localhost8080testMinimalehomeIt.html";
		assertEquals(folderNameSite, page.getPageName());

	}
	
}
