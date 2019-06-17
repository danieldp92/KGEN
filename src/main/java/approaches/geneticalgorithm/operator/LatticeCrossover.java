package approaches.geneticalgorithm.operator;

import approaches.geneticalgorithm.encoding.GeneralizationSolution;
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
        GeneralizationSolution [] parents = (GeneralizationSolution[]) object;
        GeneralizationSolution [] offsprings = new GeneralizationSolution[2];
        offsprings[0] = new GeneralizationSolution(parents[0]);
        offsprings[1] = new GeneralizationSolution(parents[1]);

        double random = Math.random();
        double crossoverProbability = (Double)parameters_.get("probability");

        if (random < crossoverProbability) {
            for (int i = 0; i < parents[0].getDecisionVariables().length; i++) {
                double maxValue = Math.max(parents[0].getDecisionVariables()[i].getValue(),
                        parents[1].getDecisionVariables()[i].getValue());
                double minValue = Math.min(parents[0].getDecisionVariables()[i].getValue(),
                        parents[1].getDecisionVariables()[i].getValue());

                offsprings[0].getDecisionVariables()[i].setValue(minValue);
                offsprings[1].getDecisionVariables()[i].setValue(maxValue);
            }
        } else {
            offsprings[0] = parents[0];
            offsprings[1] = parents[1];
        }

        return offsprings;
    }
}
