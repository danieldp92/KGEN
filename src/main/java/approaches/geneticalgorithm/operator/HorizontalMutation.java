package approaches.geneticalgorithm.operator;

import anonymization.KAnonymity;
import approaches.geneticalgorithm.encoding.GeneralizationSolution;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.operators.mutation.Mutation;
import jmetal.util.JMException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class HorizontalMutation extends Mutation {
    private static final int MIN_K_LEVEL = 2;

    private double mutationProbability;
    private int numberOfAttributeToMutate;

    public HorizontalMutation(HashMap<String, Object> parameters) {
        super(parameters);

        this.mutationProbability = (double) parameters_.get("probability");
        this.numberOfAttributeToMutate = (int) parameters.get("numberOfAttributeToMutate");
    }

    public Object execute(Object object) throws JMException {
        GeneralizationSolution solution = (GeneralizationSolution) object;

        this.mutationProbability = (double)solution.getPenalty() / 10;
        double random = Math.random();

        if (random < mutationProbability) {
            ArrayList<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < solution.getDecisionVariables().length; i++) {
                indexes.add(i);
            }

            Collections.shuffle(indexes);

            for (int i = 0; i < this.numberOfAttributeToMutate; i++) {
                int randomIndex = indexes.remove(0);
                int value = (int) solution.getDecisionVariables()[randomIndex].getValue();
                int randomValue = 0;

                //Increase
                if (i % 2 == 0) {
                    int maxValue = (int) solution.getDecisionVariables()[randomIndex].getUpperBound();
                    randomValue = (int)(Math.random() * (maxValue - value + 1)) + value;
                }

                //Decrease
                else {
                    int minValue = (int) solution.getDecisionVariables()[randomIndex].getLowerBound();
                    randomValue = (int)(Math.random() * (value - minValue + 1)) + minValue;
                }

                solution.getDecisionVariables()[randomIndex].setValue(randomValue);
            }
        }

        return solution;
    }
}
