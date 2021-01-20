package main.java.context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;

import main.java.SCM_TA_V1.DevMetrics;
import main.java.SCM_TA_V1.Developer;
import main.java.SCM_TA_V1.GA_Problem_Parameter;
import main.java.SCM_TA_V1.Ranking;
import main.java.SCM_TA_V1.Zone;
//import org.apache.commons.math3.distribution.*;
import smile.stat.distribution.PoissonDistribution;

public class Environment_s1 extends Environment {
	public static double deletionRate=0;
	public static double attachmentRate=0;
	public static double TCR_ratio=0;
	public static int totalChanged=0;
	public static ArrayList<Integer> deletedNodes=new ArrayList<Integer>();
	public static ArrayList<Integer> readyForAttachment=new ArrayList<Integer>();
	static Random random;
	static int numOfNodes;
	public static int numOfShouldBeDeleted=0;
	public static int numberOfFiles=0;
	public static ArrayList<State> stateSequence=new ArrayList<State>();
	public static ArrayList<Observation> observationSequence=new ArrayList<Observation>();
	static HashMap<Integer, Observation> listOfObservation=new HashMap<Integer, Observation>();
	public static HashMap<Integer, State> listOfState=new HashMap<Integer, State>();
	static ArrayList<Integer> shouldBeDeleted=new ArrayList<Integer>();
	static int busFactor=2;
	static ArrayList<Integer> addedRecently=new ArrayList<Integer>();
	static ArrayList<Integer> tempState=new ArrayList<Integer>();
	static HashMap<Integer, Double> exclusives = new HashMap<Integer, Double>();
	static HashMap<Integer, Double>  probs= new HashMap<Integer, Double>();
	static HashMap<Integer, Double>  entropies= new HashMap<Integer, Double>();
	static HashMap<String, Double>  entropiesAndexclusivies = new HashMap<String, Double>();

	public static void insantiateObjects(int lambda){
		devNetwork=new DefaultDirectedWeightedGraph<Map.Entry<Integer, Developer>, DefaultEdge>(DefaultEdge.class);
		random=new Random();
		TCR=new PoissonDistribution(lambda);
	}
	

	public static  DefaultDirectedWeightedGraph<Entry<Integer, Developer>, DefaultEdge> getDevNetwork(){
		return devNetwork;
	}
	//prepare the input data set for training
	public static void generaetListOfObservation(){
		//clear former sequence
		listOfObservation.clear();
		
		//create new observation
		Observation o1=new Observation(0);
		Observation o2=new Observation(2);
		listOfObservation.put(0, o1);
		listOfObservation.put(1,o2);
	}
	
	public int getNumOfBugs(){
		return GA_Problem_Parameter.tasks.size();
	}
	
	
	public static void generaetListOfState(){
		//clear list of states
		listOfState.clear();
		
		//introduce the states
		final State steady_state=new State("steady",0);
		steady_state.setAction("diffusion");
		final State dynamic_state=new State("dynamic",1);
		dynamic_state.setAction("cost");
		
		listOfState.put(0, steady_state);
		listOfState.put(1,dynamic_state);
	}
	
	public static void initializeDevNetwork(){	
		//set devs node and assign weights to the developers
		int size=GA_Problem_Parameter.devListId.size();
		int sumOfWeights=0;
		for(Map.Entry<Integer, Developer> entry:GA_Problem_Parameter.developers_all.entrySet()){
			if(GA_Problem_Parameter.devListId.contains(entry.getKey())) {
				entry.getValue().weight=size;
				sumOfWeights+=size;
				size--;
				devNetwork.addVertex(entry);
			}
		}
		/*
		 * //add the edges Random r=new Random(); int numOfEdges=(size*(size-1))/2;
		 * ArrayList<Map.Entry<Integer, Developer>> edgeTails=new
		 * ArrayList<Map.Entry<Integer,Developer>>(); for(int i=0;i<numOfEdges;i++){
		 * setRandomEdge(devNetwork, edgeTails, sumOfWeights, r); }
		 */

		//set fully connected graph
		makeNetworkFullyConnectd(devNetwork);
		
		//set the weights of the edges
		setEdgesWeight();
		
	}
	
	
	
	public static void updateDevNetwork() {
		makeNetworkFullyConnectd(devNetwork);
		setEdgesWeight();
	}
	
