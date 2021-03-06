package it.uniroma3.parallelcorpora.detection;

import java.io.IOException;
import java.net.URISyntaxException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallelcorpora.model.Page;
import it.uniroma3.parallelcorpora.model.ParallelPages;
import it.uniroma3.parallelcorpora.utils.UrlUtils;

/**
 * Classe che rappresenta la prima l'euristica fa la ricerca degli attributi
 * hreflang nei link.
 * 
 * 
 * @author davideorlando
 * @see MultilingualDetector
 * @see Detector
 */
public class HreflangMultilingualDetector extends MultilingualDetector {

	private static final String HREFLANG = "hreflang";
	private static final String X_DEFAULT = "x-default";
	private static final String LINK_HREFLANG = "link[hreflang]";

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
		Elements linksInHomePage = page.getDocument().select(LINK_HREFLANG);
		if (linksInHomePage.isEmpty())
			return null;
		ParallelPages groupOfHomepages = new ParallelPages(page);
		for (Element link : linksInHomePage) {
			if (!link.attr(HREFLANG).equals(X_DEFAULT))
				groupOfHomepages.addCandidateParallelHomepage(UrlUtils.getInstance().getAbsoluteURL(link));
		}
		return groupOfHomepages;
	}

}
