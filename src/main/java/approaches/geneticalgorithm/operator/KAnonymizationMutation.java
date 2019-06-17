package approaches.geneticalgorithm.operator;

import anonymization.KAnonymity;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.operators.mutation.Mutation;
import jmetal.util.JMException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class KAnonymizationMutation extends Mutation {
    private static final int MIN_K_LEVEL = 2;

    private KAnonymity kAnonymity;

    public KAnonymizationMutation(HashMap<String, Object> parameters) {
        super(parameters);

        this.kAnonymity = (KAnonymity) parameters.get("kanonyminity");
    }

    public Object execute(Object object) throws JMException {
        Solution solution = (Solution) object;

        double random = Math.random();
        Double mutationProbability = (Double) parameters_.get("probability");

        if (random < mutationProbability) {
            validateSolution(solution);
        }

        return solution;
    }

    public void validateSolution (Solution solution) throws JMException {
        ArrayList<Integer> chromosome = new ArrayList<Integer>();
        for (Variable var : solution.getDecisionVariables()) {
            chromosome.add((int) var.getValue());
        }

        boolean kAnonymized = this.kAnonymity.kAnonymityTest(chromosome, MIN_K_LEVEL);

        while (!kAnonymized) {
            ArrayList<Integer> indexToChoose = new ArrayList<Integer>();
            for (int i = 0; i < chromosome.size(); i++) {
                if (solution.getDecisionVariables()[i].getUpperBound() > chromosome.get(i)) {
                    indexToChoose.add(i);
                }
            }

            Collections.shuffle(indexToChoose);


            //Increase its value
            int randomIndex = indexToChoose.remove(0);
            chromosome.set(randomIndex, chromosome.get(randomIndex)+1);

            kAnonymized = this.kAnonymity.kAnonymityTest(chromosome, MIN_K_LEVEL);
        }

        for (int i = 0; i < chromosome.size(); i++) {
            solution.getDecisionVariables()[i].setValue(chromosome.get(i));
        }
    }
}