	/**
	 * The method crates edges among the nodes in the network
	 * @param devNetwork
	 * @param edgeTails
	 * @param sumOfWeights
	 * @param r
	 */
	
	public static void makeNetworkFullyConnectd(DefaultDirectedWeightedGraph<Map.Entry<Integer, Developer>, DefaultEdge> devNetwork) {
		for(Map.Entry<Integer, Developer> nodeS:devNetwork.vertexSet()) {
			for(Map.Entry<Integer, Developer> nodeE:devNetwork.vertexSet()) {
				if(nodeS.getKey()!=nodeE.getKey()) {
					if(!devNetwork.containsEdge(nodeS, nodeE))
						devNetwork.addEdge(nodeS, nodeE);
				}	
			}
		}
			
	}
	
	public static void setRandomEdge(DefaultDirectedWeightedGraph<Map.Entry<Integer, Developer>, DefaultEdge> devNetwork, 
			 ArrayList<Map.Entry<Integer, Developer>> edgeTails, int sumOfWeights, Random r){
		int randNum;
		for(Map.Entry<Integer, Developer> dev:GA_Problem_Parameter.developers.entrySet()){
			randNum=r.nextInt(sumOfWeights);
			randNum-=dev.getValue().weight;
			if(randNum<0){
				edgeTails.add(dev);
			}
			if(edgeTails.size()==2){
				devNetwork.addEdge(edgeTails.get(0), edgeTails.get(1));
				devNetwork.addEdge(edgeTails.get(1), edgeTails.get(0));
				edgeTails.clear();
				continue;
			}
				
		}
	}
	
	public static void setEdges_newNodes(ArrayList<Integer> shouldBeDeleted){
		int numOfEdges=5;
		Map.Entry<Integer, Developer> dev=null;
		for(Integer i:shouldBeDeleted){
			//the selected node should not be as those in the 
			dev=getSelectedVertexByFitness(i);
			if(dev==null)
				System.out.println(dev);
			devNetwork.addEdge(dev, getDevNetworkVertex(i));
			devNetwork.addEdge(getDevNetworkVertex(i), dev);
		}
		
	}
	//set the label for all the edges in the devNetwork
	public static void setEdgesWeight(){
		//assign flow rate to each edge in developer network
		for(DefaultEdge e:devNetwork.edgeSet()){
			//compute the flow rate--start to end
			double flowRate=0;
			for(Map.Entry<Zone, Double> z:devNetwork.getEdgeSource(e).getValue().DZone_Coefficient.entrySet()){
				HashMap<Zone, Double> z_target=devNetwork.getEdgeTarget(e).getValue().DZone_Coefficient;
				if(!z_target.containsKey(z.getKey()))
					flowRate+=z.getValue();
				else{
					double difference=z.getValue()-z_target.get(z.getKey());
					flowRate+=(difference>0)?difference:0;
				}
					
			}
			//assign the flow rate
			devNetwork.setEdgeWeight(e,flowRate);
		}
		
	}

	//EFFECT: the developer network gets updated with nodes removed from that
	public static void nodeDeletion(){	
		//is done with the a rate of "r"
		double p=0;
		totalChanged=0;
		for(Map.Entry<Integer, Developer> node:devNetwork.vertexSet()){
			p=random.nextDouble();
			
			//ignore those who added recently
			if(addedRecently.contains(node.getKey()))
				continue;
			
			if(p<TCR_ratio && devNetwork.vertexSet().size() > GA_Problem_Parameter.devListIdSize && numOfShouldBeDeleted>0 
					&& GA_Problem_Parameter.devListId.size()>GA_Problem_Parameter.earlyDevListSize){
				devNetwork.removeVertex(getVertex(node.getKey()));
				GA_Problem_Parameter.developers.remove(node.getKey());
				GA_Problem_Parameter.devListId.remove(node.getKey());
				totalChanged++;
				numOfShouldBeDeleted--;
			}
		}
	}
	
