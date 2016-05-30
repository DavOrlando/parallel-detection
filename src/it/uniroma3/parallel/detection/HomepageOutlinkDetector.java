package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.net.URL;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.GroupOfHomepages;
import it.uniroma3.parallel.model.GroupOfParallelUrls;
import it.uniroma3.parallel.model.Homepage;
import it.uniroma3.parallel.utils.Utils;

public class HomepageOutlinkDetector extends OutlinkDetector {

	public HomepageOutlinkDetector(Lock errorLogLock) {
		this.errorLogLock = errorLogLock;
	}

	/**
	 * Si cercano nella homepage del sito passato come parametro dei link
	 * uscenti che portano a pagine di linguaggio differente e stessa struttura
	 * del DOM, euristica che ci porta a dire che il sito è multilingua. Si
	 * collezionano tutti questi URL, che rappresentano gli URL verso le pagine
	 * nelle varie lingue.
	 * 
	 * @see GroupOfParallelUrls
	 * @param homepage
	 *            la pagina che rappresenta l'homepage del sito
	 * @return GroupOfParallelUrls
	 * @throws IOException
	 */

	@Override
	public GroupOfParallelUrls detect(Homepage homepage) throws IOException, InterruptedException, LangDetectException {
		// da ritornare alla fine
		GroupOfParallelUrls parallelHomepageUrl = new GroupOfParallelUrls();
		GroupOfHomepages groupOfHomepage = new GroupOfHomepages(homepage);
		downloadPagesInLocal(groupOfHomepage);
		runRoadRunner(groupOfHomepage);
		// mappa link visitati e path locale dove risiedono in locale
		Map<String, String> localPath2url = groupOfHomepage.getLocalPath2Url();
		// controllo ora l'output di rr, se lingua pagine accoppiate è diversa e
		// se hanno abbastanza label
		try {
			// lista dei file accoppiabili
			List<String> list = new LinkedList<String>();
			// lancio metodo che ritorna lista file (in locale) accoppiabili con
			// la home, sfoltisce ancora
			list.addAll(langDetectAndThresholdLabel(groupOfHomepage,errorLogLock));
			for (String outlink : list) {
				parallelHomepageUrl.addURL(new URL(localPath2url.get(outlink)));
			}
		} catch (LangDetectException e) {
			e.printStackTrace();
			synchronized (errorLogLock) {
				Utils.csvWr(new String[] { homepage.getUrl().toString(), e.toString() }, ERROR_LOG_CSV);
			}
		}
		// delete dei file output RR creati con questo metodo
		for (String ftv : groupOfHomepage.getFileToVerify())
			Utils.deleteDir("output/" + ftv);
		// System.out.println("RPE " +resultsPageExploration);

		return parallelHomepageUrl;
	}



}