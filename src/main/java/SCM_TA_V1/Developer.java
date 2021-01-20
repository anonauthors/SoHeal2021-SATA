package main.java.SCM_TA_V1;

import java.util.HashMap;

public class Developer implements Cloneable{
	private int competenceProfileCount;
	private int ID;
	private double competenceProfile[];
	public double developerNextAvailableHour=0.0;
	private int totalAssignedBugs;
	public double hourlyWage;
	public int weight;
	public DevMetrics devMetrics=new DevMetrics();
	public HashMap<Zone, Double> DZone_Wage=new HashMap<Zone,Double>();
	public HashMap<Zone, Double> DZone_Coefficient=new HashMap<Zone,Double>();
	public HashMap<Zone, Double> DZone_Coefficient_static=new HashMap<Zone,Double>();
	public double preferentialAttachment=0;
	public double fitness=0;
	
	public int getTotalAssignedBugs() {
		return totalAssignedBugs;
	}

	public void setTotalAssignedBugs(int totalAssignedBugs) 
	{
		this.totalAssignedBugs = totalAssignedBugs;
	}

	public double getDeveloperNextAvailableHour() {
		return this.developerNextAvailableHour;
	}

	public void setDeveloperNextAvailableHour(int developerNextAvailableHour) {
		this.developerNextAvailableHour = developerNextAvailableHour;
	}

	public Developer(int competenceCount)
	{
		this.competenceProfileCount=competenceCount;
		competenceProfile=new double[competenceCount];
	}
	
	public int getCompetenceProfileCount() {
		return competenceProfileCount;
	}
	
	public int getID() {
		return ID;
	}
	
	public void setID(int iD) {
		ID = iD;
	}
	
	public double[] getCompetenceProfile() {
		return competenceProfile;
	}
	public void setCompetenceProfile(double[] competenceProfile) {
		this.competenceProfile = competenceProfile;
	}
	
	public HashMap< Zone, Double> getDZone_Coefficient(){
		return this.DZone_Coefficient;
	}
	public HashMap< Zone, Double> getDZone_Coefficient_static(){
		return this.DZone_Coefficient_static;
	}
	
	public HashMap< Zone, Double> getDZone_Wage(){
		return DZone_Wage;
	}
	
	public Object clone() throws CloneNotSupportedException{
		return super.clone();
	}
}