	public static void nodeDeletion(int numOfDevs, Boolean random) throws FileNotFoundException{	
		//is done with the a rate of "r"
		
		//create a file for record devs who are removed
		File file = new File(System.getProperty("user.dir") + File.separator + "results" + File.separator + "self-adaptive"
				+ File.separator + "devs_deleted.txt");
		file.getParentFile().mkdirs();
		/* set num of devs should be deleted*/
		numOfDevs = (numOfDevs / 100) * GA_Problem_Parameter.devListId.size();
		
		PrintWriter pw = new PrintWriter(new FileOutputStream(file, true));
		//report total changed
		totalChanged = 0;
		//sort devs prior to removing
		ArrayList<Integer> listOfDevs = rankDevsByProfile(GA_Problem_Parameter.devListId);
		//Collections.reverse(listOfDevs);
		
		//shuffle in case random flag is true
		if (random) 
			Collections.shuffle(listOfDevs);
		
		for(Integer devID:listOfDevs){
			//ignore those who added recently
			if(addedRecently.contains(devID))
				continue;
			
			if( numOfDevs > 0  && GA_Problem_Parameter.devListId.size() > GA_Problem_Parameter.earlyDevListSize){
				numOfDevs--;
				pw.append(devID + "\n");
				devNetwork.removeVertex(getVertex(devID));
				GA_Problem_Parameter.developers.remove(devID);
				GA_Problem_Parameter.devListId.remove(devID);
				totalChanged++;
			}
		}
		pw.close();
	}
	
	/**
	 * Attaches the nodes from a developer pool
	 * 
	 * EFFECT: a new developer network with updated nodes
	 * @throws FileNotFoundException 
 	 */
	public static void nodeAttachment() throws FileNotFoundException{
		shouldBeDeleted.clear(); //it's needed to then update ready for attachment list
		addedRecently.clear();
		numOfShouldBeDeleted = 0;
		double p;
		File file = new File(System.getProperty("user.dir")+File.separator+"results"+ File.separator+ "self-adaptive"
				+ File.separator + "devs_added.txt");
		file.getParentFile().mkdirs();
		PrintWriter pw = new PrintWriter(new FileOutputStream(file, true));
		for(Integer i : readyForAttachment){
			p=random.nextDouble();
			if(p<TCR_ratio && numOfNodes>0){
				numOfNodes--;	/* decrease num of nodes should be deleted*/
				
				//check weather developer i exists
				if(GA_Problem_Parameter.getDev(i) != null){
					Map.Entry<Integer, Developer> developer = GA_Problem_Parameter.getDev(i);
					devNetwork.addVertex(developer);
					//GA_Problem_Parameter.developers.put(i, GA_Problem_Parameter.developers_all.get(i));
					pw.append(i + "\n");
					GA_Problem_Parameter.devListId.add(i);
					addedRecently.add(i);
					shouldBeDeleted.add(i);
					numOfShouldBeDeleted++;
				}
			}
		}
		//remove nodes from readyForAttachment after added to the devNetwork
		for(Integer i:shouldBeDeleted){
			readyForAttachment.remove(i);
		}
		
		//establish the links for the newly added nodes
		setEdges_newNodes(shouldBeDeleted);
		pw.close();
	}
	
	public static void nodeAttachment(int numberOfDevs) throws FileNotFoundException{
		File file = new File(System.getProperty("user.dir") + File.separator + "results" + File.separator 
				+ "self-adaptive" + File.separator + "devs_added.txt");
		file.getParentFile().mkdirs();
		PrintWriter pw = new PrintWriter(new FileOutputStream(file, true));
		
		shouldBeDeleted.clear(); //it's needed to then update ready for attachment list
		addedRecently.clear();
		numOfShouldBeDeleted = 0;
		
		//int numOfShouldBeAdded = numberOfDevs;
		int numOfShouldBeAdded = (numberOfDevs / 100) * GA_Problem_Parameter.devListId.size();
		ArrayList<Integer> shuffeledReadyForAttachment = (ArrayList<Integer>) readyForAttachment.clone();
		Collections.shuffle(shuffeledReadyForAttachment);
		for(Integer i : shuffeledReadyForAttachment){
			//check weather developer with the id of i exists
			if(GA_Problem_Parameter.getDev(i) != null && numOfShouldBeAdded > 0){
				numOfShouldBeAdded--;
				System.out.println("The id should be added: " + i);
				//numOfNodes--;	/* decrease num of nodes should be deleted*/
				pw.append(i + "\n");
				Map.Entry<Integer, Developer> developer = GA_Problem_Parameter.getDev(i);
				devNetwork.addVertex(developer);
				//GA_Problem_Parameter.developers.put(i, GA_Problem_Parameter.developers_all.get(i));
				GA_Problem_Parameter.devListId.add(i);
				addedRecently.add(i);
				shouldBeDeleted.add(i);
				numOfShouldBeDeleted++;
			}
		}
		
		//remove nodes from readyForAttachment after added to the devNetwork
		for(Integer i:shouldBeDeleted){
			readyForAttachment.remove(i);
		}
		
		//establish the links for the newly added nodes
		setEdges_newNodes(shouldBeDeleted);
		pw.close();
	}
	
