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


	// OK
	// metodo per creare file style per output di rr
	public static void backupFile(String folder) throws FileNotFoundException, IOException {
		new File(OUTPUT).mkdir();

		String pathF = "output/" + folder;

		new File(pathF).mkdir();

		String pathF2 = "output/" + folder + "/style";

		new File(pathF2).mkdir();

		File dbOrig = new File("translated/style/data.xsl");
		File dbCopy = new File("output/" + folder + "/style/data.xsl");
		InputStream in = new FileInputStream(dbOrig);
		OutputStream out = new FileOutputStream(dbCopy);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();

		File dbOrig2 = new File("translated/style/index.xsl");
		File dbCopy2 = new File("output/" + folder + "/style/index.xsl");
		InputStream in2 = new FileInputStream(dbOrig2);
		OutputStream out2 = new FileOutputStream(dbCopy2);
		byte[] buf2 = new byte[1024];
		int len2;
		while ((len2 = in2.read(buf2)) > 0) {
			out2.write(buf2, 0, len2);
		}
		in2.close();
		out2.close();

	}




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
