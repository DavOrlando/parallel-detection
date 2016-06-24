package src.it.uniroma3.parallel.adapter;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import it.uniroma3.parallel.configuration.ConfigurationProperties;
import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.utils.Utils;

/**
 * Adapter per la fase successiva. Ci sono alcune politiche da comprendere:
 * lancio del crawler su gruppi di 5 anzichè coppie nel caso dell'euristica
 * hreflang. Nella fase successiva di refactoring questo package andrà pulito o
 * eliminato del tutto.
 * 
 * @author davideorlando
 *
 */
public class CrawlingFrancElefanteAdapter implements CrawlingAdapter {

	@Override
	public void crawl(ParallelPages parallelPages, Integer lengthGroupOfEntryPoint, int depthT, Lock errorLogLock,
			String nameFolder, Lock productivityLock, Lock timeLock) throws IOException {
		int countEntryPoints = 0;
		for (List<String> currentGroupEP : getGroupOfEntryPoints(lengthGroupOfEntryPoint, parallelPages)) {
			// creo l'oggetto parallelCollection con il gruppetto di
			// entry points paralleli corrente
			ParallelCollections parallelColl = new ParallelCollections(nameFolder + countEntryPoints, currentGroupEP,
					depthT, parallelPages.getStarterPage().getURLString());
			// incremento l'id della collezione di entry points
			// corrente, per dare nomi diversi alle collezioni di file
			// di output future
			countEntryPoints++;
			// lancio la fase di crawling
			try {
				// R2cursiveCrawling.recursiveCrawling(parallelColl,depthT,errorLogLock,productivityLock,timeLock);
			} catch (Exception e) {
				e.printStackTrace();
				synchronized (errorLogLock) {
					Utils.csvWr(parallelPages.getStarterPage().getURLString(), e,
							ConfigurationProperties.getInstance().getStringOfErrorLogCSV());
				}
			}
		}
	}

	/***
	 * Ritorna un insieme di liste in cui ciascuna ha al massimo un numero di
	 * URL paralleli.
	 * 
	 * @param numberMaxOfEntryPoints
	 *            il numero di entry points massimi che vogliamo in ciascuna
	 *            lista.
	 * @return
	 */
	public Set<List<String>> getGroupOfEntryPoints(int numberMaxOfEntryPoints, ParallelPages parallelPages) {
		Set<List<String>> group = new HashSet<>();
		List<String> listOfNumberedURL = new LinkedList<String>();
		for (URI uri : parallelPages.getParallelURLs()) {
			if (listOfNumberedURL.size() == numberMaxOfEntryPoints) {
				group.add(listOfNumberedURL);
				listOfNumberedURL = new LinkedList<String>();
			}
			listOfNumberedURL.add(uri.toString());
		}
		if (listOfNumberedURL.size() != 0)
			group.add(listOfNumberedURL);
		return group;

	}

}