	public static Map.Entry<Integer, Developer> getVertex(Integer i){
		Map.Entry<Integer, Developer> nodeForDeletion=null;
		for(Map.Entry<Integer, Developer> node:devNetwork.vertexSet())
			if(node.getKey()==i)
				nodeForDeletion=node;
			else
				nodeForDeletion=null;
		return nodeForDeletion;
	}
	
	public static void initializeR(double probability){
		Environment_s1.deletionRate=probability;
		Environment_s1.attachmentRate=1-deletionRate;
		
	}
	
	
	
	/*** after round update method ***/
	public static void recomputeNodeFitness(){
		double globalFitness=0;
		for(Map.Entry<Integer, Developer> node:devNetwork.vertexSet()){
			double individualFitness=0;
			for(Map.Entry<Zone, Double> zone:node.getValue().DZone_Coefficient.entrySet()){
			individualFitness+=zone.getValue();
			}
			individualFitness=individualFitness*devNetwork.degreeOf(node);
			node.getValue().fitness=individualFitness;
			globalFitness+=individualFitness;
		}
		
		for(Map.Entry<Integer, Developer> dev:devNetwork.vertexSet())
			dev.getValue().preferentialAttachment=dev.getValue().fitness/globalFitness;
		
	}
	 
	public static Map.Entry<Integer, Developer> getSelectedVertexByFitness(Integer selfEdge){
		
		Map.Entry<Integer, Developer> selected=null;
		
		ArrayList<Integer> vertexSet=new ArrayList<>();
		
		for(Map.Entry<Integer, Developer> node:devNetwork.vertexSet()){
			if(node.getKey()!=selfEdge)
				vertexSet.add(node.getKey());
		}
		
		int devIDIndex=ThreadLocalRandom.current().nextInt(0,vertexSet.size());
		
		for(Map.Entry<Integer, Developer> node:devNetwork.vertexSet()){
			if(node.getKey()==vertexSet.get(devIDIndex))
				selected=node;
		}
		
		return selected;
	}
	
	public static Map.Entry<Integer, Developer> getDevNetworkVertex(Integer i){
		Map.Entry<Integer, Developer> node=null;
		for(Map.Entry<Integer, Developer> vertex:devNetwork.vertexSet())
			if(vertex.getKey()==i)
				node=vertex;
		return node;
	}
	
	public static void rankDevs(){
		ArrayList<Ranking<Developer, Double>> Devs = new ArrayList<Ranking<Developer,Double>>();
		
		System.out.println("prelimenary dev list size: "+GA_Problem_Parameter.developers.size());
		
		for(Developer d:GA_Problem_Parameter.developers.values()){
			Devs.add(DevMetrics.computeMetric(d));
		}
		
		DevMetrics.sortByMetric(Devs);
		
		for(Ranking<Developer, Double> r:Devs){
			System.out.println(r.getEntity() + "--->" + r.getMetric());
		}
		
		System.out.println("secondary dev list size: " + Devs.size());
		
		//cut off the low experienced developers---add ready for attachment developers
		System.out.println("secondary dev list size: " + (Devs.size() - 100 * (75 / Devs.size())));
		GA_Problem_Parameter.pruneDevList(GA_Problem_Parameter.developers, Devs, 75); /* 75% of developers are moved to ready for attachment*/
		
	}

	public static double getTCR_ratio(){
		/*double down=devNetwork.vertexSet().size();
		TCR_ratio=totalChanged/down;*/
		return TCR_ratio;
	}
	
	public static Observation getObservation(){
		if(numOfNodes>=busFactor)
			return listOfObservation.get(1);
		else 
			return listOfObservation.get(0);
	}
	
	public static void addToSequenceOfStates(State state){
		stateSequence.add(state);
	}
	
	public static void addToSequenceOfObservation(Observation observation){
		observationSequence.add(observation);
	}
	
	public static State getTheLastState(){
		return stateSequence.get(stateSequence.size()-1);
	}

