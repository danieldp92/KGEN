package approaches.metaheuristics.geneticalgorithm;

import approaches.metaheuristics.geneticalgorithm.operator.HorizontalMutation;
import approaches.metaheuristics.geneticalgorithm.operator.LatticeCrossover;
import approaches.metaheuristics.geneticalgorithm.operator.MultiObjectiveRouletteSelection;
import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.experiments.Settings;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;

import java.util.HashMap;

public class AnonymizationSetting extends Settings {
    private static final double PERCENTAGE_OF_VARIABLE_TO_MUTATE = 0.2;

    protected int populationSize;
    protected int maxEvaluations;
    protected double crossoverProbability;
    protected double mutationProbability;
    protected double horizontalMutationProbability;
    protected int numberOfAttributeToMutate;

    public AnonymizationSetting (Problem problem) {
        this.problem_ = problem;

        this.populationSize = 100;
        this.maxEvaluations = 5000;
        this.crossoverProbability = 0.9;
        this.mutationProbability = 0.2;
        this.horizontalMutationProbability = 0.4;

        this.numberOfAttributeToMutate = (int) (PERCENTAGE_OF_VARIABLE_TO_MUTATE * problem.getNumberOfVariables());
        if (this.numberOfAttributeToMutate % 2 != 0) {
            this.numberOfAttributeToMutate++;
        }
    }


    public Algorithm configure() throws JMException {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        AnonymizationAlgorithm algorithm = new AnonymizationAlgorithm(this.problem_);

        algorithm.setInputParameter("crossover", crossoverProbability);
        algorithm.setInputParameter("mutation", mutationProbability);

        Operator selection = new MultiObjectiveRouletteSelection(parameters);
        //Operator selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters);
        parameters.put("probability", crossoverProbability);
        parameters.put("kAnonymity", ((AnonymizationProblem)problem_).getkAnonymity());
        Operator crossover = new LatticeCrossover(parameters);
        parameters.put("probability", mutationProbability);
        Operator mutation = MutationFactory.getMutationOperator("BitFlipMutation", parameters);
        parameters.put("probability", horizontalMutationProbability);
        parameters.put("numberOfAttributeToMutate", this.numberOfAttributeToMutate);
        Operator horizontalMutation = new HorizontalMutation(parameters);

        algorithm.addOperator("selection", selection);
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("horizontalMutation", horizontalMutation);

        algorithm.setInputParameter("populationSize", populationSize);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations);

        return algorithm;
    }
}
