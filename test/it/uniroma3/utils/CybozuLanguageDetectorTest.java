package it.uniroma3.utils;

import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Test;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallelcorpora.model.Page;
import it.uniroma3.parallelcorpora.utils.CybozuLanguageDetector;

public class CybozuLanguageDetectorTest {

	private static final String HOME_IT = "http://localhost:8080/test/homeIt.html";
	private static final String HOME_EN = "http://localhost:8080/test/homeEn.html";
	private Page pageIt;
	private Page pageEn;

	@Before
	public void setUp() throws Exception {
		pageIt = new Page(HOME_IT);
		pageEn = new Page(HOME_EN);
	}

	@Test
	public void detectItalianPage_test() {
		try {
			assertEquals("it", CybozuLanguageDetector.getInstance().detect(pageIt.getDocument()));
		} catch (LangDetectException e) {
			e.printStackTrace();
			fail();
		}
	}
	

	@Test
	public void detectEnglishPage_test() {
		try {
			assertEquals("en", CybozuLanguageDetector.getInstance().detect(pageEn.getDocument()));
		} catch (LangDetectException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	//altri metodi testati nel filtro di linguaggio
}
