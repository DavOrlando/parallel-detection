package it.uniroma3.parallel.model;

import java.io.IOException;
import java.net.URL;
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

	// /**
	// * Seleziona solo le pagine che superano i controlli sui vari filtri.
	// *
	// * @param outlinks
	// * @return
	// * @throws IOException
	// */
	// public List<Page> getMultilingualPage() {
	// List<Page> filteredPages = new ArrayList<>();
	// EditDistanceFilter editDistanceFilter = new EditDistanceFilter();
	// LanguageFilter languageFilter = new LanguageFilter();
	// for (Element link : this.getAllOutlinks()) {
	// Page outlinkPage;
	// try {
	// outlinkPage = new Page(link.absUrl("href"));
	// if (!filteredPages.contains(outlinkPage) &&
	// editDistanceFilter.filter(this, outlinkPage)
	// && languageFilter.filter(this, outlinkPage))
	// filteredPages.add(outlinkPage);
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// return filteredPages;
	// }



}
