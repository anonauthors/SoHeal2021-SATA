package main.java.context;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;

import main.java.SCM_TA_V1.Developer;
//import org.apache.commons.math3.distribution.*;
import smile.stat.distribution.PoissonDistribution;

public abstract class Environment {
	
	static PoissonDistribution TCR;
	public static DefaultDirectedWeightedGraph<Map.Entry<Integer, Developer>, DefaultEdge> devNetwork;	
	static HashMap<Integer,Developer> developers=null;
	
	//public abstract void generaeListOfObservation();
}
