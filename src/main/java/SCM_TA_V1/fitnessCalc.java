package main.java.SCM_TA_V1;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.inference.GTest;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import main.java.context.Environment_s1;
import main.java.featureTuning.FeatureInitializationV1;
import main.java.mainPipeline.Approach;
import main.java.mainPipeline.Response;

public class fitnessCalc {


	public static double compeletionTime(Bug bug, Entry<Zone, Double> zone, Developer developer) {	
		//compute the total time for each zone 
		double tct=(bug.getTotalEstimatedEffort()*bug.BZone_Coefficient.get(zone.getKey()))/((developer.getDZone_Coefficient().get(zone.getKey())));
	
		return tct;
	}
	
	public static double compeletionTime(Bug bug, Entry<Zone, Double> zone, Developer developer, String approach) {	
		double tct=0;
		//compute the total time for each zone 
		switch(approach) {
			case "static":
				tct = (bug.getTotalEstimatedEffort() * bug.BZone_Coefficient.get(zone.getKey())) / ((developer.getDZone_Coefficient_static().get(zone.getKey())));
				break;
			case "adaptive":
				tct = (bug.getTotalEstimatedEffort() * bug.BZone_Coefficient.get(zone.getKey()))/((developer.getDZone_Coefficient().get(zone.getKey())));
				break;
		}
		return tct;
	}
	
	/**
	 * the method gets all the input for computing the completion time for a particular assignment
	 * @param bug
	 * @param zone
	 * @param developer
	 * @param team
	 * @return the completion time required to get the subtask done
	 */
	public static double completionTime_extended(Bug bug, Entry<Zone, Double> zone, Developer developer, ArrayList<Developer> team) {
		double inCommon = Math.min(bug.BZone_Coefficient.get(zone.getKey()), developer.getDZone_Coefficient().get(zone.getKey()));
		double tct=0;
		double bestFit=0.00001;
		if(inCommon > 0.001) { /*in case developer is already familiar with */
			tct = (bug.getTotalEstimatedEffort() * bug.BZone_Coefficient.get(zone.getKey())) 
					/ ((developer.getDZone_Coefficient().get(zone.getKey())));
		}
		else {
			for (Developer dev : team) {
				if (dev.getID() != developer.getID()) {
					bestFit = bestFit != 0.00001 ? Math.max(bestFit, dev.getDZone_Coefficient().get(zone.getKey())) : bestFit;
				}
			}
			
			//tct = (bug.getTotalEstimatedEffort() * bug.BZone_Coefficient.get(zone.getKey())) / bestFit;
			if (bestFit > 0.0001) {
				tct = ((bug.getTotalEstimatedEffort() * bug.BZone_Coefficient.get(zone.getKey())) 
						/ ((developer.getDZone_Coefficient().get(zone.getKey())))) * 1.2;
			}
			else {
				tct = ((bug.getTotalEstimatedEffort() * bug.BZone_Coefficient.get(zone.getKey())) 
						/ ((developer.getDZone_Coefficient().get(zone.getKey())))) * 5.8;
			}
		}
		
		//return tct == 0.0 ? 1 : tct;
		if (FeatureInitializationV1.datasetName == "Platform") {
			return tct / 100;
		}
		else {
			return tct;
		}
	}
	
