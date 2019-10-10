package approaches.metaheuristics.geneticalgorithm;

import anonymization.KAnonymity;
import approaches.Algorithm;
import approaches.metaheuristics.utils.SolutionUtils;
import dataset.beans.Dataset;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.util.JMException;

import java.util.ArrayList;
import java.util.List;

public class KGENAlgorithm extends Algorithm {
    private int maxEvaluations;
    private AnonymizationAlgorithm anonymizationAlgorithm;

    public KGENAlgorithm(Dataset dataset, double suppressionThreshold) {
        this.dataset = dataset;
        this.suppressionThreshold = suppressionThreshold;
        this.name = "KGEN";
        this.kAnonymity = new KAnonymity(this.dataset, suppressionThreshold);

        initAlgorithm();
    }

    private void initAlgorithm() {
        AnonymizationProblem anonymizationProblem = new AnonymizationProblem(this.kAnonymity, this.suppressionThreshold);
        AnonymizationSetting anonymizationSetting = new AnonymizationSetting(anonymizationProblem, this.suppressionThreshold);
        this.anonymizationAlgorithm = null;
        try {
            this.anonymizationAlgorithm = (AnonymizationAlgorithm) anonymizationSetting.configure();
            this.anonymizationAlgorithm.setKgenAlgorithm(this);
        } catch (JMException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

        this.maxEvaluations = anonymizationSetting.maxEvaluations;
    }

    @Override
    public List<List<Integer>> run() {
        List<List<Integer>> solutions = null;

        SolutionSet bestSolutions = null;
        try {
            bestSolutions = this.anonymizationAlgorithm.execute();
        } catch (JMException | ClassNotFoundException e) {
            e.printStackTrace();
        }


        if (bestSolutions != null && bestSolutions.size() > 0) {
            solutions = new ArrayList<>();
            for (int i = 0; i < bestSolutions.size(); i++) {
                solutions.add(getSolutionValues(bestSolutions.get(i)));
            }

            //Remove all k-anonymous solutions that are of the same strategy path (except for the minimal k-anonymous node)
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
