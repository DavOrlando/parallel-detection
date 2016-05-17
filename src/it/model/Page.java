package it.model;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

/**
 * Classe che rappresenta il concetto di sito. Ovvero possiede i dati che sono
 * di interesse per il sistema e che riguardano un sito.
 * 
 * @author davideorlando
 *
 */

public class Page {

	private static final String USER_AGENT = "Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1";
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
	public Page(URL url) {
		this();
		this.url = url;
		// get del sito
		Document document;
		try {
			document = Jsoup.connect(url.toString()).userAgent(USER_AGENT)
					.timeout(8000).get();
			this.document = document;
			this.urlRedirect = new URL(document.location());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Stringa dove salverò il redirect eventuale del sito, quindi url che
		// effettivamente vado ad analizzare
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
	
	// seleziono i paragrafi per fare lang detection
	// TODO selezionare gli elementi per fare lang detection : parametrico
	// con una lista specificata in un file di properties
	public String getLanguage() throws LangDetectException{
			Elements paragraphHtmlDocument = this.getDocument().select("p");
			String stringParagraphHtmlDocument = paragraphHtmlDocument.text();

			// caricamento dei profili per la language detection
			if (DetectorFactory.getLangList().size() == 0)
				DetectorFactory.loadProfile("profiles.sm");

			Detector detector = DetectorFactory.create();
			if (paragraphHtmlDocument.text().length() == 0) {
				detector.append(this.getDocument().text());
			} else
				detector.append(stringParagraphHtmlDocument);
			return (detector.detect());
	}
	
	public List<Element> getHtmlElements(String elementName){
		ArrayList<Element> elements = new ArrayList<Element>();
		for(Element element:  this.getDocument().select(elementName)){
			elements.add(element);
		}
		return elements;
	}
	
	public List<Element> getAllOutlinks(){
		List<Element> elements = getHtmlElements("a");
		elements.addAll(this.getHtmlElements("option"));
		return elements;
	}
}
