package it.uniroma3.parallel.detection;

import it.uniroma3.parallel.model.GroupOfParallelUrls;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.ParallelCollections;
import it.uniroma3.parallel.utils.UrlUtil;
import it.uniroma3.parallel.utils.Utils;

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
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

//siti su cui lanciare il processo
//String ssite="http://nato.int";
//String ssite="http://ferrari.com";
//String ssite="http://lohmann-stahl.de/";
//String ssite="http://www.archos.com";
//String ssite="http://www.ferrari.com";
//String ssite="http://www.lg.com";
//String ssite="http://www.toyota.com";
//String ssite="http://www.beringtime.de/";
//String ssite="http://www.opera.com/";
//String ssite="http://www.kongregate.com/";
//String ssite="http://www.fairmont.com";
//String ssite="http://www.speedtest.net/fr/";
//String ssite="http://www.vmware.com/";
//String ssite="https://www.articulate.com/";
//String ssite="http://worldtimeserver.com/";
//String ssite="http://www.box.com";
//String ssite="http://www.uc3m.es/Home";
//http://www.bulthaup.com/

//classe che permette di sapere se un sito è multilingua e avere coppie candidate di link paralleli (entry points)
public class M2ltilingualSite {

	private static final String OUTPUT = "output";
	private static final String PRE_HOMEPAGE = "PreHomepage";
	private static final String MULT_DETECTION_PREHOMEPAGE = "mult detection prehomepage";
	private static final String HTML_PAGES_PRELIMINARY = "htmlPagesPreliminary";
	private static final String VISIT_HOMEPAGE = "visitHomepage";
	private static final String MULT_DETECTION_OUTLINKS = "mult detection outlinks";
	private static final String ERROR_LOG_CSV = "ErrorLog.csv";
	private static final String HOMEPAGE_HREF_LANG = "homepageHrefLang";
	private static final String SITE_MULTILINGUAL_CSV = "SiteMultilingual.csv";
	private static final String TIME_CSV = "time.csv";
	private static final String MULT_DETECTION_HREFLANG = "mult detection hreflang";
	private static final String SITE_NOT_MULTILINGUAL_CSV = "SiteNotMultilingual.csv";

	// main per debugging
	public static void main(String[] argv) throws IOException, InterruptedException, LangDetectException {
		multilingualDetection("localhost:8080/testMinimale/homeIt.html", 2, new ReentrantLock(), new ReentrantLock(),
				new ReentrantLock(), new ReentrantLock());
	}

