package it.uniroma3.parallel.detection;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Before;
import org.junit.Test;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.Page;

public class HomepageOutlinkDetectorTest {

	private Page homepage;
	private OutlinkDetector homepageOutlinkDetector;

	@Before
	public void setUp() throws Exception {
		this.homepage = new Page("http://localhost:8080/testSenzaHreflang/homeIt.html");
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
