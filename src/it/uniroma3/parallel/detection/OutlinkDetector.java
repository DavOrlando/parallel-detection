package it.uniroma3.parallel.detection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;

import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.roadrunner.RoadRunnerInvocator;

/**
 * Classe che rappresenta un rilevatore di siti multilingua attraverso
 * l'euristica della ricerca fra gli outlink di pagine parallele e multilingua.
 * 
 * 
 * @author davideorlando
 *
 */

public abstract class OutlinkDetector extends MultilingualDetector {

	protected static final String USER_AGENT = "Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1";
	protected static final String ERROR_LOG_CSV = "ErrorLog.csv";
	protected static final String OUTPUT = "output";
	protected static final String HTML_PAGES_PRELIMINARY = "htmlPagesPreliminary";

	protected Lock errorLogLock;




	/**
	 * Lancia RoadRunner sul gruppo di homepage. Ovvero divide il gruppo in
	 * coppie (HomepagePrimitiva,HomepageTrovata) e su questa coppia lancia
	 * RoadRunner. La coppia avrà associato l'output di RoadRunner relativo.
	 * 
	 * @param groupOfHomepage
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected void runRoadRunner(ParallelPages groupOfHomepage)
			throws FileNotFoundException, IOException, InterruptedException {
		for (PairOfPages pair : groupOfHomepage.getListOfPairs()) {
			RoadRunnerInvocator.launchRR(pair, errorLogLock,groupOfHomepage.getStarterPage());
		}
	}

	
	
}