	// argomenti: homepage e depth di visita, lock scrivere txt errori e siti
	// mult o not mult rilevati
	// trova entry points e li passa al metodo di crawling effettivo
	public static void multilingualDetection(String homepageStringUrl, int depthT, Lock multSiteLogLock,
			Lock errorLogLock, Lock productivityLock, Lock timeLock)
					throws IOException, InterruptedException, LangDetectException {

		long startDetectionTime = Utils.getTime();

		// creo oggetto per fare detection con le varie euristiche DAVIDE
		MultilingualDetector multilingualDetector = new MultilingualDetector();

		//oggetto che rappresenta l'homepage su cui si fa la detection
		Page homepageToDetect = new Page(homepageStringUrl);


		// controllo per escludere alcuni siti falsi positivi multilingua
		if (multilingualDetector.isInBlacklist(homepageToDetect.getUrlRedirect())) {
			synchronized (multSiteLogLock) {
				// scrivo su un csv che il sito non è multilingua
				Utils.csvWr(new String[] { homepageStringUrl, Utils.getDate() }, SITE_NOT_MULTILINGUAL_CSV);
			}
			return;
		}

		// blocco try in cui provo attraverso varie euristiche a rilevare se un
		// sito sia multilingua
		try {
			// Prendo il nome della cartella di output dall'URL della homepage
			String nameFolder = UrlUtil.getNameFolderFromSiteUrl(homepageToDetect.getUrlRedirect());
			GroupOfParallelUrls parallelHomepageURLs;

			long startTime = Utils.getTime();

			// su gruppi di 5 viene lanciata la visita ricorsiva
			parallelHomepageURLs = multilingualDetector.detectByHreflang(homepageToDetect);

			long endTime = Utils.getTime();
			synchronized (timeLock) {
				Utils.csvWr(
						new String[] { homepageStringUrl, "", MULT_DETECTION_HREFLANG, "",
								Long.toString(endTime - startTime) },
						java.lang.Thread.currentThread().toString() + TIME_CSV);
			}

			if (parallelHomepageURLs != null) {
				long endDetectionTime = Utils.getTime();
				synchronized (multSiteLogLock) {
					Utils.csvWr(new String[] { homepageStringUrl, HOMEPAGE_HREF_LANG,
							Long.toString(endDetectionTime - startDetectionTime) }, SITE_MULTILINGUAL_CSV);
				}
				// elimino folder di output e di crawling TODO forse inutile
				// Utils.deleteDir(HTML_PAGES_PRELIMINARY + nameFolder);
				recursiveCrawler(parallelHomepageURLs, depthT, errorLogLock, nameFolder);
				return;
			}

			// System.out.println("FINE STEP 1 HREFLANG terminata verifica delle
			// presenza di hreflang");

			// RICERCA TRA GLI OUTLINK DI UNA PAGINA DA ACCOPPIARE CON LA ROOT
			// Se le rilevazioni di hreflang nella hp o nella sitemap non
			// producono risultati
			// lancio il metodo isMultilingual per la rilevazione visitando i
			// link uscenti dalla hp

			// coppie trovate con metodo lang detection+analisi strutturale
			// TODO far diventare groupofparallelurls
			Set<Set<String>> resultsPageExploration = new HashSet<Set<String>>();
			startTime = Utils.getTime();

			// TODO capire???
			// if (parallelHomepageURLs == null) {
			// resultsPageExploration =
			// (isMultilingualByExplorationLink(homepageToDetect, nameFolder,
			// errorLogLock));
			// }
			resultsPageExploration = isMultilingualByExplorationLink(homepageToDetect, nameFolder, errorLogLock);
			endTime = Utils.getTime();

			synchronized (timeLock) {

				Utils.csvWr(
						new String[] { homepageStringUrl, "", MULT_DETECTION_OUTLINKS, "",
								Long.toString(endTime - startTime) },
						java.lang.Thread.currentThread().toString() + TIME_CSV);
			}

			// se rilevato presenza contenuto multilingua stampa sito come
			// multilingua
			if (resultsPageExploration.size() != 0) {
				long endDetectionTime = Utils.getTime();

				synchronized (multSiteLogLock) {

					Utils.csvWr(new String[] { homepageStringUrl, VISIT_HOMEPAGE,
							Long.toString(endDetectionTime - startDetectionTime) }, SITE_MULTILINGUAL_CSV);
				}
			}

			// pulisco le folder di output e di crawling e lancio visita
			// ricorsiva
			Utils.deleteDir(HTML_PAGES_PRELIMINARY + nameFolder);

			// se ho coppie candidate lancio visita ricorsiva
			if (resultsPageExploration.size() != 0) {

				int countGroupEP = 0;
				for (Set<String> currentPairEP : resultsPageExploration) {

					List<String> entryPoints = new ArrayList<String>();
					entryPoints.addAll(currentPairEP);
					ParallelCollections parallelColl = new ParallelCollections(nameFolder + countGroupEP, entryPoints,
							(depthT), homepageStringUrl);
					countGroupEP++;

					// lancio rr
					try {
						// R2cursiveCrawling.recursiveCrawling(parallelColl,depthT,errorLogLock,productivityLock,timeLock);
					} catch (Exception e) {
						e.printStackTrace();
						synchronized (errorLogLock) {
							Utils.csvWr(homepageStringUrl, e, ERROR_LOG_CSV);
						}
					}
				}
				return;
			}

			// System.out.println("FINE STEP 2 OUTLINK terminata verifica delle
			// presenza di outlinks lang detec+struttura");

			// ----------------------------------------------------------------------------------------------------
			// se ancora non ho reperito entry points, provo a rilevare
			// eventuali preHomepage
			// (con link uscenti paralleli tra loro, ciascuno che porta ad una
			// lingua diversa)

			startTime = Utils.getTime();

			if (resultsPageExploration.size() == 0) {
				resultsPageExploration = preHomePage(homepageToDetect, nameFolder, errorLogLock);
			}
			endTime = Utils.getTime();

			synchronized (timeLock) {
				Utils.csvWr(
						new String[] { homepageStringUrl, "", MULT_DETECTION_PREHOMEPAGE, "",
								Long.toString(endTime - startTime) },
						java.lang.Thread.currentThread().toString() + TIME_CSV);
			}

			if (resultsPageExploration.size() != 0) {
				// elimino folder di output e di crawling
				Utils.deleteDir(HTML_PAGES_PRELIMINARY + nameFolder);

				int countGroupEP = 0;
				for (Set<String> currentPairEP : resultsPageExploration) {

					List<String> entryPoints = new ArrayList<String>();
					entryPoints.addAll(currentPairEP);
					ParallelCollections parallelColl = new ParallelCollections(nameFolder + countGroupEP, entryPoints,
							(depthT), homepageStringUrl);
					countGroupEP++;

					try {
						// R2cursiveCrawling.recursiveCrawling(parallelColl,depthT,errorLogLock,productivityLock,timeLock);
					} catch (Exception e) {
						e.printStackTrace();
						synchronized (errorLogLock) {
							Utils.csvWr(homepageStringUrl, e, ERROR_LOG_CSV);
						}
					}
				}
				{
					synchronized (multSiteLogLock) {
						long endDetectionTime = Utils.getTime();

						Utils.csvWr(new String[] { homepageStringUrl, PRE_HOMEPAGE,
								Long.toString(endDetectionTime - startDetectionTime) }, SITE_MULTILINGUAL_CSV);
					}

				}
				// System.out.println("FINE STEP 3 PREHOMEPAGE");

				return;
			}

			// se neanche con ultimo metodo non si trova nulla si considera il
			// sito non mutlilingua
			else {

				try {
					Utils.deleteDir(HTML_PAGES_PRELIMINARY + nameFolder);
				} catch (Exception e) {
					e.printStackTrace();
					synchronized (errorLogLock) {
						// stampo nell'error log il sito che da il problema e
						// l'errore
						Utils.csvWr(homepageStringUrl, e, ERROR_LOG_CSV);
					}
				}

				synchronized (multSiteLogLock) {
					long endDetectionTime = Utils.getTime();

					Utils.csvWr(
							new String[] { homepageStringUrl, Long.toString(endDetectionTime - startDetectionTime) },
							SITE_NOT_MULTILINGUAL_CSV);
				}
				// System.out.println("FINE STEP 3 PREHOMEPAGE");

				return;
			}
		}

		// in caso di errori, scrivo sul fiel di log e considero il sito non
		// multilingua
		catch (Exception e) {
			e.printStackTrace();
			synchronized (errorLogLock) {
				Utils.csvWr(homepageStringUrl, e, ERROR_LOG_CSV);
			}
			{
				synchronized (multSiteLogLock) {
					long endDetectionTime = Utils.getTime();

					Utils.csvWr(
							new String[] { homepageStringUrl, Long.toString(endDetectionTime - startDetectionTime) },
							SITE_NOT_MULTILINGUAL_CSV);
				}
				// System.out.println("FINE STEP 3");
				return;
			}
		}

	}// fine main

