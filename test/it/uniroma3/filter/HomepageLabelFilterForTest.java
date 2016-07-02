package it.uniroma3.filter;

import java.util.List;
import org.apache.log4j.Logger;

import it.uniroma3.parallelcorpora.configuration.ConfigurationProperties;
import it.uniroma3.parallelcorpora.filter.HomepageLabelFilter;
import it.uniroma3.parallelcorpora.utils.CybozuLanguageDetector;

public class HomepageLabelFilterForTest extends HomepageLabelFilter {

	private static final int LABEL_MINIME = ConfigurationProperties.getInstance().getIntOfNumLabelMin();
	private static final int SECONDA_HOMEPAGE = 1;
	private static final Logger logger = Logger.getLogger(HomepageLabelFilterForTest.class);

	

	/**
	 * Ritorna true se cè abbastanza testo.
	 * 
	 * @param testiConcatenati
	 * @return
	 */
	public boolean isEnoughText(List<String> testiConcatenati) {
		// se non ho elementi nella lista su cui fare lang detect ritorno false
		if (testiConcatenati.size() == 0)
			return false;
		// se ho poco testo restituisco false
		for (String testo : testiConcatenati)
			if (testo.length() < 15 && testiConcatenati.size() <= 2)
				return false;
		return true;
	}

	/**
	 * Ritorna true se i testi sono in lingua differente.
	 * 
	 * @param testiConcatenati
	 * @return
	 */
	public boolean isDifferentLanguage(List<String> testiConcatenati) {
		// verifico che un set creato con i linguaggi dei testi abbia una
		// cardinalitò uguale al numero di testi.Ovvero abbiamo tutte lingue
		// differenti.
		return testiConcatenati.size() != 0 && CybozuLanguageDetector.getInstance()
				.getLanguagesOfStrings(testiConcatenati).size() == testiConcatenati.size();
	}
}
