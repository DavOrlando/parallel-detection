package it.uniroma3.parallel.detection;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.Page;

/**
 * Classe che rappresenta un filtro adatto all'euristiche che prevedono la
 * visita degli outlink. Infatti lascia passare gli outlink della pagina che
 * rispettano le condizioni sull'edit distance e che portano a pagine con una
 * lingua differente rispetto alla lingua della homepage.
 * 
 * @author davideorlando
 *
 */
public class LevenshteinAndLanguageFilter {
	/**
	 * Ritorna una lista di stringhe (gli outlink) che soddisfano le condizioni
	 * sull'edit distance e portano a pagine con una lingua differente.
	 * 
	 * @param homepage
	 * @return
	 * @throws MalformedURLException
	 * @throws LangDetectException
	 */
	public List<String> doFilter(Page homepage) throws MalformedURLException, LangDetectException {
		Set<Element> outlinks = new HashSet<Element>(homepage.getAllOutlinks());
		List<String> filteredOutlinks = new ArrayList<>();
		for (Element link : outlinks) {
			String url = link.absUrl("href");
			if (soddisfaCondizioni(homepage, url))
				filteredOutlinks.add(url);
		}
		return filteredOutlinks;
	}

	/**
	 * Ritorna true se sono verificate le seguenti condizioni: -Gli URL non sono
	 * uguali; -L'edit distance è ragionevole; -Il secondo link non porta ad una
	 * pagina con lo stesso linguaggio della homepage;
	 * 
	 * @param homepage
	 * @param url
	 * @return
	 * @throws LangDetectException
	 * @throws MalformedURLException
	 */
	private boolean soddisfaCondizioni(Page homepage, String url) throws LangDetectException, MalformedURLException {
		return !homepage.getUrlRedirect().toString().equals(url)
				&& (isEditDistanceFine(homepage.getUrlRedirect().toString(), url, homepage.getDomain().length()))
				&& (!isSameLanguage(homepage.getLanguage(), new URL(url)));
	}

	/***
	 * Ritorna true se l'edit distance è ragionevole.
	 * 
	 * @param homepage
	 * @param linkPossible
	 * @param domainLength
	 * @return
	 */
	public boolean isEditDistanceFine(String homepage, String linkPossible, int domainLength) {
		int lengthURLHomepage = homepage.length();
		int linkLength = linkPossible.length();
		return ((linkLength >= lengthURLHomepage)
				&& (StringUtils.getLevenshteinDistance(homepage, linkPossible) < linkLength - lengthURLHomepage + 4
						|| StringUtils.getLevenshteinDistance(homepage, linkPossible) < linkLength - domainLength + 4)
				&& (linkLength < lengthURLHomepage * 2.7))
				|| ((linkLength <= lengthURLHomepage)
						&& (StringUtils.getLevenshteinDistance(homepage, linkPossible) < linkLength / 2));
	}

	/***
	 * Ritorna true se l'URL porta ad una pagina con lo stesso linguaggio della homepage.
	 * @param homepageLanguage
	 * @param differentLanguageURL
	 * @return
	 * @throws LangDetectException
	 */
	public boolean isSameLanguage(String homepageLanguage, URL differentLanguageURL) throws LangDetectException {
		Page differentLanguagePage = new Page(differentLanguageURL);
		return homepageLanguage.equals(differentLanguagePage.getLanguage());
	}
}
