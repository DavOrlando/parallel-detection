package it.uniroma3.parallel.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.utils.UrlUtil;

/**
 * Classe che rappresenta il concetto di pagina. Ovvero possiede i dati che sono
 * di interesse per il Sistema e che riguardano una pagina.
 * 
 * @author davideorlando
 *
 */

public class Page {

	private static final String USER_AGENT = "Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1";
	private URL url;
	private URL urlRedirect;
	private Document document;
	private String localPath;

	public Page() {
	}

	/***
	 * Costruttore parametrico rispetto a una stringa che è la rappresentazione
	 * dell'URL dell'homepage.
	 * 
	 * @param homepageStringUrl
	 * @throws MalformedURLException
	 */
	public Page(String homepageStringUrl) throws MalformedURLException {
		this(new URL(UrlUtil.addHttp(homepageStringUrl)));
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
	public Page(URL url) {
		this.url = url;
		try {
			// get del document della pagina
			this.document = Jsoup.connect(url.toString()).userAgent(USER_AGENT).timeout(8000).get();
			// se c'è il redirect ci prendiamo l'URL finale.
			this.urlRedirect = new URL(this.document.location());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public URL getUrlRedirect() {
		return urlRedirect;
	}

	public void setUrlRedirect(URL urlRedirect) {
		this.urlRedirect = urlRedirect;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	/**
	 * Ritorna se presente il percorso in locale della pagina.
	 * 
	 * @return
	 */
	public String getLocalPath() {
		return this.localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	/**
	 * Ritorna la stringa che rappresenta l'URL della pagina.
	 * 
	 * @return
	 */
	public String getURLString() {
		return this.getUrlRedirect().toString();
	}

	/**
	 * Ritorna la root dell'URL della pagina sottoforma di stringa. Esempio:
	 * "http://www.test.com/test" diventa "http://www.test.com". Serve per
	 * confrontare sempre con root del sito, per misurare edit distance anche
	 * dal site iniziale e non solo dal redirect
	 * 
	 * @return urlRoot
	 */

	public String getDomain() {
		return this.urlRedirect.getProtocol() + "://" + this.urlRedirect.getAuthority();
	}

	/**
	 * Ritorna l'URL sotto forma di stringa senza "http://" o '/' o "/?" o
	 * "https://" utile per creare il nome di una cartella.
	 * 
	 * @return
	 */
	public String getNameFolder() {
		return this.getUrlRedirect().toString().replace("http://", "").replaceAll(":", "").replaceAll("/", "")
				.replace("https://", "").replaceAll("/?", "");
	}

	/***
	 * Ritorna una stringa che rappresenta il linguaggio della pagina.
	 * 
	 * @return language
	 * @throws LangDetectException
	 */
	// TODO selezionare gli elementi per fare lang detection : parametrico
	// con una lista specificata in un file di properties
	public String getLanguage() throws LangDetectException {
		// caricamento dei profili per la language detection
		if (DetectorFactory.getLangList().size() == 0)
			DetectorFactory.loadProfile("profiles.sm");
		Detector detector = DetectorFactory.create();
		String paragraphHtmlDocument = getStringElement("p");
		if (paragraphHtmlDocument.length() == 0) {
			detector.append(this.getDocument().text());
		} else
			detector.append(paragraphHtmlDocument);
		return detector.detect();
	}

	/***
	 * Ritorna una stringa con il contenuto degli elementi della pagina HTML che
	 * si chiamano elementName.
	 * 
	 * @param elementName
	 *            nome del parametro HTML
	 * @return
	 */
	private String getStringElement(String elementName) {
		Elements paragraphHtmlDocument = this.getDocument().select(elementName);
		return paragraphHtmlDocument.text();
	}

	/***
	 * Ritorna una lista di elementi HTML presenti nella pagina e che
	 * corrispondono al tag elementName passato per parametro.
	 * 
	 * @param elementName
	 *            nome dell'elemento HTML da cercare nella pagina.
	 * @return
	 */
	public List<Element> getHtmlElements(String elementName) {
		ArrayList<Element> elements = new ArrayList<Element>();
		for (Element element : this.getDocument().select(elementName)) {
			elements.add(element);
		}
		return elements;
	}

	/***
	 * Ritorna tutti gli elementi HTML della pagina che potrebbero essere dei
	 * link uscenti dalla pagina stessa.
	 * 
	 * @return
	 */

	public List<Element> getAllOutlinks() {
		List<Element> elements = getHtmlElements("a");
		elements.addAll(this.getHtmlElements("option"));
		return elements;
	}
}
