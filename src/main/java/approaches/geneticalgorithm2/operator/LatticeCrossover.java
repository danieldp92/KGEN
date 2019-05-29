package approaches.geneticalgorithm2.operator;

import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.operators.crossover.Crossover;
import jmetal.util.JMException;

import java.util.HashMap;

public class LatticeCrossover extends Crossover {

    public LatticeCrossover(HashMap<String, Object> parameters) {
        super(parameters);
    }

    public Object execute(Object object) throws JMException {
        Solution [] parents = (Solution[]) object;
        Solution [] offsprings = new Solution[2];
        offsprings[0] = new Solution();
        offsprings[1] = new Solution();

        double random = Math.random();
        double crossoverProbability = (Double)parameters_.get("probability");

        if (random < crossoverProbability) {
            Variable [] variableOffspring1 = new Variable[parents[0].getDecisionVariables().length];
            Variable [] variableOffspring2 = new Variable[parents[1].getDecisionVariables().length];

            for (int i = 0; i < parents[0].getDecisionVariables().length; i++) {
                double maxValue = Math.max(parents[0].getDecisionVariables()[i].getValue(),
                        parents[1].getDecisionVariables()[i].getValue());
                double minValue = Math.min(parents[0].getDecisionVariables()[i].getValue(),
                        parents[1].getDecisionVariables()[i].getValue());

                variableOffspring1[i].setValue(maxValue);
                variableOffspring2[i].setValue(minValue);
            }

            offsprings[0].setDecisionVariables(variableOffspring1);
            offsprings[1].setDecisionVariables(variableOffspring2);
        } else {
            offsprings[0] = parents[0];
            offsprings[1] = parents[1];
        }

        return offsprings;
    }
}
