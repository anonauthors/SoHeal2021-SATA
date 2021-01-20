package main.java.SCM_TA_V1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

import main.java.context.Environment_s1;

public class GA_Problem_Parameter {
	static int Num_of_variables;
	static int Num_of_functions_Single = 1;
	static int Num_of_functions_Multi = 2;
	static int Num_of_objectives = 1;
	static int Num_of_Active_Developers;
	static int Num_of_Bugs;
	static int Num_of_Zones;
	// set GA parameters
	public static int population;
	public static int nfe;
	public static int batch_size;
	static double sbx_rate = 0.0;
	public static double one_x_rate = 9.0;
	static double sbx_distribution_index;
	static double pm_rate = 0.50;
	public static double um_rate = 0.05;
	static double pm_distribution_index;
	static double delayPenaltyCostRate = 0.2;
	public static int upperDevId;
	public static int lowerDevId = 1;
	public static double alpha = 1.2;
	public static double beta = 3.8; 
	//
	static Bug[] bugs;
	public static HashMap<Integer, Developer> developers = null;
	public static HashMap<Integer, Developer> developers_all = null;
	public static ArrayList<Integer> devListId = new ArrayList<Integer>();
	public static int devListIdSize;
	public static final int startDevId = 1;
	public static final int endDevId = 20;
	public static int flag = 0;
	private static DAGEdge EClass = new DAGEdge();
	public static double currentTimePeriodStartTime = 0;
	public static ArrayList<Integer> DevList = new ArrayList<Integer>();
	public static ArrayList<Integer> DevList_forRandom = new ArrayList<Integer>();
	public static ArrayList<Integer> DevList_forAssignment = new ArrayList<Integer>();
	public static ArrayList<ArrayList<Bug>> candidateSchedulings = null;
	public static HashMap<Integer, ArrayList<Bug>> selectedSchedules = new HashMap<Integer, ArrayList<Bug>>();

	// generate DAG for arrival Bugs
	public static DirectedAcyclicGraph<Bug, DefaultEdge> DEP;
	public static TopologicalOrderIterator<Bug, DefaultEdge> tso_competenceMulti2;
	// public static TopologicalOrderIterator<Bug,DefaultEdge> tso_ID;
	public static TopologicalOrderIterator<Bug, DefaultEdge> tso_NormalAssignment;
	public static TopologicalOrderIterator<Bug, DefaultEdge> tso_IDAssignment;
	public static ArrayList<Bug> tasks = new ArrayList<Bug>();
	public static ArrayList<Bug> shuffledTasks;
	public static ArrayList<DefaultEdge> pEdges;
	public static DefaultDirectedGraph<Bug, DefaultEdge> DDG;
	public static DefaultDirectedGraph<Bug, DefaultEdge> DDG_1;
	public static HashMap<String, Double> priorities = new HashMap<String, Double>();
	public static TopologicalOrderIterator<Bug, DefaultEdge> tso_static;
	public static TopologicalOrderIterator<Bug, DefaultEdge> tso_adaptive;
	public static TopologicalOrderIterator<Zone, DefaultEdge> tso_Zone;
	public static ArrayList<HashMap<Integer, Bug>> listOfSubBugs = new ArrayList<HashMap<Integer, Bug>>();
	public static int numberOfTimesMakingProfileComparison = 2;
	public static int earlyDevListSize = 7;
	public static HashMap<Zone, Double> knowledgeSoFar = new HashMap<Zone, Double>();
	public static Set<Zone> allZones = new HashSet<Zone>();
	public static double totalKnowledge = 0;
	public static double knowledgeHit_static = 0;
	public static double knowledgeLoss_static = 0; /* in effect, this is: "1- knowledgeHit" */
	public static double knowledgeHit_adaptive = 0;
	public static double knowledgeLoss_adaptive = 0; /* in effect, this is: "1- knowledgeHit" */
	public static List<String> header_bus = new ArrayList<String>();
	
