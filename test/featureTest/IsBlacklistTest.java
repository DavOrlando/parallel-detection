package featureTest;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.detection.HreflangDetector;

public class IsBlacklistTest {

	private HreflangDetector multilingualDetector;

	@Before
	public void setUp() {
		multilingualDetector = new HreflangDetector();
	}

	@Test
	public void isBlacklistTest_stadsite() {
		URL falseMultilingualSite;
		try {
			falseMultilingualSite = new URL("http://www.test.stadtsite.com");
			assertTrue(this.multilingualDetector.isInBlacklist(falseMultilingualSite));
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void isBlacklistTest_citycorner() {
		URL falseMultilingualSite;
		try {
			falseMultilingualSite = new URL("http://www.test.citycorner.com");
			assertTrue(this.multilingualDetector.isInBlacklist(falseMultilingualSite));
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void isBlacklistTest_citysite() {
		URL falseMultilingualSite;
		try {
			falseMultilingualSite = new URL("http://www.test.citysite.com");
			assertTrue(this.multilingualDetector.isInBlacklist(falseMultilingualSite));
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		}
	}
}