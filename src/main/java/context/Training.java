package main.java.context;

import java.util.Random;

import smile.sequence.HMM;


public class Training {

	
	//set the initial states
	double[] initialStates=new double[]{0.8,0.2};
	
	//parameters definition
	Random random_generator=new Random();
	double[] states=new double[]{0.5,0.5};
	double[][] transitions=new double[2][2];
	double[][] emissions=new double[2][2];

	/*
	 * generate new HMM model
	 *  <p>
	 *  
	 *  @param iVector Holds the initializatin vector data
	 *  @return HMM<T> the generated HMM used for adaptive assignment
	 * 
	 */
	public HMM<Observation> getHMM() {
		HMM<Observation> HMM=new HMM<Observation>(initialStates,transitions,emissions, Environment_s1.getObservationSymbols());
		
		return HMM;
	}

	/*
	 * perform hmm initialization in terms of probability matrices
	 * 
	 * <p> the transition and emisson matrices will be initialized afterwards 
	 */
	public void initialize_params(double[][] transition, double[][] emmision){
		
		//initialize transitions probabilities
		this.transitions=transition;
		this.emissions=emmision;
	}
	
	/*//get the sequence of states
	public state[] get_statesSequence(HMM<observation> hmm, observation[] o){
		int[] state_ids=hmm.predict(o);
		state[] predictedStates=new state[state_ids.length];
		for(int i=0;i<state_ids.length;i++)
			switch (state_ids[i]) {
			case 1:
				predictedStates[i]=steady_state;
			case 2: 
				predictedStates[i]=dynamic_state;
				break;
			}
		
		return predictedStates;
	}*/

}
