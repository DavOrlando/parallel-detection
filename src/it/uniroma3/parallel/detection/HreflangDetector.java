package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.GroupOfHomepages;
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
	 * @throws LangDetectException 
	 */
	@Override
	public GroupOfHomepages detect(Homepage homepage) throws IOException, LangDetectException {
		Elements linksInHomePage = homepage.getDocument().select("link[hreflang]");
		if (linksInHomePage.isEmpty())
			return null;
		GroupOfHomepages groupOfHomepages = new GroupOfHomepages(homepage);
		for (Element link : linksInHomePage)
			groupOfHomepages.addCandidateHomepage(new URL(link.attr("abs:href")));
		return groupOfHomepages;
	}

}
