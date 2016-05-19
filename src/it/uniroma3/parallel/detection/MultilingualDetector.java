package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.net.URL;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import it.uniroma3.parallel.model.GroupOfParallelUrls;
import it.uniroma3.parallel.model.Page;

/**
 * Classe che rappresenta un rilevatore di siti multilingua. Possiede metodi per
 * comprendere se un sito è multilingua oppure no.
 * 
 * 
 * @author davideorlando
 *
 */

public class MultilingualDetector {

	public MultilingualDetector() {
	}

	/**
	 * Rileva se il sito è un falso multilingua, attraverso l'analisi del suo
	 * URL. Sito falso multlingua vuol dire parallelo nella struttura ma non
	 * nella semantica.
	 * 
	 * @param homepageURL
	 *            la stringa che corrisponde all'URL del sito
	 * @return true se il sito è un falso multilingua, false altrimenti
	 */
	public boolean isInBlacklist(URL homepageURL) {
		return homepageURL.toString().contains("citysite.") || homepageURL.toString().contains("citycorner.")
				|| homepageURL.toString().contains("stadtsite.");
	}

	/**
	 * Si cercano nella homepage del sito passato come parametro dei link
	 * uscenti con l'attributo hreflang, euristica che ci porta a dire che il
	 * sito è multilingua. Si collezionano tutti questi URL, che rappresentano
	 * gli URL verso le pagine nelle varie lingue.
	 * 
	 * @see GroupOfParallelUrls
	 * @param homepage
	 *            la pagina che rappresenta l'homepage del sito
	 * @return GroupOfParallelUrls
	 * @throws IOException
	 */
	public GroupOfParallelUrls detectByHreflang(Page homepage) throws IOException {
		Elements linksInHomePage = homepage.getDocument().select("link[hreflang]");
		if(linksInHomePage.isEmpty())
			return null;
		GroupOfParallelUrls parallelHomepageURL = new GroupOfParallelUrls();
		parallelHomepageURL.setHomepageURL(homepage.getUrlRedirect());
		parallelHomepageURL.addURL(homepage.getUrlRedirect());
		for (Element link : linksInHomePage)
			// TODO al posto di abs:href si toglieva l'ultimo slash manualmente
			parallelHomepageURL.addURL(new URL(link.attr("abs:href")));
		return parallelHomepageURL;
	}

}
