package it.uniroma3.parallel.filter;

import java.io.IOException;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PreHomepage;

/**
 * Filtro che riconosce se una pagina è una prehomepage. Secondo l'euristica
 * decisa dall'autore.
 * 
 * @author davideorlando
 *
 */
public class PreHomepageFilter implements Filter {

	private static final int MAX_NUMBER_LINKS_FOR_PREHOMEPAGE = 16;

	public boolean filter(Page page, Page outlinkPage) {
		PreHomepage preHomepage = (PreHomepage) page;
		boolean isPreHomepage = false;
		try {
			int numberOfLinks = preHomepage.getAllOutlinks().size();
			isPreHomepage = numberOfLinks < MAX_NUMBER_LINKS_FOR_PREHOMEPAGE
					&& preHomepage.getLanguagesOfOutlinks().size() >= (int)( numberOfLinks / 1.3);
		} catch (LangDetectException | IOException e) {
			e.printStackTrace();
		}
		return isPreHomepage;
	}

}
