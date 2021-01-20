package main.java.SCM_TA_V1;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

import main.java.context.Environment_s1;
import main.java.mainPipeline.Approach;

public class NormalAssignment extends AbstractProblem {
	
	static Bug[] bugs=GA_Problem_Parameter.bugs;
	HashMap<Integer,Developer> developers=GA_Problem_Parameter.developers_all;
	DirectedAcyclicGraph<Bug, DefaultEdge> DEP;
	TopologicalOrderIterator<Bug,DefaultEdge> tso;
	ArrayList<Zone> genes=new ArrayList<Zone>();
	ArrayList<Triplet<Bug, Zone, Integer>> zoneAssignee=new ArrayList<Triplet<Bug,Zone,Integer>>();
	ArrayList<Developer> developerTeam=new ArrayList<Developer>();
	public NormalAssignment(){
		super(GA_Problem_Parameter.setNum_of_Variables(bugs),GA_Problem_Parameter.Num_of_functions_Single);
	}
	
	
	public void init(){
		DEP=GA_Problem_Parameter.DEP;
		tso=GA_Problem_Parameter.tso_NormalAssignment;
		/*
		//generate DAG for arrival Bugs
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
		//topologically sort the graph
		tso=GA_Problem_Parameter.getTopologicalSorted(DEP);*/
		genes.clear();
		while(tso.hasNext()){
			Bug b=tso.next();
			b.setZoneDEP();
			TopologicalOrderIterator<Zone,DefaultEdge> tso_zones=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
			while(tso_zones.hasNext()){
				genes.add(tso_zones.next());
			}
		}
	}
	
