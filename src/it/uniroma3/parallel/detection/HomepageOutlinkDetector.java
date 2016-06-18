package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.net.URL;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.GroupOfHomepages;
import it.uniroma3.parallel.model.Homepage;
import it.uniroma3.parallel.utils.FetchManager;
import it.uniroma3.parallel.utils.Utils;

public class HomepageOutlinkDetector extends OutlinkDetector {

	public HomepageOutlinkDetector() {
		
	}

	/**
	 * Si cercano nella homepage del sito passato come parametro dei link
	 * uscenti che portano a pagine di linguaggio differente e stessa struttura
	 * del DOM, euristica che ci porta a dire che il sito è multilingua. Si
	 * collezionano tutti questi URL, che rappresentano gli URL verso le pagine
	 * nelle varie lingue.
	 * 
	 * @see GroupOfParallelUrls
	 * @param homepage
	 *            la pagina che rappresenta l'homepage del sito
	 * @return GroupOfParallelUrls
	 * @throws IOException
	 */

	@Override
	public GroupOfHomepages detect(Homepage homepage) throws IOException, InterruptedException, LangDetectException {
		// da ritornare alla fine
		GroupOfHomepages groupOfHomepage = new GroupOfHomepages(homepage);
		groupOfHomepage.findCandidateParallelHomepages();
		groupOfHomepage.organizeInPairs();
		FetchManager.getInstance().persistGroupOfHomepage(groupOfHomepage);;
		this.runRoadRunner(groupOfHomepage);
		groupOfHomepage.lasciaSoloQuestiURL(filterByLabel(groupOfHomepage));
		this.deleteOutputRROfHomepages(groupOfHomepage);
		return groupOfHomepage;
	}

	

}