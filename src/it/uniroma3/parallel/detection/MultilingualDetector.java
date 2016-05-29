package it.uniroma3.parallel.detection;

import java.io.IOException;
import java.net.URL;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.GroupOfParallelUrls;
import it.uniroma3.parallel.model.Homepage;

/**
 * Classe che rappresenta un astrazione di un rilevatore di siti multilingua.
 * Possiede metodi per comprendere se un sito è multilingua oppure no.
 * 
 * 
 * @author davideorlando
 *
 */

public abstract class MultilingualDetector {


	/**
	 * Rileva se il sito è un falso multilingua, attraverso l'analisi del suo
	 * URL. Sito falso multlingua vuol dire parallelo nella struttura ma non
	 * nella semantica.
	 * 
	 * @param homepageURL
	 *            la stringa che corrisponde all'URL del sito
	 * @return true se il sito è un falso multilingua, false altrimenti
	 */
	public boolean isInBlacklist(URL homepageURL) {
		return homepageURL.toString().contains("citysite.") || homepageURL.toString().contains("citycorner.")
				|| homepageURL.toString().contains("stadtsite.");
	}

	/**
	 * Ritorna un gruppo di entry points, ovvero URL di homepage parallele in
	 * altre lingue. Attraverso una delle euristiche.
	 * 
	 * @param homepage
	 * @return
	 * @throws IOException
	 * @throws LangDetectException
	 * @throws InterruptedException
	 */
	public abstract GroupOfParallelUrls detect(Homepage homepage)
			throws IOException, InterruptedException, LangDetectException;
}
