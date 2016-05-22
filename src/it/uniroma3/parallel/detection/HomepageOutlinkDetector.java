package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.DownloadManager;
import it.uniroma3.parallel.model.GroupOfParallelUrls;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.utils.UrlUtil;
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

	public GroupOfParallelUrls detect(Page homepage) throws IOException, InterruptedException, LangDetectException {
		GroupOfParallelUrls parallelHomepageUrl = new GroupOfParallelUrls();
		// creo set dove mettere coppie trovate, (set di set(coppie))
		Set<Set<String>> resultsPageExploration = new HashSet<Set<String>>();

		// lista con link che andranno a fare coppia con la homepage
		List<String> outlinkToVisit = this.getMultilingualOutlink(homepage);
		
		List<String> fileToVerify = new ArrayList<String>();
		// mappa link visitati e path locale dove risiedono in locale
		Map<String, String> localPath2url = detectOutlinkWithRR(homepage, outlinkToVisit, fileToVerify);

		// controllo ora l'output di rr, se lingua pagine accoppiate è diversa e
		// se hanno abbastanza label,
		try {
			// lista dei file accoppiabili
			List<String> list = new ArrayList<String>();

			// lancio metodo che ritorna lista file (in locale) accoppiabili con
			// la home
			list.addAll(langDetectAndThresholdLabel(homepage.getNameFolder(), fileToVerify, errorLogLock, homepage));

			// System.out.println("ASDD "+list);

			for (String outlink : list) {
				Set<String> currPair = new HashSet<String>();
				currPair.add(homepage.getUrlRedirect().toString());
				currPair.add(localPath2url.get(outlink));
				resultsPageExploration.add(currPair);
			}

		} catch (LangDetectException e) {
			e.printStackTrace();
			synchronized (errorLogLock) {
				Utils.csvWr(new String[] { homepage.getUrl().toString(), e.toString() }, ERROR_LOG_CSV);
			}
		}

		// delete dei file output RR creati con questo metodo
		for (String ftv : fileToVerify)
			Utils.deleteDir("output/" + ftv);

		// System.out.println("RPE " +resultsPageExploration);

		return parallelHomepageUrl;
	}

}