	public static Observation[] getObservationSymbols(){
		Observation o1=new Observation(2);
		Observation o2=new Observation(0);
		
		return new Observation[]{o1,o2};
	}

	@SuppressWarnings("null")
	public static int[] getStateSequence(){
		int[] stateSeqId=new int[stateSequence.size()];
		for(int i=0;i<stateSequence.size();i++){
			stateSeqId[i]=stateSequence.get(i).id;
		}
		return stateSeqId;
	}
	
	public static int[] getObsercationSequence(){
		int[] obsercationSeqId=new int[observationSequence.size()];
		for(int i=0;i<observationSequence.size();i++){
			obsercationSeqId[i]=observationSequence.get(i).getTeamChangeRate();
		}
		return obsercationSeqId;
	}
	
	public static void initializeParameters(){
		//clear sequences
		stateSequence.clear();
		observationSequence.clear();
		
		Environment_s1.addToSequenceOfStates(listOfState.get(0));
		Environment_s1.addToSequenceOfObservation(Environment_s1.getObservation());	
	}

	public static void reinitializeParameters(){
		TCR_ratio=ThreadLocalRandom.current().nextDouble(0.4,0.55);
		numOfNodes=getNearestK(TCR_ratio);
	}
	
	/**
	 * The method intends to find the nearest number of developers for input churn ration
	 * @param tcr of type double and denotes output of poisson process
	 * @return k  of type integer as the number of poisson algorithm input
	 */
	public static int getNearestK(double tcr) {
		int k=0;
		double diff=0.0;
		for(int i=0; i<getDevNetwork().vertexSet().size();i++) {
			if(diff==0.0)
				diff=Math.abs(TCR.p(i)-tcr);
			
			if(Math.abs(TCR.p(i)-tcr)<diff) {
				k=i;
				diff=Math.abs(TCR.p(i)-diff);
			}
		}
		return k;
	}
	
	public static ArrayList<Integer> rankDevsByProfile(ArrayList<Integer> devList) {
		HashMap<Integer, Developer> devsToBeSoreted=new HashMap<Integer, Developer>();
		ArrayList<Ranking<Developer, Double>> DevstoBeDeleted=new ArrayList<Ranking<Developer,Double>>();
		ArrayList<Integer> sortedDevs=new ArrayList<Integer>();
		
		for(Integer i:devList) {
			devsToBeSoreted.put(i, GA_Problem_Parameter.developers_all.get(i));
		}
		
		for(Developer d:devsToBeSoreted.values()){
			DevstoBeDeleted.add(DevMetrics.computeMetric(d));
		}
		
		DevMetrics.sortByMetric(DevstoBeDeleted);
		
		for(Ranking<Developer, Double> r:DevstoBeDeleted){
			sortedDevs.add(r.getEntity().getID());
			System.out.println(r.getMetric());
		}
		return sortedDevs;
	}

	public static void interRoundProfileUpdate() {
		ArrayList<Integer> listOfDevs=rankDevsByProfile(GA_Problem_Parameter.devListId);
		Collections.reverse(listOfDevs);
		int count=0;
		for (int devID:listOfDevs) {
			if (count > 1)
				return;
			for(Map.Entry<Zone, Double> zone:GA_Problem_Parameter.developers_all.get(devID).DZone_Coefficient_static.entrySet()) {
				GA_Problem_Parameter.developers_all.get(devID).getDZone_Coefficient_static().put(zone.getKey(), 0.0001);
			}
			count++;
		}
	}

