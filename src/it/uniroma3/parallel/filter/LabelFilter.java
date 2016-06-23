package it.uniroma3.parallel.filter;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.roadrunner.RoadRunnerDataSet;
import it.uniroma3.parallel.utils.CybozuLanguageDetector;
import it.uniroma3.parallel.utils.FetchManager;

public class LabelFilter {
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
		// memorizzeremo solo l'URL con più label
		Map<String, URL> language2Url = new HashMap<String, URL>();
		try {
			language2Url.put(groupOfHomepage.getStarterPage().getLanguage(),groupOfHomepage.getStarterPage().getUrlRedirect());
			// il valore è il num di label attuale e sostituiremo un URL in
			// language2Url se e solo se troviamo per quel linguaggio una pagina
			// con
			// più label di quelle attuali
			Map<String, Integer> language2NumberOfLabel = new HashMap<String, Integer>();
			// per ogni coppia di homepage analizzo l'output di RR
			for (PairOfPages pair : groupOfHomepage.getListOfPairs()) {
				RoadRunnerDataSet roadRunnerDataSet = FetchManager.getInstance().getRoadRunnerDataSet(pair);
				if (roadRunnerDataSet == null || roadRunnerDataSet.getNumberOfLabels() < 16)
					continue;
				List<String> textFromAllLabels = roadRunnerDataSet.getTextFromAllLabels();
				if (textFromAllLabels == null)
					continue;
				if (isEnoughText(textFromAllLabels) && isDifferentLanguage(textFromAllLabels)) {
					String languagePage = pair.getHomepageFromList(SECONDA_HOMEPAGE).getLanguage();
					if (language2NumberOfLabel.get(languagePage) == null || language2NumberOfLabel.get(languagePage)
							.compareTo(roadRunnerDataSet.getNumberOfLabels()) < 0) {
						language2Url.put(languagePage, pair.getHomepageFromList(SECONDA_HOMEPAGE).getUrlRedirect());
						language2NumberOfLabel.put(languagePage, roadRunnerDataSet.getNumberOfLabels());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return language2Url.values();
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
