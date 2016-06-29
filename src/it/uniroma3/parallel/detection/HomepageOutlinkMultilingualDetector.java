package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.roadrunner.RoadRunnerInvocator;
import it.uniroma3.parallel.filter.HomepageLabelFilter;
import it.uniroma3.parallel.model.Page;
import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.utils.FetchManager;

/**
 * Classe che è rappresenta la seconda euristica. Supponendo che abbiamo davanti
 * un homepage selezioniamo solo i link uscenti con più regioni parallele
 * all'homepage stessa e di diverso linguaggio e che possiedono come valori
 * stringhe rilevanti per il cambiamento di lingua(come 'english' oppure 'en').
 * 
 * @author davideorlando
 *
 */
public class HomepageOutlinkMultilingualDetector extends OutlinkMultilingualDetector {

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
	 * @throws InterruptedException
	 * @throws LangDetectException
	 * @throws URISyntaxException
	 */

	@Override
	public ParallelPages detect(Page page)
			throws IOException, InterruptedException, LangDetectException, URISyntaxException {
		// da ritornare alla fine
		ParallelPages parallelPage = new ParallelPages(page);
		findCandidatePages(parallelPage);
		organizeInPairs(parallelPage);
		FetchManager.getInstance().persistParallelPages(parallelPage);
		RoadRunnerInvocator.getInstance().runRoadRunner(parallelPage);
		parallelPage.lasciaSoloQuestiURL(new HomepageLabelFilter().filter(parallelPage));
		return parallelPage;
	}

	/**
	 * Trova le pagine candidate ad essere parallele oltre alla homepage che è
	 * già fra le candidate.
	 * 
	 * @param parallelPage
	 */
	public void findCandidatePages(ParallelPages parallelPage){
		for (Page page : getMultilingualPage(parallelPage.getStarterPage())) {
			parallelPage.addCandidateParallelHomepage(page);
		}
	}

	/***
	 * Crea una lista con tutte le coppie generate da
	 * (firstHomepage,possibleParallelHomepages[i-esima]). E' possibile accedere
	 * alla lista, dopo averla creata con questo metodo, grazie a getPair().
	 * 
	 * @return
	 */
	@Override
	public void organizeInPairs(ParallelPages parallelPage) {
		LinkedList<PairOfPages> listOfPairs = new LinkedList<>();
		int i = 1;
		for (Page page : parallelPage.getCandidateParallelHomepages()) {
			if (!page.equals(parallelPage.getStarterPage())) {
				PairOfPages pair = new PairOfPages(parallelPage.getStarterPage(), page, i);
				listOfPairs.add(pair);
				i++;
			}
		}
		parallelPage.setListOfPair(listOfPairs);
	}

}