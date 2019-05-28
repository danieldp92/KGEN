package approaches.geneticalgorithm;

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


    public AnonymizationSetting (Problem problem) {
        this.problem_ = problem;

        this.populationSize = 100;
        this.maxEvaluations = 2000;
        this.crossoverProbability = 0.9;
        this.mutationProbability = 0.1;
    }


    public Algorithm configure() throws JMException {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        AnonymizationAlgorithm algorithm = new AnonymizationAlgorithm(this.problem_);


        algorithm.setInputParameter("crossover", crossoverProbability);
        algorithm.setInputParameter("mutation", mutationProbability);

        Operator selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters);
        parameters.put("probability", crossoverProbability);
        Operator crossover = CrossoverFactory.getCrossoverOperator("SinglePointCrossover", parameters);
        parameters.put("probability", mutationProbability);
        Operator mutation = MutationFactory.getMutationOperator("BitFlipMutation", parameters);

        algorithm.addOperator("selection", selection);
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);

        algorithm.setInputParameter("populationSize", populationSize);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations);

        return algorithm;
    }
}
