package main.java.mainPipeline;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.*;
import java.util.stream.Collectors;

import org.moeaframework.algorithm.single.AggregateObjectiveComparator;
import org.moeaframework.algorithm.single.GeneticAlgorithm;
import org.moeaframework.algorithm.single.LinearDominanceComparator;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.GAVariation;
import org.moeaframework.core.operator.OnePointCrossover;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.operator.real.PM;
import org.moeaframework.core.variable.EncodingUtils;

import com.opencsv.CSVWriter;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;

import main.java.SCM_TA_V1.Developer;
import main.java.SCM_TA_V1.GA_Problem_Parameter;
import main.java.SCM_TA_V1.Zone;
import main.java.featureTuning.FeatureInitialization;
import main.java.featureTuning.FeatureInitializationV1;
import main.java.featureTuning.FeatureSetV1;
import main.java.featureTuning.Stubs;


public class Driver {
	static Population finalPopulation;
	static HashMap<String, Object> allMaps = new HashMap<String, Object>();
	static Options options;
	static Boolean si = false;
	static Boolean pi = false;
	static List<Double> crossover = Arrays.asList(0.6, 0.7, 0.8, 0.9);
	static List<Double> mutation = Arrays.asList(0.01, 0.02, 0.05, 0.1, 0.15);
	//static List<Integer> developersize = Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18);
	static List<Integer> developersize = Arrays.asList(1, 5, 10, 15, 20, 25);
	static List<Integer> windowssize = Arrays.asList(3);
	static List<Integer> batchSize = Arrays.asList(30);
	static List<Integer> population = Arrays.asList(100, 200, 300);
	static List<Integer> nfe = Arrays.asList(10000, 20000, 50000);
	static List<Double>  alpha = Arrays.asList(1.2);
	static List<Double>  beta = Arrays.asList(5.2);
	
