package main.java.featureTuning;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class FeatureInitialization implements Serializable {
	ArrayList<Integer> devNum=new ArrayList<Integer>();
	ArrayList<Integer> bugNum=new ArrayList<Integer>();
	ArrayList<Integer> TCR=new ArrayList<Integer>();
 	ArrayList<double[][]> em=new ArrayList<double[][]>();
 	ArrayList<double[][]> tm=new ArrayList<double[][]>();
 	public static String datasetName;
 	public static int windowSize;
 	public static int churnRate;
 	public static String actionProbOverRound = "";
 	
 	public ArrayList<Integer> getDevNum() {
		return this.devNum;
	}
	public ArrayList<Integer> getBugNum() {
		return this.bugNum;
	}

	public ArrayList<Integer> getTCR() {
		return this.TCR;
	}

	public ArrayList<double [][]> getEm() {
		return this.em;
	}

	public ArrayList<double[][]> getTm() {
		return this.tm;
	}
 	
	public abstract void initializeAllFeatures();
	
	public abstract void initializeSourceDevNum();
	
	public abstract void initializeSourceBugNum();
	
	public abstract void initializeSourceTCR();
	
	public abstract void initializeSourceEM();
	
	public abstract void initializeSourceTM();
}
