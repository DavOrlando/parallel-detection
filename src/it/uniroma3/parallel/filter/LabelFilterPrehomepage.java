package it.uniroma3.parallel.filter;

import java.util.ArrayList;
import java.util.List;
import it.uniroma3.parallel.model.PairOfPages;
import it.uniroma3.parallel.model.ParallelPages;
import it.uniroma3.parallel.roadrunner.RoadRunnerDataSet;
import it.uniroma3.parallel.utils.FetchManager;

public class LabelFilterPrehomepage {

	/**
	 * Ritorna una collezione di URL dove ognuno corrisponde alla pagina
	 * multilingua e parallela più probabile per quel linguaggio differente
	 * rispetto alla homepage. La pagina è scelta in base al criterio del numero
	 * di label che RoadRunner riesce ad allineare con la homepage vera e
	 * propria. Quindi ogni lingua ci viene ritornato solo l'URL che corrisponde
	 * alla pagina con più label allineate con la homepage.
	 * 
	 * @param parallelPages
	 * @return
	 * 
	 */
	public List<PairOfPages> filter(ParallelPages parallelPages) {
		List<PairOfPages> pairOfPages = new ArrayList<>();
		try {
			for (PairOfPages pair : parallelPages.getListOfPairs()) {
				RoadRunnerDataSet roadRunnerDataSet = FetchManager.getInstance().getRoadRunnerDataSet(pair);
				if (roadRunnerDataSet == null || roadRunnerDataSet.getNumberOfLabels() < 1)
					continue;
				pairOfPages.add(pair);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pairOfPages;
	}

}
