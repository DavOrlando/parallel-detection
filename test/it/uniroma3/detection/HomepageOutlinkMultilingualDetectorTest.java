package it.uniroma3.detection;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.configuration.ConfigurationProperties;
import it.uniroma3.parallel.detection.HomepageOutlinkMultilingualDetector;
import it.uniroma3.parallel.model.Page;

public class HomepageOutlinkMultilingualDetectorTest {


	private URL urlSite;
	private HomepageOutlinkMultilingualDetector homepageOutlinkDetector;
	private Page page;

	@Before
	public void setUp() throws Exception {
		this.urlSite = new URL("http://localhost:8080/testMinimale/homeIt.html");
		this.page = new Page(urlSite);
		homepageOutlinkDetector = new HomepageOutlinkMultilingualDetector();
	}


}
