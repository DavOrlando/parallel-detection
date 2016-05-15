package it.multilingualDetection;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import it.model.Page;
import it.model.ParallelPages;
import it.utils.UrlUtil;

/**
 * Classe che rappresenta un rilevatore di siti multilingua. Possiede metodi per
 * comprendere se un sito è multilingua oppure no.
 * 
 * 
 * @author davideorlando
 *
 */

public class MultilingualDetector {

	public MultilingualDetector() {
	}

	/**
	 * Rileva se il sito è un falso multilingua, attraverso l'analisi del suo
	 * URL. Sito falso multlingua vuol dire parallelo nella struttura ma non
	 * nella semantica.
	 * 
	 * @param homepageURL
	 *            la stringa che corrisponde all'URL del sito
	 * @return true se il sito è un falso multilingua, false altrimenti
	 */
	public boolean detectFalseMultilingualSite(URL homepageURL) {
		return homepageURL.toString().contains("citysite.") || homepageURL.toString().contains("citycorner.")
				|| homepageURL.toString().contains("stadtsite.");
	}

	/*
	 * OK: RICERCA LINK USCENTI DALLA HOMEPAGE CON ATTRIBUTE HREFLANG metodo che
	 * ricerca l'attributo hreflang nella hp e ritorna lista coppie candidate
	 * per iniziare visita ricorsiva del sito metodo di supporto per
	 * isMultilingual, 1) modo, restituisce coppie candidate trovate cercando
	 * nella hp hreflang sono le coppie degli hreflang tra loro e della home con
	 * gli hreflang
	 * 
	 * 
	 * Questi commenti sopra sono errati. Il codice fa una cosa diversa: Non
	 * torna pair o coppie ma un insieme di liste dove ciascuna possiede al
	 * massimo 5 hreflang.
	 */

	/**
	 * Si cercano nella homepage del sito passato come parametro dei link
	 * uscenti con l'attributo hreflang, euristica che ci porta a dire che il
	 * sito è multilingua. Si collezionano tutti questi URL, che rappresentano
	 * le pagine nelle varie lingue, in delle liste di stringhe (con al massimo
	 * 5 elementi), successivamente si inseriscono le liste all'interno di un
	 * set.
	 * 
	 * @param homepage
	 *            il sito che si deve analizzare
	 * @return un set di liste dove ciascuna possiede al massimo 5 homepage
	 *         multilingua e parallele
	 * @throws IOException
	 */
	public ParallelPages detectByHreflang(Page homepage) throws IOException {
		// dove andiamo a mettere tutte le pagine parallele.
		ParallelPages parallelHomepage = new ParallelPages();

		Document document = homepage.getDocument();
		// seleziono i link della pagina
		Elements linksInHomePage = document.select("link");

		String homePagePurge = homepage.getUrlRedirect().toString();
		parallelHomepage.addURL(new URL(UrlUtil.getUrlWithoutSlashesAtEnd(
				UrlUtil.getUrlWithoutSlashesAtEnd(UrlUtil.getUrlWithoutSlashesAtEnd(homePagePurge)))));
		
		for (Element link : linksInHomePage) {
			if (!link.attr("hreflang").isEmpty()) {
				String outLinkWithHrefLang = UrlUtil.getUrlWithoutSlashesAtEnd(link.attr("href"));
				// se link è relativo modificalo adeguatemente
				if (!outLinkWithHrefLang.contains("http")) {
					outLinkWithHrefLang = UrlUtil.getAbsoluteUrlFromRelativeUrl(homepage.getUrlRedirect().toString(),
							outLinkWithHrefLang);
				}
				parallelHomepage.addURL(new URL(outLinkWithHrefLang));
			}
		}

		// se solo homepage restituisco set vuoto
		if (parallelHomepage.getParallelPageUrls().size() == 1)
			return new ParallelPages();

		return parallelHomepage;
	}

}