	public static HashMap<String, Double> getEntropy() {
		double numOfEX = 0;
		//clean map
		entropiesAndexclusivies.clear();
	
		//check devs knowledge
		for (Integer i : GA_Problem_Parameter.devListId) {
			System.out.println("Dev ID: " + i);
			for (Double d : GA_Problem_Parameter.developers_all.get(i).getDZone_Coefficient().values()) {
				System.out.print(d + ", ");
			}
			System.out.println();
		}
		
		exclusives.clear();
		probs.clear();
		entropies.clear();
		
		double entropy = 0.0;
		Developer d = null;
		double knoweldgeSummation = 0;
		//set the max value for all the zones
		for(Integer devId : GA_Problem_Parameter.devListId) {
			d = GA_Problem_Parameter.developers_all.get(devId);
			for (Map.Entry<Zone, Double> devZone : d.getDZone_Coefficient().entrySet()) {
				if (exclusives.get(devId) == null)
					exclusives.put(devId, getSubtraction(d, devZone.getKey()));
				else
					exclusives.put(devId, exclusives.get(devId) + getSubtraction(d, devZone.getKey()));
			}
		}
		
		//compute the probs
		knoweldgeSummation = exclusives.values().stream().mapToDouble(ex -> Double.parseDouble(ex.toString())).sum();
		for (Map.Entry<Integer, Double> eKnowledge : exclusives.entrySet()) {
			probs.put(eKnowledge.getKey(), eKnowledge.getValue() / knoweldgeSummation);
		}
		
		double sum = probs.values().stream().mapToDouble(x -> Double.parseDouble(x.toString())).sum();
		
		//compute the num of dev exclusive
		for (Double ex : exclusives.values()) {
			if (ex > 0) {
				numOfEX += 1;
			}
		}
		/* FIXME: it should be replace with amount of knowledge, which is knowledgeSummation*/ 
		//entropiesAndexclusivies.put("Ex", numOfEX);		/* compute the ex number of*/
		entropiesAndexclusivies.put("Ex", knoweldgeSummation);	/* included amount of exclusive knowledge*/
		entropiesAndexclusivies.put("Entropy", computeEntropy());		/* compute the entropy*/
		
		return entropiesAndexclusivies;
	}
	
	public static double getSubtraction(Developer d, Zone z) {
		double max = 0;
		
		for (Integer devId : GA_Problem_Parameter.devListId) {
			if (devId != d.getID()) {
				if (GA_Problem_Parameter.developers_all.get(devId).getDZone_Coefficient().get(z) > max) {
					max = GA_Problem_Parameter.developers_all.get(devId).getDZone_Coefficient().get(z);
				}	
			}
		}
		
		double subtraction = d.getDZone_Coefficient().get(z) - max;
		if (subtraction > 0)
			return subtraction;
		else
			return 0.0;
	}
	
	public static double getEntropy_static() {
			
			//check devs knowledge
			for (Integer i : GA_Problem_Parameter.devListId) {
				System.out.println("Dev ID: " + i);
				for (Double d : GA_Problem_Parameter.developers_all.get(i).getDZone_Coefficient().values()) {
					System.out.print(d + ", ");
				}
				System.out.println();
			}
			
			exclusives.clear();
			probs.clear();
			entropies.clear();
			
			double entropy = 0.0;
			Developer d = null;
			double knoweldgeSummation = 0;
			//set the max value for all the zones
			for(Integer devId : GA_Problem_Parameter.devListId) {
				d = GA_Problem_Parameter.developers_all.get(devId);
				for (Map.Entry<Zone, Double> devZone : d.getDZone_Coefficient_static().entrySet()) {
					if (exclusives.get(devId) == null)
						exclusives.put(devId, getSubtraction(d, devZone.getKey()));
					else
						exclusives.put(devId, exclusives.get(devId) + getSubtraction(d, devZone.getKey()));
				}
			}
			
			//compute the probs
			knoweldgeSummation = exclusives.values().stream().mapToDouble(ex -> Double.parseDouble(ex.toString())).sum();
			for (Map.Entry<Integer, Double> eKnowledge : exclusives.entrySet()) {
				probs.put(eKnowledge.getKey(), eKnowledge.getValue() / knoweldgeSummation);
			}
			
			double sum = probs.values().stream().mapToDouble(x -> Double.parseDouble(x.toString())).sum();
			//compute the entropy
			entropy = computeEntropy();
			
			return entropy;
		}
	
	public static double getSubtraction_static(Developer d, Zone z) {
		double max = 0;
		
		for (Integer devId : GA_Problem_Parameter.devListId) {
			if (devId != d.getID()) {
				if (GA_Problem_Parameter.developers_all.get(devId).getDZone_Coefficient_static().get(z) > max) {
					max = GA_Problem_Parameter.developers_all.get(devId).getDZone_Coefficient_static().get(z);
				}	
			}
		}
		
		double subtraction = d.getDZone_Coefficient_static().get(z) - max;
		if (subtraction > 0)
			return subtraction;
		else
			return 0.0;
	}
	
	public static double computeEntropy() {
		double entropy = 0.0;
		for (Double prob : probs.values()) {
			if (prob != 0.0)
				entropy -=  prob * (Math.log(prob) / Math.log(2));
		}
		return entropy;
	}
}
