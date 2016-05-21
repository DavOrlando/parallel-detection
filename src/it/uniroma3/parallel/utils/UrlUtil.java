package it.uniroma3.parallel.utils;

import java.io.IOException;
import java.net.URL;

/***
 * 
 * @author davideorlando
 * 
 */
public class UrlUtil {

	/**
	 * Ritorna una stringa senza "http://" o '/' o "/?" o "https://" utile per
	 * creare il nome di una cartella partendo da un URL di un sito.
	 * 
	 * @param siteUrl
	 * @return nameFolder
	 */
	public static String getNameFolderFromSiteUrl(URL siteUrl) {
		return siteUrl.toString().replace("http://", "").replaceAll(":", "").replaceAll("/", "").replace("https://", "")
				.replaceAll("/?", ""); // TODO qui si deve togliere replaceAll e
										// mettere replace giusto?
	}

	/**
	 * Ritorna un URL con lo scheme http all'inizio.
	 * 
	 * @param siteUrl
	 * @return "http://" + site
	 */

	public static String addHttp(String siteUrl) {
		if (hasHttp(siteUrl))
			return siteUrl;
		return "http://".concat(siteUrl);
	}

	/**
	 * Ritorna una stringa senza '/' o "\\" se ci sono alla fine.
	 * 
	 * @param urlToPurge
	 * @return urlWithoutSlashesAtEnd
	 */

	// public static String getUrlWithoutSlashesAtEnd(String urlToPurge) {
	// if (urlToPurge.endsWith("/") || urlToPurge.endsWith("\\"))
	// return urlToPurge.substring(0, urlToPurge.length() - 1);
	// return urlToPurge;
	// }

	/**
	 * Ritorna un URL assoluto da un URL relativo.
	 * 
	 * @param homePage
	 * @param outlink
	 * @return AbsoluteUrl
	 * @throws IOException
	 */

	public static String getAbsoluteUrlFromRelativeUrl(String homePage, String outlink) throws IOException {
		URL url = new URL(new URL(homePage), (outlink));
		return url.toString();
	}

	/**
	 * 
	 * @param urlSite
	 * @return true se contiene http all'inizio, false altrimenti.
	 */
	public static boolean hasHttp(String urlSite) {
		return urlSite.contains("http");
	}

}
