package main.java.context;

import org.apache.commons.math3.distribution.*;

public class Observation {
	private int teamChangeRate;
	private int symbol;
	public Observation(int churnNum){
		if(churnNum>=2)
			this.teamChangeRate=1;
		else 
			this.teamChangeRate=0;
	}
	
	public int getTeamChangeRate() {
		return this.teamChangeRate;
	}
	
	public void setTeamChangeRate(int teamChangeRate) {
		this.teamChangeRate = teamChangeRate;
	}
}
