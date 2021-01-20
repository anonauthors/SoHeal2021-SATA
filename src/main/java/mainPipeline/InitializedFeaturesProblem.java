package main.java.mainPipeline;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;
import main.java.SCM_TA_V1.Developer;
import main.java.featureTuning.FeatureSetV1;


/**
 * The InitializationFeatureProble implements the problem of examining of all possible clss
 * @author DistLab3
 *
 */
public class InitializedFeaturesProblem extends AbstractProblem {

	AdaptiveAssignmentPipline adaptive=null;
	FeatureSetV1 featureSetV1=null;
	HashMap<String, Double> totals = new HashMap<String, Double>();
	HashMap<String, ArrayList<Double>> totalsOverTime = new HashMap<String, ArrayList<Double>>();
	HashMap<Integer, HashMap<Approach, List<String>>> bus_factor_zones = new HashMap<Integer, HashMap<Approach, List<String>>>();
	HashMap<Integer, HashMap<Integer, Developer>> devsProfileOverTime = new HashMap<Integer, HashMap<Integer, Developer>>();
	@SuppressWarnings("static-access")
	public InitializedFeaturesProblem(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives);
		// TODO Auto-generated constructor stub
		adaptive = AdaptiveAssignmentPipline.getInstance();
		featureSetV1 = FeatureSetV1.getInstance();
		
		//add <key, value>s to the trendOverTime list
		/*
		 * tredOverTim.put("CoT_static", new ArrayList<Double>());
		 * tredOverTim.put("IDoT_static", new ArrayList<Double>());
		 * tredOverTim.put("CoT_adaptive", new ArrayList<Double>());
		 * tredOverTim.put("IDoT_adaptive", new ArrayList<Double>());
		 */
	}

	// MODIFY get a solution and compute its objective and  
	// EFFECT the cost associated to a solution is computed
	@Override
	public void evaluate(Solution solution) {
		// TODO Auto-generated method stub
		//try {
			//makeListClear();
			try {
				adaptive.run(solution, totals, totalsOverTime, devsProfileOverTime, bus_factor_zones);
			} catch (NoSuchElementException | ClassNotFoundException | IOException | URISyntaxException
					| CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			solution.setObjective(0, totals.get("TCT_adaptive"));
			solution.setAttribute("TCT_static", totals.get("TCT_static"));
			solution.setAttribute("TID_static", totals.get("TID_static"));
			solution.setAttribute("TID_adaptive", totals.get("TID_adaptive"));
			solution.setAttribute("CoT_static", totalsOverTime.get("CoT_static"));
			solution.setAttribute("CoT_adaptive", totalsOverTime.get("CoT_adaptive"));
			solution.setAttribute("IDoT_static", totalsOverTime.get("IDoT_static"));
			solution.setAttribute("IDoT_adaptive", totalsOverTime.get("IDoT_adaptive"));
			solution.setAttribute("SoT", totalsOverTime.get("SoT"));
			solution.setAttribute("devsProfile0", devsProfileOverTime.get(0));
			solution.setAttribute("devsProfile1", devsProfileOverTime.get(1));
			solution.setAttribute("costPerRound_static", totalsOverTime.get("costPerRound_static"));
			solution.setAttribute("costPerRound_adaptive", totalsOverTime.get("costPerRound_adaptive"));
			solution.setAttribute("idPerRound_static", totalsOverTime.get("idPerRound_static"));
			solution.setAttribute("idPerRound_adaptive", totalsOverTime.get("idPerRound_adaptive"));
			solution.setAttribute("EoT_adaptive", totalsOverTime.get("EoT_adaptive"));
			solution.setAttribute("EoT_static", totalsOverTime.get("EoT_static"));
			solution.setAttribute("ExoTperRound_adaptive", totalsOverTime.get("ExoTperRound_adaptive"));
			solution.setAttribute("actionProbVector", totalsOverTime.get("actionProbVector"));
			solution.setAttribute("churnRate", totalsOverTime.get("churnRate"));
			solution.setAttribute("actions", totalsOverTime.get("actions"));
			solution.setAttribute("retainedKnowledge_static", totalsOverTime.get("retainedKnowledge_static"));
			solution.setAttribute("lostKnowledge_static", totalsOverTime.get("lostKnowledge_static"));
			solution.setAttribute("retainedKnowledge_adaptive", totalsOverTime.get("retainedKnowledge_adaptive"));
			solution.setAttribute("lostKnowledge_adaptive", totalsOverTime.get("lostKnowledge_adaptive"));
			solution.setAttribute("busFactor_static", totalsOverTime.get("busFactor_static"));
			solution.setAttribute("busFactor_adaptive", totalsOverTime.get("busFactor_adaptive"));
			solution.setAttribute("busFactor_zones", bus_factor_zones);
		///}
		//catch (Exception e) {
			//e.printStackTrace();
		//}
	}

	//JsonObject jObject=Json.createObjectBuilder().build();
	
	@Override
	public Solution newSolution() {
		// TODO Auto-generated method stub
		Solution solution= new Solution(numberOfVariables, numberOfObjectives);
		featureSetV1.setFeatureVector(solution);
		return solution;
	}

}
