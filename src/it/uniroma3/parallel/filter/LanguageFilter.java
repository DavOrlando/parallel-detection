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
	 * Ritorna true se sono verificate le seguenti condizioni: -Il secondo link
	 * non porta ad una pagina con lo stesso linguaggio della homepage;
	 * 
	 * @param homepage
	 * @param outlinkString
	 * @return
	 * @throws LangDetectException
	 * @throws IOException
	 */
	@Override
	public boolean filter(Page homepage, String outlinkString) {
		return !isSameLanguage(homepage, outlinkString);
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
	private boolean isSameLanguage(Page homepage, String outlinkString) {
		// nel caso di un exception allora torno il valore che mi fa fallire il
		// filtraggio
		boolean stessoLinguaggio = true;
		try {
			Page differentLanguagePage = new Page(new URL(outlinkString));
			stessoLinguaggio = homepage.getLanguage().equals(differentLanguagePage.getLanguage());
		} catch (LangDetectException | IOException e) {
			e.printStackTrace();
		}
		return stessoLinguaggio;
	}
}
