package it.uniroma3.parallelcorpora.filter;

import java.util.LinkedList;
import java.util.List;

import it.uniroma3.parallelcorpora.model.PairOfPages;
import it.uniroma3.parallelcorpora.model.ParallelPages;
import it.uniroma3.parallelcorpora.roadrunner.RoadRunnerDataSet;
import it.uniroma3.parallelcorpora.utils.FetchManager;

/**
 * Classe che rappresenta un filtro per prehomepage.
 * 
 * @author davideorlando
 *
 */
public class PrehomepageLabelFilter {

	/**
	 * Utilizza i risultati di RoadRunner per filtrare le coppie, devono avere
	 * abbastanza label.
	 * 
	 * @param parallelPages
	 * @return
	 * 
	 */
	public List<PairOfPages> filter(ParallelPages parallelPages) {
		List<PairOfPages> pairOfPages = new LinkedList<>();
		for (PairOfPages pair : parallelPages.getListOfPairs()) {
			RoadRunnerDataSet roadRunnerDataSet = FetchManager.getInstance().getRoadRunnerDataSet(pair);
			// TODO controllare se <16 va bene
			if (roadRunnerDataSet != null && roadRunnerDataSet.getNumberOfLabels() >= 16)
				pairOfPages.add(pair);
		}
		return pairOfPages;
	}

}
