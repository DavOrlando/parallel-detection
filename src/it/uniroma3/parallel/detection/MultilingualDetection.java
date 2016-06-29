package it.uniroma3.parallel.detection;


import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.model.PreHomepage;
import it.uniroma3.parallel.adapter.CrawlingAdapter;
import it.uniroma3.parallel.adapter.CrawlingFrancElefanteAdapter;
import it.uniroma3.parallel.configuration.ConfigurationProperties;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.utils.FetchManager;
import it.uniroma3.parallel.utils.Utils;


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

	
	public void multilingualDetection(String homepageStringUrl, int depthT, Lock multSiteLogLock,
			Lock errorLogLock, Lock productivityLock, Lock timeLock)
			throws IOException, InterruptedException, LangDetectException {

		ConfigurationProperties properties = ConfigurationProperties.getInstance();

		long startDetectionTime = Calendar.getInstance().getTimeInMillis();
		// blocco try in cui ci sono le tre euristiche
		try {
			CrawlingAdapter crawling = new CrawlingFrancElefanteAdapter();
			// l'homepage su cui si fa la detection
			Page homepageToDetect = new Page(homepageStringUrl);
			// detector per la prima euristica
			MultilingualDetector multilingualDetector = new HreflangDetector();
			// controllo per escludere alcuni siti falsi positivi multilingua
			if (multilingualDetector.isInBlacklist(homepageToDetect.getUrlRedirect())) {
				synchronized (multSiteLogLock) {
					// scrivo su un csv che il sito non è multilingua
					Utils.csvWr(new String[] { homepageStringUrl, Utils.getDate() },
							properties.getStringOfPathForSiteNotMultilingual());
				}
				return;
			}
			// Prendo il nome della cartella di output dall'URL della homepage
			String outputName = homepageToDetect.getPageName();
			ParallelPages groupOfHomepages;
			long startTime = Calendar.getInstance().getTimeInMillis();
			groupOfHomepages = multilingualDetector.detect(homepageToDetect);
			long endTime = Calendar.getInstance().getTimeInMillis();
			synchronized (timeLock) {
				Utils.csvWr(
						new String[] { homepageStringUrl, STRINGA_VUOTA, properties.getStringOfMultDetecionHreflang(),
								STRINGA_VUOTA, Long.toString(endTime - startTime) },
						java.lang.Thread.currentThread().toString() + properties.getStringOfPathForTimeCSV());
			}
			if (groupOfHomepages != null) {
				long endDetectionTime = Calendar.getInstance().getTimeInMillis();
				synchronized (multSiteLogLock) {
					Utils.csvWr(
							new String[] { homepageStringUrl, properties.getStringOfHomepageHreflang(),
									Long.toString(endDetectionTime - startDetectionTime) },
							properties.getStringOfPathForSiteMultilingualCSV());
				}
				// su gruppi di 5 viene lanciata la visita ricorsiva
				crawling.crawl(groupOfHomepages, properties.getIntOfEntryNumberHreflang(), depthT, errorLogLock,
						outputName, productivityLock, timeLock);
				return;
			}
			// detector per la seconda euristica
			multilingualDetector = new HomepageOutlinkDetector();
			startTime = Calendar.getInstance().getTimeInMillis();
			groupOfHomepages = multilingualDetector.detect(homepageToDetect);
			endTime = Calendar.getInstance().getTimeInMillis();
			synchronized (timeLock) {
				Utils.csvWr(
						new String[] { homepageStringUrl, STRINGA_VUOTA, properties.getStringOfMultDetectionOutlinks(),
								STRINGA_VUOTA, Long.toString(endTime - startTime) },
						java.lang.Thread.currentThread().toString() + properties.getStringOfPathForTimeCSV());
			}
			// se ho coppie candidate il sito è multilingua
			if (!groupOfHomepages.isEmpty()) {
				long endDetectionTime = Calendar.getInstance().getTimeInMillis();
				synchronized (multSiteLogLock) {
					Utils.csvWr(
							new String[] { homepageStringUrl, properties.getStringOfVisitHomepage(),
									Long.toString(endDetectionTime - startDetectionTime) },
							properties.getStringOfPathForSiteMultilingualCSV());
				}
				// pulisco le folder di output e di crawling
				FetchManager.getInstance().deleteOutput(outputName);
				crawling.crawl(groupOfHomepages, properties.getIntOfEntryNumberOutlink(), depthT, errorLogLock,
						outputName, productivityLock, timeLock);
				return;
			}
			startTime = Calendar.getInstance().getTimeInMillis();
			// detector per la terza euristica
			multilingualDetector = new PreHomepageOutlinkDetector();
			groupOfHomepages = multilingualDetector.detect(new PreHomepage(homepageStringUrl));
			endTime = Calendar.getInstance().getTimeInMillis();
			synchronized (timeLock) {
				Utils.csvWr(
						new String[] { homepageStringUrl, STRINGA_VUOTA,
								properties.getStringOfMultDetectionPrehomepage(), STRINGA_VUOTA,
								Long.toString(endTime - startTime) },
						java.lang.Thread.currentThread().toString() + properties.getStringOfPathForTimeCSV());
			}
			if (groupOfHomepages != null && groupOfHomepages.getListOfPairs().size() != 0) {
				synchronized (multSiteLogLock) {
					long endDetectionTime = Calendar.getInstance().getTimeInMillis();
					Utils.csvWr(
							new String[] { homepageStringUrl, properties.getStringOfPrehomepage(),
									Long.toString(endDetectionTime - startDetectionTime) },
							properties.getStringOfPathForSiteMultilingualCSV());
				}
				// elimino folder di output e di crawling
				FetchManager.getInstance().deleteOutput(outputName);
				crawling.crawl(groupOfHomepages, properties.getIntOfEntryNumberOutlink(), depthT, errorLogLock,
						outputName, productivityLock, timeLock);
				return;
			}
			// altrimenti considero il sito non multilingua
			else {
				FetchManager.getInstance().deleteOutput(outputName);
				synchronized (multSiteLogLock) {
					long endDetectionTime = Calendar.getInstance().getTimeInMillis();
					Utils.csvWr(
							new String[] { homepageStringUrl, Long.toString(endDetectionTime - startDetectionTime) },
							properties.getStringOfPathForSiteNotMultilingual());
				}
				return;
			}
		}
		// in caso di errori considero il sito non multilingua.
		catch (Exception e) {
			e.printStackTrace();
			synchronized (errorLogLock) {
				Utils.csvWr(homepageStringUrl, e, properties.getStringOfErrorLogCSV());
			}
			synchronized (multSiteLogLock) {
				long endDetectionTime = Calendar.getInstance().getTimeInMillis();
				Utils.csvWr(new String[] { homepageStringUrl, Long.toString(endDetectionTime - startDetectionTime) },
						properties.getStringOfPathForSiteNotMultilingual());
			}
			return;
		}
	}
}