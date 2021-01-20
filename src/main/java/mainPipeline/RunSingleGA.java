package main.java.mainPipeline;

import java.util.Arrays;

import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.GAVariation;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.operator.real.PM;
import org.moeaframework.core.operator.real.SBX;
import org.moeaframework.core.variable.EncodingUtils;

public class RunSingleGA {

    public void runSingleGA() {
    	
        Problem problem = new InitializedFeaturesProblem(5, 1);

        Selection selection = new TournamentSelection(2, 
                new ParetoDominanceComparator());

        Variation variation = new GAVariation(
                new SBX(15.0, 1.0),
                new PM(20.0, 0.5));

        Initialization initialization = new RandomInitialization(problem, 100);

        GA ga = new GA(problem, selection, variation, initialization);

        while (ga.getNumberOfEvaluations() < 10000) {
            ga.step();
        }

        NondominatedPopulation result = ga.getResult();

        for (Solution solution : result) {
            System.out.println(Arrays.toString(EncodingUtils.getReal(solution)) +
                    " => " + solution.getObjective(0));
        }

    }

}