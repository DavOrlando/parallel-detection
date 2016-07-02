package it.uniroma3.utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallelcorpora.utils.UrlUtils;

public class UrlUtilTest {
	String urlSite;

	@Before
	public void setUp() throws Exception {
		this.urlSite = "www.test.com/test/";
	}

	@Test
	public void addHttpTest() {
		String completeUrlString = "http://www.test.com/test/";
		assertEquals(completeUrlString, UrlUtils.getInstance().addHttp(this.urlSite));
	}

	
}
