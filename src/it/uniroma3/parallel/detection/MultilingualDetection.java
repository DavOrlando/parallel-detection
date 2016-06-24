package it.uniroma3.parallel.detection;

import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.model.PreHomepage;
import it.uniroma3.parallel.configuration.ConfigurationProperties;
import it.uniroma3.parallel.model.Homepage;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.utils.FetchManager;
import it.uniroma3.parallel.utils.Utils;
import src.it.uniroma3.parallel.adapter.CrawlingAdapter;
import src.it.uniroma3.parallel.adapter.CrawlingFrancElefanteAdapter;

import java.io.IOException;
import java.util.Calendar;
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

	private static final String STRINGA_VUOTA = "";

	// argomenti: homepage e depth di visita, lock scrivere txt errori e siti
	// mult o not mult rilevati
	// trova entry points e li passa al metodo di crawling effettivo
	public static void multilingualDetection(String homepageStringUrl, int depthT, Lock multSiteLogLock,
			Lock errorLogLock, Lock productivityLock, Lock timeLock)
			throws IOException, InterruptedException, LangDetectException {

		ConfigurationProperties configuration = ConfigurationProperties.getInstance();

		long startDetectionTime = Calendar.getInstance().getTimeInMillis();
		// blocco try in cui ci sono le tre euristiche
		try {
			CrawlingAdapter crawling = new CrawlingFrancElefanteAdapter();
			// l'homepage su cui si fa la detection
			Page homepageToDetect = new Homepage(homepageStringUrl);
			// detector per Hreflang
			MultilingualDetector multilingualDetector = new HreflangDetector();
			// controllo per escludere alcuni siti falsi positivi multilingua
			if (multilingualDetector.isInBlacklist(homepageToDetect.getUrlRedirect())) {
				synchronized (multSiteLogLock) {
					// scrivo su un csv che il sito non è multilingua
					Utils.csvWr(new String[] { homepageStringUrl, Utils.getDate() },
							configuration.getStringOfSiteNotMultilingual());
				}
				return;
			}
			// Prendo il nome della cartella di output dall'URL della homepage
			String nameFolder = homepageToDetect.getPageName();
			ParallelPages groupOfHomepages;
			long startTime = Calendar.getInstance().getTimeInMillis();
			groupOfHomepages = multilingualDetector.detect(homepageToDetect);
			long endTime = Calendar.getInstance().getTimeInMillis();
			synchronized (timeLock) {
				Utils.csvWr(
						new String[] { homepageStringUrl, STRINGA_VUOTA, configuration.getStringOfMultDetecionHreflang(), STRINGA_VUOTA,
								Long.toString(endTime - startTime) },
						java.lang.Thread.currentThread().toString() + configuration.getStringOfTimeCSV());
			}
			if (groupOfHomepages != null) {
				long endDetectionTime = Calendar.getInstance().getTimeInMillis();
				synchronized (multSiteLogLock) {
					Utils.csvWr(
							new String[] { homepageStringUrl, configuration.getStringOfHomepageHreflang(),
									Long.toString(endDetectionTime - startDetectionTime) },
							configuration.getStringOfSiteMultilingual());
				}
				// su gruppi di 5 viene lanciata la visita ricorsiva
				crawling.crawl(groupOfHomepages, configuration.getIntOfEntryNumberHreflang(), depthT, errorLogLock,
						nameFolder, productivityLock, timeLock);
				return;
			}
			// detection con euristica degli outlink
			multilingualDetector = new HomepageOutlinkDetector();
			startTime = Calendar.getInstance().getTimeInMillis();
			groupOfHomepages = multilingualDetector.detect(homepageToDetect);
			endTime = Calendar.getInstance().getTimeInMillis();
			synchronized (timeLock) {
				Utils.csvWr(
						new String[] { homepageStringUrl, STRINGA_VUOTA, configuration.getStringOfMultDetectionOutlinks(), STRINGA_VUOTA,
								Long.toString(endTime - startTime) },
						java.lang.Thread.currentThread().toString() + configuration.getStringOfTimeCSV());
			}
			// se ho coppie candidate lancio visita ricorsiva
			if (!groupOfHomepages.isEmpty()) {
				long endDetectionTime = Calendar.getInstance().getTimeInMillis();
				synchronized (multSiteLogLock) {
					Utils.csvWr(
							new String[] { homepageStringUrl, configuration.getStringOfVisitHomepage(),
									Long.toString(endDetectionTime - startDetectionTime) },
							configuration.getStringOfSiteMultilingual());
				}
				// pulisco le folder di output e di crawling
				FetchManager.getInstance().deleteFolders(nameFolder);
				crawling.crawl(groupOfHomepages, configuration.getIntOfEntryNumberOutlink(), depthT, errorLogLock,
						nameFolder, productivityLock, timeLock);
				return;
			}
			FetchManager.getInstance().deleteFolders(nameFolder);
			// se ancora non ho reperito entry points, provo a rilevare
			// eventuali preHomepage
			// (con link uscenti paralleli tra loro, ciascuno che porta ad una
			// lingua diversa)
			startTime = Calendar.getInstance().getTimeInMillis();
			PreHomepageOutlinkDetector preHomepageOutlinkDetector = new PreHomepageOutlinkDetector();
			groupOfHomepages = preHomepageOutlinkDetector.detect(new PreHomepage(homepageStringUrl));
			endTime = Calendar.getInstance().getTimeInMillis();
			synchronized (timeLock) {
				Utils.csvWr(
						new String[] { homepageStringUrl, STRINGA_VUOTA, configuration.getStringOfMultDetectionPrehomepage(), STRINGA_VUOTA,
								Long.toString(endTime - startTime) },
						java.lang.Thread.currentThread().toString() + configuration.getStringOfTimeCSV());
			}
			if (groupOfHomepages != null && groupOfHomepages.getListOfPairs().size() != 0) {
				synchronized (multSiteLogLock) {
					long endDetectionTime = Calendar.getInstance().getTimeInMillis();
					Utils.csvWr(
							new String[] { homepageStringUrl, configuration.getStringOfPrehomepage(),
									Long.toString(endDetectionTime - startDetectionTime) },
							configuration.getStringOfSiteMultilingual());
				}
				// elimino folder di output e di crawling
				FetchManager.getInstance().deleteFolders(nameFolder);
				crawling.crawl(groupOfHomepages, configuration.getIntOfEntryNumberOutlink(), depthT, errorLogLock,
						nameFolder, productivityLock, timeLock);
				return;
			}
			// se neanche con ultimo metodo non si trova nulla si considera il
			// sito non mutlilingua
			else {
				try {
					FetchManager.getInstance().deleteFolders(nameFolder);
				} catch (Exception e) {
					e.printStackTrace();
					synchronized (errorLogLock) {
						// stampo nell'error log il sito che da il problema e
						// l'errore
						Utils.csvWr(homepageStringUrl, e, configuration.getStringOfErrorLogCSV());
					}
				}
				synchronized (multSiteLogLock) {
					long endDetectionTime = Calendar.getInstance().getTimeInMillis();
					Utils.csvWr(
							new String[] { homepageStringUrl, Long.toString(endDetectionTime - startDetectionTime) },
							configuration.getStringOfSiteNotMultilingual());
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
				Utils.csvWr(homepageStringUrl, e, configuration.getStringOfErrorLogCSV());
			}
			{
				synchronized (multSiteLogLock) {
					long endDetectionTime = Calendar.getInstance().getTimeInMillis();
					Utils.csvWr(
							new String[] { homepageStringUrl, Long.toString(endDetectionTime - startDetectionTime) },
							configuration.getStringOfSiteNotMultilingual());
				}
				// System.out.println("FINE STEP 3");
				return;
			}
		}
	}
}