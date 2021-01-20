package main.java.SCM_TA_V1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class changeRepresentation {
	static String pName="", numOfDevs="", RQ="";
	public changeRepresentation(String dataset_name){
		this.pName=dataset_name;
	}

	public static void txtToCSV() throws FileNotFoundException{
		
		Scanner sc;
		int numOfFiles=0;
		
		String[] items;
		String[] items_name = null;
		String[] fileName;
		int fileNum; 
		String runNum;
		int i_fileNUmber=0;
		System.out.println("Enter the number of files:");
		Scanner sc2=new Scanner(System.in);
		numOfFiles=Integer.parseInt(sc2.nextLine());
		System.out.println("Enter the name of the project:");
		Scanner sc3=new Scanner(System.in);
		pName=sc3.nextLine();
		System.out.println("Enter the number of developers:");
		Scanner sc4=new Scanner(System.in);
		numOfDevs=sc4.nextLine();
		System.out.println("Enter the associated RQ:");
		Scanner sc5=new Scanner(System.in);
		RQ=sc5.nextLine();
		PrintWriter[] pw=new PrintWriter[numOfFiles];
		int[] firstRow= new int[numOfFiles];
		for(int i=0;i<numOfFiles;i++ ){
			pw[i]=new PrintWriter(new File(System.getProperty("user.dir")+"//results//R1//"+pName+"_"+RQ+"_"+numOfDevs+"Devs_Analysis_"+(i+1)+".csv"));
			firstRow[i]=0;
		}
		for(File fileEntry:new File(System.getProperty("user.dir")+"//results//results").listFiles()){
			StringBuilder sb1=new StringBuilder();
			StringBuilder sb2=new StringBuilder();
			sc=new Scanner(new File(fileEntry.toURI()));
			fileName=fileEntry.getName().split("_|\\.");
			runNum=fileName[1];
			fileNum=Integer.parseInt(fileName[2]);
			int i=0;
			while(sc.hasNextLine()){
				if(i%11!=0){
					items=sc.nextLine().split(": ");
					sb2.append(items[1]+",");
					if(firstRow[fileNum-1]==0)
						sb1.append(items_name[0]+items[0]+",");
				}
				else{
					if(firstRow[fileNum-1]==0)
						items_name=sc.nextLine().split(": ");
					else
						sc.nextLine();
				}
				i++;
			}
			if(firstRow[fileNum-1]==0){
				sb1.append("\n");
				firstRow[fileNum-1]=1;
			}
			//sb2.setLength(sb2.length()-1);
			sb2.append("\n");
			i_fileNUmber++;
			pw[fileNum-1].write(sb1.toString());
			pw[fileNum-1].write(sb2.toString());
		}
		for(int i=0;i<numOfFiles;i++ ){
			pw[i].close();
		}
		System.out.println("it's done");
	}
}