	public static double completionTime_extended_static(Bug bug, Entry<Zone, Double> zone, Developer developer, ArrayList<Developer> team) {
		double x = bug.BZone_Coefficient.get(zone.getKey());
		double inCommon = Math.min(bug.BZone_Coefficient.get(zone.getKey()), developer.getDZone_Coefficient_static().get(zone.getKey()));
		double tct = 0;
		double bestFit = 0.00001;
		if(inCommon > 0.001) { /*in case developer is already familiar with */
			tct = (bug.getTotalEstimatedEffort() * bug.BZone_Coefficient.get(zone.getKey())) 
					/ ((developer.getDZone_Coefficient_static().get(zone.getKey())));
		}
		else {
			for (Developer dev : team) {
				if (dev.getID() != developer.getID()) {
					bestFit = bestFit != 0.00001 ? Math.max(bestFit, dev.getDZone_Coefficient_static().get(zone.getKey())) : bestFit;
				}
			}
			
			//tct = (bug.getTotalEstimatedEffort() * bug.BZone_Coefficient.get(zone.getKey())) / bestFit;
			if (bestFit > 0.0001) {
				tct = ((bug.getTotalEstimatedEffort() * bug.BZone_Coefficient.get(zone.getKey())) 
						/ ((developer.getDZone_Coefficient_static().get(zone.getKey())))) * 1.2;
			}
			else {
				tct = ((bug.getTotalEstimatedEffort() * bug.BZone_Coefficient.get(zone.getKey())) 
						/ ((developer.getDZone_Coefficient_static().get(zone.getKey())))) * 5.8;
			}
		}
		
		//return tct == 0.0 ? 1 : tct;
		return tct;
	}
	
	public static double getDelayTime(Bug bug, Entry<Zone, Double> zone, Developer developer){
		double delayTime=Math.max(taskDependencyDelayTime(bug, zone, developer), developer.developerNextAvailableHour);
		return delayTime;
	}
	
	public static double taskDependencyDelayTime(Bug bug, Entry<Zone, Double> zone,
	Developer developer){
		for(Zone z:zone.getKey().DZ){
			zone.getKey().zoneStartTime_evaluate=Math.max(zone.getKey().zoneStartTime_evaluate,z.zoneEndTime_evaluate);
		}
		
		for(int j=0;j<bug.DB.size();j++){
			bug.startTime_evaluate=Math.max(bug.startTime_evaluate, bug.DB.get(j).endTime_evaluate);
		}
	
	
		return zone.getKey().zoneStartTime_evaluate+ bug.startTime_evaluate;
	}

	public static double getSimDev(Developer d1, Developer d2){
		 double DDSim_intersection=0.0;
		 double DDSim_union=0.0;
		 for (Entry<Zone, Double>  zone:d1.DZone_Coefficient.entrySet()){
			 DDSim_intersection+=Math.min(Double.parseDouble(zone.getValue().toString()),d2.getDZone_Coefficient().get(zone.getKey()));
			 DDSim_union+=Math.min(Double.parseDouble(zone.getValue().toString()),d2.getDZone_Coefficient().get(zone.getKey()));
		 }
			 
		 return 1/(DDSim_intersection/DDSim_union);
	 }
  
	public static double getID(ArrayList<Developer> developers, Developer candidate, Bug b, Zone z, Approach approach) {
		double ID = 0.0;
		double deltaID = 0.0;
		deltaID = getZoneDiff(candidate, b, z, approach);
		if(deltaID > 0)
			ID = deltaID * 1;
		return ID;
	 }
	 
	public static double getID_scaled(ArrayList<Developer> developers, Developer candidate, Bug b, Zone z) {
		double totalFlow=0;
		for(Developer d : developers) {
			if(d.getID() != candidate.getID()) {
				totalFlow += Math.abs(candidate.getDZone_Coefficient().get(z) - d.getDZone_Coefficient().get(z));
			}
		}
		totalFlow /= developers.size();
		
		return totalFlow;
	}
	
	public static double getID_scaled_adaptive(ArrayList<Developer> developers, Developer candidate, Bug b, Zone z) {
		boolean diff = false;
		double threshold = 0.20;
		double Inflow = 0;
		if (candidate.DZone_Coefficient.containsKey(z)) {
			if (candidate.DZone_Coefficient.get(z) > threshold)
				diff = false;
			else 
				diff = true;
		}
		else {
			diff = true;
		}
		
		if (diff) {
			for(Developer d:developers) {
				if(d.getID() != candidate.getID()) {
					if (d.getDZone_Coefficient().get(z) > threshold)
						 Inflow = 1;
				}
			}
		}
		
		return Inflow;
	}
	