	private static void recursiveCrawler(GroupOfParallelUrls parallelHomepageURLs, int depthT, Lock errorLogLock,
			String nameFolder) throws IOException {
		int countEntryPoints = 0;
		for (List<String> currentGroupEP : parallelHomepageURLs.getGroupOfEntryPoints()) {

			// creo l'oggetto parallelCollection con il gruppetto di
			// entry points paralleli corrente
			ParallelCollections parallelColl = new ParallelCollections(nameFolder + countEntryPoints, currentGroupEP,
					depthT, parallelHomepageURLs.getHomepageURL().toString());

			// incremento l'id della collezione di entry points
			// corrente, per dare nomi diversi alle collezioni di file
			// di output future
			countEntryPoints++;
			// lancio rr
			try {
				// R2cursiveCrawling.recursiveCrawling(parallelColl,depthT,errorLogLock,productivityLock,timeLock);
			} catch (Exception e) {
				e.printStackTrace();
				synchronized (errorLogLock) {
					Utils.csvWr(parallelHomepageURLs.getHomepageURL().toString(), e, ERROR_LOG_CSV);
				}
			}
		}
	}

	private static Set<Set<String>> preHomePage(Page site, String nameFolder, Lock errorLogLock)
			throws IOException, LangDetectException {
		// creo set dove mettere coppie trovate
		Set<Set<String>> parallelEntryPoints = null;

		// mappa link visitati e path locale dove risiedono in locale
		Map<String, String> localPath2url = new HashMap<String, String>();

		Set<String> differentLanguage = new HashSet<String>();

		// System.out.println("IN PREHOMEPAGE ");

		// langauge homepage
		Elements ps = site.getDocument().select("p");
		String stringLangHP = (ps.text());
		if (DetectorFactory.getLangList().size() == 0)
			DetectorFactory.loadProfile("profiles.sm");

		Detector detector = DetectorFactory.create();
		if (ps.text().length() == 0) { // DAVIDE: se non ci sono <p>∆
			detector.append(site.getDocument().text());
		} else
			detector.append(stringLangHP);

		List<ArrayList<String>> linkPairToExplore = new ArrayList<ArrayList<String>>();

		// select all link to search other half of candidate pair (hp, half)
		Elements links = site.getDocument().select("a");

		// var per verifica su numero lingue diverse
		boolean firstTime = true;

		// scorro i link della homepage, selezionando i più interessanti e
		// lancio RR
		if (links.size() < 16)
			for (Element link1 : links) {
				String linkPossible1 = link1.absUrl("href");
				detector = DetectorFactory.create();
				Document document3 = Jsoup.connect(linkPossible1)
						.userAgent("Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1").timeout(8000).get();
				Elements ps3 = document3.select("p");
				String stringLangHP1 = (ps3.text());
				if (ps3.text().length() == 0) {
					detector.append(document3.text());
				} else
					detector.append(stringLangHP1);
				String langOutlinkHP1 = (detector.detect());

				for (Element link2 : links) {
					String linkPossible2 = link2.absUrl("href");
					if (linkPossible2.compareTo(linkPossible1) < 0)
						continue;
					if (!linkPossible1.equals(linkPossible2)) {
						try {
							detector = DetectorFactory.create();
							Document document2 = Jsoup.connect(linkPossible2)
									.userAgent("Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1").timeout(8000).get();
							Elements ps2 = document2.select("p");
							String stringLangHP2 = (ps2.text());
							if (ps2.text().length() == 0) {
								detector.append(document2.text());
							} else
								detector.append(stringLangHP2);
							String langOutlinkHP2 = (detector.detect());

							differentLanguage.add(langOutlinkHP2);

							if (!langOutlinkHP2.equals(langOutlinkHP1)) {
								ArrayList<String> linkPossib = new ArrayList<String>();
								linkPossib.add(linkPossible2);
								linkPossib.add(linkPossible1);
								linkPairToExplore.add(linkPossib);
							}
						} catch (Exception e) {
							e.printStackTrace();
							synchronized (errorLogLock) {
								Utils.csvWr(new String[] { site.getUrl().toString(), e.toString() }, ERROR_LOG_CSV);
							}
						}
					}
				}

				if (firstTime) {
					firstTime = false;
					if (differentLanguage.size() < (int) (links.size() / 1.3))
						return parallelEntryPoints;
				}
			}

		// System.out.println("SONO ANCORA QUA ");

		List<String> fileToVerify = new ArrayList<String>();

		int countPotentialEntryPoints = 0;
		// scorro i link della homepage, selezionando i più interessanti e
		// lancio RR
		for (List<String> linkPossible : linkPairToExplore) {
			countPotentialEntryPoints++;

			fileToVerify.add(nameFolder + countPotentialEntryPoints);
			localPath2url.putAll(launchRRDownloadPagesToDecideIfMultilingual(nameFolder, linkPossible.get(0),
					linkPossible.get(1), countPotentialEntryPoints, errorLogLock, true, errorLogLock,
					site.getUrlRedirect().toString()));

		}

		// controllo ora l'output di rr, se lingua pagine accoppiate è diversa e
		// se hanno abbastanza label
		try {

			// lista dei file accoppiabili
			parallelEntryPoints = new HashSet<Set<String>>();

			// lancio metodo che ritorna lista file accoppiabili
			parallelEntryPoints.addAll(langDetectAndThresholdLabelPreHomepage(nameFolder, fileToVerify, errorLogLock,
					site, localPath2url));

		} catch (LangDetectException e) {
			synchronized (errorLogLock) {
				e.printStackTrace();
				Utils.csvWr(new String[] { site.getUrl().toString(), e.toString() }, ERROR_LOG_CSV);

			}
		}

		// delete dei file creati con questo metodo
		for (String ftv : fileToVerify)
			Utils.deleteDir("output/" + ftv);

		// elimino file crawlati
		Utils.deleteDir(HTML_PAGES_PRELIMINARY + nameFolder);

		return parallelEntryPoints;

	}

