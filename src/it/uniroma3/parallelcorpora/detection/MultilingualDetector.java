package it.uniroma3.parallelcorpora.detection;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.login.Configuration;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallelcorpora.configuration.ConfigurationProperties;
import it.uniroma3.parallelcorpora.model.Page;
import it.uniroma3.parallelcorpora.model.ParallelPages;

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
		boolean isInBlacklist = false;
		List<String> blacklist = ConfigurationProperties.getInstance().getBlacklist();
		for (Iterator<String> iterator = blacklist.iterator(); !isInBlacklist && iterator.hasNext();) {
			String s = iterator.next();
			isInBlacklist = homepageURL.toString().contains(s);
		}
		return isInBlacklist;
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
	 * @throws URISyntaxException
	 */
	public abstract ParallelPages detect(Page page)
			throws IOException, InterruptedException, LangDetectException, URISyntaxException;
}
