package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import it.uniroma3.parallel.filter.LanguageFilter;
import it.uniroma3.parallel.filter.LinkValueFilter;
import it.uniroma3.parallel.model.Page;

/**
 * Classe che rappresenta un rilevatore di siti multilingua attraverso
 * l'euristica della ricerca fra gli outlink di pagine parallele e multilingua.
 * 
 * 
 * @author davideorlando
 *
 */

public abstract class OutlinkDetector extends MultilingualDetector {


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
	 * @param page 
	 * 
	 * @return
	 */

	public HashSet<Element> getAllOutlinks(Page page) {
		HashSet<Element> elements = getHtmlElements("a",page.getDocument());
		elements.addAll(this.getHtmlElements("option",page.getDocument()));
		return elements;
	}
	
	/**
	 * Seleziona solo le pagine che superano i controlli sui vari filtri.
	 * 
	 * @param page
	 * @return
	 * @throws IOException
	 */
	public List<Page> getMultilingualPage(Page page) {
		List<Page> filteredPages = new ArrayList<>();
		LanguageFilter languageFilter = new LanguageFilter();
		LinkValueFilter linkValueFilter = new LinkValueFilter();
		for (Element link : this.getAllOutlinks(page)) {
			try {
				Page outlinkPage;
				if (linkValueFilter.filter(link.text().toLowerCase())) {
					outlinkPage = new Page(link.absUrl("href"));
					if (languageFilter.filter(page, outlinkPage))
						filteredPages.add(outlinkPage);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return filteredPages;
	}
	
	
	
}
