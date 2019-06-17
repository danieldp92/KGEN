package approaches.geneticalgorithm;

import approaches.geneticalgorithm.operator.KAnonymizationMutation;
import approaches.geneticalgorithm.operator.LatticeCrossover;
import approaches.geneticalgorithm.operator.MultiObjectiveRouletteSelection;
import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.experiments.Settings;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;

import java.util.HashMap;

public class AnonymizationSetting extends Settings {
    protected int populationSize;
    protected int maxEvaluations;
    protected double crossoverProbability;
    protected double mutationProbability;
    protected double kAnonymizationMutationProbability;


    public AnonymizationSetting (Problem problem) {
        this.problem_ = problem;

        this.populationSize = 100;
        this.maxEvaluations = 2000;
        this.crossoverProbability = 0.9;
        this.mutationProbability = 1;
        this.kAnonymizationMutationProbability = 1;
    }


    public Algorithm configure() throws JMException {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        AnonymizationAlgorithm algorithm = new AnonymizationAlgorithm(this.problem_);

        algorithm.setInputParameter("crossover", crossoverProbability);
        algorithm.setInputParameter("mutation", mutationProbability);

        Operator selection = new MultiObjectiveRouletteSelection(parameters);
        parameters.put("probability", crossoverProbability);
        Operator crossover = new LatticeCrossover(parameters);
        parameters.put("probability", mutationProbability);
        Operator mutation = MutationFactory.getMutationOperator("BitFlipMutation", parameters);
        parameters.put("probability", kAnonymizationMutationProbability);
        parameters.put("kanonyminity", ((AnonymizationProblem)problem_).getkAnonymity());
        Operator kAnonymizationMutation = new KAnonymizationMutation(parameters);

        algorithm.addOperator("selection", selection);
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("kAnonymizationMutation", kAnonymizationMutation);

        algorithm.setInputParameter("populationSize", populationSize);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations);

        return algorithm;
    }
}
