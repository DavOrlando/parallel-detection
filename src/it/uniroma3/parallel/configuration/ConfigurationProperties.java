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
			e.printStackTrace();
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
	public String getStringOfSiteMultilingual() {
		return System.getProperty("site_multilingual");
	}

	/**
	 * Ritorna il valore della chiave "site_not_multilingual" nel file di
	 * proprietà conf.properties
	 * 
	 * @return
	 */
	public String getStringOfSiteNotMultilingual() {
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
	public String getStringOfFolderForRROutput() {
		return System.getProperty("outputRR");
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
	public String getStringOfTimeCSV() {
		return System.getProperty("time");
	}

	/**
	 * Ritorna un insieme contenente tutte le stringhe rappresentanti i valori
	 * che si ritrovano nelle ancore (o negli altri elementi del DOM) di un sito
	 * multilingua. Un esempio sono i linguaggi: Italian, English, ecc..
	 * 
	 * @return
	 */
	public Set<String> makeSetOfAllMultilingualProperties() {
		if (setOfAllMultilingualValues == null) {
			setOfAllMultilingualValues = new HashSet<>();
			for (String file : getPropertyList(configuration, FILTRO_INSIEME))
				setOfAllMultilingualValues.addAll(makeSetOf(file));
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
	 * Crea un insieme di stringhe, ognuna delle quali è una linea del file
	 * specificato dalla stringa passata per parametro.
	 * 
	 * @param fileName
	 * @return
	 */
	public Set<String> makeSetOf(String fileName) {
		Set<String> setOfElementForLanguages = new HashSet<>();
		ArrayList<String> fileContent;
		try {
			fileContent = new ArrayList<String>(FileUtils.readLines(new File(fileName)));
			for (String entryLine : fileContent) {
				for (String elementInLine : entryLine.split(","))
					setOfElementForLanguages.add(elementInLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return setOfElementForLanguages;
	}

}