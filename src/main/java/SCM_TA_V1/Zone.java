package main.java.SCM_TA_V1;

import java.util.ArrayList;
import java.util.HashMap;

import main.java.mainPipeline.Approach;

public class Zone {
	public int zId;
	public String zName;
	public ArrayList<Zone> DZ = new ArrayList<Zone>();
	public double zoneEndTime = 0.0;
	public double zoneStartTime = 0.0;
	public double zoneEndTime_evaluate = 0.0;
	public double zoneStartTime_evaluate = 0.0;
	public int assignedDevID = 0;
	public HashMap<Approach, Integer> bus_factor = new HashMap<Approach, Integer>();
	/*
	 * { { put(Approach.STATIC, 0); put(Approach.ADAPTIVE, 0); } };
	 */
	public Zone(int id, String name){
		this.zId = id;
		this.zName = name;
	}		
	public ArrayList<Zone> getDZ(){
		return DZ;
	}

}
