package main.java.context;

import java.util.ArrayList;
import java.util.Arrays;

public class Operations {
	
	static ArrayList<ArrayList<String>> setOfObjective=new ArrayList<ArrayList<String>>();

	public static double getAVG(ArrayList<Double> list){
		double sum=0;
		for(double d:list){
			sum+=d;
		}
		return sum/list.size();
	}
	
	public static double getVAR(ArrayList<Double> list){
		double sumDiffsSquared = 0.0;
		   double avg = getAVG(list);
		   for (double value : list)
		   {
		       double diff = value - avg;
		       diff *= diff;
		       sumDiffsSquared += diff;
		   }
		   return sumDiffsSquared  / (list.size()-1);
		}
	//public static 
	public static void applyFeedback(){
		//feedback on the environmental conditions
		
		
		
		//feedback on the profile of developers who got assigned by the on reigning work-- should be updated
		//by a particular rate (namely α)
		
		
		
	}
	
	public static void addObjectives(){
		setOfObjective.add(new ArrayList<String>(Arrays.asList("time", "cost")));
		setOfObjective.add(new ArrayList<String>(Arrays.asList("time", "cost", "information diffusion")));
	}
	
	//implement the policy function to get the right action 
	public static ArrayList<String> policyFunction(State s){
		switch (s.name){
			case "steday_state":
				return setOfObjective.get(0);
			case "dynamic_state":
				return setOfObjective.get(1);
			default:
				return null;
		}
				
	}
	
}
