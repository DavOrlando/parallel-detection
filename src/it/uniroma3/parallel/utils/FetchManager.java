package it.uniroma3.parallel.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.configuration.ConfigurationProperties;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.roadrunner.RoadRunnerDataSet;

/**
 * Classe che si occupa della gestione del caricamento dei dati nel file system
 * (per adesso).
 * 
 * @author davideorlando
 *
 */
public class FetchManager {

	private static final String HTML = ".html";
	private static final String HOME_PAGE = "HomePage";
	public static FetchManager instance;
	private Map<URI, String> uri2LocalPath;
	private Map<PairOfPages, RoadRunnerDataSet> pair2RRDataSet;
	private ConfigurationProperties configuration;

	private FetchManager() {
		this.uri2LocalPath = new HashMap<>();
		this.pair2RRDataSet = new HashMap<>();
		this.configuration = ConfigurationProperties.getInstance();
	}

	public static synchronized FetchManager getInstance() {
		if (instance == null)
			instance = new FetchManager();
		return instance;
	}

	/**
	 * Ritorna il percorso locale della pagina.
	 * 
	 * @param url
	 * @return
	 * @throws URISyntaxException
	 */
	public String findPageByURL(URL url) throws URISyntaxException {
		URI uri = url.toURI();
		String string = this.uri2LocalPath.get(uri);
		return string;
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
	 * Ritorna la base del percorso locale nel file system in cui verrano
	 * salvate le pagine.
	 * 
	 * @param nameOfPage
	 * @return
	 */
	private String getBasePath(String nameOfPage) {
		return configuration.getStringOfFolderForHtmlPages() + nameOfPage + "/" + nameOfPage;
	}

	/**
	 * Scarica in locale l'intero gruppo di pagine. Popola una mappa che mi dice
	 * per ogni URL quale è il percorso in locale.
	 * 
	 * @param parallelPages
	 */
	public void persistParallelPages(ParallelPages parallelPages) {
		int pageNumber = 1;
		boolean isHomepage = true;
		// scarico le possibili homepage
		for (Page page : parallelPages.getCandidateParallelHomepages()) {
			makeDirectories(getBasePath(parallelPages.getStarterPage().getPageName()), pageNumber);
			// segno l'homepage
			if (isHomepage && page.equals(parallelPages.getStarterPage())) {
				savePageInLocal(parallelPages.getStarterPage(), parallelPages.getStarterPage().getPageName(),
						pageNumber, true);
				isHomepage = false;
			} else {
				savePageInLocal(page, parallelPages.getStarterPage().getPageName(), pageNumber, false);
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
		makeDirectories(getBasePath(nameOfPreHomepage), pairOfPages.getPairNumber());
		savePageInLocal(pairOfPages.getMainHomepage(), nameOfPreHomepage, pairOfPages.getPairNumber(), true);
		savePageInLocal(pairOfPages.getHomepageFromList(1), nameOfPreHomepage, pairOfPages.getPairNumber(), false);
	}

	/**
	 * Scarica la pagina facendo attenzione se si sta trattando della
	 * homepage(pageNumber è impostato a zero) oppure di una altra qualsiasi
	 * pagina accoppiabile con la homepage(altro valore di pageNumber).
	 * 
	 * @param page
	 * @param nameOfPrimaryPage
	 * @param pageNumber
	 * @param isHomepage
	 */
	private void savePageInLocal(Page page, String nameOfPrimaryPage, int pageNumber, boolean isHomepage) {
		// cartella dove scaricare la pagina
		String urlBase = getBasePath(nameOfPrimaryPage) + pageNumber;
		try {
			if (isHomepage)// E' l'homepage allora sarà la prima della
							// coppia.
				this.saveAs(page, urlBase + "/" + HOME_PAGE + pageNumber + "-1" + HTML);
			else// E' l'altra pagina allora sarà la seconda della coppia.
				this.saveAs(page, urlBase + "/" + HOME_PAGE + pageNumber + "-2" + HTML);
		} catch (IOException | URISyntaxException e) {
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
	 * @throws URISyntaxException
	 */
	private void saveAs(Page page, String localFilename) throws IOException, URISyntaxException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(localFilename);
			fos.write(page.getDocument().toString().getBytes());
			this.uri2LocalPath.put(page.getUrlRedirect().toURI(), localFilename);
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
	private void makeDirectories(String basePath, int countEntryPoints) {
		new File(basePath + countEntryPoints).mkdirs();
	}

	/**
	 * Cancella le cartelle di output relative a RR e alle pagine HTML scaricate
	 * in locale.
	 * 
	 * @param nameFolder
	 */
	public void deleteOutput(String nameFolder) {
		try {
			FileUtils.deleteDirectory(new File(configuration.getStringOfFolderOutput()));
			FileUtils.deleteDirectory(new File(configuration.getStringOfFolderForHtmlPages() + nameFolder));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Crea le cartelle necessarie a RoadRunner e copia il file di style.
	 * 
	 * @param folder
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void createFolderAndCopyStyleFile(String folder) throws FileNotFoundException, IOException {
		ConfigurationProperties confProp = ConfigurationProperties.getInstance();
		new File(confProp.getStringOfFolderOutput()).mkdir();
		new File(confProp.getStringOfFolderOutput() + "/" + folder).mkdir();
		new File(confProp.getStringOfFolderOutput() + "/" + folder + "/" + confProp.getStringOfContainerDataXSLCopy())
				.mkdir();
		File dbOrig = new File(confProp.getStringOfPathDataXSL());
		File dbCopy = new File(
				confProp.getStringOfFolderOutput() + "/" + folder + confProp.getStringOfPathDataXSLCopy());
		InputStream in = new FileInputStream(dbOrig);
		OutputStream out = new FileOutputStream(dbCopy);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();

		File dbOrig2 = new File(confProp.getStringOfIndexXSL());
		File dbCopy2 = new File(
				confProp.getStringOfFolderOutput() + "/" + folder + confProp.getStringOfPathIndexXSLCopy());
		InputStream in2 = new FileInputStream(dbOrig2);
		OutputStream out2 = new FileOutputStream(dbCopy2);
		byte[] buf2 = new byte[1024];
		int len2;
		while ((len2 = in2.read(buf2)) > 0) {
			out2.write(buf2, 0, len2);
		}
		in2.close();
		out2.close();
	}
}
