package featureTest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import it.model.Site;
import it.multilingualDetection.M2ltilingualSite;
import it.multilingualDetection.MultilingualDetector;

public class DetectFalseMultilingualSiteTest {
	
	private MultilingualDetector multilingualDetector;
	
	@Before
	public void setUp(){
		multilingualDetector = new MultilingualDetector();
	}
	
	
	@Test
	public void detectFalseMultilingualSiteTest_stadsite() {
		String falseMultilingualSite = "www.test.stadtsite.com";
		assertTrue(this.multilingualDetector.detectFalseMultilingualSite(falseMultilingualSite));
	}
	
	@Test
	public void detectFalseMultilingualSiteTest_citycorner() {
		String falseMultilingualSite = "www.test.citycorner.com";
		assertTrue(this.multilingualDetector.detectFalseMultilingualSite(falseMultilingualSite));
	}
	@Test
	public void detectFalseMultilingualSiteTest_citysite() {
		String falseMultilingualSite = "www.test.citysite.com";
		assertTrue(this.multilingualDetector.detectFalseMultilingualSite(falseMultilingualSite));
	}
}
