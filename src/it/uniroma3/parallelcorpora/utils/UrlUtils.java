package it.uniroma3.parallelcorpora.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.nodes.Element;

/***
 * Classe con delle operazioni utili per gli URL.
 * 
 * @author davideorlando
 * 
 */
public class UrlUtils {

	private static final String WWW = "www";
	private static final String HTTP = "http";
	private static final String HREF = "href";
	private static final String ABS_HREF = "abs:href";
	private static final String ABS_DATA_HREF = "abs:data-href";
	private static UrlUtils instance;

	private UrlUtils() {

	}

	public static synchronized UrlUtils getInstance() {
		if (instance == null)
			instance = new UrlUtils();
		return instance;
	}

	/**
	 * Ritorna un URL con lo scheme http all'inizio.
	 * 
	 * @param siteUrl
	 * @return "http://" + site
	 */

	public String addHttp(String siteUrl) {
		if (hasHttp(siteUrl))
			return siteUrl;
		return "http://".concat(siteUrl);
	}

	/**
	 * 
	 * @param urlSite
	 * @return true se contiene http all'inizio, false altrimenti.
	 */
	private boolean hasHttp(String urlSite) {
		return urlSite.contains(HTTP);
	}

	/**
	 * Preso un link ci restituisce l'URL assoluto associato a quel link.
	 * 
	 * @param link
	 * @return
	 * @throws MalformedURLException
	 */
	public URL getAbsoluteURL(Element link) throws MalformedURLException {
		String urlLink = link.attr(HREF);
		if(urlLink.length()>1 && urlLink.charAt(0) == '/' && urlLink.charAt(1) =='/')
			urlLink = link.attr(ABS_HREF);
		if (!urlLink.contains(WWW) && !urlLink.contains(HTTP))
			urlLink = link.attr(ABS_HREF);
		if(urlLink.equals(""))
			urlLink = link.attr(ABS_DATA_HREF);
		return new URL(addHttp(urlLink));
	}

}
