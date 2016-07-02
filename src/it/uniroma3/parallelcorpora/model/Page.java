package it.uniroma3.parallelcorpora.model;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallelcorpora.configuration.ConfigurationProperties;
import it.uniroma3.parallelcorpora.utils.CybozuLanguageDetector;
import it.uniroma3.parallelcorpora.utils.UrlUtils;

/**
 * Classe che rappresenta il concetto di pagina. Ovvero possiede i dati che sono
 * di interesse per il Sistema e che riguardano una pagina.
 * 
 * @author davideorlando
 *
 */

public class Page {

	private static final int TIMEOUT = 8000;
	private URL url;
	private URL urlRedirect;
	private Document document;
	private String language;
	private String pageName;

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
		this(new URL(UrlUtils.getInstance().addHttp(homepageStringUrl)));
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
		this.document = Jsoup.connect(url.toString())
				.userAgent(ConfigurationProperties.getInstance().getStringOfUserAgent()).timeout(TIMEOUT).get();
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
	public String getPageName() {
		if (pageName == null)
			this.pageName = this.getUrlRedirect().toString().replace("http://", "").replaceAll(":", "")
					.replaceAll("/", "").replace("https://", "").replaceAll("/?", "");
		return pageName;
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

	@Override
	public int hashCode() {
		return this.getURLString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		Page page = (Page) obj;
		return this.getURLString().equals(page.getURLString());
	}

	@Override
	public String toString() {
		return "Pagina relativa all' URL: " + this.getURLString();
	}

}
