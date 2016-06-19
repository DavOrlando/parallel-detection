package it.uniroma3.parallel.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import it.uniroma3.parallel.model.GroupOfHomepages;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PairOfHomepages;
import it.uniroma3.parallel.roadrunner.RoadRunnerDataSet;

public class FetchManager {
	private static final String HTML = ".html";
	private static final String HOME_PAGE = "HomePage";
	private static final String USER_AGENT = "Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1";
	private static final String HTML_PAGES_PRELIMINARY = "htmlPagesPreliminary";
	public static FetchManager instance;
	private Map<URL, String> url2LocalPath;
	private Map<PairOfHomepages,RoadRunnerDataSet> pair2RRDataSet;

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
	public void persistGroupOfHomepage(GroupOfHomepages groupOfHomepage) {
		String nameFolder=groupOfHomepage.getPrimaryHomepage().getPageName() ;
		String basePath = HTML_PAGES_PRELIMINARY + nameFolder + "/" + nameFolder;
		int pageNumber = 1;
		boolean isHomepage = true;
		// scarico le possibili homepage
		for (Page page : groupOfHomepage.getCandidateParallelHomepages()) {
			makeDirectories(pageNumber,basePath);
			// segno l'homepage
			if (isHomepage) {
				download(groupOfHomepage.getPrimaryHomepage(),basePath, pageNumber, true);
				isHomepage = false;
			} else {
				download(page,basePath, pageNumber, false);
				pageNumber++;
			}
		}

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
	// private void downloadPageIntoOldVersion(Page page, String localFilename)
	// throws IOException {
	// InputStream is = null;
	// FileOutputStream fos = null;
	// try {
	// URLConnection urlConn = page.getUrlRedirect().openConnection();
	// urlConn.setReadTimeout(2000);
	// if (USER_AGENT != null) {
	// urlConn.setRequestProperty("User-Agent", USER_AGENT);
	// }
	// is = urlConn.getInputStream();
	// fos = new FileOutputStream(localFilename);
	// byte[] buffer = new byte[4096];
	// int len;
	// while ((len = is.read(buffer)) > 0) {
	// fos.write(buffer, 0, len);
	// }
	// //page.setLocalPath(localFilename);
	// } finally {
	// try {
	// if (is != null)
	// is.close();
	// } finally {
	// if (fos != null) {
	// fos.close();
	// }
	// }
	// }
	// }

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
	 * Aggiunge un dataset di RoadRunner relativo ad una coppia di homepage alla mappa (coppieDiHomepage,RRDataSet).
	 * @param pairOfHomepage
	 * @param roadRunnerDataSet
	 */
	public void addRRDataSet(PairOfHomepages pairOfHomepage, RoadRunnerDataSet roadRunnerDataSet) {
		this.pair2RRDataSet.put(pairOfHomepage, roadRunnerDataSet);
	}
	
	/**
	 * Ritorna il dataset di RoadRunner relativo alla pair di homepage.
	 * @param pairOfHomepages
	 * @return
	 */
	public RoadRunnerDataSet getRoadRunnerDataSet(PairOfHomepages pairOfHomepages){
		return this.pair2RRDataSet.get(pairOfHomepages);
	}
}
