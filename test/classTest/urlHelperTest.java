package classTest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import it.utils.UrlHelper;

public class urlHelperTest {
	private static final String HTTP_WWW_TEST_COM = "http://www.test.com";
	String urlSite;

	@Before
	public void setUp() throws Exception {
		this.urlSite = "www.test.com/test/";
	}


	@Test
	public void getUrlWithHttpTest() {
		String completeUrlString = "http://www.test.com/test/";
		assertEquals(completeUrlString,UrlHelper.getUrlWithHttp(this.urlSite));
	}
	
	@Test
	public void getNameFolderFromSiteUrlTest_removeSlash() {
		String folderNameSite = "www.test.comtest";
		assertEquals(folderNameSite,UrlHelper.getNameFolderFromSiteUrl(this.urlSite));
	}
	

	@Test
	public void getNameFolderFromSiteUrlTest_removeQuestionPoint() {
		String urlSiteWithQuestionPoint = "www.test.com/test/?";
		String folderNameSite = "www.test.comtest";
		assertEquals(folderNameSite,UrlHelper.getNameFolderFromSiteUrl(urlSiteWithQuestionPoint));
	}
	
	@Test
	public void getUrlRootTest_completeUrlString() {
		String completeUrlString = UrlHelper.getUrlWithHttp(this.urlSite);
		assertEquals(HTTP_WWW_TEST_COM,UrlHelper.getUrlRoot(completeUrlString));
	}
	
	@Test
	public void getUrlRootTest_incompleteUrlString() {
		assertEquals(HTTP_WWW_TEST_COM,UrlHelper.getUrlRoot(this.urlSite));
	}
	
	@Test
	public void getUrlRootTest_incompleteUrlStringWith3Slashes() {
		String incompleteUrlString = "www.test.com/test/test2/test3";
		assertEquals(HTTP_WWW_TEST_COM,UrlHelper.getUrlRoot(incompleteUrlString));
	}
	
	
	@Test
	public void hasHttp_UrlWithHttp() {
		assertTrue(UrlHelper.hasHttp(UrlHelper.getUrlWithHttp(this.urlSite)));
	}
	
	@Test
	public void hasHttp_UrlWithoutHttp() {
		assertFalse(UrlHelper.hasHttp(this.urlSite));
	}


}
