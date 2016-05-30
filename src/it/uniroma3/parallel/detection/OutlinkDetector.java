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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

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
import it.uniroma3.parallel.model.RoadRunnerDataSet;
import it.uniroma3.parallel.utils.DownloadManager;
import it.uniroma3.parallel.utils.RoadRunnerInvocator;
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
	// usato nella fase di detection
	// data una lista di file di output di RR (e una cartella) verifica i file
	// di ouptut di rr(della hp con i suoi link uscenti):
	// vede se ci sono abbastanza label e controlla che le lingue siano
	// differenti,
	// e restituisce una lista di file(in locale) accoppiabili
	// lavora su cartelle contententi molti output che sono sempre relativi a
	// coppie(e non gruppi) di link allineati
	public Collection<String> langDetectAndThresholdLabel(GroupOfHomepages groupOfHomepage, Lock errLogLock)
			throws LangDetectException, IOException {

		Map<String, List<String>> fileOutputRR2textParallel;

		// mappa che conterrà come chiave la lingua e come valore la pagina
		// parallela alla homepage in locale
		Map<String, String> pathLocalCandidate = new HashMap<String, String>();

		// mappa dove salvo nella chiave la lingua(che deve essere diversa da
		// quella della home) ma deve anche essere
		// singola rispetto a tutte le altre, e nel valore il numero delle label
		// di quel file di output
		// solo un file per ogni lingua verrà conservato e partirà su di esso la
		// visita ricorsiva
		// chiave è la lingua, il valore è il num di label
		Map<String, Integer> onlyOne4Language = new HashMap<String, Integer>();

		for (PairOfHomepages pair : groupOfHomepage.getListOfPairs()) {

			// scorro tutti le cartelle presenti nella folder di output di rr
			try {

				RoadRunnerDataSet roadRunnerDataSet = pair.getRoadRunnerDataSet();
				Integer numberOfLabel = roadRunnerDataSet.getLabelNodes().getLength();
				fileOutputRR2textParallel = new HashMap<String, List<String>>();
				if (numberOfLabel < 1) {
					continue;
				}
				fileOutputRR2textParallel = roadRunnerDataSet.getDatasetToTextExtracted();
				if (fileOutputRR2textParallel.get(roadRunnerDataSet.getOutputPath()) != null) {
					// metodo che restituisce una coppia formata da un booleano
					// se lingue sono diverse e
					String languagePage = pair.getOneHomepage(1).getLanguage();
					boolean isDifferentLanguage = detectLanguageListStringoniToDecidePreliminary(
							fileOutputRR2textParallel.get(roadRunnerDataSet.getOutputPath()));

					// mi serve la chiave che è un true banalmente

					// se metodo riscontra che le lingue sono tutte diverse ok
					// aggiungo file, altrimenti scelgo
					// come entry point per una certa lingua la pagina più
					// simile strutturalmente alla homepage
					if (isDifferentLanguage) {

						// mi faccio restituire il nome del file su cui ho fatto
						// la detect
						File folderR = new File(groupOfHomepage.getLocalPath() + pair.getPairNumber());

						File[] listOfFilesR = folderR.listFiles();

						String fileInFolder = "";
						// la potenziale pagina parallela alla homepage
						// analizzata con RR e scaricata in locale
						if (listOfFilesR.length == 1)
							fileInFolder = listOfFilesR[0].toString();
						else {
							for (File file : listOfFilesR)
								if (!file.toString().contains("HomePage1-1"))
									fileInFolder = file.toString();
						}

						// System.out.println("file in folder "+listOfFilesR[0]
						// + " "+ listOfFilesR[1]);

						// se ho pagine più simile strutturalmene nella stessa
						// lingua aggiorno le mappe
						try {
							if (onlyOne4Language.get(languagePage) == null) {
								pathLocalCandidate.put(languagePage, fileInFolder);
								onlyOne4Language.put(languagePage, numberOfLabel);
							}

							if (onlyOne4Language.get(languagePage) != null)
								if (onlyOne4Language.get(languagePage).compareTo(numberOfLabel) < 0) {
									pathLocalCandidate.put(languagePage, fileInFolder);
									onlyOne4Language.put(languagePage, numberOfLabel);
								}

						} catch (Exception e) {
							e.printStackTrace();
							synchronized (errLogLock) {
								// stampo nell'error log il sito che da il
								// problema e l'errore
								Utils.csvWr(new String[] { pair.getMainHomepage().getURLString(), e.toString() },
										ERROR_LOG_CSV);
							}
						}

					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				Utils.csvWr(new String[] { pair.getMainHomepage().getURLString(), e.toString() }, ERROR_LOG_CSV);
			}
		}

		return pathLocalCandidate.values();
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
	 * Ritorna true se c'è abbastanza testo e se i testi sono in lingua differente.
	 * 
	 * @param testiConcatenati
	 * @return
	 * @throws LangDetectException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static boolean detectLanguageListStringoniToDecidePreliminary(List<String> testiConcatenati)
			throws LangDetectException, ParserConfigurationException, SAXException, IOException, InterruptedException {
		// verifico che un set creato con i linguaggi dei testi abbia una
		// cardinalitò uguale al numero di testi.Ovvero abbiamo tutte lingue
		// differenti.
		return !isEnoughText(testiConcatenati) && getLanguageSet(testiConcatenati).size() == testiConcatenati.size();
	}

	private static Set<String> getLanguageSet(List<String> testiConcatenati) throws LangDetectException {
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

	private static boolean isEnoughText(List<String> testiConcatenati) {
		// se non ho elementi nella lista su cui fare lang detect ritorno null
		if (testiConcatenati.size() == 0)
			return false;

		// se ho poco testo restituisco null
		for (String testo : testiConcatenati)
			if (testo.length() < 15 && testiConcatenati.size() == 2)
				return false;
		return true;
	}

	public static String generateNewPrefsXmlNew(String attribute, String nameSite, String pathFiles)
			throws IOException {

		String parseTagResult = "";
		String[] files = pathFiles.split(" ");

		for (String a : files) {
			File input = new File(a);
			org.jsoup.nodes.Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
			// Elements g = (doc.getElementsByAttribute(attribute));
			Elements g = doc.getElementsByAttributeStarting(attribute);
			for (Element e : g) {
				String parseTag = e.toString().split(" ")[0].replaceAll("<", "").replaceAll(">", "");
				parseTagResult = parseTagResult.concat(parseTag + ", ");
			}
		}
		// System.out.println("step 2: tag extraction: "+parseTagResult);

		// --------------------------------------------------

		// file flat-prefs.xml di default
		File file = new File("etc/flat-prefs.xml");
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line;

		// file dove scrivo file prefs.xml che user� per questo sito
		String fileXmlNew = "etc/flat-prefs" + nameSite + ".xml";
		FileOutputStream provaa = new FileOutputStream(fileXmlNew);
		PrintStream scrivii = new PrintStream(provaa);

		// scandisco prefs.xml e riga da cambiare la cambio skippando il tag
		// opportuno
		while ((line = br.readLine()) != null) {
			if (line.contains("<skipTags")) {
				String[] split = line.split("=\"");
				String skiptag = split[0].concat("=" + "\"" + parseTagResult).concat(split[1]);
				scrivii.println(skiptag);
			} else {
				scrivii.println(line);
			}
		}

		br.close();
		scrivii.close();
		return fileXmlNew;
	}

	/**
	 * Chiede ad un DownloadManager di scaricare le pagine in locale.
	 * 
	 * @param groupOfHomepage
	 */
	protected void downloadPagesInLocal(GroupOfHomepages groupOfHomepage) {
		DownloadManager downloadManager = new DownloadManager(groupOfHomepage.getHomepage().getNameFolder());
		downloadManager.downloadGroupOfHomepage(groupOfHomepage);
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
}
