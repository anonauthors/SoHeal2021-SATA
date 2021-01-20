/* Copyright 2019- Vahid Etemadi
 * 
 */
package main.java.mainPipeline;

//import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;

import javax.print.attribute.standard.MediaSize.Engineering;

import org.apache.commons.collections.functors.TruePredicate;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

import main.java.SCM_TA_V1.Bug;
import main.java.SCM_TA_V1.Developer;
import main.java.SCM_TA_V1.GATaskAssignment;
import main.java.SCM_TA_V1.GA_Problem_Parameter;
import main.java.SCM_TA_V1.Zone;
import main.java.context.Environment_s1;
import main.java.context.Observation;
import main.java.context.Training;
import main.java.context.State;
import main.java.featureTuning.FeatureInitialization;
import main.java.featureTuning.FeatureInitializationV1;
import main.java.featureTuning.FeatureSetV1;
import main.java.featureTuning.Stubs;
import smile.sequence.HMM;

public class AdaptiveAssignmentPipline {

	static Training training_instance = new Training();
	static ArrayList<String> objectiveSet = new ArrayList<String>();
	static Random random = new Random();
	static HMM<Observation> HMM = null;
	static String datasetName = null;
	//static List<String> headers = new ArrayList<String>();
	public static HashMap<Action, Double> LAProbes = new HashMap<Action, Double>(){
		{
			put(Action.COST, 0.5);
			put(Action.DIFFUSION, 0.5);
		}
	};
 	private static AdaptiveAssignmentPipline adaptivePipeline = null;
	GATaskAssignment test;
	
	static FeatureInitialization featureIni = FeatureInitializationV1.getInstance();
	static HashMap<String, Integer> listOfConfig = new HashMap<String, Integer>(){
		{
			put("numOfDevs", 0);
			put("numOfBugs",1);
			put("TCR",2);
			put("EM",3);
			put("TM",4);
		}	
	};
	
	private AdaptiveAssignmentPipline() {
		test=GATaskAssignment.getInstance();
	}

	public static AdaptiveAssignmentPipline getInstance() {
		if(adaptivePipeline==null)
			adaptivePipeline=new AdaptiveAssignmentPipline();
		return adaptivePipeline;
	}
	
