package classTest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Before;
import org.junit.Test;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.detection.HomepageOutlinkDetector;
import it.uniroma3.parallel.detection.OutlinkDetector;
import it.uniroma3.parallel.model.Homepage;

public class HomepageOutlinkDetectorTest {

	private Homepage homepage;
	private OutlinkDetector homepageOutlinkDetector;

	@Before
	public void setUp() throws Exception {
		this.homepage = new Homepage("www.toyota.com");
		this.homepageOutlinkDetector = new HomepageOutlinkDetector(new ReentrantLock());
	}

	@Test
	public void testDetect() {
		try {
			this.homepageOutlinkDetector.detect(homepage);
		} catch (IOException | InterruptedException | LangDetectException e) {
			e.printStackTrace();
			fail();
		}
	}

}
