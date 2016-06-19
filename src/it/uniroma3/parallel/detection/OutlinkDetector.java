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

import it.uniroma3.parallel.model.GroupOfHomepages;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PairOfHomepages;
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

	private static final int SECONDA_HOMEPAGE = 1;
	protected static final String USER_AGENT = "Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1";
	protected static final String ERROR_LOG_CSV = "ErrorLog.csv";
	protected static final String OUTPUT = "output";
	protected static final String HTML_PAGES_PRELIMINARY = "htmlPagesPreliminary";

	protected Lock errorLogLock;

	/**
	 * Ritorna una collezione di URL dove ognuno corrisponde alla pagina
	 * multilingua e parallela più probabile per quel linguaggio differente
	 * rispetto alla homepage. La pagina è scelta in base al criterio del numero
	 * di label che RoadRunner riesce ad allineare con la homepage vera e
	 * propria. Quindi ogni lingua ci viene ritornato solo l'URL che corrisponde
	 * alla pagina con più label allineate con la homepage.
	 * 
	 * @param groupOfHomepage
	 * @return
	 * @throws IOException
	 * @throws LangDetectException 
	 */
	public Collection<URL> filterByLabel(GroupOfHomepages groupOfHomepage) throws IOException, LangDetectException {
		// memorizzeremo solo l'URL con più label
		Map<String, URL> language2Url = new HashMap<String, URL>();
		language2Url.put(groupOfHomepage.getPrimaryHomepage().getLanguage(), groupOfHomepage.getPrimaryHomepage().getUrlRedirect());
		// il valore è il num di label attuale e sostituiremo un URL in
		// language2Url se e solo se troviamo per quel linguaggio una pagina con
		// più label di quelle attuali
		Map<String, Integer> language2NumberOfLabel = new HashMap<String, Integer>();
		// per ogni coppia di homepage analizzo l'output di RR
		for (PairOfHomepages pair : groupOfHomepage.getListOfPairs()) {
			try {
				RoadRunnerDataSet roadRunnerDataSet = FetchManager.getInstance().getRoadRunnerDataSet(pair);
				if (roadRunnerDataSet == null)
					continue;
				if (roadRunnerDataSet.getNumberOfLabels() < 1)
					continue;
				List<String> textFromAllLabels = roadRunnerDataSet.getTextFromAllLabels();
				if (textFromAllLabels == null)
					continue;
				if (isEnoughText(textFromAllLabels) && isDifferentLanguage(textFromAllLabels)) {
					String languagePage = pair.getHomepageFromList(SECONDA_HOMEPAGE).getLanguage();
					if (language2NumberOfLabel.get(languagePage) == null || language2NumberOfLabel.get(languagePage)
							.compareTo(roadRunnerDataSet.getNumberOfLabels()) < 0) {
						language2Url.put(languagePage, pair.getHomepageFromList(SECONDA_HOMEPAGE).getUrlRedirect());
						language2NumberOfLabel.put(languagePage, roadRunnerDataSet.getNumberOfLabels());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				synchronized (errorLogLock) {
					Utils.csvWr(new String[] { pair.getMainHomepage().getURLString(), e.toString() }, ERROR_LOG_CSV);
				}
			}
		}
		return language2Url.values();
	}

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
	 * Ritorna true se i testi sono in lingua differente.
	 * 
	 * @param testiConcatenati
	 * @return
	 * @throws LangDetectException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean isDifferentLanguage(List<String> testiConcatenati)
			throws LangDetectException, ParserConfigurationException, SAXException, IOException, InterruptedException {
		// verifico che un set creato con i linguaggi dei testi abbia una
		// cardinalitò uguale al numero di testi.Ovvero abbiamo tutte lingue
		// differenti.
		return this.getLanguageSet(testiConcatenati).size() == testiConcatenati.size();
	}

	private Set<String> getLanguageSet(List<String> testiConcatenati) throws LangDetectException {
		Set<String> setOfLanguages = new HashSet<>();
		// carico i profili delle lingue
		if (DetectorFactory.getLangList().size() == 0)
			DetectorFactory.loadProfile("profiles.sm");
		for (String testo : testiConcatenati) {
			String langDetect = textLanguageDetection(testo);
			setOfLanguages.add(langDetect);
		}
		return setOfLanguages;
	}

	private static String textLanguageDetection(String testo) throws LangDetectException {
		// risultati della language detection(en, it, ...)
		String langDetect = "";
		// detect su stringone
		Detector detector = DetectorFactory.create();
		detector.append(testo);
		langDetect = detector.detect().toString();
		// aggiungo la lingua rilevata al set
		return langDetect;
	}

	/**
	 * Ritorna true se cè abbastanza testo.
	 * 
	 * @param testiConcatenati
	 * @return
	 */
	private boolean isEnoughText(List<String> testiConcatenati) {
		// se non ho elementi nella lista su cui fare lang detect ritorno false
		if (testiConcatenati.size() == 0)
			return false;

		// se ho poco testo restituisco false
		for (String testo : testiConcatenati)
			if (testo.length() < 15 && testiConcatenati.size() == 2)
				return false;
		return true;
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
	protected void runRoadRunner(GroupOfHomepages groupOfHomepage)
			throws FileNotFoundException, IOException, InterruptedException {
		for (PairOfHomepages pair : groupOfHomepage.getListOfPairs()) {
			RoadRunnerInvocator.launchRR(pair, errorLogLock);
		}
	}

	/**
	 * Cancella l'output di RoadRunner di un gruppo di pagine.
	 * 
	 * @param groupOfHomepage
	 */
	protected void deleteOutputRROfHomepages(GroupOfHomepages groupOfHomepage) {
		// delete dei file output RR
		Utils.deleteDir("output");
	}
	
	
}
