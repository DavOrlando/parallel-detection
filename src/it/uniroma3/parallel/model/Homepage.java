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
import com.cybozu.labs.langdetect.Language;

import it.uniroma3.parallel.filter.EditDistanceFilter;
import it.uniroma3.parallel.filter.LanguageFilter;
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
	 * Seleziona solo i link che superano dei controlli sulla multilingua e
	 * sull'edit distance.
	 * 
	 * @param outlinks
	 * @return
	 */
	public List<String> getMultilingualLinks() {
		List<String> filteredOutlinks = new ArrayList<>();
		EditDistanceFilter editDistanceFilter = new EditDistanceFilter();
		LanguageFilter languageFilter = new LanguageFilter();
		for (Element link : this.getAllOutlinks()) {
			String urlString = link.absUrl("href");
			if (!filteredOutlinks.contains(urlString) && editDistanceFilter.filter(this, urlString)
					&& languageFilter.filter(this, urlString))
				filteredOutlinks.add(urlString);
		}
		return filteredOutlinks;
	}

}
