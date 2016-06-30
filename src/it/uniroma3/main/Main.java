package it.uniroma3.main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Set;
import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.configuration.ConfigurationProperties;
import it.uniroma3.parallel.utils.Utils;



//classe con metodo principale main dell'intero progetto 
//data una lista di seed lancia isMultilingual su ogni elemento della lista (per avere entry points (links paralleli)) 
//su cui poi lanciare la visita ricorsiva
public class Main {

	//argomenti sono: lista di seed e profondità alla quale arrivare nella visita
	public static void main(String[] args) throws IOException, InterruptedException, LangDetectException {
		
		long start = Calendar.getInstance().getTimeInMillis();
		//argomento: profondità della visita
		int depth=2;

		//argomento: file da dove leggo lista seed
		File file = new File("exp.txt"); 	
		FileReader fr = new FileReader(file); 
		BufferedReader br = new BufferedReader(fr);
		String line;

		//creo struttura dati comune da cui i thread estraggono i seed
		Set<String> setSeed=new HashSet<String>();
		LinkedBlockingDeque<String> dq = new LinkedBlockingDeque<String>();
		while((line = br.readLine()) != null){
			setSeed.add(line);
			dq.add(line);
		}
		br.close();

		//file dove scrivo error log e lock per gestire scritture concorrenti
		new File(ConfigurationProperties.getInstance().getStringOfErrorLogCSV());
		Lock errorLogLock=new ReentrantLock();

		//lock per aggiornare strutture dati relative alla priorità contemporaneamente
		//Lock errorPrior=new ReentrantLock();


		//file dove scrivo i siti multilingua trovati e lock per gestire scritture concorrenti
		new File(ConfigurationProperties.getInstance().getStringOfPathForSiteMultilingualCSV());
		Lock multSiteLogLock=new ReentrantLock();

		//file per tempi detection not multilingual
		new File(ConfigurationProperties.getInstance().getStringOfPathForSiteNotMultilingual());

		//file dove scrivo i siti multilingual trovati
//		new File("percentageProgress.csv");
		Lock progressLock=new ReentrantLock();

//		new File("word.csv");
//		new File("triword.csv");
//		new File("token.csv");
//		new File("time.csv");
		Lock productivityLock=new ReentrantLock();
		Lock timeLock=new ReentrantLock();

		synchronized(productivityLock){
			Utils.csvWr(new String[]{"SITO","DEPTH","LINGUA","FILE","ALLINEAMENTO","WORD NUOVE","TRIWORD NUOVE","TOKEN"}, "produttività.csv");
		}

		synchronized(timeLock){
			Utils.csvWr(new String[]{"SITO","DEPTH","EVENTO","FILE","TEMPO"}, "time.csv");
		}

		//lancio thread paralleli
		int NCPU = Runtime.getRuntime().availableProcessors();
		ExecutorService pool = Executors.newFixedThreadPool(NCPU);
		CompletionService<Integer> ecs = new ExecutorCompletionService<Integer>(pool);
		for (int i = 0; i <NCPU; i++){
			ecs.submit(new OnerousTask(dq,depth,multSiteLogLock,errorLogLock,progressLock,productivityLock,timeLock));
		}


		pool.shutdown();
		long end = Calendar.getInstance().getTimeInMillis();
		System.out.println(end-start);
	}//fine main


}
