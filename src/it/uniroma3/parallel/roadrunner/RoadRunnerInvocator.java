package it.uniroma3.parallel.roadrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import it.uniroma3.parallel.configuration.ConfigurationProperties;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.utils.FetchManager;
import it.uniroma3.parallel.utils.Utils;

public class RoadRunnerInvocator {

	private static final String _1 = "1 ";
	private static final String ATTR = "attr ";
	private static final String LO_SPAZIO_DI_NOMI = "Lo spazio di nomi";
	private static final String PARAMETRO_N = "-N:";
	private static final String HOME_PAGE = "HomePage";
	private static final String _2 = "-2";
	private static final String HTML = ".html";
	private static RoadRunnerInvocator instance;
	private ConfigurationProperties properties = ConfigurationProperties.getInstance();
	protected Lock errorLogLock;

	private RoadRunnerInvocator() {
		this.errorLogLock = new ReentrantLock();
	}
	

	public static synchronized RoadRunnerInvocator getInstance() {
		if(instance==null)
			instance = new RoadRunnerInvocator();
		return instance;
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
	public void runRoadRunner(ParallelPages groupOfHomepage)
			throws FileNotFoundException, IOException, InterruptedException {
		for (PairOfPages pair : groupOfHomepage.getListOfPairs()) {
			launchRR(pair, errorLogLock, groupOfHomepage.getStarterPage());
		}
	}

	public void launchRR(PairOfPages pairOfHomepage, Lock errorLogLock, Page primaryPage)
			throws FileNotFoundException, IOException, InterruptedException {
		int pairNumber = pairOfHomepage.getPairNumber();

		String urlBase = properties.getStringOfFolderForHtmlPages()
				+ primaryPage.getPageName() + "/" + primaryPage.getPageName() + pairNumber;
		FetchManager.getInstance().createFolderAndCopyStyleFile(primaryPage.getPageName() + pairNumber);
		
		Thread t3 = new Thread() {
			@Override
			public void run() {
				try {
					String localPath = FetchManager.getInstance().findPageByURL(primaryPage.getUrlRedirect());
					rr(PARAMETRO_N + primaryPage.getPageName() + pairNumber, properties.getStringOfCommandAndPrefs(), localPath,
							urlBase + "/" + HOME_PAGE + pairNumber + _2 + HTML);
					String ftv = primaryPage.getPageName() + pairNumber;
					// alla coppia associo il suo output se esiste
					if (new File(properties.getStringOfFolderOutput()+ "/" + ftv + "/" + ftv + properties.getStringOfDataset()).exists())
						FetchManager.getInstance().addRRDataSet(pairOfHomepage,
								new RoadRunnerDataSet(properties.getStringOfFolderOutput() + "/" + ftv + "/" + ftv + properties.getStringOfDataset()));
				} catch (Exception e1) {
					e1.printStackTrace();
					synchronized (errorLogLock) {
						// stampo nell'error log il sito che da il
						// problema e l'errore
						try {
							Utils.csvWr(new String[] { primaryPage.getURLString(), e1.toString() }, properties.getStringOfErrorLogCSV());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		t3.start();
		t3.join(30000);// aspetta 30 secondi al massimo e chiude se ancora in
						// esecuzione
		if (t3.isAlive())
			t3.stop();
	}

	// lancia rr
	public void rr(String... argv) throws Exception {

		try {
			it.uniroma3.dia.roadrunner.Shell.main(argv);
			it.uniroma3.dia.roadrunner.tokenizer.token.TagFactory.reset();
			it.uniroma3.dia.roadrunner.tokenizer.token.Tag.reset();

			Utils.csvWr(new String[] { argv[0] }, properties.getStringOfRRCSV());

		} catch (Exception e) {
			e.printStackTrace();
			String attribute = "";
			String line = e.toString().split("/n")[0];
			if (line.contains(LO_SPAZIO_DI_NOMI))
				attribute = attribute.concat(line.split("'")[3]).concat(":");
			else
				return;
			System.out.println(ATTR + attribute);
			System.out.println(_1 + argv[0].substring(3));
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

	public String generateNewPrefsXmlNew(String attribute, String nameSite, String pathFiles) throws IOException {

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
