package it.uniroma3.parallel.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.configuration.ConfigurationProperties;

/**
 * Classe che rappresenta il rilevatore di linguaggio che utilizza il servizio
 * esterno Cybozu.
 * 
 * @author davideorlando
 *
 */
public class CybozuLanguageDetector {

	private static final String PROFILES_SM = "profiles.sm";
	private static CybozuLanguageDetector instance;
	private Detector detector;

	/**
	 * Costruisce un rilevatore di linguaggio per document attraverso l'utilizzo
	 * del servizio com.cybozu.labs.langdetect.
	 */
	private CybozuLanguageDetector() {
		// caricamento dei profili per la language detection
		if (DetectorFactory.getLangList().size() == 0)
			try {
				DetectorFactory.loadProfile(PROFILES_SM);
				this.detector = DetectorFactory.create();
			} catch (LangDetectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/**
	 * Ritorna un istanza di questo servizio.
	 * 
	 * @return
	 */
	public static synchronized CybozuLanguageDetector getInstance() {
		if (instance == null)
			instance = new CybozuLanguageDetector();
		return instance;
	}

	/**
	 * Ritorna la lingua del document della pagina analizzando attraverso un
	 * servizio esterno i vari testi all'interno del document.
	 * 
	 * @param document
	 * @return
	 * @throws LangDetectException
	 */
	public String detect(Document document) throws LangDetectException {
		String paragraphHtmlDocument="";
		for(String element : ConfigurationProperties.getInstance().getElementsForLanguageDetection())
			paragraphHtmlDocument += getStringElement(element, document);
		if (paragraphHtmlDocument.length() == 0) {
			detector.append(document.text());
		} else
			detector.append(paragraphHtmlDocument);
		String language = detector.detect();
		// una volta usato va ricreato
		detector = DetectorFactory.create();
		return language;
	}

	/***
	 * Ritorna una stringa con il contenuto degli elementi della pagina HTML che
	 * si chiamano elementName.
	 * 
	 * @param elementName
	 *            nome del parametro HTML
	 * @param document
	 * @return
	 */
	private String getStringElement(String elementName, Document document) {
		Elements paragraphHtmlDocument = document.select(elementName);
		return paragraphHtmlDocument.text();
	}

	public Set<String> getLanguagesOfStrings(List<String> testiConcatenati) throws LangDetectException {
		Set<String> setOfLanguages = new HashSet<>();
		for (String testo : testiConcatenati) {
			String langDetect = textLanguageDetection(testo);
			setOfLanguages.add(langDetect);
		}
		return setOfLanguages;
	}

	public String textLanguageDetection(String testo) throws LangDetectException {
		// risultati della language detection(en, it, ...)
		String langDetect = "";
		// detect su stringone
		detector.append(testo);
		langDetect = detector.detect().toString();
		detector = DetectorFactory.create();
		// aggiungo la lingua rilevata al set
		return langDetect;
	}

}