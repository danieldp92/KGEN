package approaches.geneticalgorithm.operator;

import approaches.geneticalgorithm.encoding.GeneralizationSolution;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.operators.crossover.Crossover;
import jmetal.util.JMException;

import java.util.ArrayList;
import java.util.HashMap;

public class LatticeCrossover extends Crossover {
    private static final int MIN_K_LEVEL = 2;
    private static final int ffLOG_OBJECTIVE = 0;
    private static final int ffKLV_OBJECTIVE = 1;

    public LatticeCrossover(HashMap<String, Object> parameters) {
        super(parameters);
    }

    public Object execute(Object object) throws JMException {
        GeneralizationSolution [] parents = (GeneralizationSolution[]) object;
        GeneralizationSolution min = new GeneralizationSolution(parents[0]);
        GeneralizationSolution max = new GeneralizationSolution(parents[1]);

        ArrayList<GeneralizationSolution> dynamicOffsprings = new ArrayList<>();

        double random = Math.random();
        double crossoverProbability = (Double)parameters_.get("probability");

        if (random < crossoverProbability) {
            //Find min/max nodes
            for (int i = 0; i < parents[0].getDecisionVariables().length; i++) {
                double maxValue = Math.max(parents[0].getDecisionVariables()[i].getValue(),
                        parents[1].getDecisionVariables()[i].getValue());
                double minValue = Math.min(parents[0].getDecisionVariables()[i].getValue(),
                        parents[1].getDecisionVariables()[i].getValue());

                min.getDecisionVariables()[i].setValue(minValue);
                max.getDecisionVariables()[i].setValue(maxValue);
            }


            dynamicOffsprings.add(min);
            dynamicOffsprings.add(max);

        } else {
            dynamicOffsprings.add(parents[0]);
            dynamicOffsprings.add(parents[1]);
        }

        GeneralizationSolution [] offsprings = new GeneralizationSolution[dynamicOffsprings.size()];
        for (int i = 0; i < dynamicOffsprings.size(); i++) {
            offsprings[i] = dynamicOffsprings.get(i);
        }

        return offsprings;
    }
}
