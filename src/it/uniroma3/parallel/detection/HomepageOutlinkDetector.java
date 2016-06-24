package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.net.URISyntaxException;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.filter.LabelFilter;
import it.uniroma3.parallel.model.Homepage;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.utils.FetchManager;

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
	 * @throws URISyntaxException 
	 */

	@Override
	public ParallelPages detect(Page page) throws IOException, InterruptedException, LangDetectException, URISyntaxException {
		Homepage homepage = (Homepage) page;
		// da ritornare alla fine
		ParallelPages groupOfHomepage = new ParallelPages(homepage);
		FetchManager.getInstance().persistParallelPages(groupOfHomepage);;
		this.runRoadRunner(groupOfHomepage);
		LabelFilter labelFilter = new LabelFilter();
		groupOfHomepage.lasciaSoloQuestiURL(labelFilter.filter(groupOfHomepage));
		return groupOfHomepage;
	}

	

}