	/*
	 * Gets invoked to run a new pipeline of task assignment in an adaptive way
	 *
	 * <p>
	 * the method orchestrates the sequence of tasks prior to self-adaptive assignment
	 * the objective is to compute the overall cost for a particular optimization
	 * this method only initialize the inputs
	 * 
	 * EFFECT the overall cost is computed and is returned as the fitness of input solution 
	 */	
	public HashMap<String, Double> run(Solution solution, HashMap<String, Double> totals, HashMap<String, ArrayList<Double>> totalsOverTime,
			HashMap<Integer, HashMap<Integer, Developer>> devsProfileOverTime, HashMap<Integer, HashMap<Approach, List<String>>> bus_factor_zones)
					throws NoSuchElementException, IOException, URISyntaxException, CloneNotSupportedException, ClassNotFoundException{
		//set the num of devs-- all dev set will be pruned by the number comes from solution
		listOfConfig.put("numOfDevs", EncodingUtils.getInt(solution.getVariable(FeatureSetV1.featureVectorIndex.get("numOfDevs"))));
		System.out.println("% of devs should be ignored-----"+featureIni.getDevNum().get(listOfConfig.get("numOfDevs")));
		
		//set the number of bugs -- the bug list will be cut down by the number of valid bugs 
		listOfConfig.put("numOfBugs", EncodingUtils.getInt(solution.getVariable(FeatureSetV1.featureVectorIndex.get("numOfBugs"))));
		System.out.println("% of bugs should be ignored------"+featureIni.getBugNum().get(listOfConfig.get("numOfBugs")));
		
		//create the Poisson distribution with the lambda value from solution
		listOfConfig.put("TCR", EncodingUtils.getInt(solution.getVariable(FeatureSetV1.featureVectorIndex.get("TCR"))));
		System.out.println("Value of lambda------"+featureIni.getTCR().get(listOfConfig.get("TCR")));
		
		//initialize HMM with the value of solution
		listOfConfig.put("TM", EncodingUtils.getInt(solution.getVariable(FeatureSetV1.featureVectorIndex.get("TM"))));
		System.out.println("Candidate TM------"+featureIni.getTm().get(listOfConfig.get("TM")));
		double[][] t=featureIni.getTm().get(listOfConfig.get("TM"));
		listOfConfig.put("EM", EncodingUtils.getInt(solution.getVariable(FeatureSetV1.featureVectorIndex.get("EM"))));	
		System.out.println("Candidate EM------"+featureIni.getEm().get(listOfConfig.get("EM")).toString());
		double[][] te=featureIni.getTm().get(listOfConfig.get("TM"));
		//set dataset name
		datasetName=FeatureInitializationV1.datasetName;
		//initialize return array
		totals.put("TCT_static", 0.0);
		totals.put("TCT_adaptive", 0.0);
		totals.put("TID_static", 0.0);
		totals.put("TID_adaptive", 0.0);
		
		totalsOverTime.put("CoT_static", new ArrayList<Double>());
		totalsOverTime.put("IDoT_static", new ArrayList<Double>());
		totalsOverTime.put("CoT_adaptive", new ArrayList<Double>());
		totalsOverTime.put("IDoT_adaptive", new ArrayList<Double>());
		totalsOverTime.put("SoT", new ArrayList<Double>());
		totalsOverTime.put("costPerRound_static", new ArrayList<Double>());
		totalsOverTime.put("costPerRound_adaptive", new ArrayList<Double>());
		totalsOverTime.put("idPerRound_static", new ArrayList<Double>());
		totalsOverTime.put("idPerRound_adaptive", new ArrayList<Double>());
		totalsOverTime.put("EoT_static", new ArrayList<Double>());
		totalsOverTime.put("EoT_adaptive", new ArrayList<Double>());
		totalsOverTime.put("ExoTperRound_adaptive", new ArrayList<Double>());
		totalsOverTime.put("actionProbVector", new ArrayList<Double>());
		totalsOverTime.put("churnRate", new ArrayList<Double>());
		totalsOverTime.put("actions", new ArrayList<Double>());
		totalsOverTime.put("retainedKnowledge_static", new ArrayList<Double>());
		totalsOverTime.put("lostKnowledge_static", new ArrayList<Double>());
		totalsOverTime.put("retainedKnowledge_adaptive", new ArrayList<Double>());
		totalsOverTime.put("lostKnowledge_adaptive", new ArrayList<Double>());
		totalsOverTime.put("lostKnowledge_adaptive", new ArrayList<Double>());
		totalsOverTime.put("busFactor_adaptive", new ArrayList<Double>());
		totalsOverTime.put("busFactor_static", new ArrayList<Double>());
		//start the pipeline
		start(totals, totalsOverTime, devsProfileOverTime, bus_factor_zones);
		
		return totals;
	}

	
	public void start(HashMap<String, Double> totals, HashMap<String, ArrayList<Double>> totalsOverTime, 
			HashMap<Integer, HashMap<Integer, Developer>> devsProfileOverTime, HashMap<Integer, HashMap<Approach, List<String>>> bus_factor_zones)
					throws NoSuchElementException, IOException, URISyntaxException, CloneNotSupportedException, ClassNotFoundException{
		//get the trained Markov model with the predefined model
		training_instance.initialize_params(featureIni.getTm().get(listOfConfig.get("TM")), featureIni.getTm().get(listOfConfig.get("EM")));
		HMM=training_instance.getHMM();
		
		//create the sequence of states and observation
		Environment_s1.generaetListOfState();
		Environment_s1.generaetListOfObservation();
		
		//instantiate the objects required for the environment
		Environment_s1.insantiateObjects(featureIni.getTCR().get(listOfConfig.get("TCR"))); 

		Environment_s1.readyForAttachment.clear();
		//pull in the developer profile
		test.devInitialization(datasetName, featureIni.getDevNum().get(listOfConfig.get("numOfDevs")));
		
		//cut off the low experienced developers---need to fill ready for attachment list
		//starting with half of the developers
		
		Environment_s1.rankDevs();
		
		//Initialize the devNetwork
		Environment_s1.initializeDevNetwork();
		
		//supposed to change initialize the deletion and attachment rate
		//Environment_s1.initializeR(0.3);
		Environment_s1.initializeParameters();
		
		for(Entry<Integer, Developer> i:Environment_s1.getDevNetwork().vertexSet()){
			System.out.print(i.getKey()+" , ");
		}
		
		System.out.println();
		
		for(Integer i:Environment_s1.readyForAttachment){
			System.out.print(i+" , ");
		}
		
		//set the number of files
		String uri = Thread.currentThread().getContextClassLoader().getResource("main/resources/bug-data/" + datasetName + "/efforts").getFile();
		Environment_s1.numberOfFiles = new File(uri).list().length;
		//if(datasetName.equals("JDT"))
		//	Environment_s1.numberOfFiles = 9;
		//else
		//	Environment_s1.numberOfFiles=10;
		
		//set the initial observation and 
		int roundNum = 1;
		//clear knoweldgesofar to prevent duplicate zone object reference hasing
		GA_Problem_Parameter.knowledgeSoFar.clear();
		
		for (int i = 1; i <= Environment_s1.numberOfFiles; i++){
			//call for run
			GA_Problem_Parameter.listOfSubBugs.clear();
			//GATaskAssignment.run(datasetName, i, featureIni.getDevNum().get(listOfConfig.get("numOfBugs")));
			GATaskAssignment.run(datasetName, i, 0);
			//GATaskAssignment.run(datasetName, i, 5);
			if (i == Environment_s1.numberOfFiles / 2 || i == 1)
				devsProfileOverTime.put(0, (HashMap<Integer, Developer>) GA_Problem_Parameter.developers_all.clone());
			//int j=0;
			for (HashMap<Integer,Bug> bugList : GA_Problem_Parameter.listOfSubBugs) {
				if (bugList.size() < GA_Problem_Parameter.batch_size)
					continue;
				//increase the vlaue of total knowledge 
				for (Map.Entry<Integer, Bug> entry : bugList.entrySet()) {
					for (Zone z : entry.getValue().BZone_Coefficient.keySet()) {
						GA_Problem_Parameter.knowledgeSoFar.put(z, 1.0);
					}
				} 
				
				//log devs profile at the point of time
				logDevProfilePerRound(roundNum);
				
				//set bug dependencies
				GATaskAssignment.setBugDependencies(datasetName, bugList);
				//call the GA initialization--after party call
				GATaskAssignment.initializeGAParameter(bugList);
				//generate the models for create the candidates
				GA_Problem_Parameter.generateModelofBugs();
				GA_Problem_Parameter.candidateSolutonGeneration();
				test.initializeProblems();
				
				//find most probable state
				//State state=getState(HMM);
				State state = getState(Stubs.tempStates.get(roundNum-1));		/** using new version of getting state to test the state**/
				Environment_s1.addToSequenceOfStates(state);
				
				//call the assignment algorithm
				test.Assigning(state.getActionSet().get(0), 1, roundNum, datasetName, totals, totalsOverTime);
				
				HashMap<Integer, Developer> devs=GA_Problem_Parameter.developers_all;
				
				//update devNetwork
				//Environment_s1.nodeAttachment();
				//Environment_s1.nodeAttachment(Stubs.tempChurns.get(roundNum-1)); 		/**insert the num of devs by hand**/
				//Environment_s1.nodeDeletion();
				//Environment_s1.nodeDeletion(Stubs.tempChurns.get(roundNum-1));
				//Environment_s1.updateDevNetwork();
				
				
				/* Assessing bus factor for project and the zones*/
				for (Approach approach : Approach.values()) {
					totalsOverTime.get("busFactor_" + approach.toString().toLowerCase()).add(compute_bus_factor_project(approach, roundNum));
				}
				
				// reset bus factor
				reset_bus_factor();
				// compute bus factor for the zones
				compute_bus_factor_zones();
				
				/* insert the bus factor of zones */
				insert_bus_factor_zones(roundNum, bus_factor_zones);
				
				
				
				if (roundNum % FeatureInitializationV1.windowSize == 0 && roundNum > 3) {
					/* made the following line commented to use a fixed churnrate all over the other rounds*/
					//FeatureInitializationV1.churnRate = FeatureInitializationV1.churnRate + 1;
					
					//pass num of developer who will be added
					Environment_s1.nodeAttachment(FeatureInitializationV1.churnRate);
					
					//true as the second argument indicates random deletion
					Environment_s1.nodeDeletion(FeatureInitializationV1.churnRate, true);
					//Environment_s1.updateDevNetwork();
				}
				//add churn rate over time
				totalsOverTime.get("churnRate").add((double) FeatureInitializationV1.churnRate);
				
				//update over time dev profile
				if(roundNum % 10 == 0)
					Environment_s1.interRoundProfileUpdate();
				
				//developers need to be shuffled
				GA_Problem_Parameter.setDevelopersIDForRandom();
				System.out.println("number of developers---devNetwork: " + Environment_s1.devNetwork.vertexSet().size()
						+ "\n*** total changed: "
						+ Environment_s1.totalChanged);
				//add to the sequence of observation
				//the updates behind poisson process
				//update lambda
				Environment_s1.reinitializeParameters();
				//Environment_s1.reinitializeParameters(random.nextInt(Environment_s1.getDevNetwork().vertexSet().size()),
						//random.nextInt((Environment_s1.getDevNetwork().vertexSet().size()/2)));
				
				Environment_s1.addToSequenceOfObservation(Environment_s1.getObservation());

				//j++;
				
				// computing the knowledge loss and knowledge hit
				GA_Problem_Parameter.totalKnowledge = 0;
				for (Double d : GA_Problem_Parameter.knowledgeSoFar.values()) {
					GA_Problem_Parameter.totalKnowledge += d;
				}
				GA_Problem_Parameter.knowledgeHit_static = knowledgeHit_static() / GA_Problem_Parameter.totalKnowledge;
				GA_Problem_Parameter.knowledgeLoss_static = 1 - GA_Problem_Parameter.knowledgeHit_static;
				GA_Problem_Parameter.knowledgeHit_adaptive = knowledgeHit_adaptive() / GA_Problem_Parameter.totalKnowledge;
				GA_Problem_Parameter.knowledgeLoss_adaptive = 1 - GA_Problem_Parameter.knowledgeHit_adaptive;
				totalsOverTime.get("retainedKnowledge_static").add(GA_Problem_Parameter.knowledgeHit_static);
				totalsOverTime.get("lostKnowledge_static").add(GA_Problem_Parameter.knowledgeLoss_static);
				totalsOverTime.get("retainedKnowledge_adaptive").add(GA_Problem_Parameter.knowledgeHit_adaptive);
				totalsOverTime.get("lostKnowledge_adaptive").add(GA_Problem_Parameter.knowledgeLoss_adaptive);
				roundNum++;
			}
		}	
		
		//add after assignment profile
		devsProfileOverTime.put(1, GA_Problem_Parameter.developers_all);
		File file = new File(System.getProperty("user.dir") + File.separator + "results" + File.separator+ "self-adaptive"
				+ File.separator+ "devs_added.txt");
		file.getParentFile().mkdirs();
		PrintWriter pw = new PrintWriter(new FileOutputStream(file, true));
		pw.append("------------------" + "\n" + roundNum + "\n" + "--------------------" + "\n");
		pw.close();
		
		File file2 = new File(System.getProperty("user.dir") + File.separator+"results" + File.separator + "self-adaptive"
				+ File.separator+ "devs_deleted.txt");
		file.getParentFile().mkdirs();
		PrintWriter pw2 = new PrintWriter(new FileOutputStream(file2, true));
		pw2.append("------------------" + "\n" + roundNum + "\n" + "--------------------" + "\n");
		pw2.close();
		ArrayList<Double> laProbs = new ArrayList<Double>(LAProbes.values()); 
		totalsOverTime.put("actionProbVector", laProbs);
	}
	
