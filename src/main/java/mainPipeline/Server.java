package main.java.mainPipeline;

import java.net.*;
import java.io.*;

import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

import com.opencsv.CSVWriter;

public class Server{

	private Socket socket=null;
	private ServerSocket server=null;
	private ObjectInputStream in=null;

	public Server(int port) throws ClassNotFoundException{
		try{
			server=new ServerSocket(port);
			System.out.println("Server started");
			System.out.println("Wating for client....");

			socket=server.accept();
			System.out.println("Connected");

			in=new ObjectInputStream(socket.getInputStream());
			Population p=(Population) in.readObject();
			writeResutls(p, "");
		}
		catch(IOException e){

		}
	}
	/**
	Method to get the results over socket and write them
	*/
	public void writeResutls(Population p, String datasetName) throws IOException {
		File file=new File(System.getProperty("user.dir")+File.separator+"results"+ File.separator+ "self-adaptive"
							+File.separator+ datasetName);
		PrintWriter printWriter=new PrintWriter(file);
		CSVWriter csvWriter=new CSVWriter(printWriter);
		String[] csvFileOutputHeader= {"solution","totalCostStatic", "totalCostID", "totalIDID"};
		csvWriter.writeNext(csvFileOutputHeader); //write the header of the csv file
		Solution tempSolution;
		
		for(int i=0; i<p.size(); i++) {
			tempSolution=p.get(i);
			csvWriter.writeNext(new String[] {EncodingUtils.getInt(tempSolution).toString() ,Double.toString(tempSolution.getObjective(0)),
												tempSolution.getAttribute("TCT_adaptive").toString(), tempSolution.getAttribute("TID").toString()});
		}
		
		//close the writers
		csvWriter.close();
		printWriter.close();
	}	
}