	// Parameter for new solution in ID approach
	// ArrayList<DefaultEdge> pEdges=new ArrayList<DefaultEdge>();
	public static int setNum_of_Variables(Bug[] bugs) {
		Num_of_variables = 0;
		for (int i = 0; i < bugs.length; i++) {
			Num_of_variables += bugs[i].BZone_Coefficient.size();
		}
		return Num_of_variables;
	}

	public static void initializeDeveloperPool() {
		for (int i = 0; i < 3; i++) {
			for (Integer dev : DevList)
				DevList_forAssignment.add(dev);
		}
	}

	public static void setDevelopersIDForRandom() {
		DevList_forRandom.clear();
		for (Map.Entry<Integer, Developer> dev : developers.entrySet())
			DevList_forRandom.add(dev.getKey());
	}

	public static int getRandomDevId() {
		Random rg = new Random();
		int index = rg.nextInt(DevList_forRandom.size());
		return DevList_forRandom.get(index);
	}

	public static int getDevId() {
		if (DevList_forAssignment.size() > 0) {
			Random rg = new Random();
			int index = rg.nextInt(DevList_forAssignment.size());
			int devId = DevList_forAssignment.get(index);
			DevList_forAssignment.remove(index);
			return devId;
		} else {
			return -1;
		}
	}

	public static ArrayList<ArrayList<Bug>> getValidSchedulings(DirectedAcyclicGraph<Bug, DefaultEdge> DAG) {
		// all valid schedules(without any loop)
		// ArrayList<ArrayList<DefaultEdge>> validSchedulings=new
		// ArrayList<ArrayList<DefaultEdge>>();
		DDG = new DefaultDirectedGraph<Bug, DefaultEdge>(DefaultEdge.class);
		DDG = convertToDirectedGraph(DAG, DDG);
		ArrayList<DefaultEdge> potentilEdges = new ArrayList<DefaultEdge>();

		System.out.println();
		// generate all valid schedules
		for (Bug b1 : DAG.vertexSet()) {
			for (Bug b2 : DAG.vertexSet()) {
				// System.out.print(b1.ID+">>>>"+b2.ID+"....."+CI.pathExists(b1, b2)+",,,");
				if (b1.ID != b2.ID && !(DAG.containsEdge(b1, b2) || DAG.containsEdge(b2, b1))) {
					DDG.addEdge(b1, b2);
					// DDG.addEdge(b2,b1);
					potentilEdges.add(DDG.getEdge(b1, b2));
				}
			}
		}
		DDG_1 = (DefaultDirectedGraph<Bug, DefaultEdge>) DDG.clone();
		System.out.println();
		pEdges = potentilEdges;

		/*
		 * System.out.println(); ConnectivityInspector<Bug, DefaultEdge> GCI=new
		 * ConnectivityInspector<Bug, DefaultEdge>(DAG); List<Set<Bug>> components =
		 * GCI.connectedSets(); ArrayList<AsSubgraph<Bug, DefaultEdge>> subgraphs=new
		 * ArrayList<AsSubgraph<Bug,DefaultEdge>>(); for(Set<Bug> s:components){
		 * subgraphs.add(new AsSubgraph(DAG, s)); }
		 */
		ArrayList<ArrayList<Bug>> validSchedulings = new ArrayList<ArrayList<Bug>>();
		for (int k = 0; k < 500; k++) {
			ArrayList<Bug> va = new ArrayList<Bug>();
			ArrayList<Bug> travesredNodes = new ArrayList<Bug>();
			Random randomGenerator = new Random();
			int rIndex = 0;
			for (Bug b : DAG.vertexSet()) {
				if (DAG.inDegreeOf(b) == 0) {
					travesredNodes.add(b);
				}
			}
			while (!travesredNodes.isEmpty()) {
				rIndex = randomGenerator.nextInt(travesredNodes.size());
				va.add(travesredNodes.get(rIndex));
				Set<DefaultEdge> edges = DAG.outgoingEdgesOf(travesredNodes.get(rIndex));
				travesredNodes.remove(travesredNodes.get(rIndex));
				for (DefaultEdge d : edges) {
					travesredNodes.add(DAG.getEdgeTarget(d));
				}
			}
			validSchedulings.add(va);
		}

		/*
		 * System.out.println("comp: "+components.size()); for(int i=0;i<10;i++){
		 * Collections.shuffle(subgraphs); for(AsSubgraph<Bug, DefaultEdge>
		 * g:subgraphs){ TopologicalOrderIterator<Bug, DefaultEdge> TO=new
		 * TopologicalOrderIterator(g); while(TO.hasNext()){ va.add(TO.next()); } }
		 * 
		 * }
		 */

		/*
		 * for(ArrayList<Bug> ab:validSchedulings){ for(Bug b:ab){
		 * System.out.print(b.ID+"---"); } System.out.println(); }
		 */

		return validSchedulings;
	}

