package it.model;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Classe che rappresenta il concetto di sito. Ovvero possiede i dati che sono
 * di interesse per il sistema e che riguardano un sito.
 * 
 * @author davideorlando
 *
 */

public class Page {

	private URL url;
	private URL urlRedirect;
	private Document document;

	public Page() {
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
		this();
		this.url = url;
		// get del sito
		Document document = Jsoup.connect(url.toString()).userAgent("Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1")
				.timeout(8000).get();
		this.document = document;
		// Stringa dove salverò il redirect eventuale del sito, quindi url che
		// effettivamente vado ad analizzare
		this.urlRedirect = new URL(document.location());
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
}
