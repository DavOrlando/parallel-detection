package it.uniroma3.parallel.filter;

import java.util.Set;

import it.uniroma3.parallel.configuration.ConfigurationProperties;

/**
 * Classe che rappresenta un filtro per valori di link. Utilizza l'insieme di
 * paesi e dei linguaggi del mondo per filtrare il testo che li contiene (che
 * proviene da un link e quindi filtrare quel determinato link).
 * 
 * @author davideorlando
 *
 */
public class LinkValueFilter {

	/**
	 * Torna l'insieme di quei testi che possono rappresentare un cambiamento di
	 * lingua all'interno di un sito. Il nome di una lingua o di un paese del
	 * mondo.
	 * 
	 * @return
	 */
	public Set<String> getSetForFilter() {
		return ConfigurationProperties.getInstance().makeSetOfAllMultilingualProperties();
	}

	/**
	 * Torna true se il testo contiene una delle classiche parole all'interno di
	 * un sito per cambiare lingua ('english', 'en' e così via...).
	 * 
	 * @param text
	 * @return
	 */
	public boolean filter(String text) {
		String normalizedText = text.toLowerCase();
		String[] texts = normalizedText.split(" ");
		if(texts.length == 2)
			return this.getSetForFilter().contains(texts[0]) || this.getSetForFilter().contains(texts[1]);
		return this.getSetForFilter().contains(normalizedText);
	}

}