	public static double getID_scaled_static(ArrayList<Developer> developers, Developer candidate, Bug b, Zone z) {
		boolean diff = false;
		double threshold = 0.20;
		double Inflow = 0;
		if (candidate.DZone_Coefficient_static.containsKey(z)) {
			if (candidate.DZone_Coefficient_static.get(z) > threshold)
				diff = false;
			else 
				diff = true;
		}
		else {
			diff = true;
		}
		
		if (diff) {
			for(Developer d:developers) {
				if(d.getID() != candidate.getID()) {
					if (d.getDZone_Coefficient_static().get(z) > threshold)
						 Inflow = 1;
				}
			}
		}
		
		return Inflow;
	}
	
	public static double getZoneDiff(Developer d1, Bug b2, Zone z1, Approach approach){
		 double DBDiff = 0.0;
		 //for (Entry<Zone, Double>  zone:b2.BZone_Coefficient.entrySet())
		 switch (approach) {
			case STATIC:
				if(d1.DZone_Coefficient_static.containsKey(z1)) {
					 double fromBug = b2.BZone_Coefficient.get(z1);
					 double fromDev = d1.DZone_Coefficient_static.get(z1);
					 DBDiff = fromBug - fromDev;
				 }
				break;
			case ADAPTIVE:
				if(d1.DZone_Coefficient.containsKey(z1)) {
					 double fromBug = b2.BZone_Coefficient.get(z1);
					 double fromDev = d1.DZone_Coefficient.get(z1);
					 DBDiff = fromBug - fromDev;
				 }
				break;
			default:
				break;
		}
		 return DBDiff;
	}
	 //former definition for ID
	 
	/*
	 * public static double getID(ArrayList<Developer> developers, Developer
	 * candidate, Bug b, Zone z) {
	 * 
	 * double ID=0.0; for(Developer developer:developers) {
	 * if(developer.getID()!=candidate.getID()) ID+=getSimBug(developer, b, z); else
	 * ID+=(1/getSimBug(candidate, b, z)); } return ID; }
	 */
	 
	/*
	 * public static double getSimBug(Developer d1,Bug b2, Zone z1){ double
	 * DBSim=0.0; //for (Entry<Zone, Double> zone:b2.BZone_Coefficient.entrySet())
	 * if(d1.DZone_Coefficient.containsKey(z1)) { double
	 * fromBug=b2.BZone_Coefficient.get(z1); double
	 * fromDev=d1.DZone_Coefficient.get(z1);
	 * DBSim+=Math.min(b2.BZone_Coefficient.get(z1),d1.DZone_Coefficient.get(z1)); }
	 * return DBSim; }
	 */
	 
	public static double getDissim(ArrayList<Developer> developers, Bug b, Zone z) {
		 
		 return 0.0;
	 }
 
	public static void setBugEndTime(Bug bug){
		 for(int j=0;j<bug.DB.size();j++){
				if(bug.endTime>bug.startTime)
					bug.startTime=bug.DB.get(j).endTime;
			}
	 }
	 
	public static double getTZoneSim(HashMap<Zone, Double> bugZone, ArrayList<Developer> devs){
		 HashMap<Zone, Double> devsUnionZone=new HashMap<Zone, Double>();
		 double tZoneSim=0;
		 for (Entry<Zone, Double>  devZone:devs.get(0).DZone_Coefficient.entrySet()){
			 devsUnionZone.put(devZone.getKey(), devZone.getValue());
		 }
		 for(int i=1;i<devs.size();i++){
			 for (Entry<Zone, Double>  devZone:devs.get(i).DZone_Coefficient.entrySet()){
				 if(devsUnionZone.get(devZone.getKey())<devZone.getValue())
					 devsUnionZone.put(devZone.getKey(),devZone.getValue());
			 }
		 }
		 
		 for (Map.Entry<Zone, Double>  bZone:bugZone.entrySet()){
			 tZoneSim+=Math.min(bZone.getValue(), devsUnionZone.get(bZone.getKey()));
		 }
		return tZoneSim/bugZone.size();
		
	 }
 
