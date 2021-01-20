package main.java.mainPipeline;

import java.net.*;

import org.moeaframework.core.Population;

import java.io.*;

public class Client {

	private Socket socket=null;
	private ObjectOutputStream out=null;
	
	public Client(String address, int port) {
		try {
			socket=new Socket(address, port);
			System.out.println("connected");
		}
		catch (UnknownHostException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public void sendPopulation(Population p) {
		//out=new ObjectOutputStream(arg0)
	}
	
}
