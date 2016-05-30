package it.uniroma3.parallel.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.filter.OutlinkFilter;
import it.uniroma3.parallel.utils.UrlUtil;

public class Homepage extends Page {
	
	/***
	 * Costruttore parametrico rispetto a una stringa che è la rappresentazione
	 * dell'URL dell'homepage.
	 * 
	 * @param homepageStringUrl
	 * @throws IOException 
	 */
	public Homepage(String homepageStringUrl) throws IOException {
		super(new URL(UrlUtil.addHttp(homepageStringUrl)));
	}

	/***
	 * 
	 * Costruttore parametrico rispetto all' URL. Si connette all'URL tramite
	 * Jsoup per prendere le informazioni utili per l'inizializzazione dello
	 * stato dell'oggetto
	 * 
	 * @param url
	 *            String nel formato "http://www.dominio.com/risorsa/"
	 * @throws IOException
	 */
	public Homepage(URL url) throws IOException {
		super(url);
	}

	/**
	 * Ritorna una lista di stringhe (gli outlink) che soddisfano le condizioni
	 * sull'edit distance e portano a pagine con una lingua differente.
	 * 
	 * @param homepage
	 * @return
	 * @throws LangDetectException
	 * @throws IOException 
	 */
	public List<String> getMultilingualOutlinks() throws LangDetectException, IOException {
		Set<Element> outlinks = new HashSet<Element>(this.getAllOutlinks());
		List<String> filteredOutlinks = new ArrayList<>();
		OutlinkFilter outlinkFilter = new OutlinkFilter();
		for (Element link : outlinks) {
			String urlString = link.absUrl("href");
			if (outlinkFilter.filter(this, urlString))
				filteredOutlinks.add(urlString);
		}
		return filteredOutlinks;
	}

	/***
	 * Ritorna tutti gli elementi HTML della pagina che potrebbero essere dei
	 * link uscenti dalla pagina stessa.
	 * 
	 * @return
	 */

	private List<Element> getAllOutlinks() {
		List<Element> elements = getHtmlElements("a");
		elements.addAll(this.getHtmlElements("option"));
		return elements;
	}

	/***
	 * Ritorna una lista di elementi HTML presenti nella pagina e che
	 * corrispondono al tag elementName passato per parametro.
	 * 
	 * @param elementName
	 *            nome dell'elemento HTML da cercare nella pagina.
	 * @return
	 */
	private List<Element> getHtmlElements(String elementName) {
		ArrayList<Element> elements = new ArrayList<Element>();
		for (Element element : this.getDocument().select(elementName)) {
			elements.add(element);
		}
		return elements;
	}


}
