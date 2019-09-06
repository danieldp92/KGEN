package runner.experimentation;

import approaches.metaheuristics.geneticalgorithm.AnonymizationAlgorithm;
import approaches.metaheuristics.geneticalgorithm.AnonymizationProblem;
import approaches.metaheuristics.geneticalgorithm.AnonymizationSetting;
import approaches.metaheuristics.utils.SolutionUtils;
import approaches.ola.OLAAlgorithm;
import exception.DatasetNotFoundException;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.util.JMException;
import runner.Main;
import runner.experimentation.exceptions.ControllerNotFoundException;
import utils.*;

import java.util.ArrayList;
import java.util.List;

public class KGENExperimentation extends Experimentation{
    private AnonymizationProblem anonymizationProblem;
    private AnonymizationSetting anonymizationSetting;
    private AnonymizationAlgorithm anonymizationAlgorithm;

    private double suppressionTreshold;

    public KGENExperimentation(String resultPath) {
        super(resultPath);
    }

    @Override
    public void execute(int numberOfRun, double suppressionTreshold) throws DatasetNotFoundException, ControllerNotFoundException {
        this.suppressionTreshold = suppressionTreshold;

        if (Main.SHOW_LOG_MESSAGE) System.out.println("\nKGEN");
        //Initialize the genetic algorithm
        this.anonymizationProblem = new AnonymizationProblem(dataset, suppressionTreshold);
        this.anonymizationSetting = new AnonymizationSetting(this.anonymizationProblem, latticeController);
        try {
            this.anonymizationAlgorithm = (AnonymizationAlgorithm) this.anonymizationSetting.configure();
        } catch (JMException e) {
            e.printStackTrace();
        }

        for (int run = 1; run <= numberOfRun; run++) {
            if (Main.SHOW_LOG_MESSAGE) System.out.println("KGEN " + run);
            long start = System.currentTimeMillis();

            SolutionSet bestSolutions = null;
            try {
                bestSolutions = this.anonymizationAlgorithm.execute();
            } catch (JMException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            List<List<List<Integer>>> allExactSolutions = null;

            if (bestSolutions.size() > 0) {
                this.solutions = new ArrayList<>();
                for (int i = 0; i < bestSolutions.size(); i++) {
                    this.solutions.add(getSolutionValues(bestSolutions.get(i)));
                }

                //Remove all k-anonymous solutions that are of the same strategy path (except for the minimal k-anonymous node)
                SolutionUtils.removeGreaterElements(this.solutions);

                this.executionTime = (double)(System.currentTimeMillis()-start)/1000;

                if (Main.EXACT_METAHEURISTIC_VERIFICATION) allExactSolutions = getExactSolutions(solutions);
            }

            saveInfoExperimentation("KGEN", anonymizationProblem.getkAnonymity(), run, allExactSolutions);
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

    private List<List<List<Integer>>> getExactSolutions (List<List<Integer>> solutions) {
        // Find the best solution, for each solution found with KGEN, to evaluate every distance from the best solution
        if (Main.SHOW_LOG_MESSAGE) System.out.println("Analyze all exact solutions for each pseudo optimal solution");
        OLAAlgorithm olaAlgorithm = new OLAAlgorithm(dataset, suppressionTreshold);

        List<List<List<Integer>>> allExactSolutions = new ArrayList<>();

        int i = 1;
        if (Main.SHOW_LOG_MESSAGE) System.out.print("Analizing solution 0 of " + solutions.size());
        for (List<Integer> solution : solutions) {
            if (Main.SHOW_LOG_MESSAGE) System.out.print("\rAnalizing solution " + i++ + " of " + solutions.size());
            List<List<Integer>> exactSolutions = olaAlgorithm.run(anonymizationProblem.getkAnonymity().lowerBounds(), new ArrayList<>(solution));
            allExactSolutions.add(exactSolutions);
        }

        if (Main.SHOW_LOG_MESSAGE) System.out.println("\nAll solutions successfully analyzed");

        return allExactSolutions;
    }
}
