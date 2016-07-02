package it.uniroma3.parallelcorpora.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Classe di utilità generale.
 * 
 * @author francescoelefante
 *
 */
public class Utils {

	// funzione che ritorna la data attuale
	public static String getDate() {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
		int day = now.get(Calendar.DAY_OF_MONTH);
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		int millis = now.get(Calendar.MILLISECOND);
		return (year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second + "." + millis);
	}

	/**
	 * Scrive su un file csv i campi specificati per parametro.
	 * 
	 * @param fields
	 * @param pathCSV
	 * @throws IOException
	 */
	public static void csvWr(String[] fields, String pathCSV) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(pathCSV, true), '\t');
		writer.writeNext(fields);
		writer.close();
	}

	/**
	 * Scrive su un file csv l'eccezione passata per parametro.
	 * 
	 * @param site
	 * @param e
	 * @param pathCSV
	 * @throws IOException
	 */
	public static void csvWr(String site, Exception e, String pathCSV) throws IOException {
		e.printStackTrace();
		String[] fields = new String[] { site, e.toString() };
		CSVWriter writer = new CSVWriter(new FileWriter(pathCSV, true), '\t');
		writer.writeNext(fields);
		writer.close();
	}

}
