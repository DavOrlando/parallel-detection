package it.multilingualDetection;

import it.model.ParallelCollections;
import it.model.Site;
import it.utils.UrlHelper;
import it.utils.Utils;

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

	private static final int LABEL_MINIME = 8;

	// main per debugging
	public static void main(String[] argv) throws IOException, InterruptedException, LangDetectException {
		multilingualDetection("localhost:8080/testMinimale/homeIt.html", 2, new ReentrantLock(), new ReentrantLock(),
				new ReentrantLock(), new ReentrantLock());
	}

	// argomenti: homepage e depth di visita, lock scrivere txt errori e siti
	// mult o not mult rilevati
	// trova entry points e li passa al metodo di crawling effettivo
	public static void multilingualDetection(String siteUrl, int depthT, Lock multSiteLogLock, Lock errorLogLock,
			Lock productivityLock, Lock timeLock) throws IOException, InterruptedException, LangDetectException {

		// creo oggetto per fare detection con le varie euristiche DAVIDE
		MultilingualDetector multilingualDetector = new MultilingualDetector();
		// creo oggetto che ha la responsabilità di lavorare sulla stringa url
		// del sito DAVIDE

		long noww = Utils.getTime();

		// aggiungo http alla stringa dell'url
		if (!UrlHelper.hasHttp(siteUrl))
			siteUrl = UrlHelper.getUrlWithHttp(siteUrl);

		// controllo per escludere alcuni siti falsi positivi multilingua
		// (paralleli nella struttura ma non nella semantica)
		if (multilingualDetector.detectFalseMultilingualSite(siteUrl)) {
			synchronized (multSiteLogLock) {
				// scrivo su un csv che il sito non è multilingua
				long noww2 = Utils.getTime();
				Utils.csvWr(new String[] { siteUrl, Utils.getDate() }, "SiteNotMultilingual.csv");
			}
			return;
		}

		// System.out.println("STEP 0... sono dentro la multilingual site
		// detection");

		// ----------------------------------------------------------------------------------------------------
		// blocco try in cui provo attraverso varie euristiche a rilevare se un
		// sito sia multilingua
		try {

			/*
			 * modifico la stringa del sito in modo opportuno per creare
			 * cartelle di output
			 */

			String nameFolder = UrlHelper.getNameFolderFromSiteUrl(siteUrl);

			// creo set dove mettere coppie trovate e su cui lanciare visita
			// ricorsiva
			// coppie trovate con metodo hreflang
			Set<List<String>> resultsPageExplorationHP = new HashSet<List<String>>();
			// coppie trovate con metodo lang detection+analisi strutturale
			Set<Set<String>> resultsPageExploration = new HashSet<Set<String>>();

			/*
			 * oggetto che rappresenta le info sul sito che si sta analizzando.
			 * il costruttore si va a creare il sito facendo get dal sito vero e
			 * proprio DAVIDE
			 */
			Site siteToDetect = new Site(siteUrl);

			long now = Utils.getTime();

			// lancio metodo per cercare l'attribute hreflang nella homepage, se
			// c'è restituisce set liste di url nelle varie lingue
			// in particolare RITORNA GRUPPI DI 5 SU CUI lanciare la visita
			// ricorsiva
			resultsPageExplorationHP = multilingualDetector.detectByHreflang(siteToDetect);

			long now2 = Utils.getTime();
			synchronized (timeLock) {
				Utils.csvWr(new String[] { siteUrl, "", "mult detection hreflang", "", Long.toString(now2 - now) },
						java.lang.Thread.currentThread().toString() + "time.csv");
			}

			// se ottengo entry points stampo il sito tra quelli multilingua e
			// aggiorno la struttura dati del sito
			if (resultsPageExplorationHP.size() != 0) {
				// aggiungo info al sito
				long noww2 = Utils.getTime();

				synchronized (multSiteLogLock) {
					// scrivo su csv il sito, l'eventuale redirect, il tipo di
					// rilevamento e l'istante in cui lo rilevo
					Utils.csvWr(new String[] { siteUrl, "homepageHrefLang", Long.toString(noww2 - noww) },
							"SiteMultilingual.csv");
				}

				// elimino folder di output e di crawling
				Utils.deleteDir("htmlPagesPreliminary" + nameFolder);

				int countEntryPoints = 0;
				for (List<String> currentGroupEP : resultsPageExplorationHP) {

					// creo l'oggetto parallelCollection con il gruppetto di
					// entry points paralleli corrente
					ParallelCollections parallelColl = new ParallelCollections(nameFolder + countEntryPoints,
							currentGroupEP, (depthT), siteUrl);

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
							// stampo nell'error log il sito che da il problema
							// e l'errore
							Utils.csvWr(siteUrl, e, "ErrorLog.csv");
						}
					}
				}
				return;
			}

			// System.out.println("FINE STEP 1 HREFLANG terminata verifica delle
			// presenza di hreflang");

			// ---------SOPRA METODO MENO ONEROSO
			// -----------------------------------------------------------------------------------

			// RICERCA TRA GLI OUTLINK DI UNA PAGINA DA ACCOPPIARE CON LA ROOT
			// Se le rilevazioni di hreflang nella hp o nella sitemap non
			// producono risultati
			// lancio il metodo isMultilingual per la rilevazione visitando i
			// link uscenti dalla hp

			now = Utils.getTime();

			if (resultsPageExplorationHP.size() == 0) {
				resultsPageExploration = (isMultilingualByExplorationLink(siteToDetect, nameFolder, errorLogLock));
			}
			now2 = Utils.getTime();

			synchronized (timeLock) {

				Utils.csvWr(new String[] { siteUrl, "", "mult detection outlinks", "", Long.toString(now2 - now) },
						java.lang.Thread.currentThread().toString() + "time.csv");
			}

			// se rilevato presenza contenuto multilingua stampa sito come
			// multilingua
			if (resultsPageExploration.size() != 0) {
				long noww2 = Utils.getTime();

				synchronized (multSiteLogLock) {

					Utils.csvWr(new String[] { siteUrl, "visitHomepage", Long.toString(noww2 - noww) },
							"SiteMultilingual.csv");
				}
			}

			// pulisco le folder di output e di crawling e lancio visita
			// ricorsiva
			Utils.deleteDir("htmlPagesPreliminary" + nameFolder);

			// se ho coppie candidate lancio visita ricorsiva
			if (resultsPageExploration.size() != 0) {

				int countGroupEP = 0;
				for (Set<String> currentPairEP : resultsPageExploration) {

					List<String> entryPoints = new ArrayList<String>();
					entryPoints.addAll(currentPairEP);
					ParallelCollections parallelColl = new ParallelCollections(nameFolder + countGroupEP, entryPoints,
							(depthT), siteUrl);
					countGroupEP++;

					// lancio rr
					try {
						// R2cursiveCrawling.recursiveCrawling(parallelColl,depthT,errorLogLock,productivityLock,timeLock);
					} catch (Exception e) {
						e.printStackTrace();
						synchronized (errorLogLock) {
							Utils.csvWr(siteUrl, e, "ErrorLog.csv");
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

			now = Utils.getTime();

			if (resultsPageExploration.size() == 0) {
				resultsPageExploration = preHomePage(siteToDetect, nameFolder, errorLogLock);
			}
			now2 = Utils.getTime();

			synchronized (timeLock) {
				Utils.csvWr(new String[] { siteUrl, "", "mult detection prehomepage", "", Long.toString(now2 - now) },
						java.lang.Thread.currentThread().toString() + "time.csv");
			}

			if (resultsPageExploration.size() != 0) {
				// elimino folder di output e di crawling
				Utils.deleteDir("htmlPagesPreliminary" + nameFolder);

				int countGroupEP = 0;
				for (Set<String> currentPairEP : resultsPageExploration) {

					List<String> entryPoints = new ArrayList<String>();
					entryPoints.addAll(currentPairEP);
					ParallelCollections parallelColl = new ParallelCollections(nameFolder + countGroupEP, entryPoints,
							(depthT), siteUrl);
					countGroupEP++;

					try {
						// R2cursiveCrawling.recursiveCrawling(parallelColl,depthT,errorLogLock,productivityLock,timeLock);
					} catch (Exception e) {
						e.printStackTrace();
						synchronized (errorLogLock) {
							Utils.csvWr(siteUrl, e, "ErrorLog.csv");
						}
					}
				}
				{
					synchronized (multSiteLogLock) {
						long noww2 = Utils.getTime();

						Utils.csvWr(new String[] { siteUrl, "PreHomepage", Long.toString(noww2 - noww) },
								"SiteMultilingual.csv");
					}

				}
				// System.out.println("FINE STEP 3 PREHOMEPAGE");

				return;
			}

			// se neanche con ultimo metodo non si trova nulla si considera il
			// sito non mutlilingua
			else {

				try {
					Utils.deleteDir("htmlPagesPreliminary" + nameFolder);
				} catch (Exception e) {
					e.printStackTrace();
					synchronized (errorLogLock) {
						// stampo nell'error log il sito che da il problema e
						// l'errore
						Utils.csvWr(siteUrl, e, "ErrorLog.csv");
					}
				}

				synchronized (multSiteLogLock) {
					long noww2 = Utils.getTime();

					Utils.csvWr(new String[] { siteUrl, Long.toString(noww2 - noww) }, "SiteNotMultilingual.csv");
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
				Utils.csvWr(siteUrl, e, "ErrorLog.csv");
			}
			{
				synchronized (multSiteLogLock) {
					long noww2 = Utils.getTime();

					Utils.csvWr(new String[] { siteUrl, Long.toString(noww2 - noww) }, "SiteNotMultilingual.csv");
				}
				// System.out.println("FINE STEP 3");
				return;
			}
		}

	}// fine main

	private static Set<Set<String>> preHomePage(Site site, String nameFolder, Lock errorLogLock)
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
								Utils.csvWr(new String[] { site.getUrl(), e.toString() }, "ErrorLog.csv");
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
			localPath2url.putAll(
					launchRRDownloadPagesToDecideIfMultilingual(nameFolder, linkPossible.get(0), linkPossible.get(1),
							countPotentialEntryPoints, errorLogLock, true, errorLogLock, site.getUrlRedirect()));

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
				Utils.csvWr(new String[] { site.getUrl(), e.toString() }, "ErrorLog.csv");

			}
		}

		// delete dei file creati con questo metodo
		for (String ftv : fileToVerify)
			Utils.deleteDir("output/" + ftv);

		// elimino file crawlati
		Utils.deleteDir("htmlPagesPreliminary" + nameFolder);

		return parallelEntryPoints;

	}

	// OK
	// se sito multilingua tramite esplorazione link uscenti (verifica lingua e
	// struttura), se sito multilingua ritorna set di coppie candidate
	public static Set<Set<String>> isMultilingualByExplorationLink(Site site, String nameFolder, Lock errorLogLock)
			throws IOException, InterruptedException, LangDetectException {

		// creo set dove mettere coppie trovate, (set di set(coppie))
		Set<Set<String>> resultsPageExploration = new HashSet<Set<String>>();

		// mappa link visitati e path locale dove risiedono in locale
		Map<String, String> localPath2url = new HashMap<String, String>();

		// lista con link su cui lanciare rr
		List<String> outlinkToVisit = outlinkToVisit(site, errorLogLock);

		// System.out.println("VISIT OUTLINKS 1 outlink: " + outlinkToVisit);

		List<String> fileToVerify = new ArrayList<String>();

		int countPotentialEntryPoints = 0;
		// sui link più interessanti appena selezionati lancio RR
		for (String linkPossible : outlinkToVisit) {
			countPotentialEntryPoints++;

			// lancio rr e faccio language detection
			// String[] a ={nameFolder,site.getUrlRedirect(),
			// linkPossible,Integer.toString(countEntryPoints)};

			// lingua già verificata, lancia rr solo se superata scrematura di
			// verifica strutt blanda
			fileToVerify.add(nameFolder + countPotentialEntryPoints);
			localPath2url.putAll(launchRRDownloadPagesToDecideIfMultilingual(nameFolder, site.getUrlRedirect(),
					linkPossible, countPotentialEntryPoints, errorLogLock, false, errorLogLock, site.getUrlRedirect()));
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
			list.addAll(langDetectAndThresholdLabel(nameFolder, fileToVerify, errorLogLock, site));

			// System.out.println("ASDD "+list);

			for (String outlink : list) {
				Set<String> currPair = new HashSet<String>();
				currPair.add(site.getUrlRedirect());
				currPair.add(localPath2url.get(outlink));
				resultsPageExploration.add(currPair);
			}

		} catch (LangDetectException e) {
			e.printStackTrace();
			synchronized (errorLogLock) {
				Utils.csvWr(new String[] { site.getUrl(), e.toString() }, "ErrorLog.csv");
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

	// OK
	// link da visitare, scorrendo link del sito e scremando quelli con edit
	// distance troppo grande rispetto alla homepage
	// verifico anche che siano in lingua diversa, altrimenti non mi interessano
	public static List<String> outlinkToVisit(Site site, Lock errorLogLock) throws IOException, LangDetectException {

		// set di link già visitati
		Set<String> alreadyVisit = new HashSet<String>();

		// seleziono i paragrafi per fare lang detection
		Elements paragraphHtmlDocument = site.getDocument().select("p");
		String stringParagraphHtmlDocument = (paragraphHtmlDocument.text());

		// caricamento dei profili per la language detection
		if (DetectorFactory.getLangList().size() == 0)
			DetectorFactory.loadProfile("profiles.sm");

		Detector detector = DetectorFactory.create();
		if (paragraphHtmlDocument.text().length() == 0) {
			detector.append(site.getDocument().text());
		} else
			detector.append(stringParagraphHtmlDocument);

		// language homepage
		String langHP = (detector.detect());

		// lista di link da esplorare
		List<String> linkToExplore = new ArrayList<String>();

		// select all link to search other half of candidate pair (hp, half)
		Elements links = site.getDocument().select("a");

		// funzione di filtraggio che mi da una lista di link (tag a) possibili
		// entrypoints (perchè hanno lingua diversa homepage)
		editDistanceAndLanguageFilter(links, alreadyVisit, site, linkToExplore, errorLogLock, detector, langHP);

		// uguale a sopra ma per gli option
		links = site.getDocument().select("option");
		editDistanceAndLanguageFilter(links, alreadyVisit, site, linkToExplore, errorLogLock, detector, langHP);

		return linkToExplore;
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

	// OK
	// metodo che verifica che edit distance e lingua dei link uscenti dalla
	// homepage siano compatibili con lo stato di entry points
	public static void editDistanceAndLanguageFilter(Elements links, Set<String> alreadyVisit, Site site,
			List<String> linkToExplore, Lock errorLogLock, Detector detector, String langHP) throws IOException {

		for (Element link : links) {

			// show absolute url
			String linkPossible = link.absUrl("href");

			// controllo se link è già stato visitato
			if (!alreadyVisit.contains(linkPossible))

				// controllo se link è diverso da homepage
				if (!site.getUrlRedirect().equals(linkPossible)) {

					// controllo se la edit distance tra i link sia ragionevole
					if (((linkPossible.length() >= site.getUrlRedirect().length())
							&& (StringUtils.getLevenshteinDistance(site.getUrlRedirect(),
									linkPossible) < linkPossible.length() - site.getUrlRedirect().length() + 4
							|| StringUtils.getLevenshteinDistance(site.getUrlRedirect(),
									linkPossible) < linkPossible.length() - site.getUrlRoot().length() + 4)
							&& (linkPossible.length() < site.getUrlRedirect().length() * 2.7))
							|| ((linkPossible.length() <= site.getUrlRedirect().length())
									&& (StringUtils.getLevenshteinDistance(site.getUrlRedirect(),
											linkPossible) < linkPossible.length() / 2))) {
						// verifico che la lingua sia diversa
						try {
							alreadyVisit.add(linkPossible);
							detector = DetectorFactory.create();
							Document documentLinkPossible = Jsoup.connect(linkPossible)
									.userAgent("Opera/9.63 (Windows NT 5.1; U; en) Presto/2.1.1").timeout(8000).get();
							Elements paragraphLinkPossible = documentLinkPossible.select("p");
							String stringParagraphLinkPossible = (paragraphLinkPossible.text());
							if (paragraphLinkPossible.text().length() == 0) {
								detector.append(documentLinkPossible.text());
							} else
								detector.append(stringParagraphLinkPossible);
							String langLinkPossible = (detector.detect());

							if (!langLinkPossible.equals(langHP)) {
								linkToExplore.add(linkPossible);
							}

						} catch (Exception e) {
							e.printStackTrace();
							synchronized (errorLogLock) {
								Utils.csvWr(new String[] { site.getUrlRedirect(), e.toString() }, "ErrorLog.csv");

							}
						}

					}
				}
		}

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
			new File("htmlPagesPreliminary" + nameFolder + "/").mkdir();

			// this create folder in order to save download pages
			new File("htmlPagesPreliminary" + nameFolder + "/" + nameFolder + countEntryPoints).mkdir();

			// folder url where download page
			String urlBase = "htmlPagesPreliminary" + nameFolder + "/" + nameFolder + countEntryPoints;

			// page 1
			String page1;
			if (!preHomepage) {
				// page 1(della coppia 1,2 da dare a rr) sempre stessa in questa
				// fase: la home:
				if (countEntryPoints == 1) {
					downloadFromUrl(new URL(site), urlBase + "/" + "HomePage" + countEntryPoints + "-1" + ".html",
							userAgent);
					// aggiorno mappa link visitati
					localPath2url.put("htmlPagesPreliminary" + nameFolder + "/" + nameFolder + 1 + "/" + "HomePage" + 1
							+ "-1" + ".html", site);
				}

				// string with homepage path
				page1 = "htmlPagesPreliminary" + nameFolder + "/" + nameFolder + 1 + "/" + "HomePage" + 1 + "-1"
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
									Utils.csvWr(new String[] { site, e1.toString() }, "ErrorLog.csv");
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
				Utils.csvWr(new String[] { site, ex.toString() }, "ErrorLog.csv");
			}
		}

		return localPath2url;
	}

	// OK
	// metodo per creare file style per output di rr
	public static void backupFile(String folder) throws FileNotFoundException, IOException {
		new File("output").mkdir();

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
			Lock errLogLock, Site site) throws LangDetectException, IOException {

		List<String> nameFileParallel;
		Map<String, List<String>> fileOutputRR2textParallel;

		// mappa che conterrà come chiave la lingua e come valore la pagina
		// parallela alla homepage in locale
		Map<String, String> pathLocalCandidate = new HashMap<String, String>();

		// parametro con root path risultati dove vado a verificare i suddetti
		// file di output di rr
		String pathRoot = "output";

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
						File folderR = new File("htmlPagesPreliminary" + folderRoot + "/" + ftv);

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
								Utils.csvWr(new String[] { site.getUrlRedirect(), e.toString() }, "ErrorLog.csv");
							}
						}

					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				Utils.csvWr(new String[] { site.getUrlRedirect(), e.toString() }, "ErrorLog.csv");
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
	public static List textParallel(String path, Lock errorLogLock, Site site) throws IOException {

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
				Utils.csvWr(new String[] { site.getUrlRedirect(), e.toString() }, "ErrorLog.csv");
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
			Lock errLogLock, Site site, Map<String, String> localPath2url) throws LangDetectException, IOException {

		List<String> nameFileParallel;
		Map<String, List<String>> fileOutputRR2textParallel;

		Set<Set<String>> parallelPair = new HashSet<Set<String>>();

		// parametro con root path risultati dove vado a verificare i suddetti
		// file di output di rr
		String pathRoot = "output";

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
				Utils.csvWr(new String[] { site.getUrlRedirect(), e.toString() }, "ErrorLog.csv");
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
