package main.java.mainPipeline;

import java.util.Comparator;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;

public class GA extends AbstractEvolutionaryAlgorithm {

    private final Selection selection;

    private final Variation variation;

    public GA(Problem problem, Selection selection,
            Variation variation, Initialization initialization) {
        super(problem, new Population(), null, initialization);
        this.variation = variation;
        this.selection = selection;
    }

    @Override
    public void iterate() {
        Population population = getPopulation();
        Population offspring = new Population();
        int populationSize = population.size();

        while (offspring.size() < populationSize) {
            Solution[] parents = selection.select(variation.getArity(),
                    population);
            Solution[] children = variation.evolve(parents);

            offspring.addAll(children);
        }

        evaluateAll(offspring);

        population.addAll(offspring);
        population.truncate(populationSize, new Comparator<Solution>() {

            @Override
            public int compare(Solution s1, Solution s2) {
                return Double.compare(s1.getObjective(0), s2.getObjective(0));
            }

        });
    }

}