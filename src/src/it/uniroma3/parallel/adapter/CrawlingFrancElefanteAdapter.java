package src.it.uniroma3.parallel.adapter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;

import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.utils.Utils;

public class CrawlingFrancElefanteAdapter implements CrawlingAdapter {

	private static final String ERROR_LOG_CSV = "ErrorLog.csv";

	@Override
	public void crawl(ParallelPages groupOfHomepages, int lengthGroupOfEntryPoint, int depthT,
			Lock errorLogLock, String nameFolder, Lock productivityLock, Lock timeLock) throws IOException {
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
				//R2cursiveCrawling.recursiveCrawling(parallelColl,depthT,errorLogLock,productivityLock,timeLock);
			} catch (Exception e) {
				e.printStackTrace();
				synchronized (errorLogLock) {
					Utils.csvWr(groupOfHomepages.getStarterPage().getURLString(), e, ERROR_LOG_CSV);
				}
			}
		}
	}
}
