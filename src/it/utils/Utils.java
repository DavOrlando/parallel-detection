package it.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import com.cybozu.labs.langdetect.LangDetectException;

import au.com.bytecode.opencsv.CSVWriter;

public class Utils {

	public static void main(String[] argv) throws InterruptedException {
		Calendar a = getDate2();
	    TimeUnit.SECONDS.sleep(1);
		Calendar b = getDate2();
		long c=b.getTimeInMillis()-a.getTimeInMillis();
		System.out.println(c);
		
	}
	
	
	
	public static Calendar getDate2() {
		Calendar now = Calendar.getInstance();
		return now;
	}
	
	public static long getTime(){
		return getDate2().getTimeInMillis();
	}
	
	
	//funzione che ritorna la data attuale
	public static String getDate() {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH)+1; // Note: zero based!
		int day = now.get(Calendar.DAY_OF_MONTH);
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		int millis = now.get(Calendar.MILLISECOND);
	
		return (year+"-"+month+"-"+day+" "+hour+":"+ minute+":"+second+"."+millis);
	}
	
	
	//funzione che cancella una directory
	public static void deleteDir(String args)
    {	
 
    	File directory = new File(args);
 
    	//make sure directory exists
    	if(!directory.exists()){
 
           //System.out.println("Directory does not exist.");
           //System.exit(0);
           return;
        }else{
 
           try{
 
               delete(directory);
 
           }catch(IOException e){
               e.printStackTrace();
               System.exit(0);
           }
        }
 
    	//System.out.println("Done");
    }
 
	//funzione che cancella un file
    public static void delete(File file)

    	throws IOException{
 
    	if(file.isDirectory()){
 
    		//directory is empty, then delete it
    		if(file.list().length==0){
 
    		   file.delete();
    	//	   System.out.println("Directory is deleted : " + file.getAbsolutePath());
 
    		}else{
 
    		   //list all the directory contents
        	   String files[] = file.list();
 
        	   for (String temp : files) {
        	      //construct the file structure
        	      File fileDelete = new File(file, temp);
 
        	      //recursive delete
        	     delete(fileDelete);
        	   }
 
        	   //check the directory again, if empty then delete it
        	   if(file.list().length==0){
           	     file.delete();
        	//     System.out.println("Directory is deleted : " + file.getAbsolutePath());
        	   }
    		}
 
    	}else{
    		//if file, then delete it
    		file.delete();
    	//	System.out.println("File is deleted : " + file.getAbsolutePath());
    	}
    }

	
    //funzione che scrive su un file csv
    public static void csvWr(String[] fields, String pathCSV) throws IOException
	{
		CSVWriter writer = new CSVWriter(new FileWriter(pathCSV,true), '\t');
	     writer.writeNext(fields);
		 writer.close();
	}
	

    //conta le label di un file allineato da RR
    public static int countLabel (String pathOutputRR){
	
	return 0;
	
}



	public static void csvWr(String site, Exception e, String pathCSV) throws IOException {
		e.printStackTrace();
		String[] fields = new String[]{site,e.toString()};
		CSVWriter writer = new CSVWriter(new FileWriter(pathCSV,true), '\t');
	     writer.writeNext(fields);
		 writer.close();
		
	}



    


}
