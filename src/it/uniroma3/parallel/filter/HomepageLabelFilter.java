package it.uniroma3.parallel.filter;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.roadrunner.RoadRunnerDataSet;
import it.uniroma3.parallel.utils.CybozuLanguageDetector;
import it.uniroma3.parallel.utils.FetchManager;

/**
 * Classe che rappresenta un filtro usato quando si conosce la homepage. Quindi
 * analizzerà coppie dove il primo elemento è sempre la stessa homepage. (Vedi
 * seconda euristica).
 * 
 * @author davideorlando
 *
 */
public class HomepageLabelFilter {
	private static final int SECONDA_HOMEPAGE = 1;

	/**
	 * Ritorna una collezione di URL dove ognuno corrisponde alla pagina
	 * multilingua e parallela più probabile per quel linguaggio differente
	 * rispetto alla homepage. La pagina è scelta in base al criterio del numero
	 * di label che RoadRunner riesce ad allineare con la homepage vera e
	 * propria. Quindi ogni lingua ci viene ritornato solo l'URL che corrisponde
	 * alla pagina con più label allineate con la homepage.
	 * 
	 * @param groupOfHomepage
	 * @return
	 * 
	 */
	public Collection<URL> filter(ParallelPages groupOfHomepage) {
		Map<String, URL> language2Url = new HashMap<String, URL>();
		try {
			language2Url.put(groupOfHomepage.getStarterPage().getLanguage(),
					groupOfHomepage.getStarterPage().getUrlRedirect());
			Map<String, Integer> language2NumberOfLabel = new HashMap<String, Integer>();
			// per ogni coppia di homepage analizzo l'output di RR
			for (PairOfPages pair : groupOfHomepage.getListOfPairs()) {
				analyzesPair(language2Url, language2NumberOfLabel, pair);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return language2Url.values();
	}

	/**
	 * Analizza la coppia in confronto a quelle già analizzate. Se la coppia
	 * possiede più label, ovvero regioni parallele all'interno del DOM, per una
	 * determinata lingua, sostituisce per quel linguaggio la pagina della
	 * coppia analizzata.
	 * 
	 * @param language2Url
	 * @param language2NumberOfLabel
	 * @param pair
	 * @throws XPathExpressionException
	 * @throws LangDetectException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void analyzesPair(Map<String, URL> language2Url, Map<String, Integer> language2NumberOfLabel,
			PairOfPages pair) throws XPathExpressionException, LangDetectException, ParserConfigurationException,
			SAXException, IOException, InterruptedException {
		RoadRunnerDataSet roadRunnerDataSet = FetchManager.getInstance().getRoadRunnerDataSet(pair);
		if (roadRunnerDataSet != null && roadRunnerDataSet.getNumberOfLabels() >= 16) {
			List<String> textFromAllLabels = roadRunnerDataSet.getTextFromAllLabels();
			if (textFromAllLabels != null && isEnoughText(textFromAllLabels)
					&& isDifferentLanguage(textFromAllLabels)) {
				String languagePage = pair.getHomepageFromList(SECONDA_HOMEPAGE).getLanguage();
				if (language2NumberOfLabel.get(languagePage) == null || language2NumberOfLabel.get(languagePage)
						.compareTo(roadRunnerDataSet.getNumberOfLabels()) < 0) {
					language2Url.put(languagePage, pair.getHomepageFromList(SECONDA_HOMEPAGE).getUrlRedirect());
					language2NumberOfLabel.put(languagePage, roadRunnerDataSet.getNumberOfLabels());
				}
			}
		}
	}

	/**
	 * Ritorna true se cè abbastanza testo.
	 * 
	 * @param testiConcatenati
	 * @return
	 */
	private boolean isEnoughText(List<String> testiConcatenati) {
		// se non ho elementi nella lista su cui fare lang detect ritorno false
		if (testiConcatenati.size() == 0)
			return false;

		// se ho poco testo restituisco false
		for (String testo : testiConcatenati)
			if (testo.length() < 15 && testiConcatenati.size() == 2)
				return false;
		return true;
	}

	/**
	 * Ritorna true se i testi sono in lingua differente.
	 * 
	 * @param testiConcatenati
	 * @return
	 * @throws LangDetectException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean isDifferentLanguage(List<String> testiConcatenati)
			throws LangDetectException, ParserConfigurationException, SAXException, IOException, InterruptedException {
		// verifico che un set creato con i linguaggi dei testi abbia una
		// cardinalitò uguale al numero di testi.Ovvero abbiamo tutte lingue
		// differenti.
		return CybozuLanguageDetector.getInstance().getLanguagesOfStrings(testiConcatenati).size() == testiConcatenati
				.size();
	}

}
