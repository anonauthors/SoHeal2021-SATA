package main.java.SCM_TA_V1;

import java.util.ArrayList;
import java.util.Collections;

public class DevMetrics extends Metrics{
	
	public static void sortByMetric(ArrayList<Ranking<Developer, Double>> Devs){
		//sort by the value of specific metric
		Collections.sort(Devs, new Sorting());
	}
	public static Ranking<Developer, Double> computeMetric(Developer d){
		double pVal = 0.0;
		for(Double pValue:d.getDZone_Coefficient_static().values()){
			pVal += pValue;
		}
		pVal = (pVal / d.getDZone_Coefficient_static().size());
		Ranking<Developer, Double> item = new Ranking<Developer, Double>(d, pVal);
		return item;
	}
}
