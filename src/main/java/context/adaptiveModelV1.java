package main.java.context;

import java.util.ArrayList;
import java.util.Random;


import smile.sequence.HMM;

public class adaptiveModelV1 {
	
	//the feedback function-- used to change the parameters for the next period of time!!!
	//act as a (reward, penalty) function
	//separately change the parameters for each source of observation 
	//need a reference table to apply the feedbacks to both observations' source and the developer profile
	
	public void feedbackFunction(){
		//at first all the observations get updated at the same time
		Operations.applyFeedback();
		
		//change the params for SLA source of data:
		
		//change the params for teamChangeRate source of data:
		
		
		//change the params for teamChangeRate source of data:
		
		
		//change the params for teamChangeRate source of data:
		
	}
	
	
	public void pickTheBestPoicy(){
		
	}
	
	
	//train HMM
	
	
	
	
	

}
