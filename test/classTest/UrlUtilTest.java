package classTest;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import it.utils.UrlUtil;

public class UrlUtilTest {
	String urlSite;

	@Before
	public void setUp() throws Exception {
		this.urlSite = "www.test.com/test/";
	}

	@Test
	public void addHttpTest() {
		String completeUrlString = "http://www.test.com/test/";
		assertEquals(completeUrlString, UrlUtil.addHttp(this.urlSite));
	}

	@Test
	public void getNameFolderFromSiteUrlTest_removeSlash() {
		String folderNameSite = "www.test.comtest";
		try {
			assertEquals(folderNameSite, UrlUtil.getNameFolderFromSiteUrl(new URL(UrlUtil.addHttp(this.urlSite))));
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void getNameFolderFromSiteUrlTest_removeQuestionPoint() {
		String urlSiteWithQuestionPoint = "http://www.test.com/test/?";
		String folderNameSite = "www.test.comtest";
		try {
			assertEquals(folderNameSite, UrlUtil.getNameFolderFromSiteUrl(new URL(urlSiteWithQuestionPoint)));
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void hasHttp_UrlWithHttp() {
		assertTrue(UrlUtil.hasHttp(UrlUtil.addHttp(this.urlSite)));
	}

	@Test
	public void hasHttp_UrlWithoutHttp() {
		assertFalse(UrlUtil.hasHttp(this.urlSite));
	}

}
