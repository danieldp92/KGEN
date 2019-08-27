package runner.experimentation;

import approaches.metaheuristics.geneticalgorithm.AnonymizationAlgorithm;
import approaches.metaheuristics.geneticalgorithm.AnonymizationProblem;
import approaches.metaheuristics.geneticalgorithm.AnonymizationSetting;
import approaches.metaheuristics.utils.SolutionUtils;
import exception.DatasetNotFoundException;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.util.JMException;
import runner.experimentation.exceptions.ControllerNotFoundException;
import utils.*;

import java.util.ArrayList;

public class KGENExperimentation extends Experimentation{
    private AnonymizationProblem anonymizationProblem;
    private AnonymizationSetting anonymizationSetting;
    private AnonymizationAlgorithm anonymizationAlgorithm;

    @Override
    public void execute(int numberOfRun) throws DatasetNotFoundException, ControllerNotFoundException {
        System.out.println("\nKGEN");
        //Initialize the genetic algorithm
        this.anonymizationProblem = new AnonymizationProblem(dataset);
        this.anonymizationSetting = new AnonymizationSetting(this.anonymizationProblem, latticeController);
        try {
            this.anonymizationAlgorithm = (AnonymizationAlgorithm) this.anonymizationSetting.configure();
        } catch (JMException e) {
            e.printStackTrace();
        }

        for (int run = 1; run <= numberOfRun; run++) {
            System.out.println("KGEN " + run);
            long start = System.currentTimeMillis();

            SolutionSet bestSolutions = null;
            try {
                bestSolutions = this.anonymizationAlgorithm.execute();
            } catch (JMException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (bestSolutions.size() > 0) {
                this.solutions = new ArrayList<>();
                for (int i = 0; i < bestSolutions.size(); i++) {
                    this.solutions.add(getSolutionValues(bestSolutions.get(i)));
                }

                //Remove all k-anonymous solutions that are of the same strategy path (except for the minimal k-anonymous node)
                SolutionUtils.removeGreaterElements(this.solutions);

                this.executionTime = (double)(System.currentTimeMillis()-start)/1000;
            }

            saveInfoExperimentation("KGEN", anonymizationProblem.getkAnonymity(), run);
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