	// se sito multilingua tramite esplorazione link uscenti (verifica lingua e
	// struttura), se sito multilingua ritorna set di coppie candidate
	public static Set<Set<String>> isMultilingualByExplorationLink(Page homepage, String nameFolder, Lock errorLogLock)
			throws IOException, InterruptedException, LangDetectException {

		// creo set dove mettere coppie trovate, (set di set(coppie))
		Set<Set<String>> resultsPageExploration = new HashSet<Set<String>>();

		// mappa link visitati e path locale dove risiedono in locale
		Map<String, String> localPath2url = new HashMap<String, String>();

		// lista con link su cui lanciare rr
		List<String> outlinkToVisit = outlinkToVisit(homepage, errorLogLock);

		List<String> fileToVerify = new ArrayList<String>();

		int countPotentialEntryPoints = 0;
		// sui link più interessanti appena selezionati lancio RR
		for (String linkPossible : outlinkToVisit) {
			countPotentialEntryPoints++;

			// lancio rr e faccio language detection
			// String[] a ={nameFolder,site.getUrlRedirect().toString(),
			// linkPossible,Integer.toString(countEntryPoints)};

			// lingua già verificata, lancia rr solo se superata scrematura di
			// verifica strutt blanda
			fileToVerify.add(nameFolder + countPotentialEntryPoints);
			localPath2url.putAll(launchRRDownloadPagesToDecideIfMultilingual(nameFolder,
					homepage.getUrlRedirect().toString(), linkPossible, countPotentialEntryPoints, errorLogLock, false,
					errorLogLock, homepage.getUrlRedirect().toString()));
		}

		// System.out.println("VISIT OUTLINKS 2 fileToVerify: " +
		// localPath2url);

		// controllo ora l'output di rr, se lingua pagine accoppiate è diversa e
		// se hanno abbastanza label,
		try {
			// lista dei file accoppiabili
			List<String> list = new ArrayList<String>();

			// lancio metodo che ritorna lista file (in locale) accoppiabili con
			// la home
			list.addAll(langDetectAndThresholdLabel(nameFolder, fileToVerify, errorLogLock, homepage));

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

		return resultsPageExploration;
	}

	// OK
	public static void downloadFromUrl(URL url, String localFilename, String userAgent) throws IOException {
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			URLConnection urlConn = url.openConnection();
			urlConn.setReadTimeout(2000);
			if (userAgent != null) {
				urlConn.setRequestProperty("User-Agent", userAgent);
			}
			is = urlConn.getInputStream();
			fos = new FileOutputStream(localFilename);
			byte[] buffer = new byte[4096];
			int len;
			while ((len = is.read(buffer)) > 0) {

				fos.write(buffer, 0, len);
			}
		} finally {
			try {
				if (is != null)
					is.close();
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		}
	}

	// link da visitare, scorrendo link del sito e scremando quelli con edit
	// distance troppo grande rispetto alla homepage
	// verifico anche che siano in lingua diversa, altrimenti non mi interessano
	public static List<String> outlinkToVisit(Page homepage, Lock errorLogLock)
			throws IOException, LangDetectException {
		// funzione di filtraggio che mi da una lista di link (tag a) possibili
		// entrypoints (perchè hanno lingua diversa homepage)
		return editDistanceAndLanguageFilter(homepage, errorLogLock);
	}

	// OK
	// dati due doc html dice approssimativamente se sono similari
	// strutturalmente o no
	// utile per filtrare i link su cui lanciare RR,
	public static boolean executeRR(String first, String second) throws IOException {

		// File input = new File(first);
		// File input2 = new File(second);
		// List<String> tag1 = m(input);
		// List<String> tag2 = m(input2);
		//
		// // //System.out.println(tag1.size());
		// // //System.out.println(tag2.size());
		//
		// Set<String> union=new HashSet<String>();
		// union.addAll(tag1);
		// int d= union.size();
		// union.addAll(tag2);
		// Set<String> union2=new HashSet<String>();
		// union2.addAll(tag2);
		// int j=union2.size();
		//
		// ////System.out.println("tag min "+Integer.min(d, j) + " union " +
		// union.size());
		//
		// if(((union.size()-Integer.min(d,j))<=(Integer.min(d,j)/10)) &&
		// ((Integer.max(tag1.size(),tag2.size())-Integer.min(tag1.size(),
		// tag2.size()))<(Integer.max(tag1.size(), tag2.size())/20)))
		// return true;
		// else
		// return false;

		return true;
	}

	// OK
	// dato un file crea lista con tutti i suoi tag
	public static List<String> m(File f) throws IOException {

		File input = f;
		Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

		List<String> tags = new ArrayList<String>();

		tags.add(doc.nodeName());
		//// System.out.println();
		List<org.jsoup.nodes.Node> d = doc.childNodes();
		printNode(d, tags);

		//// System.out.println(tags);
		return tags;

	}

	// OK
	// stampa tag figli di un nodo
	public static void printNode(List<org.jsoup.nodes.Node> d, List<String> tags) {
		if (d == null)
			return;
		else {
			for (int h = 0; h < d.size(); h++) {
				//// System.out.println(d.get(h));
				tags.add(d.get(h).nodeName());
				//// System.out.println();
				printNode(d.get(h).childNodes(), tags);
			}
		}

	}

	// metodo che verifica che edit distance e lingua dei link uscenti dalla
	// homepage siano compatibili con lo stato di entry points
	// TODO verificare se conviene fare prima detection lingua o controllo edit
	// distance
	public static List<String> editDistanceAndLanguageFilter(Page homepage, Lock errorLogLock)
			throws IOException, LangDetectException {
		String langHp = homepage.getLanguage();
		// select all link to search other half of candidate pair (hp, half)
		List<Element> links = homepage.getAllOutlinks();
		List<String> linkToExplore = new ArrayList<String>();
		for (Element link : links) {
			// show absolute url
			String linkPossible = link.absUrl("href");
			Set<String> alreadyVisit = new HashSet<String>();
			// controllo se link è già stato visitato
			if (!alreadyVisit.contains(linkPossible)) {
				String stringOfHomepageURL = homepage.getUrlRedirect().toString();
				if (!stringOfHomepageURL.equals(linkPossible)) {
					int lengthURLHomepage = stringOfHomepageURL.length();
					// controllo se la edit distance tra i link sia ragionevole
					int linkLength = linkPossible.length();
					if (((linkLength >= lengthURLHomepage) && (StringUtils.getLevenshteinDistance(stringOfHomepageURL,
							linkPossible) < linkLength - lengthURLHomepage + 4
							|| StringUtils.getLevenshteinDistance(stringOfHomepageURL, linkPossible) < linkLength
									- homepage.getDomain().length() + 4)
							&& (linkLength < lengthURLHomepage * 2.7))
							|| ((linkLength <= lengthURLHomepage) && (StringUtils
									.getLevenshteinDistance(stringOfHomepageURL, linkPossible) < linkLength / 2))) {
						// verifico che la lingua sia diversa
						try {
							alreadyVisit.add(linkPossible);
							Page pagePossible = new Page(new URL(linkPossible));
							String langPagePossible = pagePossible.getLanguage();
							if (!langPagePossible.equals(langHp))
								linkToExplore.add(linkPossible);
						} catch (Exception e) {
							e.printStackTrace();
							synchronized (errorLogLock) {
								Utils.csvWr(new String[] { stringOfHomepageURL, e.toString() }, ERROR_LOG_CSV);
							}
						}
					}
				}
			}
		}
		return linkToExplore;
	}

	// OK
	// metodo che lancia RR su potenziali pagine parallele che hanno superato un
	// filtraggio iniziale basato sui tag in comune
	public static Map<String, String> launchRRDownloadPagesToDecideIfMultilingual(String nameFolder, String site,
			String outlink, int countEntryPoints, Lock errLogLock, boolean preHomepage, Lock errorLogLock,
			String string) throws IOException {

		// nameFolder name of output folder
		// (site,outlink) pair of candidate url
		// countEntryPoints number of files

		// mappa con link visitati e loro pathLocale
		Map<String, String> localPath2url = new HashMap<String, String>();
		try {
			// userAgent
			String userAgent = "Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1";

			// folder where download pages
			// this create in the first call and then already exist
			new File(HTML_PAGES_PRELIMINARY + nameFolder + "/").mkdir();

			// this create folder in order to save download pages
			new File(HTML_PAGES_PRELIMINARY + nameFolder + "/" + nameFolder + countEntryPoints).mkdir();

			// folder url where download page
			String urlBase = HTML_PAGES_PRELIMINARY + nameFolder + "/" + nameFolder + countEntryPoints;

			// page 1
			String page1;
			if (!preHomepage) {
				// page 1(della coppia 1,2 da dare a rr) sempre stessa in questa
				// fase: la home:
				if (countEntryPoints == 1) {
					downloadFromUrl(new URL(site), urlBase + "/" + "HomePage" + countEntryPoints + "-1" + ".html",
							userAgent);
					// aggiorno mappa link visitati
					localPath2url.put(HTML_PAGES_PRELIMINARY + nameFolder + "/" + nameFolder + 1 + "/" + "HomePage" + 1
							+ "-1" + ".html", site);
				}

				// string with homepage path
				page1 = HTML_PAGES_PRELIMINARY + nameFolder + "/" + nameFolder + 1 + "/" + "HomePage" + 1 + "-1"
						+ ".html";
			} else {
				downloadFromUrl(new URL(site), urlBase + "/" + "HomePage" + countEntryPoints + "-1" + ".html",
						userAgent);
				// aggiorno mappa link visitati
				localPath2url.put(urlBase + "/" + "HomePage" + countEntryPoints + "-1" + ".html", site);

				// string with homepage path
				page1 = urlBase + "/" + "HomePage" + countEntryPoints + "-1" + ".html";
			}

			// page 2
			downloadFromUrl(new URL(outlink), urlBase + "/" + "HomePage" + countEntryPoints + "-2" + ".html",
					userAgent);

			// aggiorno map link visitati
			localPath2url.put(urlBase + "/" + "HomePage" + countEntryPoints + "-2" + ".html", outlink);

			// creo folder e file style per l'output di rr
			backupFile(nameFolder + countEntryPoints);

			// System.out.println("RRRRRR " + page1 + " "+
			// urlBase+"/"+"HomePage"+countEntryPoints+"-2"+".html");

			// filtro rr, non applico rr su pagine palesemente troppo diverse
			// strutturalmente
			if (executeRR(page1, urlBase + "/" + "HomePage" + countEntryPoints + "-2" + ".html")) {

				Thread t3 = new Thread() {
					public void run() {
						try {
							rr("-N:" + nameFolder + countEntryPoints, "-O:etc/flat-prefs.xml", page1,
									urlBase + "/" + "HomePage" + countEntryPoints + "-2" + ".html");
						} catch (Exception e1) {
							e1.printStackTrace();
							synchronized (errorLogLock) {
								// stampo nell'error log il sito che da il
								// problema e l'errore
								try {
									Utils.csvWr(new String[] { site, e1.toString() }, ERROR_LOG_CSV);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				};
				t3.start();
				t3.join(30000);
				if (t3.isAlive())
					t3.stop();

				// System.out.println("rr terminated");

			}
		} catch (Exception ex) {
			ex.printStackTrace();
			synchronized (errLogLock) {
				Utils.csvWr(new String[] { site, ex.toString() }, ERROR_LOG_CSV);
			}
		}

		return localPath2url;
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

	// OK
	// lacia rr
	private static void rr(String... argv) throws Exception {

		try {
			it.uniroma3.dia.roadrunner.Shell.main(argv);
			it.uniroma3.dia.roadrunner.tokenizer.token.TagFactory.reset();
			it.uniroma3.dia.roadrunner.tokenizer.token.Tag.reset();

			Utils.csvWr(new String[] { argv[0] }, "rr.csv");

		} catch (Exception e) {
			e.printStackTrace();
			String attribute = "";
			String line = e.toString().split("/n")[0];
			if (line.contains("Lo spazio di nomi"))
				attribute = attribute.concat(line.split("'")[3]).concat(":");
			else
				return;
			System.out.println("attr " + attribute);
			System.out.println("1 " + argv[0].substring(3));
			System.out.println("2 " + argv[2]);
			String pages = "";
			for (int i = 2; i < argv.length; i++)
				if (i == argv.length - 1)
					pages = pages.concat(argv[i]);
				else
					pages = pages.concat(argv[i]).concat(" ");

			System.out.println(pages);
			String newXMLpref = generateNewPrefsXmlNew(attribute, argv[0].substring(3), pages);
			System.out.println(newXMLpref);
			argv[1] = "-O:".concat(newXMLpref);
			System.out.println("newwwwww " + argv[1]);
			it.uniroma3.dia.roadrunner.Shell.main(argv);
			it.uniroma3.dia.roadrunner.tokenizer.token.TagFactory.reset();
			it.uniroma3.dia.roadrunner.tokenizer.token.Tag.reset();
		}

	}

	// OK
	// usato nella fase di detection
	// data una lista di file di output di RR (e una cartella) verifica i file
	// di ouptut di rr(della hp con i suoi link uscenti):
	// vede se ci sono abbastanza label e controlla che le lingue siano
	// differenti,
	// e restituisce una lista di file(in locale) accoppiabili
	// lavora su cartelle contententi molti output che sono sempre relativi a
	// coppie(e non gruppi) di link allineati
	public static Collection<String> langDetectAndThresholdLabel(String folderRoot, List<String> fileToVerify,
			Lock errLogLock, Page site) throws LangDetectException, IOException {

		List<String> nameFileParallel;
		Map<String, List<String>> fileOutputRR2textParallel;

		// mappa che conterrà come chiave la lingua e come valore la pagina
		// parallela alla homepage in locale
		Map<String, String> pathLocalCandidate = new HashMap<String, String>();

		// parametro con root path risultati dove vado a verificare i suddetti
		// file di output di rr
		String pathRoot = OUTPUT;

		// mappa dove salvo nella chiave la lingua(che deve essere diversa da
		// quella della home) ma deve anche essere
		// singola rispetto a tutte le altre, e nel valore il numero delle label
		// di quel file di output
		// solo un file per ogni lingua verrà conservato e partirà su di esso la
		// visita ricorsiva
		// chiave è la lingua, il valore è il num di label
		Map<String, Integer> onlyOne4Language = new HashMap<String, Integer>();

		for (String ftv : fileToVerify) {

			// scorro tutti le cartelle presenti nella folder di output di rr
			try {

				fileOutputRR2textParallel = new HashMap<String, List<String>>();
				nameFileParallel = new ArrayList<String>();

				List currentObject = textParallel(pathRoot + "/" + ftv + "/" + ftv + "_DataSet.xml", errLogLock, site);

				Integer numberOfLabel = (Integer) currentObject.get(0);
				nameFileParallel = (List<String>) currentObject.get(1);
				fileOutputRR2textParallel = (Map<String, List<String>>) currentObject.get(2);

				if (numberOfLabel < 1)
					continue;

				// System.out.println("fuori dal tunnel "+currentObject);

				if (fileOutputRR2textParallel.get(pathRoot + "/" + ftv + "/" + ftv + "_DataSet.xml") != null) {

					// metodo che restituisce una coppia formata da un booleano
					// se lingue sono diverse e
					String languagePage = detectLanguageListStringoniToDecidePreliminary(
							fileOutputRR2textParallel.get(pathRoot + "/" + ftv + "/" + ftv + "_DataSet.xml"),
							nameFileParallel, pathRoot + "/" + ftv + "/" + ftv + "_DataSet.xml");

					// mi serve la chiave che è un true banalmente

					// se metodo riscontra che le lingue sono tutte diverse ok
					// aggiungo file, altrimenti scelgo
					// come entry point per una certa lingua la pagina più
					// simile strutturalmente alla homepage
					if (languagePage != null) {

						// mi faccio restituire il nome del file su cui ho fatto
						// la detect
						File folderR = new File(HTML_PAGES_PRELIMINARY + folderRoot + "/" + ftv);

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
								Utils.csvWr(new String[] { site.getUrlRedirect().toString(), e.toString() },
										ERROR_LOG_CSV);
							}
						}

					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				Utils.csvWr(new String[] { site.getUrlRedirect().toString(), e.toString() }, ERROR_LOG_CSV);
			}
		}

		// System.out.println("candidati "+folderRoot+ " " +pathLocalCandidate);

		return pathLocalCandidate.values();
	}

	// OK
	// metodo che dato un pathFile ritorna una pair
	// con controllo delle label (label>8), se >8 ritornato un null
	// ritorna il num di label e
	// una lista di stringhe con il nome dei file html(path locale degli
	// elementi di partenza dati a RR)
	// e una mappa che ha come chiave stringhe e come valori liste di stringhe,
	// la chiave è il path che gli passo(file output) e il valore sono liste con
	// il testo concatenato delle label di ogni source
	public static List textParallel(String path, Lock errorLogLock, Page site) throws IOException {

		// lista ritornata dal metodo
		List ret = new ArrayList();

		List<String> listStringoni = new ArrayList<String>();

		List<String> files = new ArrayList<String>();

		// lista con le label del file di output di RR
		List<String> keyToUrls = new ArrayList<String>();

		Map<String, List<String>> keyToUrls2 = new HashMap<String, List<String>>();

		Integer numLabel = 0;

		try {
			// apro file e creo struttura per fare xpath query
			FileInputStream file = new FileInputStream(new File(path));
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			org.w3c.dom.Document xmlDocument = builder.parse(file);
			XPath xPath = XPathFactory.newInstance().newXPath();

			// query per sapere le label
			String expression = "//attribute/@label";
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

			numLabel = nodeList.getLength();
			// System.out.println(path+nodeList.getLength());

			///// imponi condizione su lunghezza label,
			// per ora num fisso, poi magari in funzione del numero dei tag
			///// della coppia originaria, così vedi quanto rr ha
			// allineato sul totale allineabile
			if (nodeList.getLength() < 1) {
				ret.add(nodeList.getLength());
				return ret;
			}

			// itero sulle label per sapere su quali label iterare al ciclo
			// successivo
			for (int i = 0; i < nodeList.getLength(); i++) {
				keyToUrls.add((String) nodeList.item(i).getFirstChild().getNodeValue());
			}

			// mi faccio ritornare i path file, creo list Stringoni del numero
			// di file che ho allineato
			String expression3 = "//instance/@source";
			NodeList nodeList3 = (NodeList) xPath.compile(expression3).evaluate(xmlDocument, XPathConstants.NODESET);
			for (int j = 0; j < nodeList3.getLength(); j++) {
				listStringoni.add(j, "");
				files.add((String) nodeList3.item(j).getFirstChild().getNodeValue().toString());
			}

			// itero su tutte le label, per andare a prendere il testo da tutte
			// le label
			for (String key : keyToUrls) {
				// query per sapere testo delle label
				String expression2 = "//attribute[@label='" + key + "']//inputsamples";
				NodeList nodeList2 = (NodeList) xPath.compile(expression2).evaluate(xmlDocument,
						XPathConstants.NODESET);

				// per ogni risultato avuto dalla query appena sopra (dove
				// chiedo testo per quella label)
				// j rappresenta i vari source, quante label ho con stesso nome
				// quindi quanti documenti ho allineato
				for (int j = 0; j < nodeList2.getLength(); j++) {

					// se per doc j-esimo ho un risultato non null allora setta
					// temp con result della query altrimenti setta temp=" "
					String temp = " ";
					if ((String) nodeList2.item(j).getFirstChild().getNodeValue() != null)
						temp = (String) nodeList2.item(j).getFirstChild().getNodeValue();

					// metodo remove rimuove elemento e lo restituisce
					listStringoni.add(j, listStringoni.remove(j).concat(temp) + " ");

				}
			}
			keyToUrls2.put(path, listStringoni);

		} catch (Exception e) {
			e.printStackTrace();
			synchronized (errorLogLock) {
				// stampo nell'error log il sito che da il problema e l'errore
				Utils.csvWr(new String[] { site.getUrlRedirect().toString(), e.toString() }, ERROR_LOG_CSV);
			}
			keyToUrls2.put(path, listStringoni);
			ret.add(numLabel);
			ret.add(files);
			ret.add(keyToUrls2);
			return ret;
		}

		ret.add(numLabel);
		ret.add(files);
		ret.add(keyToUrls2);
		return ret;
	}

	// OK
	// usato solo nella fase di detection
	// metodo che prende in ingresso una lista con gli stringoni delle label
	// concatenate per tutti i file accoppiati insieme
	// e verifica che siano lingue diverse, in caso di lingue uguali rilancia rr
	// sui soli documenti in lingue diverse
	public static String detectLanguageListStringoniToDecidePreliminary(List<String> stringoni, List<String> li,
			String file) throws LangDetectException, ParserConfigurationException, SAXException, IOException,
					InterruptedException {
		// carico i profili delle lingue
		if (DetectorFactory.getLangList().size() == 0)
			DetectorFactory.loadProfile("profiles.sm");

		// se ho poco testo e nn riesco quindi a fare accuratamente lang detect
		// restituisco false
		// coppie ok ma per gruppi??
		for (String s : stringoni)
			if (s.length() < 15 && stringoni.size() == 2)
				return null;

		// set e list con tutte le lingue rilevate
		Set<String> set = new HashSet<String>();
		List<String> ling = new ArrayList<String>();

		String secondPage = "";

		// aggiungo al set le lingue rilevate
		for (String t : stringoni) {

			// risultati della language detection(en, it, ...)
			String langDetect = "";

			// detect su stringone
			Detector detector = DetectorFactory.create();
			detector.append(t);
			langDetect = detector.detect().toString();

			secondPage = langDetect;

			// aggiungo la lingua rilevata al set
			set.add(langDetect);
			ling.add(langDetect);
		}

		// se non ho elementi nella lista su cui fare lang detect ritorno false
		if (stringoni.size() == 0)
			return null;

		// verifico che ogni stringone abbia lingua diversa, e quindi che la
		// cardinalità di quei due insiemi sia la stessa
		if (set.size() == stringoni.size()) {
			return secondPage;
		}

		return null;

	}

	// OK
	// usato nella fase di detection
	// data una lista di file di output di RR (e una cartella) verifica i file
	// di ouptut di rr(della hp con i suoi link uscenti):
	// vede se ci sono abbastanza label e controlla che le lingue siano
	// differenti,
	// e restituisce una lista di file(in locale) accoppiabili
	// lavora su cartelle contententi molti output che sono sempre relativi a
	// coppie(e non gruppi) di link allineati
	public static Set<Set<String>> langDetectAndThresholdLabelPreHomepage(String folderRoot, List<String> fileToVerify,
			Lock errLogLock, Page site, Map<String, String> localPath2url) throws LangDetectException, IOException {

		List<String> nameFileParallel;
		Map<String, List<String>> fileOutputRR2textParallel;

		Set<Set<String>> parallelPair = new HashSet<Set<String>>();

		// parametro con root path risultati dove vado a verificare i suddetti
		// file di output di rr
		String pathRoot = OUTPUT;

		for (String ftv : fileToVerify) {

			// scorro tutti le cartelle presenti nella folder di output di rr
			try {

				fileOutputRR2textParallel = new HashMap<String, List<String>>();
				nameFileParallel = new ArrayList<String>();

				List currentObject = textParallel(pathRoot + "/" + ftv + "/" + ftv + "_DataSet.xml", errLogLock, site);

				Integer numberOfLabel = (Integer) currentObject.get(0);
				nameFileParallel = (List<String>) currentObject.get(1);
				fileOutputRR2textParallel = (Map<String, List<String>>) currentObject.get(2);

				if (numberOfLabel < 1)
					continue;

				int first = StringUtils.lastOrdinalIndexOf(nameFileParallel.get(0), "/", 3) + 1;
				int second = StringUtils.lastOrdinalIndexOf(nameFileParallel.get(1), "/", 3) + 1;

				if (localPath2url.get(nameFileParallel.get(0).substring(first)) != null
						&& localPath2url.get(nameFileParallel.get(1).substring(second)) != null) {
					// System.out.println("NAMEFILEPARALLEL "+nameFileParallel);
					// System.out.println("NAMEFILEPARALLEL2 "+localPath2url);

					Set<String> current = new HashSet<String>();
					current.add(localPath2url.get(nameFileParallel.get(0).substring(first)));
					current.add(localPath2url.get(nameFileParallel.get(1).substring(second)));
					parallelPair.add(current);
				}

			} catch (Exception e) {
				e.printStackTrace();
				Utils.csvWr(new String[] { site.getUrlRedirect().toString(), e.toString() }, ERROR_LOG_CSV);
			}
		}

		// System.out.println("PP "+parallelPair);

		return parallelPair;
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

}
