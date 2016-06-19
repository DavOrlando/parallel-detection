package it.uniroma3.parallel.filter;

import java.io.IOException;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PreHomepage;

public class PreHomepageFilter implements Filter{
	
	private static final int MAX_NUMBER_LINKS_FOR_PREHOMEPAGE = 16;

	public boolean filter(Page page, String outlinkString) {
		PreHomepage preHomepage = (PreHomepage) page;
		boolean isPreHomepage = false;
		try {
			int numberOfLinks = preHomepage.getAllOutlinks().size();
			isPreHomepage = numberOfLinks < MAX_NUMBER_LINKS_FOR_PREHOMEPAGE &&
									preHomepage.getLanguages().size()<numberOfLinks/1.3;
		} catch (LangDetectException | IOException e) {
			e.printStackTrace();
		}
		return isPreHomepage;
	}

}
