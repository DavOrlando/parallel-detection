package featureTest;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.utils.CybozuLanguageDetector;

public class DetectLanguageTest {

	private Page pageNederlands;
	private Page pageDe;

	/*
	 * prova.html è http://www.bulthaup.nl/ in locale, per mostrare che è il
	 * servizio a non essere deterministico e non il server ad inviarci due
	 * pagine diverse.
	 */
	@Before
	public void setUp() throws Exception {
		pageNederlands = new Page("http://localhost:8080/test/prova.html");
		pageDe = new Page("http://www.bulthaup.de/");
	}

	// Fai partire più volte questo test. Il servizio non è affidabile a quanto
	// pare...
	@Test
	public void detectNLtest() throws LangDetectException, IOException {
		assertEquals("nl", CybozuLanguageDetector.getInstance().detect(pageNederlands.getDocument()));
	}

	@Test
	public void detectDEtest() throws LangDetectException {
		assertEquals("de", CybozuLanguageDetector.getInstance().detect(pageDe.getDocument()));
	}
}
