package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.utils.UrlUtils;
import it.uniroma3.parallel.model.Page;

/**
 * Classe che rappresenta la prima l'euristica fa la ricerca degli attributi
 * hreflang nei link.
 * 
 * 
 * @author davideorlando
 * @see MultilingualDetector
 * @see Detector
 */
public class HreflangDetector extends MultilingualDetector {

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
	 * @throws URISyntaxException
	 */
	@Override
	public ParallelPages detect(Page page) throws IOException, LangDetectException, URISyntaxException {
		Elements linksInHomePage = page.getDocument().select("link[hreflang]");
		if (linksInHomePage.isEmpty())
			return null;
		ParallelPages groupOfHomepages = new ParallelPages(page);
		for (Element link : linksInHomePage){
			groupOfHomepages.addCandidateParallelHomepage(UrlUtils.getInstance().getAbsoluteURL(link));
		}
		return groupOfHomepages;
	}
 

}