	public static double getDataFlow(Bug bug, ArrayList<Developer> devs){
		 double dev_bugZone_sim=0;
		 double dev_not_assigned_sim=0;
		 double dataFlow=0;
		 /*int i=0;
		 for(Map.Entry<Zone, Double>  bZone:bugZone.entrySet()){
			 double maxZoneOverlap=0;
			 double devNotAssignedZoneSim=0;
			 for(Developer dev:devs){
				 maxZoneOverlap=Math.max(dev.DZone_Coefficient.get(bZone.getKey()), maxZoneOverlap);
				 for(Map.Entry<Zone, Double>  bug_zone:bugZone.entrySet()){
					 if(bug_zone.getKey().zId!=bZone.getKey().zId){
						 devNotAssignedZoneSim+=dev.DZone_Coefficient.get(bug_zone.getKey());
					 }
				 }
				 //the only problem might happen is originated from the situation that the bugZoneItem has not had for selected developer.
			 }
			 //dev_bugZone_sim+=Math.max(bZone.getValue(), devs.get(i).DZone_Coefficient.get(bZone.getKey()));
			 dev_bugZone_sim+=maxZoneOverlap;
			 dev_not_assigned_sim+=devNotAssignedZoneSim/(bugZone.size()-1);
			 
			 i++;
		 }*/
		 
		/* HashSet<Developer> developrs=new HashSet<Developer>();
		 	for(Developer d:devs){
		 		developrs.add(d);
		 	}*/
		 	
			double IDFlow[][]=new double[devs.size()][devs.size()];
			for(int i=0;i<IDFlow.length;i++){
				for(int j=0;j<IDFlow[0].length;j++){
					if(devs.get(i)!=devs.get(j)){
						IDFlow[i][j]=getFlowD2D(bug, devs);
					}
					else{
						IDFlow[i][j]=0;
					}
				}
			}
			
		return 0; //(dev_bugZone_sim/bugZone.size())+(dev_not_assigned_sim/devs.size());
		
		
		
		
		 
	 }
	 
	public static double getFlowD2D(Bug b, ArrayList<Developer> devs){
		 // implement (Zi-Zj) and (all zones) 
		 HashMap<Zone, Double> DevsDiff=new HashMap<Zone, Double>();
		 
		return 0;
		 
	 }
	 
	public static double getNotAssignedTaskCost(){
		 return 0;
	 }
 
	public static double getMaxEndTimes(Bug b, DirectedAcyclicGraph<Bug,DefaultEdge> DEP){
		 double endTime=0;
		 //Set<Bug> dependents=DEP.getAncestors(b);
		 ArrayList<Bug> dependents=b.DB;
		 for(Bug bug:dependents){
			 endTime=Math.max(endTime, bug.endTime_evaluate);
		 }
		 return endTime;
	 }

	public static double getZoneStartTime(Developer d, ArrayList<Zone> depZones){
		 double sDate=0;
		 for(Zone zone:depZones){
			 sDate=Math.max(sDate, zone.zoneEndTime_evaluate);
		 }
		 return Math.max(sDate, d.developerNextAvailableHour);
	 }

	public static double getEstimatedDiffusionTime(Map.Entry<Integer, Developer> sourceDev ,Map.Entry<Integer, Developer> targetDev,double estimatedEffort){
		 double estimeatedTime=0.0;
		 estimeatedTime=estimatedEffort/Environment_s1.getDevNetwork().getEdgeWeight(Environment_s1.getDevNetwork().getEdge(sourceDev,targetDev));
		 return estimatedEffort;
	 }

}