	@Override
	public Solution newSolution(){
		if(GA_Problem_Parameter.flag==1){
			init();
			GA_Problem_Parameter.flag=0;
		}
		//changed NUM of variables for the solution
		Solution solution=new Solution(genes.size(),GA_Problem_Parameter.Num_of_functions_Single);
		int j=0;
		for(Zone z:genes){
			//int randDevId=GA_Problem_Parameter.getRandomDevId();
			solution.setVariable(j,EncodingUtils.newInt(0, GA_Problem_Parameter.devListIdSize-1));
			j++;
		}
		return solution;
	}
		
	
	@Override 	
	public void evaluate(Solution solution){
		@SuppressWarnings("unchecked")
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_evaluation=(DirectedAcyclicGraph<Bug, DefaultEdge>) DEP.clone();
		//reset all the associate time for the bugs and their zones
		GA_Problem_Parameter.resetParameters(DEP_evaluation,solution, developers);
		//assign associate Dev to zone
		//GA_Problem_Parameter.assignZoneDev(zoneAssignee,GA_Problem_Parameter.tasks, solution );
		TopologicalOrderIterator<Bug, DefaultEdge> tso=new TopologicalOrderIterator<Bug, DefaultEdge>(DEP_evaluation);
		double totalTime=0.0;
		double totalCost=0.0;
		double totalDevCost=0.0;
		double totalDelayTime=0.0;
		double totalDelayCost=0.0;
		double totalStartTime=0.0;
		double totalEndTime=0.0;
		double totalExecutionTime=0.0;
		double totalDiffusedKnowledge=0.0;
		int index=0;
		int index_fillDevTeam=0;
		GA_Problem_Parameter.tso_adaptive=new TopologicalOrderIterator<Bug, DefaultEdge>(DEP_evaluation);
		while(tso.hasNext()){
			double totalDiffusedOfDevTeam=0;
			Bug b=tso.next();
			//set Bug startTime
			double x=fitnessCalc.getMaxEndTimes(b, DEP_evaluation);
			b.startTime_evaluate=x;
			TopologicalOrderIterator<Zone, DefaultEdge> tso_Zone=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);		
			TopologicalOrderIterator<Zone, DefaultEdge> tso_Zone_takeDevTeam=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
			
			//fill dev list team 
			developerTeam.clear();
			
			while(tso_Zone_takeDevTeam.hasNext()) {
				tso_Zone_takeDevTeam.next();
				developerTeam.add(developers.get(GA_Problem_Parameter.devListId.get(EncodingUtils.getInt(solution.getVariable(index)))));
				index_fillDevTeam++;
			}
			Map.Entry<Integer, Developer> candidate=null;
			while(tso_Zone.hasNext()){
				Zone zone=tso_Zone.next();
				double compeletionTime=0.0;
				Entry<Zone, Double> zone_bug=new AbstractMap.SimpleEntry<Zone, Double>(zone,b.BZone_Coefficient.get(zone));
				/*if(EncodingUtils.getInt(solution.getVariable(index))==0){
					int[] g=EncodingUtils.getInt(solution);
					System.out.println(g);
				}*/
				int dID=GA_Problem_Parameter.devListId.get(EncodingUtils.getInt(solution.getVariable(index)));
				compeletionTime=fitnessCalc.compeletionTime(b,zone_bug, developers.get(dID), "adaptive");
				for(Map.Entry<Integer, Developer> developer:developers.entrySet()){
					if(developer.getKey()== dID)
						candidate=developer;
				}
				totalExecutionTime+=compeletionTime;
				totalDevCost+=compeletionTime*developers.get(dID).hourlyWage;
				zone.zoneStartTime_evaluate=b.startTime_evaluate+fitnessCalc.getZoneStartTime(developers.get(dID), zone.DZ);
				zone.zoneEndTime_evaluate=zone.zoneStartTime_evaluate+compeletionTime;
				developers.get(dID).developerNextAvailableHour=Math.max(developers.get(dID).developerNextAvailableHour,
						zone.zoneEndTime_evaluate);
				double x1=developers.get(dID).developerNextAvailableHour;
				b.endTime_evaluate=Math.max(b.endTime_evaluate, zone.zoneEndTime_evaluate);
				index++;
				
				
				
				
				double emissionTime=10000000;
				double estimatedEmissionTime=0;
				int sourceDevId = 0;
				for(Map.Entry<Integer, Developer> dev:GA_Problem_Parameter.developers.entrySet()){
					if(Environment_s1.getDevNetwork().containsEdge(dev,candidate))
						estimatedEmissionTime=fitnessCalc.getEstimatedDiffusionTime(dev,candidate,
								(b.getTotalEstimatedEffort()*b.BZone_Coefficient.get(zone_bug.getKey())));
					if(estimatedEmissionTime<emissionTime){
						emissionTime=estimatedEmissionTime;
						sourceDevId=dev.getKey();
					}
				}
				totalDiffusedOfDevTeam=fitnessCalc.getID(developerTeam ,candidate.getValue(), b, zone_bug.getKey(), Approach.STATIC);
				//compute the extra cost for information diffusion==> used to compute the cost posed due to
				//information diffusion 
			
				//totalCost+=developers.get(sourceDevId).hourlyWage*emissionTime;
				
				
				
				
				
				
				
			}
			
			totalDiffusedKnowledge+=totalDiffusedOfDevTeam;
			
			totalStartTime=Math.min(totalStartTime, b.startTime_evaluate);
			totalEndTime=Math.max(totalEndTime, b.endTime_evaluate);
			totalDelayTime+=b.endTime_evaluate-(2.5*totalExecutionTime+totalExecutionTime);
			if(totalDelayTime>0)
				totalDelayCost+=totalDelayTime*GA_Problem_Parameter.priorities.get(b.priority);
			
		}
		totalTime=totalEndTime-totalStartTime;
		totalCost=totalDevCost+totalDelayCost;
		
		//solution.setObjective(0, totalTime);
		solution.setObjective(0, totalCost);
		solution.setAttribute("time", totalTime);
		solution.setAttribute("diffusedKnowledge", totalDiffusedKnowledge);
	}	
	
}

