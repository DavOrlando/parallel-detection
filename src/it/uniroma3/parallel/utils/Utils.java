package it.uniroma3.parallel.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Classe di utilità generale.
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


	// funzione che scrive su un file csv
	public static void csvWr(String[] fields, String pathCSV) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(pathCSV, true), '\t');
		writer.writeNext(fields);
		writer.close();
	}


	public static void csvWr(String site, Exception e, String pathCSV) throws IOException {
		e.printStackTrace();
		String[] fields = new String[] { site, e.toString() };
		CSVWriter writer = new CSVWriter(new FileWriter(pathCSV, true), '\t');
		writer.writeNext(fields);
		writer.close();
	}

}
