package main.java.featureTuning;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

import java.util.HashMap;
import java.util.Map;


public class FeatureSetV1 implements FeatureGeneration {
	
	FeatureInitialization featureInitialization= FeatureInitializationV1.getInstance();;
	private static FeatureSetV1 instance=null;
	public static final HashMap<String, Integer> featureVectorIndex = new HashMap<String, Integer>(){
		{
			put("numOfDevs", 0);
			put("numOfBugs",1);
			put("TCR",2);
			put("EM",3);
			put("TM",4);
		}	
	};
		
	private FeatureSetV1() {
		// TODO Auto-generated constructor stub
	}	
	
	public static FeatureSetV1 getInstance() {
		if(instance==null)
			instance=new FeatureSetV1();
		return instance;
	}
	
	@Override
	/*
	 * Takes a solution and initialize that with the all the required variable
	 * @param solution a candidate solution needs to be initialized with the verified variables
	 * 
	 * @return solution the initialized variable
	 * @see featureTuning.FeatureGeneration#getTheFeatureVector(org.moeaframework.core.Solution)
	 */
	//MODIFY Solution solution to be added all the variables
	//EFFECT Solution solution will be ready to be evaluated
	public void setFeatureVector(Solution solution) {
		this.setNumOfDevs(solution);
		this.setNumOfBugs(solution);
		this.setTCR(solution);
		this.setEM(solution);
		this.setTM(solution);
	}
	
	
	@Override
	public void setNumOfDevs(Solution solution) {
		// TODO Auto-generated method stub
		//System.out.println(featureVectorIndex.get("numOfDevs"));
		//System.out.println(featureInitialization.getDevNum().size()-1);
		solution.setVariable(featureVectorIndex.get("numOfDevs"), EncodingUtils.newInt(0, featureInitialization.getDevNum().size()-1) );
	}
	@Override
	public void setNumOfBugs(Solution solution) {
		// TODO Auto-generated method stub
		solution.setVariable(featureVectorIndex.get("numOfBugs"), EncodingUtils.newInt(0, featureInitialization.getBugNum().size()-1) );
	}
	@Override
	public void setTCR(Solution solution) {
		// TODO Auto-generated method stub
		solution.setVariable(featureVectorIndex.get("TCR"), EncodingUtils.newInt(0, featureInitialization.getTCR().size()-1) );
	}
	@Override
	public void setEM(Solution solution) {
		// TODO Auto-generated method stub
		solution.setVariable(featureVectorIndex.get("EM"), EncodingUtils.newInt(0, featureInitialization.getEm().size()-1) );
	}
	@Override
	public void setTM(Solution solution) {
		// TODO Auto-generated method stub
		solution.setVariable(featureVectorIndex.get("TM"), EncodingUtils.newInt(0, featureInitialization.getTm().size()-1) );
	}


	

	
}
