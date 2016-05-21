package it.uniroma3.parallel.utils;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class extraxtUrlFromCC {

	public static void main(String[] args) throws IOException {
		args[0]="/home/francesco/Scaricati/CCcrawl/july2015/CC-MAIN-20150728002301-00002-ip-10-236-191-2.ec2.internal.warc.wat";
		args[1]="50krandomUrl.txt";
		File fout = new File("50krandomUrl.txt");
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 			
		Set<String> trovati=new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(args[0]));
		String line = reader.readLine();
		while(line!=null) {
			if (line.contains("WARC-Target-URI: htt")){
				System.out.println(line);
				line=line.split(" ")[1];
				int n=StringUtils.ordinalIndexOf(line, "/", 3);
				if(n==-1){
					//line=line.substring(0, n);
					System.out.println(line);
					trovati.add(line);
					line=reader.readLine();
					continue;
				}
				line=line.substring(0, n);
				System.out.println(line);
				trovati.add(line);
			}
			line = reader.readLine();
			System.out.println(trovati.size());
			
			if(trovati.size()==50000){
				break;
			}
		}
		for(String tr:trovati){
				bw.write(tr);
				bw.newLine();
			}
		 
			bw.close();
		}
			

	}

