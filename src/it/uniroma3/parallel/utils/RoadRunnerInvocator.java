package it.uniroma3.parallel.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.locks.Lock;

import it.uniroma3.parallel.detection.OutlinkDetector;
import it.uniroma3.parallel.model.Homepage;
import it.uniroma3.parallel.model.PairOfHomepages;

public class RoadRunnerInvocator {

	private static final String HTML_PAGES_PRELIMINARY = "htmlPagesPreliminary";

	public static void launchRR(PairOfHomepages pairOfHomepage, Lock errorLogLock)
			throws FileNotFoundException, IOException, InterruptedException {
		int pairNumber = pairOfHomepage.getPairNumber();
		Homepage homepage = pairOfHomepage.getMainHomepage();
		String urlBase = HTML_PAGES_PRELIMINARY + homepage.getNameFolder() + "/" + homepage.getNameFolder() + pairNumber;

		// creo folder e file style per l'output di rr
		OutlinkDetector.backupFile(homepage.getNameFolder() + pairNumber);

		// System.out.println("RRRRRR " + page1 + " "+
		// urlBase+"/"+"HomePage"+countEntryPoints+"-2"+".html");

		Thread t3 = new Thread() {
			@Override
			public void run() {
				try {
					rr("-N:" + homepage.getNameFolder() + pairNumber, "-O:etc/flat-prefs.xml", homepage.getLocalPath(),
							urlBase + "/" + "HomePage" + pairNumber + "-2" + ".html");
				} catch (Exception e1) {
					e1.printStackTrace();
					synchronized (errorLogLock) {
						// stampo nell'error log il sito che da il
						// problema e l'errore
						try {
							Utils.csvWr(new String[] { homepage.getURLString(), e1.toString() }, "ErrorLog.csv");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		t3.start();
		t3.join(30000);//aspetta 30 secondi al massimo e chiude se ancora in esecuzione
		if (t3.isAlive())
			t3.stop();
	}

	// lancia rr
	public static void rr(String... argv) throws Exception {

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
			String newXMLpref = OutlinkDetector.generateNewPrefsXmlNew(attribute, argv[0].substring(3), pages);
			System.out.println(newXMLpref);
			argv[1] = "-O:".concat(newXMLpref);
			System.out.println("newwwwww " + argv[1]);
			it.uniroma3.dia.roadrunner.Shell.main(argv);
			it.uniroma3.dia.roadrunner.tokenizer.token.TagFactory.reset();
			it.uniroma3.dia.roadrunner.tokenizer.token.Tag.reset();
		}

	}

}
