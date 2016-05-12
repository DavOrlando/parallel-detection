package it.multilingualDetection;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import it.model.Site;
import it.utils.UrlHelper;

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
	 * URL.
	 * 
	 * @param site
	 *            la stringa che corrisponde all'URL del sito
	 * @return true se il sito è un falso multilingua, false altrimenti
	 */
	public boolean detectFalseMultilingualSite(String site) {
		return site.contains("citysite.") || site.contains("citycorner.") || site.contains("stadtsite.");
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
	 * sito è multilingua. Si collezionano tutti questi link in delle liste di
	 * stringhe (con al massimo 5 elementi), successivamente si inseriscono le
	 * liste all'interno di un set.
	 * 
	 * @param site
	 *            il sito che si deve analizzare
	 * @return un set di liste dove ciascuna possiede al massimo 5 homepage
	 *         multilingua e parallele
	 * @throws IOException
	 */
	public Set<List<String>> detectByHreflang(Site site) throws IOException {
		// set ritornato dal metodo
		Set<List<String>> setOfListThatContainEntryPoints = new HashSet<List<String>>();

		// lista corrente di max 5 entry points
		List<String> listOfMax5EntryPoints= new LinkedList<String>();

		Document document = site.getDocument();
		// seleziono i link della pagina
		Elements linksInHomePage = document.select("link");

		//TODO:DAVIDE qui secondo me voleva fare un altra cosa, non ha senso così.
		String homePagePurge = site.getUrlRedirect();
		listOfMax5EntryPoints.add(homePagePurge);
		homePagePurge = UrlHelper.getUrlWithoutSlashesAtEnd(homePagePurge);

		/*
		 * groupHreflang.add(current); errore perchè aggiunge due volte se non
		 * riempie la prima lista DAVIDE
		 */

		for (Element link : linksInHomePage) {
			// invece di creare tutte coppie lancio la visita ricorsiva di 5 in 5
			if (listOfMax5EntryPoints.size() == 5) {
				setOfListThatContainEntryPoints.add(listOfMax5EntryPoints);
				listOfMax5EntryPoints = new LinkedList<String>();
			}

			if (!link.attr("hreflang").isEmpty()) {
				String outLinkWithHrefLang = UrlHelper.getUrlWithoutSlashesAtEnd(link.attr("href"));
				// se link è relativo modificalo adeguatemente
				if (!outLinkWithHrefLang.contains("http")) {
					outLinkWithHrefLang = UrlHelper.getAbsoluteUrlFromRelativeUrl(site.getUrlRedirect(), outLinkWithHrefLang);
				}
				listOfMax5EntryPoints.add(outLinkWithHrefLang);
			}
		}
		setOfListThatContainEntryPoints.add(listOfMax5EntryPoints);

		// se solo homepage restituisco set vuoto
		if (countEntryPoints(setOfListThatContainEntryPoints) == 1)
			return new HashSet<List<String>>();

		return setOfListThatContainEntryPoints;
	}

	/**
	 * Conta il numero di entry points, contenuti in liste all'interno di un
	 * insieme
	 * 
	 * @param groupOfEntryPoints
	 *            ovvero la collezione di
	 * @return int numero di entry points
	 */
	private int countEntryPoints(Collection<List<String>> groupOfEntryPoints) {
		int countEntryPoints = 0;
		for (List<String> entryPoints : groupOfEntryPoints) {
			countEntryPoints = countEntryPoints + entryPoints.size();
		}
		return countEntryPoints;
	}

}
