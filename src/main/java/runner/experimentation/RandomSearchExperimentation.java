package runner.experimentation;

import approaches.metaheuristics.geneticalgorithm.AnonymizationProblem;
import approaches.metaheuristics.randomsearch.RandomSearchSetting;
import approaches.metaheuristics.utils.SolutionUtils;
import exception.DatasetNotFoundException;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.metaheuristics.randomSearch.RandomSearch;
import jmetal.util.JMException;
import runner.experimentation.exceptions.ControllerNotFoundException;
import utils.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class RandomSearchExperimentation extends Experimentation {
    @Override
    public void execute(int numberOfRun) throws DatasetNotFoundException, ControllerNotFoundException {
        System.out.println("\nRandom Search");

        AnonymizationProblem anonymizationProblem = new AnonymizationProblem(dataset);
        RandomSearchSetting randomSearchSetting = new RandomSearchSetting(anonymizationProblem);
        RandomSearch randomSearch = null;
        try {
            randomSearch = (RandomSearch) randomSearchSetting.configure();
        } catch (JMException e) {
            e.printStackTrace();
        }

        for (int run = 1; run <= numberOfRun; run++) {
            System.out.println("Random Search " + run);

            long start = System.currentTimeMillis();

            SolutionSet randomSearchSolutions = null;

            try {
                randomSearchSolutions = randomSearch.execute();
                SolutionUtils.removeAllInvalidSolutions(randomSearchSolutions);
            } catch (JMException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (randomSearchSolutions.size() > 0) {
                this.solutions = new ArrayList<>();
                for (int i = 0; i < randomSearchSolutions.size(); i++) {
                    Solution solution = randomSearchSolutions.get(i);
                    List<Integer> arraySolution = getSolutionValues(solution);
                    this.solutions.add(arraySolution);
                }

                //Remove all k-anonymous solutions that are of the same strategy path (except for the minimal k-anonymous node)
                SolutionUtils.removeGreaterElements(this.solutions);

                this.executionTime = (double)(System.currentTimeMillis()-start)/1000;
            }

            saveInfoExperimentation("RANDOM", anonymizationProblem.getkAnonymity(), run);
        }
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
}