	public State getState(HMM<Observation> HMM){
		HashMap<State, Double> stateProbability=new HashMap<State, Double>();
		
		int[] observation=Environment_s1.getObsercationSequence();
		int[] states=null;
		 
		int i=Environment_s1.observationSequence.size();
		for(Map.Entry<Integer, State> s:Environment_s1.listOfState.entrySet()){
			if(states!=null || i>1)
				Environment_s1.addToSequenceOfStates(s.getValue());
			states=Environment_s1.getStateSequence();
			stateProbability.put(s.getValue(), HMM.p(observation,states));
			Environment_s1.stateSequence.remove(Environment_s1.stateSequence.size()-1);
			i++;
		}
		
		double totalProb=0;
		for(Map.Entry<State, Double> stateProb:stateProbability.entrySet()){
			totalProb+=stateProb.getValue();
		}
		for(Map.Entry<State, Double> stateProb:stateProbability.entrySet()){
			stateProbability.put(stateProb.getKey(), stateProb.getValue()/totalProb);
		}
		
		double r=random.nextDouble();
		Map.Entry<State, Double> selectedState=null;
		double lowerBound=0;
		for(Map.Entry<State, Double> stateProb:stateProbability.entrySet()){
			if ((lowerBound < r) && (r < (lowerBound+stateProb.getValue())))
				selectedState=stateProb;
			else
				lowerBound+=stateProb.getValue();
		}
		
		
		/*for(Map.Entry<state, Double> stateProb:stateProbability.entrySet()){
			if (selectedState==null)
				selectedState=stateProb;
			else
				if(stateProb.getValue()>selectedState.getValue())
					selectedState=stateProb;
		}*/
		
		//environment_s1.addToSequenceOfStates(selectedState.getKey());
		try {
			selectedState.getKey();
		}
		catch (NullPointerException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return selectedState.getKey();
	}
	
	/**
	 * This method serves as the sutb for state generation
	 * @param period of type int which is the period the system is currently in
	 * @return the state relevant to the current system
	 */
	public State getState(int period) {
		return Environment_s1.listOfState.get(period);
	}
	
	public State getState(int period, String typeOf) {
		//create and fill list of max value for develpers
		ArrayList<Double> maxOfProfile=new ArrayList<Double>();
		//double x=Collections.max(GA_Problem_Parameter.developers_all.values());
		return null;
		
	}
	
	/**
	 * The methods intends to log developers' status of their profile in the file
	 * @param roundNum of type int which denotes the current round of assignment
	 * Effect: developers' profile at a point of time are logged
	 * @throws FileNotFoundException 
	 */
	public static void logDevProfilePerRound(int roundNum) throws FileNotFoundException {
		//open two different files
 		File fileToLogDevs_static=new File(System.getProperty("user.dir")+File.separator+"results"+ File.separator+ "self-adaptive"
				+File.separator+"devlopers"+File.separator+datasetName+File.separator+roundNum+File.separator+"static.csv");
 		File fileToLogDevs_adaptive=new File(System.getProperty("user.dir")+File.separator+"results"+ File.separator+ "self-adaptive"
				+File.separator+"devlopers"+File.separator+ datasetName+File.separator+roundNum+File.separator+"adaptive.csv");

 		//creat path if dose not exist
 		fileToLogDevs_adaptive.getParentFile().mkdirs();
 		fileToLogDevs_static.getParentFile().mkdirs();
 		
 		PrintWriter pw_devProfile_static=new PrintWriter(fileToLogDevs_static);
 		PrintWriter pw_devProfile_adaptive=new PrintWriter(fileToLogDevs_adaptive);
 		int devCount=0;
 		
 		//create the header for the files
		pw_devProfile_static.append("Dev#");
		pw_devProfile_adaptive.append("Dev#");
		for(Map.Entry<Zone, Double> entry:GA_Problem_Parameter.developers_all.get(GA_Problem_Parameter.devListId.get(0)).getDZone_Coefficient().entrySet()) {
			pw_devProfile_static.append(","+entry.getKey().zName);
			pw_devProfile_adaptive.append(","+entry.getKey().zName);
		}
		
		pw_devProfile_static.append("\n");
		pw_devProfile_adaptive.append("\n");
		
		//write the devs' profile
		String line_static, line_adaptive;
		for(Integer devId:GA_Problem_Parameter.devListId) {
			devCount++;
			Developer dev=GA_Problem_Parameter.developers_all.get(devId);
			line_static="";
			line_adaptive="";
			line_static+=devId;
			line_adaptive+=devId;
			for(Map.Entry<Zone, Double> zoneItem:dev.getDZone_Coefficient_static().entrySet()) {
				line_static+=","+String.format("%.2f", dev.getDZone_Coefficient_static().get(zoneItem.getKey()));
				line_adaptive+=","+String.format("%.2f", dev.getDZone_Coefficient().get(zoneItem.getKey()));
			}
			
			//trim to remove the unwanted tab and then add new line
			line_static.trim();
			line_adaptive.trim();
			if(devCount<GA_Problem_Parameter.devListIdSize) {
				line_adaptive+="\n";
				line_static+="\n";
			}
			
			//add the line to the printwriter
			pw_devProfile_static.append(line_static);
			pw_devProfile_adaptive.append(line_adaptive);
		}
		
		//close the opened printwriters
		pw_devProfile_adaptive.close();
		pw_devProfile_static.close();
	}
	
	public static int[] getFeedback(int roundNum, HashMap<String, ArrayList<Double>> totalsOverTime) {
		//the first element: Entropy, the second one: Exclusive knowledge
		int[] input = new int[] {0, 0};
		
		List<Double> list0f = totalsOverTime.get("EoT_adaptive");
		//measure the entropy change
		double E1 = totalsOverTime.get("EoT_adaptive").get(roundNum - 2);
		double E2 = totalsOverTime.get("EoT_adaptive").get(roundNum - 1);
		double diffEntropy = E2 - E1;
		//measure number of exclusive change
		List<Double> list0f2 = totalsOverTime.get("ExoTperRound_adaptive");
		double dev1 = totalsOverTime.get("ExoTperRound_adaptive").get(roundNum -2);
		double dev2 = totalsOverTime.get("ExoTperRound_adaptive").get(roundNum - 1);
		int diffExclusive = (int) (dev2 - dev1);
		//set inputs
		if (diffEntropy > 0) 
			input[0] = 1;
		else if (diffEntropy < 0)
			input[0] = -1;
		else 
			input[0] = 0;
		
		if (diffExclusive > 0)
			input[1] = 1;
		else if (diffExclusive < 0)
			input[1] = -1;
		else 
			input[1] = 0;

		return input;
	}
 		
	public static int[] getFeedback(int roundNum, HashMap<String, ArrayList<Double>> totalsOverTime, int windowSize) {
		//the first element: Entropy, the second one: Exclusive knowledge
		int[] input = new int[] {0, 0};
		
		List<Double> list0f = totalsOverTime.get("EoT_adaptive");
		//measure the entropy change
		double E1 = 0.0;
		if (totalsOverTime.get("EoT_adaptive").size() > windowSize) {
			E1 = totalsOverTime.get("EoT_adaptive").get(totalsOverTime.get("EoT_adaptive").size() - windowSize);
		}
		else
			E1 = totalsOverTime.get("EoT_adaptive").get(0);
		
		double E2 = totalsOverTime.get("EoT_adaptive").get(totalsOverTime.get("EoT_adaptive").size() - 1);
		double diffEntropy = E2 - E1;
		//measure number of exclusive change
		List<Double> list0f2 = totalsOverTime.get("ExoTperRound_adaptive");
		double dev1 = 0.0;
		if (totalsOverTime.get("ExoTperRound_adaptive").size() > windowSize) {
			E1 = totalsOverTime.get("ExoTperRound_adaptive").get(totalsOverTime.get("ExoTperRound_adaptive").size() - windowSize);
		}
		else
			E1 = totalsOverTime.get("ExoTperRound_adaptive").get(0);
		double dev2 = totalsOverTime.get("ExoTperRound_adaptive").get(totalsOverTime.get("EoT_adaptive").size() - 1);
		int diffExclusive = (int) (dev2 - dev1);
		//set inputs
		if (diffEntropy > 0) 
			input[0] = 1;
		else if (diffEntropy < 0)
			input[0] = -1;
		else 
			input[0] = 0;
		
		if (diffExclusive > 0)
			input[1] = 1;
		else if (diffExclusive < 0)
			input[1] = -1;
		else 
			input[1] = 0;

		return input;
	}
	
	public static Feedback getResponse(int[] feedbackArray) {
		String feedback = "";
		for (int i:feedbackArray) {
			feedback += i;
		}
		// 0 stands for the feedback as penalty and 1 means reward -- both are applied on 
		// the information diffusion
		switch (feedback) {
			case "-11":			/* no update!*/
				return Feedback.INACTION;
			case "-10":
				return Feedback.REWARD;
			case "-1-1":
				return Feedback.REWARD;
			case "11":			/* no update!*/
				return Feedback.PENALTY;
			case "10":
				return Feedback.REWARD;
			case "1-1":			/*no update!*/
				return Feedback.INACTION;	
			case "01":
				return Feedback.PENALTY;
			case "0-1":
				return Feedback.REWARD;
			case "00":			/* no update!*/
				return Feedback.INACTION;
			default:
				return Feedback.INACTION;
		}
	}
	
	
	/*
	 * the implementation of the function which maps the internal states to the action
	 */
	public static Action getAction(HashMap<String, ArrayList<Double>> totalsOverTime) {
		double r = random.nextDouble();
		
		if (r < LAProbes.get(Action.COST)) {
			totalsOverTime.get("actions").add(1.0);
			return Action.COST;
		}
		else {
			totalsOverTime.get("actions").add(0.0);
			return Action.DIFFUSION;
		}
	}
	
	/*
	 * update the probability function
	 * @ response the boolean value denotes the response from environment
	 * @ action is the action which resulted in this particular response
	 */
	public static void updateProbs_V1(Boolean response, Action action) {
		double theta = 0.01;
		//int response = (currentCost > formerCost) ? 0 : 1;
		//set reward-- the function is P(n+1) = 1- theta()...
		if (response) {
			//do reward
			LAProbes.put(action, LAProbes.get(action) + theta * (1 - LAProbes.get(action)));
			//LAProbes.put(action, LAProbes.get(action) + theta * LAProbes.get(action));
			//LAProbes.put(action.getOpposite(), (1 - LAProbes.get(action.getOpposite())));
			LAProbes.put(action.getOpposite(), 1 - LAProbes.get(action));
		}
		else {
			// apply penalty
			LAProbes.put(action, LAProbes.get(action) * (1 - theta));
			LAProbes.put(action.getOpposite(), (theta / (1 - Action.values().length)) + LAProbes.get(action.getOpposite()) * (1 - theta));
		}
	}
	
	public static void updateProbs_V2(Feedback response, Action action) {
		action = Action.COST;
		double theta = 0.01;
		double newR = Action.values().length - 1;
		//int response = (currentCost > formerCost) ? 0 : 1;
		//set reward-- the function is P(n+1) = 1- theta()...
		if (response == Feedback.REWARD) {
			//do reward
			LAProbes.put(action, LAProbes.get(action) + theta * LAProbes.get(action.getOpposite()));
			//LAProbes.put(action, LAProbes.get(action) + theta * LAProbes.get(action));
			//LAProbes.put(action.getOpposite(), (1 - LAProbes.get(action.getOpposite())));
			LAProbes.put(action.getOpposite(), LAProbes.get(action.getOpposite()) - (theta * LAProbes.get(action.getOpposite())));
		}
		else if (response == Feedback.PENALTY){
			// apply penalty
			LAProbes.put(action, LAProbes.get(action) - ((theta * LAProbes.get(action.getOpposite())) + (theta / newR)));
			LAProbes.put(action.getOpposite(), LAProbes.get(action.getOpposite()) + ((theta * LAProbes.get(action.getOpposite())) 
					+ (theta / newR)));
		}
	}
	
	public static void updateProbs(Feedback response, Action action) {
		double theta = 0.01;
		double newR = Action.values().length - 1;
		//int response = (currentCost > formerCost) ? 0 : 1;
		//set reward-- the function is P(n+1) = 1- theta()...
		if (response == Feedback.REWARD) {
			//do reward
			LAProbes.put(action, LAProbes.get(action) + theta * LAProbes.get(action.getOpposite()));
			//LAProbes.put(action, LAProbes.get(action) + theta * LAProbes.get(action));
			//LAProbes.put(action.getOpposite(), (1 - LAProbes.get(action.getOpposite())));
			LAProbes.put(action.getOpposite(), LAProbes.get(action.getOpposite()) - (theta * LAProbes.get(action.getOpposite())));
		}
		else if (response == Feedback.PENALTY){
			// apply penalty
			LAProbes.put(action, LAProbes.get(action) - ((theta * LAProbes.get(action.getOpposite())) + (theta / newR)));
			LAProbes.put(action.getOpposite(), LAProbes.get(action.getOpposite()) + ((theta * LAProbes.get(action.getOpposite())) 
					+ (theta / newR)));
		}
	}

	public static double getMinCost(List<FinalSolution<Solution, Double, Double>> ParetoFront_normalized) {
		double minCost = 1;
		for (FinalSolution<Solution, Double, Double> f:ParetoFront_normalized) {
			if (f.getCost() < minCost)
				minCost = f.getCost();
		}
		return minCost;
	}
	
	public static FinalSolution<Solution, Double, Double> getMinCost_solution(List<FinalSolution<Solution, Double, Double>> ParetoFront_normalized) {
		double minCost = 1;
		FinalSolution<Solution, Double, Double> s = null;
		for (FinalSolution<Solution, Double, Double> f:ParetoFront_normalized) {
			if (f.getCost() <= minCost) {
				minCost = f.getCost();
				s = f;
			}
		}
		return s;
	}
	
	public static double getMaxDiffusion(List<FinalSolution<Solution, Double, Double>> ParetoFront_normalized) {
		double maxD = 0;
		assertFalse("The normalized paretofront is empty!", ParetoFront_normalized == null);
		for (FinalSolution<Solution, Double, Double> f:ParetoFront_normalized) {
			if (f.getCost() > maxD)
				maxD = f.getDiffusion();
		}
		return maxD;
	}
	
	public static FinalSolution<Solution, Double, Double> getMaxDiffusion_solution(List<FinalSolution<Solution, Double, Double>> ParetoFront_normalized) {
		double maxD = 0;
		FinalSolution<Solution, Double, Double> s = null;
		for (FinalSolution<Solution, Double, Double> f:ParetoFront_normalized) {
			if (f.getCost() >= maxD) {
				maxD = f.getDiffusion();
				s = f;
			}
		}
		return s;
	}
	
	public static double knowledgeHit_static() {
		int knowledgeHit = 0;
		for (Map.Entry<Zone, Double> entry : GA_Problem_Parameter.knowledgeSoFar.entrySet()) {
				for (Integer id : GA_Problem_Parameter.devListId) {
					if (GA_Problem_Parameter.developers_all.get(id).getDZone_Coefficient_static().get(entry.getKey()) != null)
						if (GA_Problem_Parameter.developers_all.get(id).getDZone_Coefficient_static().get(entry.getKey()) > 0.95) {
							knowledgeHit++;
							break;
						}
				}
		}
		return knowledgeHit;
	}
	
	public static double knowledgeHit_adaptive() {
		int knowledgeHit = 0;
		double temp_dobule = 0;
		for (Map.Entry<Zone, Double> entry : GA_Problem_Parameter.knowledgeSoFar.entrySet()) {
			for (Integer id : GA_Problem_Parameter.devListId) {
				if (GA_Problem_Parameter.developers_all.get(id).getDZone_Coefficient().get(entry.getKey()) != null) {
					temp_dobule = GA_Problem_Parameter.developers_all.get(id).getDZone_Coefficient().get(entry.getKey());
					if (GA_Problem_Parameter.developers_all.get(id).getDZone_Coefficient().get(entry.getKey()) > 0.95) {
						knowledgeHit++;
						break;
					}
				}
			}
		}
		return knowledgeHit;
	}

	public static void compute_bus_factor_zones() {
		for (Map.Entry<Zone, Double> entry : GA_Problem_Parameter.knowledgeSoFar.entrySet()) {
			for (Integer id : GA_Problem_Parameter.devListId) {
				for (Approach approach : Approach.values()) {
					switch (approach) {
						case STATIC:
							if (GA_Problem_Parameter.developers_all.get(id).getDZone_Coefficient_static().get(entry.getKey()) > 0.95) {
								entry.getKey().bus_factor.put(approach, entry.getKey().bus_factor.get(approach) + 1);
							}
							break;
						case ADAPTIVE:
							if (GA_Problem_Parameter.developers_all.get(id).getDZone_Coefficient().get(entry.getKey()) > 0.95) {
								entry.getKey().bus_factor.put(approach, entry.getKey().bus_factor.get(approach) + 1);
							}
							break;
						default:
							break;
					}
				}					
			}
		}
	}
	
	public static void insert_bus_factor_zones(int roundNum, HashMap<Integer, HashMap<Approach, List<String>>> bus_factor_zones) {
		List<String> BusZones;
		HashMap<Approach, List<String>> temp_hm_zone_bus = new HashMap<Approach, List<String>>();
		for (Approach approach : Approach.values()) {
			BusZones = new ArrayList<String>();
			//headers.clear();
			for (Zone entry : GA_Problem_Parameter.allZones) {
				BusZones.add(entry.bus_factor.get(approach).toString());
				//headers.add(entry.zName);
			}
			//if (GA_Problem_Parameter.header_bus == null)
			//	GA_Problem_Parameter.header_bus = headers;
			//dicOfZones = String.format("{%s}", dicOfZones);
			temp_hm_zone_bus.put(approach, BusZones);
		}
		bus_factor_zones.put(roundNum, temp_hm_zone_bus);
	}
	
	public double compute_bus_factor_project(Approach approach, int roundNum) {
		double bus_factor = 0;
		double total_zone_num = GA_Problem_Parameter.knowledgeSoFar.size();
		double dev_prof_num;
		
		for (Integer id : GA_Problem_Parameter.devListId) {
			dev_prof_num = 0;
			for (Map.Entry<Zone, Double> entry : GA_Problem_Parameter.knowledgeSoFar.entrySet()) {
				switch (approach) {
					case STATIC:
						/*
						Developer d = GA_Problem_Parameter.developers_all.get(id);
						System.out.println(d.getID());
						System.out.println(entry.getKey().zName);
						System.out.println(GA_Problem_Parameter.developers_all.get(id).getDZone_Coefficient_static().get(entry.getKey()));
						System.out.println(roundNum);
						*/
						if (GA_Problem_Parameter.developers_all.get(id).getDZone_Coefficient_static().get(entry.getKey()) > 0.5) {
							dev_prof_num++;
						}
						break;
					case ADAPTIVE:
						if (GA_Problem_Parameter.developers_all.get(id).getDZone_Coefficient().get(entry.getKey()) > 0.5) {
							dev_prof_num++;
						}
						break;
					default:
						break;
				}
			}
			if ((dev_prof_num / total_zone_num) > 0.5) {
				bus_factor++;
			}
		}
		return bus_factor;
	}
	
	public static void reset_bus_factor() {
		for (Zone entry : GA_Problem_Parameter.allZones) {
			for (Approach approach : Approach.values()) {
				switch (approach) {
					case STATIC:
							entry.bus_factor.put(approach, 0);
						break;
					case ADAPTIVE:
							entry.bus_factor.put(approach, 0);
					default:
						break;
				}
			}	
		}
	}
}
