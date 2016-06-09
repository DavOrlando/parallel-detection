package it.uniroma3.parallel.filter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.Page;

/**
 * Classe che rappresenta un filtro per gli outlink. Si filtra sulla base
 * dell'edit distance.
 * 
 * @author davideorlando
 *
 */
public class EditDistanceFilter implements Filter {

	/**
	 * Ritorna true se sono verificate le seguenti condizioni: -Gli URL non sono
	 * uguali; -L'edit distance è ragionevole;
	 * 
	 * @param homepage
	 * @param outlinkString
	 * @return
	 * @throws LangDetectException
	 * @throws IOException
	 */
	@Override
	public boolean filter(Page homepage, String outlinkString) {
		try {
			return !homepage.getURLString().equals(outlinkString) && isEditDistanceFine(homepage, outlinkString);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
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
		return ((outlinkURLLength >= homepageURLLength)
				&& (levenshteinDistance < outlinkURLLength - homepageURLLength + 4
						|| levenshteinDistance < outlinkURLLength - domainLength + 4)
				&& (outlinkURLLength < homepageURLLength * 2.7))
				|| ((outlinkURLLength <= homepageURLLength) && (levenshteinDistance < outlinkURLLength / 2));
	}


}