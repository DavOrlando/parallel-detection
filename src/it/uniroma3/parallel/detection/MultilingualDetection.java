package it.uniroma3.parallel.detection;

import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.model.PreHomepage;
import it.uniroma3.parallel.model.Homepage;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.utils.FetchManager;
import it.uniroma3.parallel.utils.Utils;
import src.it.uniroma3.parallel.adapter.ParallelCollections;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import com.cybozu.labs.langdetect.LangDetectException;


/**
 * Classe che utilizza tre euristiche per comprendere se il sito in input è
 * multilingua e parallelo. Nel caso affermativo l'output saranno le coppie di
 * homepage multilingua e parallele che il crawler userà per iniziare la sua
 * scansione.
 * 
 * @author davideorlando
 *
 */
public class MultilingualDetection {

	private static final String OUTPUT = "output";
	private static final String PRE_HOMEPAGE = "PreHomepage";
	private static final String MULT_DETECTION_PREHOMEPAGE = "mult detection prehomepage";
	private static final String HTML_PAGES_PRELIMINARY = "htmlPagesPreliminary";
	private static final String VISIT_HOMEPAGE = "visitHomepage";
	private static final String MULT_DETECTION_OUTLINKS = "mult detection outlinks";
	private static final String ERROR_LOG_CSV = "ErrorLog.csv";
	private static final String HOMEPAGE_HREF_LANG = "homepageHrefLang";
	private static final String SITE_MULTILINGUAL_CSV = "SiteMultilingual.csv";
	private static final String TIME_CSV = "time.csv";
	private static final String MULT_DETECTION_HREFLANG = "mult detection hreflang";
	private static final String SITE_NOT_MULTILINGUAL_CSV = "SiteNotMultilingual.csv";
	private static final int MAX_LENGTH_GROUP_HREFLANG = 5;
	private static final int PAIR_FOR_OUTLINK = 2;



