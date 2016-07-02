package it.uniroma3.detection;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallelcorpora.detection.OutlinkMultilingualDetector;
import it.uniroma3.parallelcorpora.filter.LinkTextFilter;
import it.uniroma3.parallelcorpora.model.Page;
import it.uniroma3.parallelcorpora.model.ParallelPages;

public class OutlinkDetectorImplForTest extends OutlinkMultilingualDetector {

	private static final CharSequence COM_HASH = "com#";

	@Override
	public ParallelPages detect(Page page)
			throws IOException, InterruptedException, LangDetectException, URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void organizeInPairs(ParallelPages parallelPage) throws LangDetectException {
		// TODO Auto-generated method stub

	}

	/***
	 * Ritorna un insieme di elementi HTML presenti nella pagina e che
	 * corrispondono al tag elementName passato per parametro.
	 * 
	 * @param elementName
	 *            nome dell'elemento HTML da cercare nella pagina.
	 * @param document
	 * @return
	 */
	@Override
	public HashSet<Element> getHtmlElements(String elementName, Document document) {
		HashSet<Element> elements = new HashSet<Element>();
		for (Element element : document.select(elementName)) {
			if (!element.toString().contains(COM_HASH))
				elements.add(element);
		}
		return elements;
	}
	
	/**
	 * Ritorna true se il testo all'interno dell'ancora contiene una delle
	 * parole che ci fanno cambiare la lingua del sito.
	 * 
	 * @param link
	 * @param page
	 * @return
	 */
	public boolean checkAnchorText(Element link, Page page) {
		return new LinkTextFilter().filter(link.text());
	}
	

	/**
	 * Ritorna true se nel valore dell'attributo alt delle immagini, che in
	 * realtà sono ancora per pagine esterne, è presente una delle parole che ci
	 * permettono di cambiare sito.
	 * 
	 * @param link
	 * @param page
	 * @return
	 */
	public boolean checkAltAttributes(Element link){
		boolean isGood = false;
		for (Iterator<Element> iterator = link.getElementsByTag("img").iterator(); !isGood && iterator.hasNext();) {
			Element element = iterator.next();
			if (element.hasAttr("alt"))
				isGood = new LinkTextFilter().filter(element.attr("alt"));
		}
		return isGood;
	}
	
}
