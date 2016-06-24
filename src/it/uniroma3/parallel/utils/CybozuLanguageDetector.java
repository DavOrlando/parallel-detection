package it.uniroma3.parallel.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

public class CybozuLanguageDetector {

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
				DetectorFactory.loadProfile("profiles.sm");
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
	// TODO selezionare gli elementi per fare lang detection : parametrico
	// con una lista specificata in un file di properties
	public String detect(Document document) throws LangDetectException {
		String paragraphHtmlDocument = getStringElement("p", document);
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
		// carico i profili delle lingue
		if (DetectorFactory.getLangList().size() == 0)
			DetectorFactory.loadProfile("profiles.sm");
		for (String testo : testiConcatenati) {
			String langDetect = textLanguageDetection(testo);
			setOfLanguages.add(langDetect);
		}
		return setOfLanguages;
	}

	public static String textLanguageDetection(String testo) throws LangDetectException {
		// risultati della language detection(en, it, ...)
		String langDetect = "";
		// detect su stringone
		Detector detector = DetectorFactory.create();
		detector.append(testo);
		langDetect = detector.detect().toString();
		// aggiungo la lingua rilevata al set
		return langDetect;
	}

}