	// argomenti: homepage e depth di visita, lock scrivere txt errori e siti
	// mult o not mult rilevati
	// trova entry points e li passa al metodo di crawling effettivo
	public static void multilingualDetection(String homepageStringUrl, int depthT, Lock multSiteLogLock,
			Lock errorLogLock, Lock productivityLock, Lock timeLock)
			throws IOException, InterruptedException, LangDetectException {
		long startDetectionTime = Utils.getTime();

		// blocco try in cui ci sono le tre euristiche
		try {

			// l'homepage su cui si fa la detection
			Page homepageToDetect = new Homepage(homepageStringUrl);

			// detector per Hreflang
			MultilingualDetector multilingualDetector = new HreflangDetector();

			// controllo per escludere alcuni siti falsi positivi multilingua
			if (multilingualDetector.isInBlacklist(homepageToDetect.getUrlRedirect())) {
				synchronized (multSiteLogLock) {
					// scrivo su un csv che il sito non è multilingua
					Utils.csvWr(new String[] { homepageStringUrl, Utils.getDate() }, SITE_NOT_MULTILINGUAL_CSV);
				}
				return;
			}

			// Prendo il nome della cartella di output dall'URL della homepage
			String nameFolder = homepageToDetect.getPageName();
			ParallelPages groupOfHomepages;

			long startTime = Utils.getTime();
			groupOfHomepages = multilingualDetector.detect(homepageToDetect);
			long endTime = Utils.getTime();

			synchronized (timeLock) {
				Utils.csvWr(
						new String[] { homepageStringUrl, "", MULT_DETECTION_HREFLANG, "",
								Long.toString(endTime - startTime) },
						java.lang.Thread.currentThread().toString() + TIME_CSV);
			}
			if (groupOfHomepages != null) {
				long endDetectionTime = Utils.getTime();
				synchronized (multSiteLogLock) {
					Utils.csvWr(new String[] { homepageStringUrl, HOMEPAGE_HREF_LANG,
							Long.toString(endDetectionTime - startDetectionTime) }, SITE_MULTILINGUAL_CSV);
				}
				// su gruppi di 5 viene lanciata la visita ricorsiva
				recursiveCrawler(groupOfHomepages, MAX_LENGTH_GROUP_HREFLANG, depthT, errorLogLock, nameFolder);
				return;
			}
			// detection con euristica degli outlink
			multilingualDetector = new HomepageOutlinkDetector();

			startTime = Utils.getTime();
			groupOfHomepages = multilingualDetector.detect(homepageToDetect);
			endTime = Utils.getTime();

			synchronized (timeLock) {
				Utils.csvWr(
						new String[] { homepageStringUrl, "", MULT_DETECTION_OUTLINKS, "",
								Long.toString(endTime - startTime) },
						java.lang.Thread.currentThread().toString() + TIME_CSV);
			}
			// se ho coppie candidate lancio visita ricorsiva
			if (!groupOfHomepages.isEmpty()) {
				long endDetectionTime = Utils.getTime();
				synchronized (multSiteLogLock) {
					Utils.csvWr(new String[] { homepageStringUrl, VISIT_HOMEPAGE,
							Long.toString(endDetectionTime - startDetectionTime) }, SITE_MULTILINGUAL_CSV);
				}
				// pulisco le folder di output e di crawling
				FetchManager.getInstance().deleteFolders(nameFolder);
				recursiveCrawler(groupOfHomepages, PAIR_FOR_OUTLINK, depthT, errorLogLock, nameFolder);
				return;
			}

			FetchManager.getInstance().deleteFolders(nameFolder);

			// se ancora non ho reperito entry points, provo a rilevare
			// eventuali preHomepage
			// (con link uscenti paralleli tra loro, ciascuno che porta ad una
			// lingua diversa)
			startTime = Utils.getTime();
			PreHomepageOutlinkDetector preHomepageOutlinkDetector = new PreHomepageOutlinkDetector();
			groupOfHomepages = preHomepageOutlinkDetector.detect(new PreHomepage(homepageStringUrl));
			endTime = Utils.getTime();

			synchronized (timeLock) {
				Utils.csvWr(
						new String[] { homepageStringUrl, "", MULT_DETECTION_PREHOMEPAGE, "",
								Long.toString(endTime - startTime) },
						java.lang.Thread.currentThread().toString() + TIME_CSV);
			}

			if (groupOfHomepages != null && groupOfHomepages.getListOfPairs().size() != 0) {
				synchronized (multSiteLogLock) {
					long endDetectionTime = Utils.getTime();
					Utils.csvWr(new String[] { homepageStringUrl, PRE_HOMEPAGE,
							Long.toString(endDetectionTime - startDetectionTime) }, SITE_MULTILINGUAL_CSV);
				}
				// elimino folder di output e di crawling
				Utils.deleteDir(HTML_PAGES_PRELIMINARY + nameFolder);
				Utils.deleteDir(OUTPUT);
				recursiveCrawler(groupOfHomepages, PAIR_FOR_OUTLINK, depthT, errorLogLock, nameFolder);
				return;
			}

			// se neanche con ultimo metodo non si trova nulla si considera il
			// sito non mutlilingua
			else {

				try {
					Utils.deleteDir(HTML_PAGES_PRELIMINARY + nameFolder);
				} catch (Exception e) {
					e.printStackTrace();
					synchronized (errorLogLock) {
						// stampo nell'error log il sito che da il problema e
						// l'errore
						Utils.csvWr(homepageStringUrl, e, ERROR_LOG_CSV);
					}
				}

				synchronized (multSiteLogLock) {
					long endDetectionTime = Utils.getTime();

					Utils.csvWr(
							new String[] { homepageStringUrl, Long.toString(endDetectionTime - startDetectionTime) },
							SITE_NOT_MULTILINGUAL_CSV);
				}
				// System.out.println("FINE STEP 3 PREHOMEPAGE");

				return;
			}
		}

		// in caso di errori, scrivo sul fiel di log e considero il sito non
		// multilingua
		catch (Exception e) {
			e.printStackTrace();
			synchronized (errorLogLock) {
				Utils.csvWr(homepageStringUrl, e, ERROR_LOG_CSV);
			}
			{
				synchronized (multSiteLogLock) {
					long endDetectionTime = Utils.getTime();

					Utils.csvWr(
							new String[] { homepageStringUrl, Long.toString(endDetectionTime - startDetectionTime) },
							SITE_NOT_MULTILINGUAL_CSV);
				}
				// System.out.println("FINE STEP 3");
				return;
			}
		}

	}// fine main

	private static void recursiveCrawler(ParallelPages groupOfHomepages, int lengthGroupOfEntryPoint, int depthT,
			Lock errorLogLock, String nameFolder) throws IOException {
		int countEntryPoints = 0;
		for (List<String> currentGroupEP : groupOfHomepages.getGroupOfEntryPoints(lengthGroupOfEntryPoint)) {

			// creo l'oggetto parallelCollection con il gruppetto di
			// entry points paralleli corrente
			ParallelCollections parallelColl = new ParallelCollections(nameFolder + countEntryPoints, currentGroupEP,
					depthT, groupOfHomepages.getStarterPage().getURLString());

			// incremento l'id della collezione di entry points
			// corrente, per dare nomi diversi alle collezioni di file
			// di output future
			countEntryPoints++;
			// lancio rr
			try {
				// R2cursiveCrawling.recursiveCrawling(parallelColl,depthT,errorLogLock,productivityLock,timeLock);
			} catch (Exception e) {
				e.printStackTrace();
				synchronized (errorLogLock) {
					Utils.csvWr(groupOfHomepages.getStarterPage().getURLString(), e, ERROR_LOG_CSV);
				}
			}
		}
	}
}