package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.filter.LanguageFilter;
import it.uniroma3.parallel.filter.LinkValueFilter;
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

public abstract class OutlinkDetector extends MultilingualDetector {

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
			if (!element.toString().contains("com#"))
				elements.add(element);
		}
		return elements;
	}

	/***
	 * Ritorna tutti gli elementi HTML della pagina che potrebbero essere dei
	 * link uscenti dalla pagina stessa.
	 * 
	 * @param page
	 * 
	 * @return
	 */

	public HashSet<Element> getAllOutlinks(Page page) {
		HashSet<Element> elements = getHtmlElements("a", page.getDocument());
		elements.addAll(this.getHtmlElements("option", page.getDocument()));
		return elements;
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
		LanguageFilter languageFilter = new LanguageFilter();
		LinkValueFilter textValueOfLinkFilter = new LinkValueFilter();
		for (Element link : this.getAllOutlinks(page)) {
			try {
				if (textValueOfLinkFilter.filter(link.text())) {
					Page outlinkPage = new Page(link.absUrl("href"));
					if (languageFilter.filter(page, outlinkPage))
						filteredPages.add(outlinkPage);
				}
				for (Element element : link.getElementsByTag("img")) {
					if (element.hasAttr("alt"))
						if (textValueOfLinkFilter.filter(element.attr("alt"))) {
							Page outlinkPage = new Page(link.absUrl("href"));
							if (languageFilter.filter(page, outlinkPage))
								filteredPages.add(outlinkPage);
						}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return filteredPages;
	}

}
