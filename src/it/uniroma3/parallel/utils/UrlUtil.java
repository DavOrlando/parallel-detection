package it.uniroma3.parallel.utils;

import java.io.IOException;
import java.net.URL;

/***
 * Classe con delle operazioni utili per gli URL.
 * 
 * @author davideorlando
 * 
 */
public class UrlUtil {

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
