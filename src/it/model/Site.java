package it.model;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import it.utils.UrlHelper;

/**
 * Classe che rappresenta il concetto di sito. Ovvero possiede i dati che sono
 * di interesse per il sistema e che riguardano un sito.
 * 
 * @author davideorlando
 *
 */

public class Site {

	String url;
	String urlRedirect;
	String urlRoot;
	Document document;

	public Site() {
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
	public Site(String url) throws IOException {
		this();
		// get del sito
		Document document = Jsoup.connect(url).userAgent("Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1")
				.timeout(8000).get();
		// Stringa dove salverò il redirect eventuale del sito, quindi url che
		// effettivamente vado ad analizzare
		String redirectSite = (document.location());

		// redirectSite less last "/" to include some cases
		// (http://www.ferrari.com/choose-your-country,
		// http://www.ferrari.com/(en-en))
		// per confrontare sempre con root del sito, per misurare edit distance
		// anche dal site iniziale e non solo dal redirect
		String urlRoot = UrlHelper.getUrlRoot(redirectSite);

		// inizializzo lo stato del sito
		this.url = url;
		this.urlRedirect = redirectSite;
		this.urlRoot = urlRoot;
		this.document = document;
	}

	public String getUrlRoot() {
		return urlRoot;
	}

	public void setUrlRoot(String urlRoot) {
		this.urlRoot = urlRoot;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrlRedirect() {
		return urlRedirect;
	}

	public void setUrlRedirect(String urlRedirect) {
		this.urlRedirect = urlRedirect;
	}

}
