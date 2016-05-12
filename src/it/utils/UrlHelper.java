package it.utils;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

/***
 * 
 * @author davideorlando
 * 
 */
public class UrlHelper {

	/**
	 * Ritorna una stringa senza "http://" o '/' o "/?" o "https://" utile per
	 * creare il nome di una cartella partendo da un URL di un sito.
	 * 
	 * @param siteUrl
	 * @return nameFolder
	 */
	public static String getNameFolderFromSiteUrl(String siteUrl) {
		return siteUrl.replace("http://", "").replaceAll(":", "").replaceAll("/", "").replaceAll("https://", "")
				.replaceAll("/?", ""); // TODO qui si deve togliere replaceAll e
										// mettere replace giusto?
	}

	/**
	 * Ritorna un URL con lo scheme http all'inizio.
	 * 
	 * @param siteUrl
	 * @return "http://" + site
	 */

	public static String getUrlWithHttp(String siteUrl) {
		return "http://".concat(siteUrl);
	}

	/**
	 * Ritorna una stringa senza '/' o "\\" se ci sono alla fine.
	 * 
	 * @param urlToPurge
	 * @return urlWithoutSlashesAtEnd
	 */

	public static String getUrlWithoutSlashesAtEnd(String urlToPurge) {
		if (urlToPurge.endsWith("/") || urlToPurge.endsWith("\\"))
			return urlToPurge.substring(0, urlToPurge.length() - 1);
		return urlToPurge;
	}

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
	 * Ritorna la root dell'URL. Esempio: "http://www.test.com/test" diventa
	 * "http://www.test.com".
	 * 
	 * @param url
	 * @return urlRoot
	 */
	public static String getUrlRoot(String url) {
		String urlRoot = url;
		if (!hasHttp(url))
			urlRoot = getUrlWithHttp(urlRoot);
		if (StringUtils.ordinalIndexOf(urlRoot, "/", 3) != -1)
			urlRoot = urlRoot.substring(0, StringUtils.ordinalIndexOf(urlRoot, "/", 3));
		return urlRoot;
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
