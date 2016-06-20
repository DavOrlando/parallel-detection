package it.uniroma3.parallel.filter;

import it.uniroma3.parallel.model.Page;

/**
 * Interfaccia per le varie operazioni di filtraggio.
 * @author davideorlando
 *
 */
public interface Filter {

	public boolean filter(Page homepage, Page outlinkPage);

}