	public static void update(ArrayList<DefaultEdge> edges, DefaultEdge e, DefaultDirectedGraph<Bug, DefaultEdge> DDG,
			DefaultDirectedGraph<Bug, DefaultEdge> DDG_2, ArrayList<DefaultEdge> verifiedEdges) {
		CycleDetector<Bug, DefaultEdge> CD = new CycleDetector<Bug, DefaultEdge>(DDG_2);
		ArrayList<DefaultEdge> edges_2 = (ArrayList<DefaultEdge>) edges.clone();
		try {
			DefaultEdge e_reverse = DDG.getEdge(DDG.getEdgeTarget(e), DDG.getEdgeSource(e));
			edges.remove(e_reverse);
			edges_2.remove(e_reverse);

			// DDG_2.removeEdge(DDG_2.getEdgeTarget(e), DDG_2.getEdgeSource(e));
		} catch (Exception e2) {
			// DDG_2.addEdge(, targetVertex)
			e2.printStackTrace();
		}
		for (DefaultEdge ed : edges_2) {
			DDG_2.addEdge(DDG.getEdgeSource(ed), DDG.getEdgeTarget(ed));
			// verifiedEdges.add(ed);
			// if(DDG_2.getEdgeSource(ed).ID!=DDG_2.getEdgeSource(e).ID &&
			// DDG_2.getEdgeTarget(ed).ID!=DDG_2.getEdgeTarget(e).ID)
			// {
			try {
				// if(CI.pathExists(DDG_2.getEdgeSource(ed), DDG_2.getEdgeTarget(ed)) &&
				// CI.pathExists(DDG_2.getEdgeTarget(ed), DDG_2.getEdgeSource(ed))){
				if (CD.detectCycles()) {
					// System.out.println(CD.detectCycles());
					edges.remove(DDG_2.getEdge(DDG_2.getEdgeTarget(ed), DDG_2.getEdgeSource(ed)));
					DDG_2.removeEdge(DDG.getEdgeSource(ed), DDG.getEdgeTarget(ed));
					// verifiedEdges.remove(ed);
				}
			} catch (Exception e2) {
				System.out.println("error occured");
				e2.printStackTrace();
			}

			// }
		}
		// System.out.println(edges.size());
	}

	public static DirectedAcyclicGraph<Bug, DefaultEdge> getDAGModel(Bug[] bugs) {

		DirectedAcyclicGraph<Bug, DefaultEdge> dag = new DirectedAcyclicGraph<Bug, DefaultEdge>(DefaultEdge.class);

		for (int k = 0; k < bugs.length; k++) {
			dag.addVertex(bugs[k]);
		}
		for (int i = 0; i < bugs.length; i++) {
			if (bugs[i].DB.size() > 0) {
				for (Bug b : bugs[i].DB) {
					if (dag.edgeSet().size() < 1 && dag.containsVertex(bugs[i])) {
						try {
							dag.addEdge(b, bugs[i]);
						} catch (Exception ex) {
							if (b == null)
								System.out.println("f");
							else if (bugs[i] == null)
								System.out.println("f-f");
							ex.printStackTrace();
						}
						// System.out.println(dag.edgeSet());
					} else if (!dag.containsEdge(bugs[i], b)) {
						if (b != null && bugs[i] != null)
							dag.addEdge(b, bugs[i]);
						// System.out.println(dag.edgeSet());
					}
				}
			} else {
				dag.addVertex(bugs[i]);
			}
		}
		return dag;
	}

