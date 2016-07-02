package it.uniroma3.parallelcorpora.filter;


import org.apache.log4j.Logger;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallelcorpora.model.Page;

/**
 * Classe che rappresenta un filtro per gli outlink. Si filtra sulla base del
 * linguaggio diverso.
 * 
 * @author davideorlando
 *
 */
public class LanguageFilter {

	private static final Logger logger = Logger.getLogger(LanguageFilter.class);

	/**
	 * Ritorna false se sono verificate le seguenti condizioni: -Il secondo link
	 * porta ad una pagina con lo stesso linguaggio della homepage;
	 * 
	 * @param homepage
	 * @param outlinkPage
	 * @return
	 */
	public boolean filter(Page homepage, Page outlinkPage) {
		return !isSameLanguage(homepage, outlinkPage);
	}

	/***
	 * Ritorna true se l'URL porta ad una pagina con lo stesso linguaggio della
	 * homepage.
	 * 
	 * @param homepage
	 * @param outlinkString
	 * @return
	 */
	private boolean isSameLanguage(Page homepage, Page outlinkPage) {
		boolean stessoLinguaggio = true;
		try {
			stessoLinguaggio = homepage.getLanguage().equals(outlinkPage.getLanguage());
		} catch (LangDetectException e) {
			logger.error(e + "\nImpossible to verify language in: " + homepage.getURLString() +" or in "+outlinkPage.getURLString());
		}
		return stessoLinguaggio;
	}
}
