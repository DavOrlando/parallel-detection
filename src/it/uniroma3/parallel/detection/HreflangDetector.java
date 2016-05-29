package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.net.URL;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import it.uniroma3.parallel.model.GroupOfParallelUrls;
import it.uniroma3.parallel.model.Homepage;

/**
 * Classe che rappresenta un rilevatore di siti multilingua attraverso
 * l'euristica della ricerca degli attributi hreflang nei link.
 * 
 * 
 * @author davideorlando
 * @see MultilingualDetector
 * @see Detector
 */
public class HreflangDetector extends MultilingualDetector{

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
	@Override
	public GroupOfParallelUrls detect(Homepage homepage) throws IOException {
		Elements linksInHomePage = homepage.getDocument().select("link[hreflang]");
		if (linksInHomePage.isEmpty())
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
