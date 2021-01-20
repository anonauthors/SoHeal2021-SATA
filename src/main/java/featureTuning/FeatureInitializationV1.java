package main.java.featureTuning;

import java.util.concurrent.ThreadLocalRandom;


public class FeatureInitializationV1 extends FeatureInitialization{
	
 	//create the private static--- required for singleton
 	private static FeatureInitializationV1 single_instance=null;
 	
 	
 	//private constructor to prevent instanceation
 	private FeatureInitializationV1() {
		// TODO Auto-generated constructor stub
 	}
 	
 	public static FeatureInitializationV1 getInstance(){
 		if(single_instance==null)
 			single_instance=new FeatureInitializationV1();
 		return single_instance;
 	}
 	
 	public void initializeAllFeatures() {
 		this.initializeSourceDevNum();
		this.initializeSourceBugNum();
		this.initializeSourceTCR();
		this.initializeSourceEM();
		this.initializeSourceTM();
		
 	}
 	
	@Override
	public void initializeSourceDevNum() {
		// TODO Auto-generated method stub
		for(int i=0;i<30;i++)
			devNum.add(i);
	}

	@Override
	public void initializeSourceBugNum() {
		// TODO Auto-generated method stub
		for(int i=0;i<30;i++)
			bugNum.add(i);
	}

	@Override
	public void initializeSourceTCR() {
		// TODO Auto-generated method stub
		//we are going to use the probability function of turn over computation
		for(int i=0;i<30;i++)
			TCR.add(ThreadLocalRandom.current().nextInt(0, 1));
	}

	@Override
	public void initializeSourceEM() {
		// TODO Auto-generated method stub
		//Double[][] EM=new Double[Environment_s1.listOfState.size()][Environment_s1.observationSequence.size()];
		for(int m=0; m<30; m++) {
			double[][] EM=new double[2][2];
			//FIXME how many candidates should be generated?
			for(int i=0; i<EM[0].length;i++)
				for(int j=0; j<EM[1].length/2;j++) {
					double r=Math.random();
					EM[i][j]=r;
					EM[i][j+1]=1-r;
				}
			em.add(EM);
		}
	}

	@Override
	public void initializeSourceTM() {
		// TODO Auto-generated method stub
		//Double[][] TM=new Double[Environment_s1.listOfState.size()][Environment_s1.listOfState.size()];
		for(int m=0; m<30; m++) {
			double[][] TM=new double[2][2];
			//FIXME how many candidates should be generated?
			for(int i=0; i<TM[0].length;i++)
				for(int j=0; j<TM[1].length/2;j++) {
					double r=Math.random();
					TM[i][j]=r;
					TM[i][j+1]=1-r;
				}
			tm.add(TM);
		}
	}

}
