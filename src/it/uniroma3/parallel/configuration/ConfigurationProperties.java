package it.uniroma3.parallel.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * Classe che rappresenta le proprietà di configurazione della fase di
 * detection. Contiene queste informazioni: -stringhe da inserire quando ha
 * successo una delle euristiche; -numero di entry point per il crawler per ogni
 * euristica; -nomi dei file CSV su cui si scrive -stringa per sito multilingua
 * o non; -nomi delle cartelle di output per RR e delle pagine html
 * 
 * @author davideorlando
 *
 */
public class ConfigurationProperties {
	private static final Logger logger = Logger.getLogger(ConfigurationProperties.class);
	private static final String FILTRO_INSIEME = "filtro.insieme";
	private static final String RESOURCES_CONF_PROPERTIES = "resources/conf.properties";

	private static ConfigurationProperties instance;
	private Properties configuration;
	private Set<String> setOfAllMultilingualValues;

	private ConfigurationProperties() {
		this.configuration = System.getProperties();
		try {
			FileInputStream inStream = new FileInputStream(RESOURCES_CONF_PROPERTIES);
			this.configuration.load(inStream);
			inStream.close();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public static synchronized ConfigurationProperties getInstance() {
		if (instance == null)
			instance = new ConfigurationProperties();
		return instance;
	}

	/**
	 * Ritorna il valore della chiave "mult_detection_hreflang" nel file di
	 * proprietà conf.properties
	 * 
	 * @return
	 */
	public String getStringOfMultDetecionHreflang() {
		return System.getProperty("mult_detection_hreflang");
	}

	/**
	 * Ritorna il valore della chiave "mult_detection_outlinks" nel file di
	 * proprietà conf.properties
	 * 
	 * @return
	 */
	public String getStringOfMultDetectionOutlinks() {
		return System.getProperty("mult_detection_outlinks");
	}

	/**
	 * Ritorna il valore della chiave "mult_detection_prehomepage" nel file di
	 * proprietà conf.properties
	 * 
	 * @return
	 */
	public String getStringOfMultDetectionPrehomepage() {
		return System.getProperty("mult_detection_prehomepage");
	}

	/**
	 * Ritorna il valore della chiave "homepage_hrefLang" nel file di proprietà
	 * conf.properties
	 * 
	 * @return
	 */
	public String getStringOfHomepageHreflang() {
		return System.getProperty("homepage_hrefLang");
	}

	/**
	 * Ritorna il valore della chiave "visit_homepage" nel file di proprietà
	 * conf.properties
	 * 
	 * @return
	 */
	public String getStringOfVisitHomepage() {
		return System.getProperty("visit_homepage");
	}

	/**
	 * Ritorna il valore della chiave "pre_homepage" nel file di proprietà
	 * conf.properties
	 * 
	 * @return
	 */
	public String getStringOfPrehomepage() {
		return System.getProperty("pre_homepage");
	}

	/**
	 * Ritorna il valore intero della chiave "entry_number_hreflang" nel file di
	 * proprietà conf.properties
	 * 
	 * @return
	 */
	public Integer getIntOfEntryNumberHreflang() {
		return Integer.valueOf(System.getProperty("entry_number_hreflang"));
	}

	/**
	 * Ritorna il valore intero della chiave "entry_number_outlink" nel file di
	 * proprietà conf.properties
	 * 
	 * @return
	 */
	public Integer getIntOfEntryNumberOutlink() {
		return Integer.valueOf(System.getProperty("entry_number_outlink"));
	}

	/**
	 * Ritorna il valore della chiave "site_multilingual" nel file di proprietà
	 * conf.properties
	 * 
	 * @return
	 */
	public String getStringOfPathForSiteMultilingualCSV() {
		return System.getProperty("site_multilingual");
	}

	/**
	 * Ritorna il valore della chiave "site_not_multilingual" nel file di
	 * proprietà conf.properties
	 * 
	 * @return
	 */
	public String getStringOfPathForSiteNotMultilingual() {
		return configuration.getProperty("site_not_multilingual");
	}

	/**
	 * Ritorna il valore della chiave "html_pages_preliminary" nel file di
	 * proprietà conf.properties
	 * 
	 * @return
	 */
	public String getStringOfFolderForHtmlPages() {
		return System.getProperty("html_pages_preliminary");
	}

	/**
	 * Ritorna il valore della chiave "outputRR" nel file di proprietà
	 * conf.properties
	 * 
	 * @return
	 */
	public String getStringOfFolderOutput() {
		return System.getProperty("outputRR");
	}

	/**
	 * Ritorna il percorso del file data.xsl
	 * 
	 * @return
	 */
	public String getStringOfPathDataXSL() {
		return System.getProperty("data_xsl_path");
	}

	/**
	 * Ritorna la stringa del nome della cartella contentente data.xsl copiato
	 * 
	 * @return
	 */
	public String getStringOfContainerDataXSLCopy() {
		return System.getProperty("data_xsl_container_folder");
	}

	/**
	 * Ritorna la stringa per creare data.xsl dentro la folder di output
	 * 
	 * @return
	 */
	public String getStringOfPathDataXSLCopy() {
		return System.getProperty("data_xsl_path_copy");
	}

	/**
	 * Ritorna la stringa del percorso del file index.xsl originale
	 * 
	 * @return
	 */
	public String getStringOfIndexXSL() {
		return System.getProperty("index_xsl_path");
	}

	/**
	 * Ritorna la stringa per creare index.xsl dentro la folder di output
	 * 
	 * @return
	 */
	public String getStringOfPathIndexXSLCopy() {
		return System.getProperty("index_xsl_path_copy");
	}

	/**
	 * Ritorna -O:etc/flat-prefs.xml
	 * 
	 * @return
	 */
	public String getStringOfCommandAndPrefs() {
		return System.getProperty("rr_parametro_prefs");
	}

	/**
	 * Ritorna la stringa dataset.xml
	 * 
	 * @return
	 */
	public String getStringOfDataset() {
		return System.getProperty("dataset");
	}

	/**
	 * Ritorna il valore della chiave "error_log" nel file di proprietà
	 * conf.properties
	 * 
	 * @return
	 */
	public String getStringOfErrorLogCSV() {
		return System.getProperty("error_log");
	}

	/**
	 * Ritorna il valore della chiave "time" nel file di proprietà
	 * conf.properties
	 * 
	 * @return
	 */
	public String getStringOfPathForTimeCSV() {
		return System.getProperty("time");
	}

	/**
	 * Ritorna il valore della chiave "rr_csv" nel file di proprietà
	 * conf.properties
	 * 
	 * @return
	 */
	public String getStringOfRRCSV() {
		return System.getProperty("rr_csv");
	}

	/**
	 * Ritorna il path del file dei profili del linguaggio.
	 * 
	 * @return
	 */
	public String getProfileFilePath() {
		return System.getProperty("profiles_lang");
	}

	/**
	 * Ritorna un insieme contenente tutte le stringhe rappresentanti i valori
	 * che si ritrovano nelle ancore (o negli altri elementi del DOM) di un sito
	 * multilingua. Un esempio sono i linguaggi: Italian, English, ecc..
	 * 
	 * @return
	 * @throws IOException
	 */
	public Set<String> makeSetOfAllMultilingualProperties() {
		try {
			if (setOfAllMultilingualValues == null) {
				setOfAllMultilingualValues = new HashSet<>();
				for (String file : getPropertyList(configuration, FILTRO_INSIEME))
					setOfAllMultilingualValues.addAll(makeSetOfStringByFile(file));
			}
		} catch (IOException e) {
			logger.error(e);
		}
		return setOfAllMultilingualValues;
	}

	/**
	 * Lì dove una proprietà (il parametro name), dell'insieme di proprietà
	 * scelto(il parametro properties) è composta da più valori ritorna una
	 * lista di questi valori.
	 * 
	 * @param properties
	 * @param name
	 * @return
	 */
	public static List<String> getPropertyList(Properties properties, String name) {
		List<String> result = new LinkedList<String>();
		for (String s : properties.getProperty(name).split(","))
			result.add(s);
		return result;
	}

	/**
	 * Crea un insieme di stringhe, ognuna delle quali è una linea del file che
	 * si trova nel percorso specificato dalla stringa passata per parametro.
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public Set<String> makeSetOfStringByFile(String fileName) throws IOException {
		Set<String> setOfElementForLanguages = new HashSet<>();
		ArrayList<String> fileContent;
		fileContent = new ArrayList<String>(FileUtils.readLines(new File(fileName)));
		for (String entryLine : fileContent) {
			for (String elementInLine : entryLine.split(","))
				setOfElementForLanguages.add(elementInLine);
		}
		return setOfElementForLanguages;
	}

	/**
	 * Ritorna la lista di elementi su cui si vuole fare language detection.
	 * Potrebbero essere i paragrafi HTML (p) per esempio.
	 * 
	 * @return
	 */
	public List<String> getElementsForLanguageDetection() {
		LinkedList<String> elementsForLanguageDetection = new LinkedList<String>();
		for (String s : getPropertyList(configuration, "language_detection_elements"))
			elementsForLanguageDetection.add(s);
		return elementsForLanguageDetection;
	}

	/**
	 * Ritorna la lista di tag che potrebbero contenere link esterni verso
	 * pagine multilingua parallele.
	 * 
	 * @return
	 */
	public List<String> getStringOfTagName() {
		LinkedList<String> tagNames = new LinkedList<String>();
		for (String s : getPropertyList(configuration, "tagNames"))
			tagNames.add(s);
		return tagNames;
	}

	/**
	 * Ritorna l'user-agent presente nel file di configurazione
	 * 
	 * @return
	 */
	public String getStringOfUserAgent() {
		return configuration.getProperty("user-agent");
	}

	/**
	 * Ritorna il numero minimo di label per considerare una pagina parallela,
	 * deciso da configurazione.
	 * 
	 * @return
	 */
	public int getIntOfNumLabelMin() {
		return Integer.valueOf(configuration.getProperty("label_min"));
	}
}
