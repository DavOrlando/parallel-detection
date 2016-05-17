package featureTest;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import it.model.Page;
import it.multilingualDetection.M2ltilingualSite;
import it.multilingualDetection.MultilingualDetector;

public class DetectFalseMultilingualSiteTest {

	private MultilingualDetector multilingualDetector;

	@Before
	public void setUp() {
		multilingualDetector = new MultilingualDetector();
	}

	@Test
	public void detectFalseMultilingualSiteTest_stadsite() {
		URL falseMultilingualSite;
		try {
			falseMultilingualSite = new URL("http://www.test.stadtsite.com");
			assertTrue(this.multilingualDetector.isInBlackList(falseMultilingualSite));
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void detectFalseMultilingualSiteTest_citycorner() {
		URL falseMultilingualSite;
		try {
			falseMultilingualSite = new URL("http://www.test.citycorner.com");
			assertTrue(this.multilingualDetector.isInBlackList(falseMultilingualSite));
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void detectFalseMultilingualSiteTest_citysite() {
		URL falseMultilingualSite;
		try {
			falseMultilingualSite = new URL("http://www.test.citysite.com");
			assertTrue(this.multilingualDetector.isInBlackList(falseMultilingualSite));
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		}
	}
}