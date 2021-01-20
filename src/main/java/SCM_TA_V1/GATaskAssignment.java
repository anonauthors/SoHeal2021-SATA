package main.java.SCM_TA_V1;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.*;
import java.util.stream.Collectors;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.moeaframework.Analyzer.AnalyzerResults;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.algorithm.PeriodicAction.FrequencyType;
import org.moeaframework.algorithm.single.AggregateObjectiveComparator;
import org.moeaframework.algorithm.single.GeneticAlgorithm;
import org.moeaframework.algorithm.single.LinearDominanceComparator;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.GAVariation;
import org.moeaframework.core.operator.OnePointCrossover;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.operator.real.PM;
import org.moeaframework.core.operator.real.SBX;
import org.moeaframework.core.operator.real.UM;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

import main.java.context.Environment_s1;
import main.java.featureTuning.FeatureInitializationV1;
import main.java.mainPipeline.Action;
import main.java.mainPipeline.AdaptiveAssignmentPipline;
import main.java.mainPipeline.Approach;
import main.java.mainPipeline.Feedback;
import main.java.mainPipeline.FinalSolution;



public class GATaskAssignment {
	public static HashMap<Integer,Developer> developers=new HashMap<Integer,Developer>();
	static HashMap<Integer,Bug> bugs=new HashMap<Integer,Bug>();
	static Queue<Bug> orderdBugs; 
	//static Solution solution=null;
	static HashMap<Integer , Zone> columns=new HashMap<Integer, Zone>();
	static Project project=new Project();
	static int roundnum=0;
	//DevMetrics devMetric=new DevMetrics();
	private static GATaskAssignment instance=null;
	AbstractProblem normal_assginment;
	AbstractProblem ID_assignment;
	AbstractProblem static_assignment;
	Selection selection;
	Variation variation;
	AggregateObjectiveComparator comparator;
	Initialization inintialization_normal;
	Initialization inintialization_ID;
	Initialization inintialization_static;
	GeneticAlgorithm GA_normal;
	GeneticAlgorithm GA_ID;
	GeneticAlgorithm GA_static;
	NondominatedPopulation result;
	FileHandler file_logger=null;
	Logger logger=null;
	static double initalLearningRate = 0.10;
	StringBuilder sb = new StringBuilder();
	static Random random = new Random();
	private GATaskAssignment() {
		selection = new TournamentSelection(new ParetoDominanceComparator()); 
		variation = new GAVariation(new OnePointCrossover(GA_Problem_Parameter.one_x_rate),
				new UM(GA_Problem_Parameter.um_rate));
		comparator = new LinearDominanceComparator();
		try {
			file_logger = new FileHandler(System.getProperty("user.dir") + File.separator + "results" + File.separator
					+ "self-adaptive" + File.separator + "solutions.txt");
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		logger.addHandler(file_logger);
		SimpleFormatter sf = new SimpleFormatter();
		file_logger.setFormatter(sf);
		logger.setUseParentHandlers(false);
	}
	
	public static GATaskAssignment getInstance() {
		if(instance == null)
			instance = new GATaskAssignment();
		return instance;
	}
	
	public static void run(String datasetName, int fileNumber, int portion) throws NoSuchElementException, IOException, URISyntaxException{	
		
		/*int numOfFiles=0;
			if(dataset_name=="Platform")
				numOfFiles=10;
			else 
				numOfFiles=9;*/
			
			runExperiment(fileNumber, datasetName, portion);
		
	}
	
	public static void runExperiment(int fileNumber,String datasetName, int portion) throws NoSuchElementException, IOException, URISyntaxException{
		GA_Problem_Parameter.createPriorityTable();
		for(int runNum = 1; runNum <= 1; runNum++){
			//developers.clear();
			bugs.clear();
			starting(fileNumber, runNum, datasetName, portion);
			System.gc();
		}
		
	}
	
	public static void starting(int fileNumber, int runNum, String datasetName, int portion) throws IOException{
		bugInitialization(fileNumber, datasetName, portion);
		//Assigning(actionSet.get(0),runNum,fileNumber, datasetName);
		//solution=results[1].get(results[1].size()/2);
		//writeResult(runNum,i,results);
		//System.out.println("finished writing");
		//afterRoundUpdating(solution);
		//removeDevelopers();
	}
		
	public static void changeRepresentation(String datasetName) throws FileNotFoundException{
		
		changeRepresentation cr=new changeRepresentation(datasetName);
		cr.txtToCSV();
	}
	
	// initialize the developer objects
	public void devInitialization(String datasetName, int portion) throws IOException,NoSuchElementException, URISyntaxException, CloneNotSupportedException{
		//initialize developers
		developers.clear();
		GA_Problem_Parameter.allZones.clear();
		System.out.println("enter the developrs file");
		Developer developer = null;
		System.out.println(System.getProperty("user.dir"));
		Scanner sc=new Scanner(System.in);
		InputStream is=Thread.currentThread().getContextClassLoader().getResourceAsStream("main/resources/bug-data/bug-data/"+datasetName+"DeveloperWithWage.txt");
		InputStream is_copy=Thread.currentThread().getContextClassLoader().getResourceAsStream("main/resources/bug-data/bug-data/"+datasetName+"DeveloperWithWage.txt");
		//String uri=Thread.currentThread().getContextClassLoader().getResource("bug-data/bug-data/"+datasetName+"DeveloperWithWage.txt").getFile();
		//InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("bug-data/bug-data/"+datasetName+"DeveloperWithWage.txt");
		//System.out.println(is.toString());
		
		sc=new Scanner(is);
		int numof=0;
		System.out.println("enter the devlopers wage file");
		Scanner scan=new Scanner(is_copy);
		int i=0;
		int j=0;
		while(sc.hasNextLine() && scan.hasNextLine()){
			numof++;
			if(i==0){
				String[] items=sc.nextLine().split("\t",-1);
				System.out.println(items);
				scan.nextLine();
				for(int k=0;k<items.length-1;k++){
					if(j!=0){
						Zone zone=new Zone(j, items[k]);
						project.zones.put(j, zone);
						columns.put(j,zone);
						GA_Problem_Parameter.allZones.add(zone);
					}
					j++;
				}
			}
			else{
				String[] items=sc.nextLine().split("\t|\\s+",-1);
				String[] wage_items=scan.nextLine().split("\t|\\s+",-1);
				System.out.println(items);
				double sumOfPro=0.0;
				for(int k=0;k<items.length-1;k++){
					sumOfPro+=Double.parseDouble(items[k]);
				}
				for(int k = 0; k < items.length-1; k++){
					if(j!=0){
						//developer.DZone_Coefficient.put(columns.get(j), Double.parseDouble(items[k]));
						//System.out.println(columns.get(j));
						developer.DZone_Coefficient.put(project.zones.get(j), (Double.parseDouble(items[k]) / sumOfPro));
						developer.DZone_Coefficient_static.put(project.zones.get(j), (Double.parseDouble(items[k]) / sumOfPro));
						developer.DZone_Wage.put(project.zones.get(j), Double.parseDouble(wage_items[k])*Double.parseDouble(wage_items[wage_items.length-1]));
						developer.hourlyWage=Double.parseDouble(wage_items[wage_items.length-1]);
						//System.out.println(Double.parseDouble(wage_items[k]));
					}
					else{
						developer=new Developer(0);
						developer.setID(i);
					}
					j++;
				}
				developers.put(developer.getID(), developer);
			}
			i++;
			j=0;
		}
		/*assign GA_Problem_Parameter DevList*/
		for(Map.Entry<Integer, Developer> dev:developers.entrySet()){
			GA_Problem_Parameter.DevList.add(dev.getKey());
		}
		/* keep all devs list as developer_all */
		GA_Problem_Parameter.developers_all = (HashMap<Integer, Developer>) developers.clone();
		GA_Problem_Parameter.developers = developers;
		for(Developer d:GA_Problem_Parameter.developers.values()){
			for(Map.Entry<Zone, Double> entry:d.DZone_Coefficient.entrySet()){
				if(entry.getValue()==0) {
					d.DZone_Coefficient.put(entry.getKey(),getNonZeroMin(d.DZone_Coefficient));
					d.DZone_Coefficient_static.put(entry.getKey(),getNonZeroMin(d.DZone_Coefficient_static));
				}
			}
		}
		
		//set minimum of value for devs experties
		for(Developer d:GA_Problem_Parameter.developers.values()){
			for(Map.Entry<Zone, Double> entry:d.DZone_Coefficient.entrySet()){
				//if(entry.getValue()==0) {
					d.DZone_Coefficient.put(entry.getKey(),0.001);
					d.DZone_Coefficient_static.put(entry.getKey(),0.001);
				//}
			}
		}
		
		//cut randomly portion of developers
		GA_Problem_Parameter.cutDevs(portion);
		sc.close();
		scan.close();
		is.close();
		is_copy.close();
		
		for(Map.Entry<Integer, Developer> d:GA_Problem_Parameter.developers_all.entrySet()) {
			System.out.print(d.getKey() + "--->");
			for(Map.Entry<Zone, Double> zone:d.getValue().DZone_Coefficient.entrySet())
				System.out.print(zone.getValue() + " ");
			System.out.println();
		}
		//add header for bus factor-- it will be used to make the header of csv file during recording 	
		GA_Problem_Parameter.header_bus.clear();
		for (Zone z_temp : GA_Problem_Parameter.allZones) {
			GA_Problem_Parameter.header_bus.add(z_temp.zName);
		}

	}
	
	// initialize the bugs objects for task assignment
	public static void bugInitialization(int fileNumber, String datasetName, int portion) throws IOException,NoSuchElementException{	
		bugs.clear();
		Scanner sc;//=new Scanner(System.in);
		int i=0;
		int j=0;
		/*generate bug objects*/
		System.out.println("enter the bugs files");
		Bug bug=null;
		//sc=new Scanner(System.in);
		//sc=new Scanner("/bug-data/JDT/efforts");
		//System.out.println(sc.nextLine());
		String uri = Thread.currentThread().getContextClassLoader().getResource("main/resources/bug-data/" + datasetName + "/efforts").getFile();
		Scanner sc1=null;
		int n=1;
		for(File fileEntry : new File(uri).listFiles()){
			sc1 = new Scanner(new File(fileEntry.toURI()));
			i=0;
			j=0;
			if(fileNumber==n)
				System.out.println(fileEntry.getPath());
			while(sc1.hasNextLine() && fileNumber==n){
				//counter "i" has set to record the name of each zone (the header of each file)
				if(i==0){
						String[] items = sc1.nextLine().split("\t",-1);
							for(int k = 0; k < items.length; k++){
								if(j > 2){
									Zone zone = new Zone(j, items[k]);
									columns.put(j, zone);
								}
								j++;
							}	
					}
					else{
						String[] items = sc1.nextLine().split("\t",-1);
						for(int k = 0; k < items.length; k++){
							if(j > 2 && Double.parseDouble(items[k]) != 0){
								bug.BZone_Coefficient.put(project.zones.get(j-2), Double.parseDouble(items[k]));  /*from real data-set*/
								//bug.BZone_Coefficient.put(project.zones.get(j-2), (random.nextDouble() * 0.5) + 0.5);  /*simulated*/
							}
							else if(j == 0){
								bug=new Bug();
								bug.setID(Integer.parseInt(items[k]));
							}
							else if(j == 2){
								bug.setTotalEstimatedEffort(Double.parseDouble(items[k]));
							}
							j++;
						}
					//create DAG for zoneItems 	
					bug.setZoneDEP();	
					//add bug to bugset
					bugs.put(bug.getID(), bug);
					}
					i++;
					j=0;
			}
			n++;
		}
				

		
		//cut portion of tasks randomly
		GA_Problem_Parameter.cutTasks(portion, bugs);
		
		//split up the bug list to several low batches-- each one will be assigned in a round
		GA_Problem_Parameter.splitBugList(bugs);
		
	}
	
	/**
	 * this method is set for initializing the static(final) parameters
	 * @param bugList the list of bugs introduced for assignment
	 */
	public static void initializeGAParameter(HashMap<Integer,Bug> bugList){
		//initialize GA parameters
		int b_index=0;
		GA_Problem_Parameter.bugs = new Bug[bugList.size()];
		
		for(Entry<Integer, Bug> b2 : bugList.entrySet()){
			GA_Problem_Parameter.bugs[b_index] = b2.getValue();
			b_index++;
		}
		
		//GA_Problem_Parameter
		GA_Problem_Parameter.Num_of_Bugs = bugs.size();
		GA_Problem_Parameter.Num_of_Active_Developers = developers.size();
		GA_Problem_Parameter.upperDevId = developers.size() + 1;
		GA_Problem_Parameter.Num_of_functions_Multi = 2;
		GA_Problem_Parameter.Num_of_variables = 0;
		
		for(Entry<Integer, Bug>  b:bugs.entrySet()){
			for(Map.Entry<Zone, Double>  zone : b.getValue().BZone_Coefficient.entrySet()){
				GA_Problem_Parameter.Num_of_variables++;
			}
		}
		
		System.out.println("size of bug list: " + GA_Problem_Parameter.bugs.length);
	}
	
	public void initializeProblems() {
		normal_assginment = new NormalAssignment();
		ID_assignment = new InformationDifussion_adaptive();
		static_assignment = new StaticAssignment();
		inintialization_normal = new RandomInitialization(normal_assginment, GA_Problem_Parameter.population);
		inintialization_ID = new RandomInitialization(ID_assignment, GA_Problem_Parameter.population);
		//inintialization_static = new RandomInitialization(static_assignment, GA_Problem_Parameter.population);
		inintialization_static = new RandomInitialization(static_assignment, GA_Problem_Parameter.population);
		GA_normal = new GeneticAlgorithm(normal_assginment, comparator, inintialization_normal, selection, variation);
		GA_ID = new GeneticAlgorithm(ID_assignment, comparator, inintialization_ID, selection, variation);
		GA_static = new GeneticAlgorithm(static_assignment, comparator, inintialization_static, selection, variation);
	}
	
	public static void setBugDependencies(String datasetName, HashMap<Integer,Bug> bugList) throws FileNotFoundException{
		/*set bug dependencies*/
		int f = 0,i=0;
		Scanner sc,sc1=null;
		System.out.println("enter the bug dependency files");
		String[] columns_bug=null;
		for(Bug b:bugList.values()){
			b.DB.clear();
		}
		String uri=Thread.currentThread().getContextClassLoader().getResource("main/resources/bug-data/"+datasetName+"/dependencies").getFile();
		for(File fileEntry:new File(uri).listFiles()){
			sc1=new Scanner(new File(fileEntry.toURI()));
			i=0;
			while(sc1.hasNextLine()){
				if(i>0){
					int l=6;
					int m=5;

					String s=sc1.nextLine();
					columns_bug=s.split(",");
					//if(columns_bug[l]=="P3")
						//System.out.println(columns_bug[l]);
					int k=1;
					try{
						for(String st:columns_bug){
							if(st.contains("P3")){
								bugList.get(Integer.parseInt(columns_bug[k-1])).priority="P3";
							}
							if(st.contains("P2")){
								bugList.get(Integer.parseInt(columns_bug[k-1])).priority="P2";
							}
							if(st.contains("P1")){
								bugList.get(Integer.parseInt(columns_bug[k-1])).priority="P1";
							}
						}
						
						if(columns_bug[k].trim().length() > 0){
							try{
								Integer ID_1=Integer.parseInt(columns_bug[k-1]);
								System.out.println(bugList.get(ID_1).ID);
								if(bugList.get(Integer.parseInt(columns_bug[k-1]))!=null && bugList.get(Integer.parseInt(columns_bug[k]))!=null){
									bugList.get(Integer.parseInt(columns_bug[k-1])).DB.add(bugList.get(Integer.parseInt(columns_bug[k])));
									f++;
								}
								
								//System.out.println(bugs.get(Integer.parseInt(columns_bug[k])));
							}
							catch(Exception e){
								
							}
						}	
					}
					catch(Exception e){
						
					}
					
				}
				else{
					sc1.nextLine();
				}
				i++;
			}
		}
		System.out.println("real F:"+f);
		/* end setting bug dependencies*/
		
		/* set zone dependencies */
		
		/* end setting zone dependencies */
		System.out.println("end of setting dependencies");
	}
	
	//find solution to assign tasks to the developers

	public void Assigning(String action, int runNum, int fileNum, String datasetName, HashMap<String, Double> totals, HashMap<String, ArrayList<Double>> totalsOverTime) throws IOException{		
		roundnum = fileNum;
		logger.log(Level.INFO, "Round Num: " + fileNum);
		int c;
		
		/************************************************starting the static part***************************/
		GA_Problem_Parameter.setArrivalTasks();
		GA_Problem_Parameter.setDevelopersIDForRandom();
		GA_Problem_Parameter.flag = 1;
		
		while(GA_static.getNumberOfEvaluations() < GA_Problem_Parameter.nfe) {
			GA_static.step();
		}
		
		result = GA_static.getResult();
		
		/***those that has been commented in favor of better GA implementation*****
		* 
		Population result_normal=new Executor().withProblemClass(normal_assignment.class).withAlgorithm("NSGAII")
		.withMaxEvaluations(30000).withProperty("populationSize",GA_Problem_Parameter.population).withProperty("operator", "UX")
		.withProperty("UX.rate", 0.9).withProperty("operator", "UM").withProperty("pm.rate", 0.05).run();
		**/
		System.out.println("finished static-based assignment");
		
		//cost based///
		////
		Solution staticSolution = null;
		for(Solution s : result)
			staticSolution = s;
		
		//write as the logs to the file
		int[] STSolution = new int[staticSolution.getNumberOfVariables()];
		for(int i = 0; i < staticSolution.getNumberOfVariables(); i++) {
			STSolution[i] = GA_Problem_Parameter.devListId.get(EncodingUtils.getInt(staticSolution.getVariable(i)));
		}
		logger.log(Level.INFO, "ST solution ," + Arrays.toString(STSolution));
		
		c = 0;
		
		while(GA_Problem_Parameter.tso_static.hasNext()){
			Bug b=GA_Problem_Parameter.tso_static.next();
			TopologicalOrderIterator<Zone, DefaultEdge> tso_Zone = new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
			while(tso_Zone.hasNext()){
				Developer d = GA_Problem_Parameter.developers_all.get(GA_Problem_Parameter.devListId.get(EncodingUtils.getInt(staticSolution.getVariable(c))));
				updateDevProfile_static(b, tso_Zone.next(), d);
				c++;
			}
		}
		
		//add to total cost ove time and total information diffusion
		totals.put("TCT_static", totals.get("TCT_static")+ staticSolution.getObjective(0));
		totals.put("TID_static", totals.get("TID_static")+ (Double)staticSolution.getAttribute("diffusedKnowledge"));
		
		if(totals.get("TCT_static") == null || totals.get("TCT_static") == 0.0)
			System.out.println("test");
		totalsOverTime.get("CoT_static").add(totals.get("TCT_static"));
		totalsOverTime.get("IDoT_static").add(totals.get("TID_static"));
		totalsOverTime.get("costPerRound_static").add(staticSolution.getObjective(0));
		totalsOverTime.get("idPerRound_static").add((Double)staticSolution.getAttribute("diffusedKnowledge"));
		totalsOverTime.get("EoT_static").add(Environment_s1.getEntropy_static());
		//TID+=(Double)staticSolution.getAttribute("diffusedKnowledge");
		//}	
		
/************************************************starting the self-adaptive part***************************/
		GA_Problem_Parameter.setArrivalTasks();
		GA_Problem_Parameter.setDevelopersIDForRandom();
		GA_Problem_Parameter.flag = 1;
		
		String path= null;
		switch (FeatureInitializationV1.datasetName) {
			case "JDT":
				path = System.getProperty("user.dir") + File.separator + "PS" + File.separator + FeatureInitializationV1.datasetName 
				+ File.separator+"JDTMilestone3.1.1" + ".ps";
				break;
			case "Platform":
				path = System.getProperty("user.dir") + File.separator + "PS" + File.separator + FeatureInitializationV1.datasetName 
				+ File.separator + "PlatformMilestone3.0" + ".ps";
				break;	
		}
		Instrumenter instrumenter_adaptive_multi = new Instrumenter().withProblem("NSGAIIITAGLS").withReferenceSet(new File(path)).withFrequency(10).attachAll()
	    		.withFrequencyType(FrequencyType.EVALUATIONS);
		NondominatedPopulation NDP_adaptive_multi = new Executor().withProblemClass(InformationDifussion_adaptive_multi.class).withAlgorithm("NSGAII")
				.withMaxEvaluations(GA_Problem_Parameter.nfe).withProperty("populationSize", GA_Problem_Parameter.population).withProperty("operator", "1x+um")
				.withProperty("1x.rate", GA_Problem_Parameter.one_x_rate).withProperty("um.rate", GA_Problem_Parameter.um_rate).withInstrumenter(instrumenter_adaptive_multi).run();
		
		//assertNotEquals(0, NDP_adaptive_multi.size());
		int size = NDP_adaptive_multi.size();
		sb.append("ID , Cost");
		sb.setLength(0);
		for(Solution s : NDP_adaptive_multi){
			int temp = s.getNumberOfVariables();
			sb.append(s.getObjective(0) + "," + s.getObjective(1));
			sb.append("\n");
	    }
		
		File file = new File(System.getProperty("user.dir") + File.separator + "paretoFronts" + File.separator + FeatureInitializationV1.datasetName 
				+ File.separator + roundnum + File.separator + runNum + ".csv");
		file.getParentFile().mkdirs();	
		PrintWriter pw = new PrintWriter(file);
	    pw.write(sb.toString());
	    pw.close();
	    
	    //select solution with the help of LA
	    double maxCost = 0;
	    double maxD = 0;
	    List<FinalSolution<Solution, Double, Double>> ParetoFront = new ArrayList<FinalSolution<Solution,Double,Double>>();
	    
	    for (Solution s : NDP_adaptive_multi) {
	    	if (s.getObjective(1) > maxCost)
	    		maxCost = s.getObjective(1);
	    	if (s.getObjective(0) > maxD)
	    		maxD = s.getObjective(0);
	    	ParetoFront.add(new FinalSolution<Solution, Double, Double>(s, s.getObjective(1), s.getObjective(0)));
	    }
	    
	    for (FinalSolution<Solution,Double,Double> f: ParetoFront) {
	    	System.out.println("the actual id:" + f.getCost());
	    	System.out.println("the actual id: "+ f.getDiffusion());
	    }
	    
	    
	    final double ratioCost	= 1.0 / maxCost;
	    final double rationD = maxD != 0.0 ? 1.0 / maxD: 0.0;
	    
	    @SuppressWarnings("unchecked")
	    List<FinalSolution<Solution, Double, Double>> ParetoFront_normalized = ParetoFront.stream().map(
	    		x -> {	x.setSolution(x.getSolution());
	    				x.setCost(x.getCost() * ratioCost);
	    				x.setDiffusion(x.getDiffusion() * rationD);
	    				return x;
	    		 }).collect(Collectors.toList());
	    Action action2 = null;
	    if (roundnum > 4)
	    	action2 = AdaptiveAssignmentPipline.getInstance().getAction(totalsOverTime);
	    else {
	    	action2 = Action.DIFFUSION;
	    	totalsOverTime.get("actions").add(0.0);
	    }
	    
	    
	    
	    Solution adaptiveSolution = null;
	    int nums = 0;
	    switch (action2) {
	    	case COST:
	    		try {
	    			adaptiveSolution = AdaptiveAssignmentPipline.getInstance().getMinCost_solution(ParetoFront_normalized).getSolution();
	    		}
	    		catch (Exception e) {
	    			nums = NDP_adaptive_multi.size();
	    			System.out.println("The nums is (cost):" + nums);
	    			System.out.println("The nums is (cost_main):" + ParetoFront_normalized.size());
				}
	    		break;
	    	case DIFFUSION:
	    		try {
	    			adaptiveSolution = AdaptiveAssignmentPipline.getInstance().getMaxDiffusion_solution(ParetoFront_normalized).getSolution();
	    		}
	    		catch (Exception e) {
	    			nums = NDP_adaptive_multi.size();
	    			System.out.println("The nums is (diffusion):" + nums);
	    			System.out.println("The nums is (diffusion_main):" + ParetoFront_normalized.size());
				}
	    		break;
	    }
	    assertNotNull(EncodingUtils.getInt(adaptiveSolution));
	    
	    c=0;
	    while(GA_Problem_Parameter.tso_adaptive.hasNext()){
			Bug b=GA_Problem_Parameter.tso_adaptive.next();
			TopologicalOrderIterator<Zone, DefaultEdge> tso_Zone=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
			while(tso_Zone.hasNext()){
				Developer d = GA_Problem_Parameter.developers_all.get(GA_Problem_Parameter.devListId.get(EncodingUtils.getInt(adaptiveSolution.getVariable(c))));
				updateDevProfile_adaptive(b, tso_Zone.next(), d);
				c++;
			}
		}
		
	    //add to total cost over time and total information diffusion
  		totals.put("TCT_adaptive", totals.get("TCT_adaptive") + adaptiveSolution.getObjective(1));
  		totals.put("TID_adaptive", totals.get("TID_adaptive") + adaptiveSolution.getObjective(0));
  		
  		if(totals.get("TCT_adptive")==null || totals.get("TCT_adaptive")==0.0)
  			System.out.println("test");
  		totalsOverTime.get("CoT_adaptive").add(totals.get("TCT_adaptive"));
  		totalsOverTime.get("IDoT_adaptive").add(totals.get("TID_adaptive"));
  		totalsOverTime.get("costPerRound_adaptive").add(adaptiveSolution.getObjective(1));
  		totalsOverTime.get("idPerRound_adaptive").add(adaptiveSolution.getObjective(0));
  		totalsOverTime.get("EoT_adaptive").add(Environment_s1.getEntropy().get("Entropy"));
  		totalsOverTime.get("ExoTperRound_adaptive").add(Environment_s1.getEntropy().get("Ex"));
  		

  		String temppp1 = FeatureInitializationV1.actionProbOverRound;
  		
  		FeatureInitializationV1.actionProbOverRound = FeatureInitializationV1.actionProbOverRound.equals("") ? "" 
  				: (FeatureInitializationV1.actionProbOverRound + "\n");
  		String temppp = FeatureInitializationV1.actionProbOverRound;
  		String t = AdaptiveAssignmentPipline.LAProbes.values().toString();
  		FeatureInitializationV1.actionProbOverRound += AdaptiveAssignmentPipline.LAProbes.values().toString()
  				.replace("[", "").replace("]", "");
  		//get the response from environment and call the update function
  		int[] feedback = roundnum > 5 ?
  				AdaptiveAssignmentPipline.getInstance().getFeedback(roundnum, totalsOverTime)
  				: new int[]{0, 0};
    	Feedback response = roundnum > 5 ? 
    			AdaptiveAssignmentPipline.getInstance().getResponse(feedback):
    			Feedback.INACTION;
	    
	    //call the update function
	    AdaptiveAssignmentPipline.getInstance().updateProbs_V2(response, action2);
	}
	
	//write the results for testing
	public static void writeResult(int runNum,int roundNum, NondominatedPopulation[] result) throws FileNotFoundException{
		//write results to CSV for each round
		System.out.println("result of the expriment for Karim approach");
		PrintWriter pw=new PrintWriter(new File(System.getProperty("user.dir")+"//results//solutions_Karim_round "+roundnum+"_"+roundNum+".csv"));
		StringBuilder sb=new StringBuilder();
		for(Solution solution:result[0]){
			for(int i=0; i<solution.getNumberOfVariables();i++){
				System.out.print(EncodingUtils.getInt(solution.getVariable(i))+",");
			}
			System.out.println();
			sb.append(solution.getObjective(0)+","+solution.getObjective(1));
			sb.setLength(sb.length()-1);
			sb.append("\n");
		}
		pw.write(sb.toString());
		pw.close();
		System.out.println("result of the expriment for proposed approach");
		pw=new PrintWriter(new File(System.getProperty("user.dir")+"//results//solutions_Me_round "+runNum+"_"+roundNum+".csv"));
		sb.setLength(0);
		for(Solution solution:result[1]){
			for(int i=0; i<solution.getNumberOfVariables();i++){
				System.out.print(EncodingUtils.getInt(solution.getVariable(i))+",");
			}
			System.out.println();
			sb.append(solution.getObjective(0)+","+solution.getObjective(1));
			sb.setLength(sb.length()-1);
			sb.append("\n");
		}
		pw.write(sb.toString());
		pw.close();
		
	}
	
	public static void writeAnalyzingResults(AnalyzerResults ar, int runNum, int roundNum) throws FileNotFoundException{
		//PrintWriter pw=new PrintWriter(new File(System.getProperty("user.dir")+"//results//AnalyzerResults"+runNum+"_"+roundNum+".csv"));
		StringBuilder sb=new StringBuilder();
		for(String AN:ar.getAlgorithms()){
			System.out.println(ar.get(AN));
		}
	}
	
	public static void afterRoundUpdating(Solution solution){
		//update developers' zone
		int variableNum=0;
		for(Map.Entry<Integer, Bug> bug:bugs.entrySet()){
			for(Map.Entry<Zone, Double> zone:bug.getValue().BZone_Coefficient.entrySet()){
				updateDeveloperSkills(EncodingUtils.getInt(solution.getVariable(variableNum)),zone);
				variableNum++;
			}
		}
		//remove 2 top developers
		
	}
	
	public static void removeDevelopers(){
		int devId=-1;
		double devScore=-1.0;
		//select dev with the max of sum of competencies 
		for(Map.Entry<Integer, Developer> dev:developers.entrySet()){
			if(devCompetenciesMeasurement(dev.getValue())>devScore){
				devId=dev.getKey();
				devScore=devCompetenciesMeasurement(dev.getValue());
			}
		}
		try{
			
		//GA_Problem_Parameter.DevList.remove(devId);
		developers.remove(devId);
		System.out.println(devId);
		}
		catch(Exception e){
		}
	
	}
	
	public static double devCompetenciesMeasurement(Developer dev){
		double CumulativeSkillLevel=0.0;
		for(Map.Entry<Zone,Double> zone:dev.getDZone_Coefficient().entrySet())
			CumulativeSkillLevel+=zone.getValue();
		return CumulativeSkillLevel;
	}
	
	public static void updateDeveloperSkills(int dev, Map.Entry<Zone, Double> zone){
		for(Map.Entry<Zone, Double> devZone:developers.get(dev).DZone_Coefficient.entrySet()){
			if(devZone.getKey().zId==zone.getKey().zId)
				developers.get(dev).DZone_Coefficient.put(devZone.getKey(),devZone.getValue()+zone.getValue());
		}
	}
	
	public static void writeResultsforRuns(){
		
	}
	
	private static double getNonZeroMin(HashMap<Zone, Double> entrySet){
		double min=300;
		for(Double d:entrySet.values()){
			if(min>d && d>0){
				min=d;
			}
		}
		return min;
	}
	
	//update the profile of developers
	public static void updateDevProfile_adaptive(Bug b,Zone z, Developer d){
		//updating dev profile using a particular learning rate
		double knowledge = d.getDZone_Coefficient().get(z) + 0.1;
		knowledge = knowledge < 1 ? knowledge : 1;
		//d.getDZone_Coefficient().put(z, d.getDZone_Coefficient().get(z) + fitnessCalc.getID(null, d, b, z, Approach.ADAPTIVE) * initalLearningRate);
		d.getDZone_Coefficient().put(z, knowledge);
		
	}
	
	/**
	 * After assignment update of the developer profiles
	 * @param b of type Bug represents the bug which zone is part of
	 * @param z of type Zone
	 * @param d
	 */
	public static void updateDevProfile_static(Bug b,Zone z, Developer d){
		//d.getDZone_Coefficient_static().put(z, d.getDZone_Coefficient_static().get(z) + fitnessCalc.getID(null, d, b, z, Approach.STATIC) * initalLearningRate);
		//updating dev profile using a particular learning rate
		double knowledge = d.getDZone_Coefficient_static().get(z) + 0.1;
		knowledge = knowledge < 1 ? knowledge : 1;
		//d.getDZone_Coefficient().put(z, d.getDZone_Coefficient().get(z) + fitnessCalc.getID(null, d, b, z, Approach.ADAPTIVE) * initalLearningRate);
		d.getDZone_Coefficient_static().put(z, knowledge);
	}

}
