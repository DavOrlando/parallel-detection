package featureTest;

import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Test;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.utils.CybozuLanguageDetector;

public class DetectLanguageTest {

	private Page pageNederlands;

	@Before
	public void setUp() throws Exception {
		pageNederlands = new Page("http://localhost:8080/testForPreHomepageMultilingualDetection/noOutlink.html");
	}

	@Test
	public void detectNLtest() {
		try {
			assertEquals("nl", CybozuLanguageDetector.getInstance().detect(pageNederlands.getDocument()));
		} catch (LangDetectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
