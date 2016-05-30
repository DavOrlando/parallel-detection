package it.uniroma3.parallel.detection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.GroupOfHomepages;
import it.uniroma3.parallel.model.GroupOfParallelUrls;
import it.uniroma3.parallel.model.Homepage;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.utils.Utils;

public class PrehomepageOutlinkDetector extends OutlinkDetector {

	public PrehomepageOutlinkDetector() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public GroupOfParallelUrls detect(Homepage homepage) throws IOException, InterruptedException, LangDetectException {
		// da ritornare alla fine
		GroupOfParallelUrls parallelHomepageUrl = new GroupOfParallelUrls();
		GroupOfHomepages groupOfHomepage = new GroupOfHomepages(homepage);
		this.downloadPagesInLocal(groupOfHomepage);
		this.runRoadRunner(groupOfHomepage);
		for (URL verifiedURL : langDetectAndThresholdLabel(groupOfHomepage)) {
			parallelHomepageUrl.addURL((verifiedURL));
		}
		this.deleteOutputRROfHomepages(groupOfHomepage);
		return parallelHomepageUrl;
	}
	
	
	private  Set<Set<String>> preHomePage(Page homepage)
			throws IOException, LangDetectException {
		String nameFolder = homepage.getNameFolder();
		// creo set dove mettere coppie trovate
		Set<Set<String>> parallelEntryPoints = null;

		// mappa link visitati e path locale dove risiedono in locale
		Map<String, String> localPath2url = new HashMap<String, String>();

		Set<String> differentLanguage = new HashSet<String>();

		// langauge homepage
		Elements ps = homepage.getDocument().select("p");
		String stringLangHP = (ps.text());
		if (DetectorFactory.getLangList().size() == 0)
			DetectorFactory.loadProfile("profiles.sm");

		Detector detector = DetectorFactory.create();
		if (ps.text().length() == 0) { // DAVIDE: se non ci sono <p>∆
			detector.append(homepage.getDocument().text());
		} else
			detector.append(stringLangHP);

		List<ArrayList<String>> linkPairToExplore = new ArrayList<ArrayList<String>>();

		// select all link to search other half of candidate pair (hp, half)
		Elements links = homepage.getDocument().select("a");

		// var per verifica su numero lingue diverse
		boolean firstTime = true;

		// scorro i link della homepage, selezionando i più interessanti e
		// lancio RR
		if (links.size() < 16)
			for (Element link1 : links) {
				String linkPossible1 = link1.absUrl("href");
				detector = DetectorFactory.create();
				Document document3 = Jsoup.connect(linkPossible1)
						.userAgent(USER_AGENT).timeout(8000).get();
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
									.userAgent(USER_AGENT).timeout(8000).get();
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
								Utils.csvWr(new String[] { homepage.getUrl().toString(), e.toString() }, ERROR_LOG_CSV);
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
		List<String> fileToVerify = new ArrayList<String>();

		int countPotentialEntryPoints = 0;
		// scorro i link della homepage, selezionando i più interessanti e
		// lancio RR
		for (List<String> linkPossible : linkPairToExplore) {
			countPotentialEntryPoints++;

			fileToVerify.add(nameFolder + countPotentialEntryPoints);
			localPath2url.putAll(launchRRDownloadPagesToDecideIfMultilingual(nameFolder, linkPossible.get(0),
					linkPossible.get(1), countPotentialEntryPoints, errorLogLock, true, errorLogLock,
					homepage.getUrlRedirect().toString()));

		}

		// controllo ora l'output di rr, se lingua pagine accoppiate è diversa e
		// se hanno abbastanza label
		try {

			// lista dei file accoppiabili
			parallelEntryPoints = new HashSet<Set<String>>();

			// lancio metodo che ritorna lista file accoppiabili
			parallelEntryPoints.addAll(langDetectAndThresholdLabelPreHomepage(nameFolder, fileToVerify, errorLogLock,
					homepage, localPath2url));

		} catch (LangDetectException e) {
			synchronized (errorLogLock) {
				e.printStackTrace();
				Utils.csvWr(new String[] { homepage.getUrl().toString(), e.toString() }, ERROR_LOG_CSV);

			}
		}

		// delete dei file creati con questo metodo
		for (String ftv : fileToVerify)
			Utils.deleteDir("output/" + ftv);

		// elimino file crawlati
		Utils.deleteDir(HTML_PAGES_PRELIMINARY + nameFolder);

		return parallelEntryPoints;

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
					downloadFromUrl(new URL(site), urlBase + "/" + "HomePage" + countEntryPoints + "-1" + ".html",
							userAgent);
					// aggiorno mappa link visitati
					localPath2url.put(urlBase + "/" + "HomePage" + countEntryPoints + "-1" + ".html", site);

					// string with homepage path
					page1 = urlBase + "/" + "HomePage" + countEntryPoints + "-1" + ".html";
				
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
						@Override
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
}