	public static ArrayList<DefaultEdge> getEdges(ArrayList<Bug> tasks) {

		return new ArrayList<DefaultEdge>();
	}

	public static TopologicalOrderIterator<Bug, DefaultEdge> getTopologicalSorted(
			DirectedAcyclicGraph<Bug, DefaultEdge> dag) {

		TopologicalOrderIterator<Bug, DefaultEdge> tso = new TopologicalOrderIterator<Bug, DefaultEdge>(dag);

		return tso;
	}

	public static ArrayList<DirectedAcyclicGraph<Bug, DefaultEdge>> getReScheduledGraphs(
			DirectedAcyclicGraph<Bug, DefaultEdge> DAG, ArrayList<ArrayList<DefaultEdge>> validSchedulings) {
		ArrayList<DirectedAcyclicGraph<Bug, DefaultEdge>> schedulings = new ArrayList<DirectedAcyclicGraph<Bug, DefaultEdge>>();
		for (ArrayList<DefaultEdge> candidateSchedule : validSchedulings) {
			@SuppressWarnings("unchecked")
			DirectedAcyclicGraph<Bug, DefaultEdge> ReScheduledDAG = (DirectedAcyclicGraph<Bug, DefaultEdge>) DAG
					.clone();
			for (DefaultEdge edge : candidateSchedule) {
				ReScheduledDAG.addEdge(DAG.getEdgeSource(edge), DAG.getEdgeTarget(edge));
			}
			schedulings.add(ReScheduledDAG);
		}
		return schedulings;
	}

	public static void resetParameters(DirectedAcyclicGraph<Bug, DefaultEdge> DEP, Solution s,
			HashMap<Integer, Developer> developers) {
		for (Bug b : DEP.vertexSet()) {
			b.startTime_evaluate = 0.0;
			b.endTime_evaluate = 0.0;
			for (Zone z : b.Zone_DEP.vertexSet()) {
				z.zoneStartTime_evaluate = 0.0;
				z.zoneEndTime_evaluate = 0.0;
			}
		}
		for (Entry<Integer, Developer> d : developers.entrySet()) {
			d.getValue().developerNextAvailableHour = 0.0;
		}
	}

	public static void assignZoneDev(ArrayList<Triplet<Bug, Zone, Integer>> zoneAssignee, List<Bug> tasks, Solution s) {
		int[] listOfSolutionsID = EncodingUtils.getInt(s);
		int variableIndex = 0;
		for (Bug b : tasks) {
			for (Zone zone : b.Zone_DEP) {
				zoneAssignee.add(new Triplet<Bug, Zone, Integer>(b, zone,
						GA_Problem_Parameter.devListId.get(listOfSolutionsID[variableIndex])));
				variableIndex++;
			}
		}
	}

	public static void setCandidateSchedulings(ArrayList<ArrayList<Bug>> validSchedulings) {
		candidateSchedulings = validSchedulings;
		/*
		 * for(DirectedAcyclicGraph<Bug, DefaultEdge> schedule:validSchedulings){
		 * candidateSchedulings.add(getTopologicalSorted(schedule)); }
		 */

	}

	public static DefaultDirectedGraph<Bug, DefaultEdge> convertToDirectedGraph(
			DirectedAcyclicGraph<Bug, DefaultEdge> DAG, DefaultDirectedGraph<Bug, DefaultEdge> DDG) {

		if (!DDG.vertexSet().isEmpty()) {
			for (DefaultEdge d : DDG.edgeSet()) {
				DDG.removeEdge(d);
			}
			for (Bug b : DDG.vertexSet()) {
				DDG.removeVertex(b);
			}
		}

		// System.out.println("size of ddg"+DDG.edgeSet().size());
		for (Bug b : DAG.vertexSet()) {
			DDG.addVertex(b);
		}
		for (DefaultEdge d : DAG.edgeSet()) {
			DDG.addEdge(DAG.getEdgeSource(d), DAG.getEdgeTarget(d));
		}
		return DDG;
	}

