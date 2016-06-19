package it.uniroma3.parallel.model;

import java.util.ArrayList;
import java.util.List;

import it.uniroma3.parallel.roadrunner.RoadRunnerDataSet;

/**
 * Classe che rappresenta una coppia di homepage. Necessaria perchè sarà l'input
 * per RoadRunner, infatti memorizza un riferimento al suo output.
 * 
 * @author davideorlando
 *
 */
public class PairOfHomepages {

	private int pairNumber;
	private List<Page> homepages;

	/**
	 * Costruisce una coppia di due pagine. La prima è l'homepage del sito
	 * mentre la seconda è una probabile homepage. Inoltre numeriamo la coppia
	 * con un numero che è relativo alle coppie trovate per una sola detection.
	 * 
	 * @param homepage
	 * @param page
	 * @param pairNumber
	 */
	public PairOfHomepages(Homepage homepage, Page page, int pairNumber) {
		this.pairNumber = pairNumber;
		this.homepages = new ArrayList<>(2);
		this.homepages.add(homepage);
		this.homepages.add(page);
	}

	public int getPairNumber() {
		return this.pairNumber;
	}

	/**
	 * Ritorna la homepage principale. Ovvero il primo elemento della coppia
	 * dato che pensiamo sempre a coppie (Homepage di partenza, Homepage in
	 * altra lingua).
	 * 
	 * @return
	 */
	public Homepage getMainHomepage() {
		return (Homepage) this.getHomepageFromList(0);
	}

	/**
	 * Ritorna la pagina chiesta dal parametro.
	 * 
	 * @param numberOfPage
	 * @return
	 */
	public Page getHomepageFromList(int numberOfPage) {
		return this.homepages.get(numberOfPage);
	}

	@Override
	public int hashCode() {
		int code = 0;
		for (Page page : this.homepages) {
			code += page.hashCode();
		}
		return code;
	}
	
	@Override
	public boolean equals(Object obj) {
		PairOfHomepages pairOfHomepages = (PairOfHomepages) obj;
		return this.homepages.equals(pairOfHomepages.homepages);
	}

}
