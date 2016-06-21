package it.uniroma3.parallel.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.roadrunner.RoadRunnerDataSet;

public class FetchManager {
	private static final String DATA_COUNTRIES_ENGLISH_TXT = "data/countriesEnglish.txt";
	private static final String DATA_COUNTRIES_ORIGINAL_TXT = "data/countriesOriginal.txt";
	private static final String DATA_LANGUAGES_ENGLISH_TXT = "data/languagesEnglish.txt";
	private static final String HTML = ".html";
	private static final String HOME_PAGE = "HomePage";
	private static final String HTML_PAGES_PRELIMINARY = "htmlPagesPreliminary";
	public static FetchManager instance;
	private Map<URL, String> url2LocalPath;
	private Map<PairOfPages, RoadRunnerDataSet> pair2RRDataSet;
	private Set<String> setOfAllMultilingualValues;

	/**
	 * Gestore dei download per questo Sistema.
	 */
	private FetchManager() {
		this.url2LocalPath = new HashMap<>();
		this.pair2RRDataSet = new HashMap<>();
	}

	public static FetchManager getInstance() {
		if (instance == null)
			instance = new FetchManager();
		return instance;
	}

	/**
	 * Scarica in locale l'intero gruppo di pagine. Popola una mappa che mi dice
	 * per ogni URL quale è il percorso in locale.
	 * 
	 * @param groupOfHomepage
	 */
	public void persistGroupOfHomepage(ParallelPages groupOfHomepage) {
		String nameFolder = groupOfHomepage.getStarterPage().getPageName();
		String basePath = HTML_PAGES_PRELIMINARY + nameFolder + "/" + nameFolder;
		int pageNumber = 1;
		boolean isHomepage = true;
		// scarico le possibili homepage
		for (Page page : groupOfHomepage.getCandidateParallelHomepages()) {
			makeDirectories(pageNumber, basePath);
			// segno l'homepage
			if (isHomepage) {
				download(groupOfHomepage.getStarterPage(), basePath, pageNumber, true);
				isHomepage = false;
			} else {
				download(page, basePath, pageNumber, false);
				pageNumber++;
			}
		}
	}

	/**
	 * Scarica in locale le coppie di pagine che derivano dall'analisi della
	 * prehomepage.
	 * 
	 * @param groupOfHomepage
	 */
	public void persistPairOfHomepage(PairOfPages pairOfPages, String nameOfPreHomepage) {
		String basePath = HTML_PAGES_PRELIMINARY + nameOfPreHomepage + "/" + nameOfPreHomepage;
		makeDirectories(pairOfPages.getPairNumber(), basePath);
		download(pairOfPages.getMainHomepage(), basePath, pairOfPages.getPairNumber(), true);
		download(pairOfPages.getHomepageFromList(1), basePath, pairOfPages.getPairNumber(), false);
	}

	/**
	 * Scarica la pagina facendo attenzione se si sta trattando della
	 * homepage(pageNumber è impostato a zero) oppure di una altra qualsiasi
	 * pagina accoppiabile con la homepage(altro valore di pageNumber).
	 * 
	 * @param page
	 * @param basePath
	 * @param pageNumber
	 * @param isHomepage
	 */
	private void download(Page page, String basePath, int pageNumber, boolean isHomepage) {
		// cartella dove scaricare la pagina
		String urlBase = basePath + pageNumber;
		try {
			if (isHomepage)// E' l'homepage allora sarà la prima della
							// coppia.
				this.downloadPageInto(page, urlBase + "/" + HOME_PAGE + pageNumber + "-1" + HTML);
			else// E' l'altra pagina allora sarà la seconda della coppia.
				this.downloadPageInto(page, urlBase + "/" + HOME_PAGE + pageNumber + "-2" + HTML);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Scarica la pagina in locale, in particolare nel percorso indicato da
	 * localFilename. Inoltre dato che ha scaricato la pagina ha la
	 * responsabilità di impostare il percorso locale per quella pagina.
	 * 
	 * @param page
	 * @param localFilename,
	 *            path dove mettere la pagina.
	 * @throws IOException
	 */
	private void downloadPageInto(Page page, String localFilename) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(localFilename);
			fos.write(page.getDocument().toString().getBytes());
			this.url2LocalPath.put(page.getUrlRedirect(), localFilename);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	/***
	 * Crea le cartelle per scaricare in locale le pagine. Bisogna ricordare che
	 * viene creata una cartella per ogni sito e all'interno delle sottocartelle
	 * per ogni pagina. In tutte le sottocartelle vi è solo una pagina tranne la
	 * prima che contiene sia la homepage che la seconda pagina che verrà
	 * accoppiata insieme alla homepage.
	 * 
	 * @param nameFolder
	 * @param countEntryPoints
	 * @param basePath
	 */
	private void makeDirectories(int countEntryPoints, String basePath) {
		new File(basePath + countEntryPoints).mkdirs();
	}

	/**
	 * Ritorna il percorso locale della pagina.
	 * 
	 * @param url
	 * @return
	 */
	public String findPageByURL(URL url) {
		return this.url2LocalPath.get(url);
	}

	/**
	 * Aggiunge un dataset di RoadRunner relativo ad una coppia di homepage alla
	 * mappa (coppieDiHomepage,RRDataSet).
	 * 
	 * @param pairOfHomepage
	 * @param roadRunnerDataSet
	 */
	public void addRRDataSet(PairOfPages pairOfHomepage, RoadRunnerDataSet roadRunnerDataSet) {
		this.pair2RRDataSet.put(pairOfHomepage, roadRunnerDataSet);
	}

	/**
	 * Ritorna il dataset di RoadRunner relativo alla pair di homepage.
	 * 
	 * @param pairOfHomepages
	 * @return
	 */
	public RoadRunnerDataSet getRoadRunnerDataSet(PairOfPages pairOfHomepages) {
		return this.pair2RRDataSet.get(pairOfHomepages);
	}

	/**
	 * Crea un insieme di stringhe, ognuna delle quali è una linea del file
	 * specificato dalla stringa passata per parametro.
	 * 
	 * @param fileName
	 * @return
	 */
	public Set<String> makeSetOf(String fileName) {
		Set<String> fileContent = new HashSet<>();
		try {
			fileContent = new HashSet<String>(FileUtils.readLines(new File(fileName)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileContent;
	}

	/**
	 * Crea l'insieme delle stringhe rappresentanti le lingue
	 * 
	 * @return
	 */
	public Set<String> makeSetOfLanguages() {
		return this.makeSetOf(DATA_LANGUAGES_ENGLISH_TXT);
	}

	/**
	 * Crea l'insieme delle stringhe rappresentanti i nomi dei paesi in inglese.
	 * 
	 * @return
	 */
	public Set<String> makeSetOfCountriesInEnglish() {
		return this.makeSetOf(DATA_COUNTRIES_ENGLISH_TXT);
	}

	/**
	 * Crea l'insieme delle stringhe rappresentanti i nomi dei paesi in lingua
	 * nativa.
	 * 
	 * @return
	 */
	public Set<String> makeSetOfCountriesInNative() {
		return this.makeSetOf(DATA_COUNTRIES_ORIGINAL_TXT);
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
			setOfAllMultilingualValues = new HashSet<String>(makeSetOfLanguages());
			setOfAllMultilingualValues.addAll(makeSetOfCountriesInEnglish());
			setOfAllMultilingualValues.addAll(makeSetOfCountriesInNative());
		}
		return setOfAllMultilingualValues;
	}

}