	public static void generateModelofBugs() {
		// generate DAG for arrival Bugs
		DEP = GA_Problem_Parameter.getDAGModel(bugs);
		// topologically sort the graph
		tso_competenceMulti2 = GA_Problem_Parameter.getTopologicalSorted(DEP);
		tso_static = GA_Problem_Parameter.getTopologicalSorted(DEP);
		tso_adaptive = GA_Problem_Parameter.getTopologicalSorted(DEP);
		tso_NormalAssignment = GA_Problem_Parameter.getTopologicalSorted(DEP);
		tso_IDAssignment = GA_Problem_Parameter.getTopologicalSorted(DEP);
	}

	public static void candidateSolutonGeneration() {
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP = GA_Problem_Parameter.getDAGModel(GA_Problem_Parameter.bugs);
		// generate all the candidate schedules
		@SuppressWarnings("unchecked")
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_evaluation_scheduling = (DirectedAcyclicGraph<Bug, DefaultEdge>) DEP
				.clone();
		ArrayList<ArrayList<Bug>> validSchedulings = GA_Problem_Parameter
				.getValidSchedulings(DEP_evaluation_scheduling);
		GA_Problem_Parameter.setCandidateSchedulings(validSchedulings);
	}

	/**
	 * update the schedules for each generated offspring produced by crossover
	 * operator
	 **/
	public static Solution setValidSchdule(Solution solution, HashMap<Integer, Bug> varToBug) {
		ArrayList<Integer> assignment = new ArrayList<Integer>();
		ArrayList<Integer> schedules = new ArrayList<Integer>();
		DefaultDirectedGraph<Bug, DefaultEdge> DEP_scheduling = new DefaultDirectedGraph<Bug, DefaultEdge>(
				DefaultEdge.class);
		DEP_scheduling = GA_Problem_Parameter.convertToDirectedGraph(GA_Problem_Parameter.DEP, DEP_scheduling);
		int[] solu = EncodingUtils.getInt(solution);
		for (int i = 0; i < solu.length; i++) {
			if (solu[i] != -100) {
				assignment.add(solu[i]);
			} else {
				break;
			}
		}
		for (int i = assignment.size(); i < solu.length - 1; i++) {
			schedules.add(solu[i]);
		}
		int m, n, p, q;
		int[] indexes = new int[2];
		AllDirectedPaths<Bug, DefaultEdge> paths = new AllDirectedPaths<Bug, DefaultEdge>(DEP);
		for (int i = 0; i < GA_Problem_Parameter.tasks.size() - 1; i++) {
			indexes = getIndex(i);
			m = indexes[0];
			n = indexes[1];
			for (int j = i + 1; j < GA_Problem_Parameter.tasks.size(); j++) {
				indexes = getIndex(j);
				p = indexes[0];
				q = indexes[1];
				if (compareSubtasksAssignee(m, n, p, q, assignment)) {
					try {
						if (paths.getAllPaths(varToBug.get(i), varToBug.get(j), true, 1000).isEmpty()
								&& paths.getAllPaths(varToBug.get(j), varToBug.get(i), true, 1000).isEmpty()) {
							int t = -1;
							try {
								t = GA_Problem_Parameter.pEdges
										.indexOf(GA_Problem_Parameter.DDG_1.getEdge(varToBug.get(i), varToBug.get(j)));
								if (t < 0) {
									t = GA_Problem_Parameter.pEdges.indexOf(
											GA_Problem_Parameter.DDG_1.getEdge(varToBug.get(j), varToBug.get(i)));
								}
								DEP_scheduling.addEdge(
										GA_Problem_Parameter.DDG.getEdgeSource(GA_Problem_Parameter.pEdges.get(t)),
										GA_Problem_Parameter.DDG.getEdgeTarget(GA_Problem_Parameter.pEdges.get(t)));
							} catch (Exception ex) {

							}
							if (!new CycleDetector<Bug, DefaultEdge>(DEP_scheduling).detectCycles())
								schedules.set(t, 1);
							else
								DEP_scheduling.removeEdge(
										GA_Problem_Parameter.DDG.getEdgeSource(GA_Problem_Parameter.pEdges.get(t)),
										GA_Problem_Parameter.DDG.getEdgeTarget(GA_Problem_Parameter.pEdges.get(t)));
						}
					} catch (IllegalArgumentException e) {
						// TODO: handle exception
					}
				}
			}
		}
		int[] temp = new int[schedules.size()];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = schedules.get(i);
		}
		int o = temp.length;

