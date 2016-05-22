package it.uniroma3.parallel.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class DownloadManager {
	private static final String HTML = ".html";
	private static final String HOME_PAGE = "HomePage";
	private static final String USER_AGENT = "Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1";
	public static final String HTML_PAGES_PRELIMINARY = "htmlPagesPreliminary";
	private String basePath;

	/**
	 * Gestore dei download per questo Sistema.
	 */
	public DownloadManager() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Costruisce un oggetto DownloadManager dandogli anche la base del percorso
	 * in cui andrà a scaricare le pagine.
	 * 
	 * @param nameFolder
	 *            per poi usarlo in questo modo: HTML_PAGES_PRELIMINARY +
	 *            nameFolder + "/" + nameFolder
	 */
	public DownloadManager(String nameFolder) {
		this.basePath = HTML_PAGES_PRELIMINARY + nameFolder + "/" + nameFolder;
	}

	public String getBasePath() {
		return basePath;
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
	 */
	public void makeDirectories(int countEntryPoints) {
		new File(basePath + countEntryPoints).mkdirs();
	}

	/**
	 * Scarica la pagina facendo attenzione se si sta trattando della
	 * homepage(pageNumber è impostato a zero) oppure di una altra qualsiasi
	 * pagina accoppiabile con la homepage(altro valore di pageNumber).
	 * 
	 * @param page
	 * @param pageNumber
	 * @param isHomepage 
	 */
	public void download(Page page, int pageNumber, boolean isHomepage) {
		// cartella dove scaricare la pagina
		String urlBase = this.getBasePath() + pageNumber;
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
	public void downloadPageInto(Page page, String localFilename) throws IOException {
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			URLConnection urlConn = page.getUrlRedirect().openConnection();
			urlConn.setReadTimeout(2000);
			if (USER_AGENT != null) {
				urlConn.setRequestProperty("User-Agent", USER_AGENT);
			}
			is = urlConn.getInputStream();
			fos = new FileOutputStream(localFilename);
			byte[] buffer = new byte[4096];
			int len;
			while ((len = is.read(buffer)) > 0) {

				fos.write(buffer, 0, len);
			}
			page.setLocalPath(localFilename);
		} finally {
			try {
				if (is != null)
					is.close();
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		}
	}
}
