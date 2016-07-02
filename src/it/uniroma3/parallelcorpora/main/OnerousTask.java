package it.uniroma3.parallelcorpora.main;

import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;


import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallelcorpora.detection.MultilingualDetection;
import it.uniroma3.parallelcorpora.utils.Utils;
public class OnerousTask implements Callable<Integer>{
	private final BlockingDeque<String> dq;
	private int depthT;
	private Lock multSiteLogLock;
	private Lock errorLogLock;
	private Lock progressLock;
	private Lock productivityLock;
	private Lock timeLock;

	public OnerousTask(BlockingDeque<String> dq,int depthT, Lock multSiteLogLock, Lock errorLogLock, Lock progressLock, Lock productivityLock, Lock timeLock) {
		this.dq = dq;
		this.depthT=depthT;
		this.multSiteLogLock=multSiteLogLock;
		this.errorLogLock=errorLogLock;
		this.progressLock=progressLock;
		this.productivityLock=productivityLock;
		this.timeLock=timeLock;
	}

	/**
	 * Task in cui si estrae ripetutamente e concorrentemente un nodo dalla testa del
	 * buffer , elaborando in seguito il 
	 * valore del nodo. La condizione di terminazione è legata al fatto di estrarre 
	 * dalla deque 'null', il che indica che la deque è vuota.
	 * @throws LangDetectException 
	 * @throws IOException 
	 */
	public Integer call() throws InterruptedException, IOException, LangDetectException {
		while (true){

			//estraggo l'elemento affiorante e lo passo al thread
			String head = null;
			synchronized (dq) {
				head = dq.poll();
				if (head == null) 
					break;
			}

			//stampo l'istante in cui inizio a valutare un determinato seed
			synchronized(progressLock){
				Utils.csvWr(new String[]{head,Utils.getDate()}, "percentageProgress.csv");
			}

			//lancio la multilingual detection sull'elemento corrente
			String site=head;
			System.out.println(head);
			MultilingualDetection detection = new MultilingualDetection();
			detection.multilingualDetection(site,depthT,multSiteLogLock,errorLogLock,productivityLock,timeLock);
		}



		return 0;

	}
}
