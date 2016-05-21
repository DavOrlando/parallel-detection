package it.uniroma3.parallel.detection;

import java.io.IOException;

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.model.GroupOfParallelUrls;
import it.uniroma3.parallel.model.Page;

/***
 * Interfaccia di un Detector. Un Detector concreto implementerà una strategia
 * di detection, in questo Sistema sono implementate le strategie di detection.
 * 
 * @author davideorlando
 *
 */
public interface Detector {

	/**
	 * Ritorna un gruppo di entry points, ovvero URL paralleli, attraverso una
	 * delle euristiche.
	 * 
	 * @param homepage
	 * @return
	 * @throws IOException
	 * @throws LangDetectException
	 * @throws InterruptedException
	 */
	public GroupOfParallelUrls detect(Page homepage) throws IOException, InterruptedException, LangDetectException;

}