	public static void main(String[] args) throws IOException {
		Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "The expriment is just started!");
		options = new Options();
		setOptions(options);
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "SATA", options );
		//creats commandline parsers
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			 cmd = parser.parse(options, args);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//getting options value
		si = Boolean.parseBoolean(cmd.getOptionValue("si"));
		pi = Boolean.parseBoolean(cmd.getOptionValue("pi"));
		if (si)
			getOptionsValue(cmd);
		//get dataset name 
		System.out.println("Enter the dataset name:");
		Scanner sc = new Scanner(System.in);
		FeatureInitializationV1.datasetName = sc.next();
		if (!si) {
			if (pi) {
				//runMultiObjective();
				FeatureInitializationV1.windowSize = Integer.parseInt(cmd.getOptionValue("ws"));
				FeatureInitializationV1.churnRate = Integer.parseInt(cmd.getOptionValue("ds"));
				GA_Problem_Parameter.batch_size = Integer.parseInt(cmd.getOptionValue("bs"));
				GA_Problem_Parameter.nfe = Integer.parseInt(cmd.getOptionValue("nfe"));
				if (cmd.hasOption("a")) {
					GA_Problem_Parameter.alpha = Double.parseDouble(cmd.getOptionValue("a"));
				}
				if (cmd.hasOption("b")) {
					GA_Problem_Parameter.beta = Double.parseDouble(cmd.getOptionValue("b"));
				}
				for (int p : population) {
					for (double cr : crossover) {
						for (double m : mutation) {
							GA_Problem_Parameter.population = p;
							GA_Problem_Parameter.um_rate = m;
							GA_Problem_Parameter.one_x_rate = cr;
							for(int i = 1; i <= 1; i++) {
								finalPopulation= runSeed(); 		/* call the run for single seed */
								writeResutls(finalPopulation, FeatureInitializationV1.datasetName, i); 		/* write down the results to the csv file */
								//sendResultsToServer(finalPopulation);				/* send the results to the central server */
							}
						}
					}
				}
			}
			else {
				//runMultiObjective();
				GA_Problem_Parameter.population = Integer.parseInt(cmd.getOptionValue("p"));
				GA_Problem_Parameter.um_rate = Double.parseDouble(cmd.getOptionValue("mr"));
				GA_Problem_Parameter.one_x_rate = Double.parseDouble(cmd.getOptionValue("cr"));
				GA_Problem_Parameter.batch_size = Integer.parseInt(cmd.getOptionValue("bs"));
				GA_Problem_Parameter.nfe = Integer.parseInt(cmd.getOptionValue("nfe"));
				/*
				if (cmd.hasOption("a")) {
					GA_Problem_Parameter.alpha = Double.parseDouble(cmd.getOptionValue("a"));
				}
				if (cmd.hasOption("b")) {
					GA_Problem_Parameter.beta = Double.parseDouble(cmd.getOptionValue("b"));
				}
				*/
				for (int w : windowssize) {
					for (int d : developersize) {
						for (int batch : batchSize) {
							for (double a : alpha) {
								for (double b : beta) {
									FeatureInitializationV1.windowSize = w;
									FeatureInitializationV1.churnRate = d;
									GA_Problem_Parameter.batch_size = batch;
									GA_Problem_Parameter.alpha = a;
									GA_Problem_Parameter.beta = b;
									for(int i = 1; i <= 1; i++) {
										finalPopulation= runSeed(); 		/* call the run for single seed */
										writeResutls(finalPopulation, FeatureInitializationV1.datasetName, i); 		/* write down the results to the csv file */
										//sendResultsToServer(finalPopulation);				/* send the results to the central server */
									}
								}
							}
						}
					}
				}
			}
		}	
	}
	
	
	public static Population runSeed() {
		//create object of type "FeatureInitialization"
		FeatureInitialization featureInitializatin=FeatureInitializationV1.getInstance();
		featureInitializatin.initializeAllFeatures();
		
		//fill the temp lists
		Stubs.createTempStateSequence();
		Stubs.fillChurnsSequence();
		
		//Run GA for InitializedfFeatureProblem
		InitializedFeaturesProblem problem = new InitializedFeaturesProblem(5, 1);

        Selection selection = new TournamentSelection(2, 
        								new ParetoDominanceComparator());

        Variation variation = new GAVariation(
                new OnePointCrossover(0.9),
                new PM(0.05, 0.5));

        Initialization initialization = new RandomInitialization(problem, 1);
		AggregateObjectiveComparator comparator=new LinearDominanceComparator();
        
        GeneticAlgorithm GA=new GeneticAlgorithm(problem, comparator, initialization, selection, variation);
        
        //run GA single objective
        while (GA.getNumberOfEvaluations() < 1) {
            GA.step();
        }
        
        NondominatedPopulation result=GA.getResult();
        Population p=GA.getPopulation();
        
        for (Solution solution : result) {
            System.out.println(Arrays.toString(EncodingUtils.getReal(solution)) +
                    " => " + solution.getObjective(0));
        }
        return p;
	}
	
	/*
	public static HashMap<String, Object> runMultiObjective(){
		
		AdaptiveAssignmentPipline adaptive=AdaptiveAssignmentPipline.getInstance();
		HashMap<String, Double> totals=new HashMap<String, Double>();
		HashMap<String, ArrayList<Double>> totalsOverTime=new HashMap<String, ArrayList<Double>>();
		HashMap<Integer, HashMap<Integer, Developer>> devsProfileOverTime=new HashMap<Integer, HashMap<Integer,Developer>>();
		
		try {
			adaptive.run(totals, totalsOverTime, devsProfileOverTime);
		} catch (NoSuchElementException | ClassNotFoundException | IOException | URISyntaxException
				| CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//add the maps to the main map list
		allMaps.put("TCT_adaptive", totals.get("TCT_adaptive"));
		allMaps.put("TCT_static", totals.get("TCT_static"));
		allMaps.put("TID_static", totals.get("TID_static"));
		allMaps.put("TID_adaptive", totals.get("TID_adaptive"));
		allMaps.put("CoT_static", totalsOverTime.get("CoT_static"));
		allMaps.put("CoT_adaptive", totalsOverTime.get("CoT_adaptive"));
		allMaps.put("IDoT_static", totalsOverTime.get("IDoT_static"));
		allMaps.put("IDoT_adaptive", totalsOverTime.get("IDoT_adaptive"));
		allMaps.put("SoT", totalsOverTime.get("SoT"));
		allMaps.put("devsProfile0", devsProfileOverTime.get(0));
		allMaps.put("devsProfile1", devsProfileOverTime.get(1));
		allMaps.put("costPerRound_static", totalsOverTime.get("costPerRound_static"));
		allMaps.put("costPerRound_adaptive", totalsOverTime.get("costPerRound_adaptive"));
		allMaps.put("EoT_adaptive", totalsOverTime.get("EoT_adaptive"));
		
		return null;
	}
	*/
	
	
	
	/**
	 * Write the results into the file according to the dataset name under analysis
	 * all the experiment runs output to a unique location
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public static void writeResutls(Population p, String datasetName, int runNum) throws IOException {
		int numOf = countLines(FeatureInitializationV1.actionProbOverRound);
		String fileNamePart = "";
		if (pi)
		 fileNamePart = String.format("%s%s%s%sW%s_D%s_S%s_P%s_Cr%s_M%s_A%s_B%s", datasetName, File.separator, "PI", File.separator, FeatureInitializationV1.windowSize, FeatureInitializationV1.churnRate,
				GA_Problem_Parameter.batch_size, GA_Problem_Parameter.population, GA_Problem_Parameter.one_x_rate, GA_Problem_Parameter.um_rate,
				GA_Problem_Parameter.alpha, GA_Problem_Parameter.beta);
		else
			fileNamePart = String.format("%s%s%s%sW%s_D%s_S%s_P%s_Cr%s_M%s_A%s_B%s", datasetName, File.separator, "Context", File.separator, FeatureInitializationV1.windowSize, FeatureInitializationV1.churnRate,
					GA_Problem_Parameter.batch_size, GA_Problem_Parameter.population, GA_Problem_Parameter.one_x_rate, GA_Problem_Parameter.um_rate,
					GA_Problem_Parameter.alpha, GA_Problem_Parameter.beta);
		
		File file = new File(System.getProperty("user.dir") + File.separator + "results" + File.separator + "self-adaptive"
				+ File.separator + fileNamePart + "_" + runNum + ".csv");
		File file_actionProbOverTime = new File(System.getProperty("user.dir") + File.separator + "results" + File.separator + "self-adaptive"
				+ File.separator + fileNamePart + "_probOverTime" + ".csv");
		File file_developersProfile_static, file_developersProfile_adaptive;
		//FIXME -- define two file object for bus factor over time for zones
		File file_busFactorZone_static = new File(System.getProperty("user.dir") + File.separator + "results" + File.separator + "self-adaptive"
				+ File.separator + datasetName + File.separator + "BusFactor" + File.separator + "static_zones_" + FeatureInitializationV1.churnRate + ".csv");
		File file_busFactorZone_adaptive = new File(System.getProperty("user.dir") + File.separator + "results" + File.separator + "self-adaptive"
				+ File.separator + datasetName + File.separator + "BusFactor" + File.separator + "adaptive_zones_" + FeatureInitializationV1.churnRate + ".csv");
		PrintWriter pw_devProfile_static, pw_devProfile_adaptive;
		//FIXME -- accordingly, defining two printwriter for bus factor
		PrintWriter pw_BF_static, pw_BF_adaptive;
		
		HashMap<Integer, Developer> devList;
	
		file.getParentFile().mkdir(); 				/* make missed dirs*/
		file_busFactorZone_adaptive.getParentFile().mkdir();
		file_busFactorZone_static.getParentFile().mkdir();
		PrintWriter printWriter = new PrintWriter(file);
		PrintWriter printWriter_probOverTime = new PrintWriter(file_actionProbOverTime);
		pw_BF_static = new PrintWriter(file_busFactorZone_static);
		pw_BF_adaptive = new PrintWriter(file_busFactorZone_adaptive);
		CSVWriter csvWriter = new CSVWriter(printWriter);
		//csv wiriter for zones' bus factor
		CSVWriter csvWriter_BF_zone_static = new CSVWriter(pw_BF_static);
		CSVWriter csvWriter_BF_zone_adaptive = new CSVWriter(pw_BF_adaptive);
		//CSVWriter csvWriter_probOverTime=new CSVWriter(printWriter_probOverTime);
		String[] csvFileOutputHeader= {"solution","totalCostID", "totalCostStatic", "totalIDStatic", "totalIDID", "CoT_static", "CoT_adaptive", "IDoT_static", "IDoT_adaptive",
					"SoT", "costPerRound_static" , "costPerRound_adaptive", "idPerRound_static", "idPerRound_adaptive", "EoT_static", "EoT_adaptive", "ExoTperRound_adaptive",
					"actionProbVector", "churnRate", "actions", "retainedKnowledge_static", "retainedKnowledge_adaptive", "lostKnowledge_static",
					"lostKnowledge_adaptive", "busFactor_static", "busFactor_adaptive"};
		String[] csvWriter_BF_zone_cols = ArrayUtils.addAll(new String[]{"runNumber"}, GA_Problem_Parameter.header_bus.toArray(new String[0]));
		
		//String[] csvFileOutputHeader_probOverTime= {"cost","diffusion"};
		csvWriter.writeNext(csvFileOutputHeader);		//write the header of the csv file
		csvWriter_BF_zone_static.writeNext(csvWriter_BF_zone_cols);
		csvWriter_BF_zone_adaptive.writeNext(csvWriter_BF_zone_cols);
		//csvWriter_probOverTime.writeNext(csvFileOutputHeader_probOverTime);		//write the header of the csv file to store prob over time
		Solution tempSolution;
		
		for(int i = 0; i < p.size(); i++) {
			tempSolution = p.get(i);
			csvWriter.writeNext(new String[] {Arrays.toString(EncodingUtils.getInt(tempSolution)),
												String.format("%.2f", tempSolution.getObjective(0)),
												String.format("%.2f", tempSolution.getAttribute("TCT_static")),
												String.format("%.2f", tempSolution.getAttribute("TID_static")),
												String.format("%.2f", tempSolution.getAttribute("TID_adaptive")),
												((ArrayList<Double>)tempSolution.getAttribute("CoT_static")).stream().map(x -> String.format("%.2f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("CoT_adaptive")).stream().map(x -> String.format("%.2f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("IDoT_static")).stream().map(x -> String.format("%.4f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("IDoT_adaptive")).stream().map(x -> String.format("%.4f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("SoT")).stream().map(x -> String.format("%.0f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("costPerRound_static")).stream().map(x -> String.format("%.2f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("costPerRound_adaptive")).stream().map(x -> String.format("%.2f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("idPerRound_static")).stream().map(x -> String.format("%.2f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("idPerRound_adaptive")).stream().map(x -> String.format("%.2f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("EoT_static")).stream().map(x -> String.format("%.4f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("EoT_adaptive")).stream().map(x -> String.format("%.4f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("ExoTperRound_adaptive")).stream().map(x -> String.format("%.4f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("actionProbVector")).stream().map(x -> String.format("%.2f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("churnRate")).stream().map(x -> String.format("%.0f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("actions")).stream().map(x -> x == 1.0 ? "Cost" : "Diffuion").collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("retainedKnowledge_static")).stream().map(x -> String.format("%.4f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("retainedKnowledge_adaptive")).stream().map(x -> String.format("%.4f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("lostKnowledge_static")).stream().map(x -> String.format("%.4f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("lostKnowledge_adaptive")).stream().map(x -> String.format("%.4f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("busFactor_static")).stream().map(x -> String.format("%.0f", x)).collect(Collectors.toList()).toString(),
												((ArrayList<Double>)tempSolution.getAttribute("busFactor_adaptive")).stream().map(x -> String.format("%.0f", x)).collect(Collectors.toList()).toString()
												});
			//log devs status over time
			int devCount=0;
			for(int j = 0; j < GA_Problem_Parameter.numberOfTimesMakingProfileComparison; j++) {
				devCount++;
				
				file_developersProfile_static = new File(System.getProperty("user.dir")+File.separator+"results"+ File.separator+ "self-adaptive"
						+File.separator+ "devProfiles"+ File.separator + i+"_static_"+j+".txt");
				file_developersProfile_adaptive = new File(System.getProperty("user.dir")+File.separator+"results"+ File.separator+ "self-adaptive"
						+File.separator+ "devProfiles"+ File.separator + i+"_adaptive_"+j+".txt");
				file_developersProfile_static.getParentFile().mkdir();
				file_developersProfile_adaptive.getParentFile().mkdir();
				pw_devProfile_static = new PrintWriter(file_developersProfile_static);
				pw_devProfile_adaptive = new PrintWriter(file_developersProfile_adaptive);
				

				devList = (HashMap<Integer, Developer>)tempSolution.getAttribute("devsProfile"+j);
				
				//create the header for the files
				pw_devProfile_static.append("Dev#");
				pw_devProfile_adaptive.append("Dev#");
				int size = devList.entrySet().size();
				for(Map.Entry<Zone, Double> entry:devList.get(1).getDZone_Coefficient().entrySet()) {
					pw_devProfile_static.append("\t"+entry.getKey().zName);
					pw_devProfile_adaptive.append("\t"+entry.getKey().zName);
				}
				
				//add new line
				pw_devProfile_static.append("\n");
				pw_devProfile_adaptive.append("\n");
				
				//write the devs' profile
				String line_static, line_adaptive;
				for(Map.Entry<Integer, Developer> dev:devList.entrySet()) {
					line_static="";
					line_adaptive="";
					line_static+=dev.getKey()+"\t";
					line_adaptive+=dev.getKey()+"\t";
					for(Map.Entry<Zone, Double> zoneItem:dev.getValue().getDZone_Coefficient_static().entrySet()) {
						line_static+=dev.getValue().getDZone_Coefficient_static().get(zoneItem.getKey())+"\t";
						line_adaptive+=dev.getValue().getDZone_Coefficient().get(zoneItem.getKey())+"\t";
					}
					
					//trim to remove the unwanted tab and then add new line
					line_static.trim();
					line_adaptive.trim();
					if(devCount<GA_Problem_Parameter.developers_all.size()) {
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
				
				System.out.println("It is done!");
				
			}
			
			//FIXME 
			/* iterate over approach to write the bus factor of the zones for each of which*/
			for (Approach approach : Approach.values()) {
				//write to predefined csv
				switch (approach) {
					case STATIC:
						for (Map.Entry<Integer, HashMap<Approach, List<String>>> item : ((HashMap<Integer, HashMap<Approach, List<String>>>) tempSolution.getAttribute("busFactor_zones")).entrySet()) {
							csvWriter_BF_zone_static.writeNext(ArrayUtils.addAll(new String[] {item.getKey().toString()}, item.getValue().get(approach).toArray(new String[0])));
						}
						break;
					case ADAPTIVE:
						for (Map.Entry<Integer, HashMap<Approach, List<String>>> item : ((HashMap<Integer, HashMap<Approach, List<String>>>) tempSolution.getAttribute("busFactor_zones")).entrySet()) {
							csvWriter_BF_zone_adaptive.writeNext(ArrayUtils.addAll(new String[] {item.getKey().toString()}, item.getValue().get(approach).toArray(new String[0])));
						}
					break;
	
					default:
						break;
				}
			}
			
			
			
			//decoding the solution
			System.out.println("TCR");
			System.out.println(FeatureInitializationV1.getInstance().getTCR().get((EncodingUtils.getInt(tempSolution))[2]));
			System.out.println("EM");
			System.out.println(Arrays.deepToString(FeatureInitializationV1.getInstance().getEm().get((EncodingUtils.getInt(tempSolution))[3])));
			System.out.println("TM");
			System.out.println(Arrays.deepToString(FeatureInitializationV1.getInstance().getTm().get((EncodingUtils.getInt(tempSolution))[4])));
			
		}
		
		
		
		//log probs of actions over time
		printWriter_probOverTime.write(FeatureInitializationV1.actionProbOverRound);
		
		FeatureInitializationV1.actionProbOverRound = "";
		//logging the end of running
		Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "Just finished the sample run");
		
		
		//close the writers
		csvWriter.close();
		csvWriter_BF_zone_adaptive.close();
		csvWriter_BF_zone_static.close();
		printWriter.close();
		printWriter_probOverTime.close();
	}
	/**
	 * Sending the results to the server over the network--- a service over in the server update the results set  
	 * @param p
	 */
	public static void sendResultsToServer(Population p) {

		
		
	}

	private static int countLines(String str){
	   String[] lines = str.split("\r\n|\r|\n");
	   return  lines.length;
	}
	
	private static void setOptions(Options option) {
		options.addOption("si","settings included", true, "To weather include or exclude individual settings");
		options.addOption("pi","parameters included", true, "Weather genetic parameters should be tested or not!");
		options.addOption("ds","developer-size", true, "The number of developers who will leave team at some point of times");
		options.addOption("ws", "window-size", true, "The window size used to get feedback");
		options.addOption("bs", "batch-size", true, "Number of bugs included in a round");
		options.addOption("p", "population", true, "The population number set for GA-based algo");
		options.addOption("cr", "crossover-rate", true, "crossover rate");
		options.addOption("mr","mutaion-rate", true, "mutation rate");
		options.addOption("nfe","number-of-fitness-evaluation", true, "number of fitness evaluation");
		options.addOption("a","alpha", true, "coefficent of interanl knowledge flow");
		options.addOption("b","beta", true, "coefficent of external knowledge flow");
	}
	
	private static void getOptionsValue(CommandLine cmd) {
		//getting options' value
		si = Boolean.parseBoolean(cmd.getOptionValue("si"));
		FeatureInitializationV1.windowSize = Integer.parseInt(cmd.getOptionValue("ws"));
		FeatureInitializationV1.churnRate = Integer.parseInt(cmd.getOptionValue("ds"));
		GA_Problem_Parameter.batch_size = Integer.parseInt(cmd.getOptionValue("bs"));
		GA_Problem_Parameter.population = Integer.parseInt(cmd.getOptionValue("p"));
		GA_Problem_Parameter.nfe = Integer.parseInt(cmd.getOptionValue("nfe"));
		GA_Problem_Parameter.um_rate = Double.parseDouble(cmd.getOptionValue("mr"));
		GA_Problem_Parameter.one_x_rate = Double.parseDouble(cmd.getOptionValue("cr"));
		if (cmd.hasOption("a")) {
			GA_Problem_Parameter.alpha = Double.parseDouble(cmd.getOptionValue("a"));
		}
		if (cmd.hasOption("b")) {
			GA_Problem_Parameter.beta = Double.parseDouble(cmd.getOptionValue("b"));
		}
	}
}
