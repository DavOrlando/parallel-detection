package it.uniroma3.parallel.model;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import com.cybozu.labs.langdetect.LangDetectException;

/**
 * Classe che rappresenta un filtro per gli outlink. Si filtra sulla base
 * dell'edit distance e sulla verifica del linguaggio diverso.
 * 
 * @author davideorlando
 *
 */
public class OutlinkFilter {

	/**
	 * Ritorna true se sono verificate le seguenti condizioni: -Gli URL non sono
	 * uguali; -L'edit distance è ragionevole; -Il secondo link non porta ad una
	 * pagina con lo stesso linguaggio della homepage;
	 * 
	 * @param homepage
	 * @param outlinkString
	 * @return
	 * @throws LangDetectException
	 * @throws MalformedURLException
	 */
	public boolean filter(Page homepage, String outlinkString) throws LangDetectException, MalformedURLException {
		return !homepage.getURLString().equals(outlinkString) && isEditDistanceFine(homepage, outlinkString)
				&& !isSameLanguage(homepage, outlinkString);
	}

	/***
	 * Ritorna true se l'edit distance è ragionevole.
	 * 
	 * @param homepage
	 * @param outlinkString
	 * @param domainLength
	 * @return
	 */
	private boolean isEditDistanceFine(Page homepage, String outlinkString) {
		int domainLength = homepage.getDomain().length();
		int homepageURLLength = homepage.getURLString().length();
		int outlinkURLLength = outlinkString.length();
		int levenshteinDistance = StringUtils.getLevenshteinDistance(homepage.getURLString(), outlinkString);
		return ((outlinkURLLength >= homepageURLLength) &&
				(levenshteinDistance < outlinkURLLength - homepageURLLength + 4 ||
				 levenshteinDistance < outlinkURLLength - domainLength + 4) &&
				 (outlinkURLLength < homepageURLLength * 2.7)) ||
				((outlinkURLLength <= homepageURLLength) &&
				 (levenshteinDistance < outlinkURLLength / 2));
	}

	/***
	 * Ritorna true se l'URL porta ad una pagina con lo stesso linguaggio della
	 * homepage.
	 * 
	 * @param homepage
	 * @param outlinkString
	 * @return
	 * @throws LangDetectException
	 * @throws MalformedURLException
	 */
	private boolean isSameLanguage(Page homepage, String outlinkString)
			throws LangDetectException, MalformedURLException {
		Page differentLanguagePage = new Page(new URL(outlinkString));
		return homepage.getLanguage().equals(differentLanguagePage.getLanguage());
	}
}
