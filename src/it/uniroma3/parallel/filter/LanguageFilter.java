package it.uniroma3.parallel.filter;

import java.io.IOException;
import java.net.URL;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.Page;

/**
 * Classe che rappresenta un filtro per gli outlink. Si filtra sulla base del
 * linguaggio diverso.
 * 
 * @author davideorlando
 *
 */
public class LanguageFilter implements Filter {

	/**
	 * Ritorna false se sono verificate le seguenti condizioni: -Il secondo link
	 * porta ad una pagina con lo stesso linguaggio della homepage;
	 * 
	 * @param homepage
	 * @param outlinkPage
	 * @return
	 * @throws LangDetectException
	 * @throws IOException
	 */
	@Override
	
	public boolean filter(Page homepage, Page outlinkPage) {
		return !isSameLanguage(homepage, outlinkPage);
	}

	/***
	 * Ritorna true se l'URL porta ad una pagina con lo stesso linguaggio della
	 * homepage.
	 * 
	 * @param homepage
	 * @param outlinkString
	 * @return
	 * @throws LangDetectException
	 * @throws IOException
	 */
	private boolean isSameLanguage(Page homepage, Page outlinkPage) {
		// nel caso di un exception allora torno il valore che mi fa fallire il
		// filtraggio
		boolean stessoLinguaggio = true;
		try {
			stessoLinguaggio = homepage.getLanguage().equals(outlinkPage.getLanguage());
		} catch (LangDetectException e) {
			e.printStackTrace();
		}
		return stessoLinguaggio;
	}
}
