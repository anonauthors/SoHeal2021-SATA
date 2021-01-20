package main.java.SCM_TA_V1;

import java.util.Comparator;

public class Sorting implements Comparator<Ranking<Developer, Double>> {

	@Override
	public int compare(Ranking<Developer, Double> arg0,
			Ranking<Developer, Double> arg1) {
		return Double.compare(arg0.getMetric(), arg1.getMetric());
	}

}
