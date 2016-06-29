package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.configuration.ConfigurationProperties;
import it.uniroma3.parallel.filter.LanguageFilter;
import it.uniroma3.parallel.filter.LinkTextFilter;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.ParallelPages;

/**
 * Classe che rappresenta un rilevatore di siti multilingua attraverso
 * l'euristica della ricerca fra gli outlink di pagine parallele e multilingua.
 * 
 * 
 * @author davideorlando
 *
 */

public abstract class OutlinkMultilingualDetector extends MultilingualDetector {

	private static final String COM_HASH = "com#";
	private static final Logger logger = Logger.getLogger(OutlinkMultilingualDetector.class);

	public abstract void organizeInPairs(ParallelPages parallelPage) throws LangDetectException;

	/***
	 * Ritorna un insieme di elementi HTML presenti nella pagina e che
	 * corrispondono al tag elementName passato per parametro.
	 * 
	 * @param elementName
	 *            nome dell'elemento HTML da cercare nella pagina.
	 * @param document
	 * @return
	 */
	protected HashSet<Element> getHtmlElements(String elementName, Document document) {
		HashSet<Element> elements = new HashSet<Element>();
		for (Element element : document.select(elementName)) {
			if (!element.toString().contains(COM_HASH))
				elements.add(element);
		}
		return elements;
	}

	/***
	 * Ritorna tutti gli elementi HTML della pagina che potrebbero essere dei
	 * link uscenti dalla pagina stessa, cercandoli fra alcuni tag decisi da
	 * configurazione.
	 * 
	 * @param page
	 * 
	 * @return
	 */

	public HashSet<Element> getAllOutlinks(Page page) {
		HashSet<Element> elements = new HashSet<>();
		for (String tagName : ConfigurationProperties.getInstance().getStringOfTagName()) {
			elements.addAll(this.getHtmlElements(tagName, page.getDocument()));
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
	private boolean checkAnchorText(Element link) {
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
	private boolean checkAltAttributes(Element link) {
		boolean isGood = false;
		for (Iterator<Element> iterator = link.getElementsByTag("img").iterator(); !isGood && iterator.hasNext();) {
			Element element = iterator.next();
			if (element.hasAttr("alt"))
				isGood = new LinkTextFilter().filter(element.attr("alt"));
		}
		return isGood;
	}

	/**
	 * Controlla il linguaggio delle due pagine e ritorna true se è diverso.
	 * 
	 * @param page
	 * @param outlinkPage
	 * @return
	 */
	private boolean checkLanguages(Page page, Page outlinkPage) {
		return new LanguageFilter().filter(page, outlinkPage);
	}

	/**
	 * Seleziona solo le pagine che superano i controlli sui vari filtri. In
	 * questo caso i filtri sono il linguaggio differente e la presenza di testo
	 * come 'english', 'en', ecc...
	 * 
	 * @param page
	 * @return
	 * @throws IOException
	 */
	public List<Page> getMultilingualPage(Page page) {
		List<Page> filteredPages = new LinkedList<>();
		for (Element link : this.getAllOutlinks(page)) {
			try {
				if (checkAnchorText(link) || checkAltAttributes(link)) {
					Page outlinkPage = new Page(link.absUrl("href"));
					if (checkLanguages(page, outlinkPage))
						filteredPages.add(outlinkPage);
				}
			} catch (Exception e) {
				logger.error(e.toString());
			}
		}
		return filteredPages;
	}

}
