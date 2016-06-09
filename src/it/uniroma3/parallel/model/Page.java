package it.uniroma3.parallel.model;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.utils.CybozuLanguageDetector;
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
	private String language;

	public Page() {
	}

	/***
	 * Costruttore parametrico rispetto a una stringa che è la rappresentazione
	 * dell'URL dell'homepage.
	 * 
	 * @param homepageStringUrl
	 * @throws IOException
	 */
	public Page(String homepageStringUrl) throws IOException {
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
	public Page(URL url) throws IOException {
		this.url = url;
		// get del document della pagina
		this.document = Jsoup.connect(url.toString()).userAgent(USER_AGENT).timeout(8000).get();
		// se c'è il redirect ci prendiamo l'URL finale.
		this.urlRedirect = new URL(this.document.location());

	}

	public URL getUrl() {
		return url;
	}

	public URL getUrlRedirect() {
		return urlRedirect;
	}

	public Document getDocument() {
		return document;
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
	public String getLanguage() throws LangDetectException {
		if (this.language == null) {
			this.language = CybozuLanguageDetector.getInstance().detect(this.getDocument());
		}
		return language;
	}

	/***
	 * Ritorna tutti gli elementi HTML della pagina che potrebbero essere dei
	 * link uscenti dalla pagina stessa.
	 * 
	 * @return
	 */

	protected HashSet<Element> getAllOutlinks() {
		HashSet<Element> elements = getHtmlElements("a");
		elements.addAll(this.getHtmlElements("option"));
		return elements;
	}

	/***
	 * Ritorna un insieme di elementi HTML presenti nella pagina e che
	 * corrispondono al tag elementName passato per parametro.
	 * 
	 * @param elementName
	 *            nome dell'elemento HTML da cercare nella pagina.
	 * @return
	 */
	private HashSet<Element> getHtmlElements(String elementName) {
		HashSet<Element> elements = new HashSet<Element>();
		for (Element element : this.getDocument().select(elementName)) {
			elements.add(element);
		}
		return elements;
	}

}
