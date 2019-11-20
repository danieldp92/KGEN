package approaches.metaheuristics.geneticalgorithm.operator;

import anonymization.KAnonymity;
import approaches.metaheuristics.geneticalgorithm.AnonymizationProblem;
import approaches.metaheuristics.geneticalgorithm.encoding.GeneralizationSolution;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.operators.crossover.Crossover;
import jmetal.util.JMException;

import java.util.ArrayList;
import java.util.HashMap;

public class LatticeCrossover extends Crossover {
    private KAnonymity kAnonymity;
    private double suppressionThreshold;

    public LatticeCrossover(HashMap<String, Object> parameters) {
        super(parameters);

        this.kAnonymity = (KAnonymity) parameters.get("kAnonymity");
        this.suppressionThreshold = (double) parameters.get("suppressionThreshold");
    }

    public Object execute(Object object) throws JMException {
        GeneralizationSolution[] parents = (GeneralizationSolution[]) object;
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


            boolean kAnonParent0 = false;
            boolean kAnonParent1 = false;
            if (this.kAnonymity.isKAnonymous(getSolutionValues(parents[0]), KAnonymity.MIN_K_LEVEL, suppressionThreshold)) {
                kAnonParent0 = true;
            }

            if (this.kAnonymity.isKAnonymous(getSolutionValues(parents[1]), KAnonymity.MIN_K_LEVEL, suppressionThreshold)) {
                kAnonParent1 = true;
            }

            /*if (parents[0].getObjective(AnonymizationProblem.ffKLV_OBJECTIVE) > 1) {
                kAnonParent0 = true;
            }

            if (parents[1].getObjective(AnonymizationProblem.ffKLV_OBJECTIVE) > 1) {
                kAnonParent1 = true;
            }*/

            if (kAnonParent0 && kAnonParent1) {
                boolean kAnonMinLatticeNode = false;
                try {
                    kAnonMinLatticeNode = this.kAnonymity.isKAnonymous(getSolutionValues(min), KAnonymity.MIN_K_LEVEL, suppressionThreshold);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (kAnonMinLatticeNode) {
                    //MIN will be an offspring solution
                    dynamicOffsprings.add(min);
                } else {
                    //Generate a random value between min (not anonymized) and parents (anonymized).
                    //This nodes could be anonymized
                    dynamicOffsprings.add((GeneralizationSolution) randomBetweenSolutions(min, parents[0]));
                    dynamicOffsprings.add((GeneralizationSolution) randomBetweenSolutions(min, parents[1]));
                }
            } else if (!kAnonParent0 && !kAnonParent1) {
                dynamicOffsprings.add(max);

                boolean kAnonMaxLatticeNode = this.kAnonymity.isKAnonymous(getSolutionValues(max), KAnonymity.MIN_K_LEVEL, suppressionThreshold);
                if (!kAnonMaxLatticeNode) {
                    dynamicOffsprings.add((GeneralizationSolution) randomBetweenSolutions(parents[0], max));
                    dynamicOffsprings.add((GeneralizationSolution) randomBetweenSolutions(parents[1], max));
                }
            } else {
                if (kAnonParent0) {
                    dynamicOffsprings.add((GeneralizationSolution) randomBetweenSolutions(min, parents[0]));
                } else {
                    dynamicOffsprings.add((GeneralizationSolution) randomBetweenSolutions(min, parents[1]));
                }
            }
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
    /*public Object execute(Object object) throws JMException {
        GeneralizationSolution[] parents = (GeneralizationSolution[]) object;
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
        }

        dynamicOffsprings.add(min);
        dynamicOffsprings.add(max);

        GeneralizationSolution [] offsprings = new GeneralizationSolution[dynamicOffsprings.size()];
        for (int i = 0; i < dynamicOffsprings.size(); i++) {
            offsprings[i] = dynamicOffsprings.get(i);
        }

        return offsprings;
    }*/


    private ArrayList<Integer> getSolutionValues (Solution solution) throws JMException {
        ArrayList<Integer> values = new ArrayList<Integer>();

        for (Variable var : solution.getDecisionVariables()) {
            values.add((int) var.getValue());
        }

        return values;
    }

    private Solution randomBetweenSolutions (Solution solution1, Solution solution2) throws JMException {
        GeneralizationSolution randomSolution = new GeneralizationSolution(solution1);

        for (int i = 0; i < solution1.getDecisionVariables().length; i++) {
            int val1 = (int) solution1.getDecisionVariables()[i].getValue();
            int val2 = (int) solution2.getDecisionVariables()[i].getValue();

            int random = (int) ((Math.random() * (val2 - val1 + 1)) + val1);
            randomSolution.getDecisionVariables()[i].setValue(random);
        }

        return randomSolution;
    }
}