		/*
		 * for(int i=assignment.size()+1;i<solution.getNumberOfVariables();i++){ int
		 * t=temp[i-(assignment.size()+1)]; solution.setVariable(i,
		 * EncodingUtils.newInt(t,t)); }
		 */
		int[] soluw = EncodingUtils.getInt(solution);
		return solution;
	}

	/** get index of the subtasks' assignees need to be compared **/
	public static int[] getIndex(int index) {
		int[] indexes = new int[2];
		int sIndex = 0;
		for (int i = 0; i < index; i++) {
			sIndex += GA_Problem_Parameter.tasks.get(i).BZone_Coefficient.size();
		}
		indexes[0] = sIndex;
		indexes[1] = sIndex + GA_Problem_Parameter.tasks.get(index).BZone_Coefficient.size();
		return indexes;
	}

	/** compare subtasks to find the potential links **/
	public static Boolean compareSubtasksAssignee(int i, int j, int p, int k, ArrayList<Integer> assignment) {
		Boolean b = false;
		for (int r = i; r < j; r++) {
			for (int s = p; s < k; s++)
				if (assignment.get(r) == assignment.get(s))
					b = true;
		}

		return b;
	}

	/**
	 * assign the each bug an index of the associated variable in the encoded
	 * solution
	 **/
	public static HashMap<Integer, Bug> getVarToBug() {
		int index = 0;
		HashMap<Integer, Bug> varToBug = new HashMap<Integer, Bug>();
		for (Bug b : GA_Problem_Parameter.tasks) {
			varToBug.put(index, b);
			index++;
		}
		return varToBug;
	}

	/** set arrival task **/
	public static void setArrivalTasks() {
		GA_Problem_Parameter.tasks.clear();
		while (tso_static.hasNext()) {
			Bug b = tso_static.next();
			GA_Problem_Parameter.tasks.add(b);
		}
		while (tso_adaptive.hasNext()) {
			Bug b = tso_adaptive.next();
			GA_Problem_Parameter.tasks.add(b);
		}
	}

	public static void createPriorityTable() {
		priorities.put("P1", 0.9);
		priorities.put("P2", 0.6);
		priorities.put("P3", 0.3);
	}

	public static void pruneList(HashMap<Integer, Bug> tasks_prune) {
		if (tasks_prune.size() > 40) {
			int _size = (tasks_prune.size() * 3) / 4;
			System.out.println(tasks_prune.size() + "***");
			ArrayList<Integer> bugsID = new ArrayList<Integer>();
			for (Integer ID : tasks_prune.keySet()) {
				bugsID.add(ID);
			}
			for (int i = 0; i < _size; i++) {
				int ran = new Random().nextInt(bugsID.size());
				tasks_prune.remove(bugsID.get(ran));
				bugsID.remove(ran);
			}

			System.out.println(tasks_prune.size() + "///");
		}
	}

	public static void pruneDevList(HashMap<Integer, Developer> devs_prune) {
		int _size = devs_prune.size() / 2;
		System.out.println(devs_prune.size() + "***devs");
		ArrayList<Integer> devsID = new ArrayList<Integer>();
		for (Integer ID : devs_prune.keySet()) {
			devsID.add(ID);
		}
		for (int i = 0; i < _size; i++) {
			int ran = new Random().nextInt(devsID.size());
			devs_prune.remove(devsID.get(ran));
			devsID.remove(ran);
		}

		System.out.println(devs_prune.size() + "///devs");
	}

	/**
	 * cut portion percent of devs--add ids to devListId-- keep them in a list for
	 * attachment in future-- set a fixed size of devs ready to be
	 * assigned--GA_Problem_parameters.developers is divided to 2 different sets
	 * different set
	 * 
	 * @param devs_prune
	 * @param devs
	 * @param portion
	 */
	public static void pruneDevList(HashMap<Integer, Developer> devs_prune, ArrayList<Ranking<Developer, Double>> devs,
			int portion) {
		int _size = devs_prune.size() - (int) (devs_prune.size() * portion) / 100;
		earlyDevListSize = _size;
		GA_Problem_Parameter.devListId.clear();
		System.out.println(devs_prune.size() + "***devs");
		int i = 1;
		for (Ranking<Developer, Double> r : devs) {
			if (i <= _size) {
				devs_prune.remove(r.getEntity().getID());
				GA_Problem_Parameter.devListId.add(r.getEntity().getID());
			}
			i++;
		}
		for (Map.Entry<Integer, Developer> d : devs_prune.entrySet())
			Environment_s1.readyForAttachment.add(d.getKey());
		// set dev fixed size
		GA_Problem_Parameter.devListIdSize = GA_Problem_Parameter.devListId.size();
		System.out.println(devs_prune.size() + "///num of devs which are ready for attachment");
	}

	/**
	 * the method to randomly cut portion of developers
	 * 
	 * @param portion portion of developers will be removed in percentage
	 */
	public static void cutDevs(double portion) {
		System.out.println("Devs size-----------------------" + developers.size());
		System.out.println("portion--------" + portion);
		int length = (int) (developers.size() * (portion / 100));
		System.out.println("Devs to be cut-----------------------" + length);
		Random r = new Random();
		int rand = 0;
		for (int i = 0; i < length; i++) {
			rand = r.nextInt(developers.size());
			if (developers.containsKey(rand))
				developers.remove(rand);
		}
		System.out.println("Devs after being cut-----------------------" + developers.size());
	}

	/**
	 * removing some tasks in random from the bugs(stored in a hashmap structure)
	 * 
	 * @param portion in percentage reveals how many bugs should be ignored at the
	 *                first of period of time
	 */
	public static void cutTasks(double portion, HashMap<Integer, Bug> bugs) {
		System.out.println("Tasks size----------------" + bugs.entrySet().size());
		System.out.println("portion--------" + portion);
		int length = (int) (bugs.size() * (portion / 100));
		System.out.println("Tasks to be cut----------------" + length);
		Random r = new Random();
		List<Integer> keys;
		int rand = 0;
		for (int i = 0; i < length; i++) {
			keys = bugs.keySet().stream().collect(Collectors.toList());
			rand = r.nextInt(keys.size());
			bugs.remove(keys.get(rand));
		}
		System.out.println("Tasks after being cut----------------" + bugs.entrySet().size());
	}

	public static Map.Entry<Integer, Developer> getDev(Integer i) {
		Map.Entry<Integer, Developer> developer = null;
		for (Map.Entry<Integer, Developer> dev : GA_Problem_Parameter.developers_all.entrySet()) {
			if (GA_Problem_Parameter.developers_all.containsKey(i) && dev.getKey() == i) {
				developer = dev;
				break;
			} else
				developer = null;
		}
		return developer;
	}

	/**
	 * The method provides sublists of bugs intended to get assigned in a batch
	 * manner
	 * 
	 * @param bugList holds the list
	 */
	public static void splitBugList(HashMap<Integer, Bug> bugList) {
		int i = 0;
		int numOfBugs = batch_size;
		HashMap<Integer, Bug> b = null;
		for (Map.Entry<Integer, Bug> bug : bugList.entrySet()) {
			if (i == 0) {
				b = new HashMap<Integer, Bug>();
				listOfSubBugs.add(b);
				i++;
			} else if (i > 0 && i <= numOfBugs) {		
				b.put(bug.getKey(), bug.getValue());
				i++;
			} else
				i = 0;
		}
	}
}
