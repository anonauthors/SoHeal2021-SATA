package main.java.SCM_TA_V1;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jgraph.JGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

public class CompetenceMulti2_problem extends AbstractProblem {
	
	static Bug[] bugs=GA_Problem_Parameter.bugs;
	HashMap<Integer,Developer> developers=GA_Problem_Parameter.developers;
	DirectedAcyclicGraph<Bug, DefaultEdge> DEP;
	TopologicalOrderIterator<Bug,DefaultEdge> tso;
	ArrayList<Zone> genes=new ArrayList<Zone>();
	ArrayList<Triplet<Bug, Zone, Integer>> zoneAssignee=new ArrayList<Triplet<Bug,Zone,Integer>>();
	public CompetenceMulti2_problem(){
		super(GA_Problem_Parameter.setNum_of_Variables(bugs),GA_Problem_Parameter.Num_of_functions_Multi);
	}
	
	
	public void init(){
		DEP=GA_Problem_Parameter.DEP;
		tso=GA_Problem_Parameter.tso_competenceMulti2;
		/*
		//generate DAG for arrival Bugs
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
		//topologically sort the graph
		tso=GA_Problem_Parameter.getTopologicalSorted(DEP);*/
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
		init();
		//changed NUM of variables for the solution
		Solution solution=new Solution(genes.size(),GA_Problem_Parameter.Num_of_functions_Multi);
		int j=0;
		for(Zone z:genes){
			int randDevId=GA_Problem_Parameter.getRandomDevId();
			solution.setVariable(j,EncodingUtils.newInt(randDevId, randDevId));
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
		int index=0;
		while(tso.hasNext()){
			Bug b=tso.next();
			//set Bug startTime
			double x=fitnessCalc.getMaxEndTimes(b, DEP_evaluation);
			b.startTime_evaluate=x;
			TopologicalOrderIterator<Zone, DefaultEdge> tso_Zone=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
			while(tso_Zone.hasNext()){
				Zone zone=tso_Zone.next();
				double compeletionTime=0.0;
				Entry<Zone, Double> zone_bug=new AbstractMap.SimpleEntry<Zone, Double>(zone,b.BZone_Coefficient.get(zone));
				/*if(EncodingUtils.getInt(solution.getVariable(index))==0){
					int[] g=EncodingUtils.getInt(solution);
					System.out.println(g);
				}*/
				compeletionTime=fitnessCalc.compeletionTime(b,zone_bug, developers.get(EncodingUtils.getInt(solution.getVariable(index))));
				totalExecutionTime+=compeletionTime;
				totalDevCost+=compeletionTime*developers.get(EncodingUtils.getInt(solution.getVariable(index))).hourlyWage;
				zone.zoneStartTime_evaluate=b.startTime_evaluate+fitnessCalc.getZoneStartTime(developers.get(EncodingUtils.getInt(solution.getVariable(index))), zone.DZ);
				zone.zoneEndTime_evaluate=zone.zoneStartTime_evaluate+compeletionTime;
				int DId=EncodingUtils.getInt(solution.getVariable(index));
				double x1=developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour;
				developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour=Math.max(developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour,
						zone.zoneEndTime_evaluate);
				x1=developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour;
				b.endTime_evaluate=Math.max(b.endTime_evaluate, zone.zoneEndTime_evaluate);
				index++;
			}
			totalStartTime=Math.min(totalStartTime, b.startTime_evaluate);
			totalEndTime=Math.max(totalEndTime, b.endTime_evaluate);
			totalDelayTime+=b.endTime_evaluate-(2.5*totalExecutionTime+totalExecutionTime);
			if(totalDelayTime>0)
				totalDelayCost+=totalDelayTime*GA_Problem_Parameter.priorities.get(b.priority);
		}
		totalTime=totalEndTime-totalStartTime;
		totalCost=totalDevCost+totalDelayCost;
		
		solution.setObjective(0, totalTime);
		solution.setObjective(1, totalCost);
	}
		
	
}

