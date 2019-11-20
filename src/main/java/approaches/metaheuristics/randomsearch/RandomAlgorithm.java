package approaches.metaheuristics.randomsearch;

import anonymization.KAnonymity;
import approaches.Algorithm;
import approaches.metaheuristics.utils.SolutionUtils;
import dataset.beans.Dataset;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.metaheuristics.randomSearch.RandomSearch;
import jmetal.util.JMException;

import java.util.ArrayList;
import java.util.List;

public class RandomAlgorithm extends Algorithm {
    private RandomSearch randomSearch;
    private int maxEvaluations;
    private RandomProblem randomProblem;

    public RandomAlgorithm (Dataset dataset, double suppressionThreshold) {
        this.dataset = dataset;
        this.suppressionThreshold = suppressionThreshold;
        this.kAnonymity = new KAnonymity(this.dataset);
        this.name = "RANDOM";

        randomProblem = new RandomProblem(this.kAnonymity, suppressionThreshold);
        RandomSearchSetting randomSearchSetting = new RandomSearchSetting(randomProblem);

        try {
            this.randomSearch = (RandomSearch) randomSearchSetting.configure();
            this.randomSearch.setRandomAlgorithm(this);
        } catch (JMException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

        this.maxEvaluations = randomSearchSetting.maxEvaluations_;
    }

    @Override
    public List<List<Integer>> run() {
        List<List<Integer>> solutions = null;

        // Clean all node analyzed before, in order to have an indipendent run
        this.kAnonymity.cleanHistoryMap();

        SolutionSet bestSolutions = null;
        try {
            bestSolutions = this.randomSearch.execute();
        } catch (JMException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }


        if (bestSolutions != null && bestSolutions.size() > 0) {
            // Feasible solutions
            SolutionUtils.removeInfeasibleSolutions(bestSolutions, kAnonymity, 2, suppressionThreshold);

            solutions = new ArrayList<>();
            for (int i = 0; i < bestSolutions.size(); i++) {
                solutions.add(getSolutionValues(bestSolutions.get(i)));
            }

            SolutionUtils.removeGreaterElements(solutions);
        }

        return solutions;
    }

    public int getMaxEvaluations() {
        return maxEvaluations;
    }

    private static ArrayList<Integer> getSolutionValues (Solution solution) {
        ArrayList<Integer> values = new ArrayList<Integer>();

        for (Variable var : solution.getDecisionVariables()) {
            try {
                values.add((int) var.getValue());
            } catch (JMException e) {
                e.printStackTrace();
            }
        }

        return values;
    }

    @Override
    public synchronized void setChanged() {
        super.setChanged();
    }
}
