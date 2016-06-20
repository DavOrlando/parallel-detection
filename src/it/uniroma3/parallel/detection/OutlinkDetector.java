package it.uniroma3.parallel.detection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.roadrunner.RoadRunnerDataSet;
import it.uniroma3.parallel.roadrunner.RoadRunnerInvocator;
import it.uniroma3.parallel.utils.FetchManager;
import it.uniroma3.parallel.utils.Utils;

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
