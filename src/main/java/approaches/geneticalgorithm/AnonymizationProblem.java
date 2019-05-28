package approaches.geneticalgorithm;

import anonymization.KAnonymity;
import dataset.Dataset;
import geneticalgorithm.encoding.Chromosome;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.util.JMException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnonymizationProblem extends Problem {
    private static final int K_LEVEL = 2;
    private Dataset dataset;
    private KAnonymity kAnonymity;

    public AnonymizationProblem (Dataset dataset) {
        //Dataset variables
        this.dataset = dataset;
        this.kAnonymity = new KAnonymity(this.dataset);

        List<Integer> tmpLowerBounds = this.kAnonymity.lowerBounds();
        List<Integer> tmpUpperBounds = this.kAnonymity.upperBounds();

        //Problem variables
        this.numberOfVariables_ = tmpLowerBounds.size();
        this.numberOfObjectives_ = 3;
        this.numberOfConstraints_ = 0;

        this.problemName_ = "F2_Anonymization";


        //Init lower and upper bounds
        this.lowerLimit_ = new double[numberOfVariables_];
        this.upperLimit_ = new double[numberOfVariables_];

        for (int i = 0; i < numberOfVariables_; i++) {
            this.lowerLimit_[i] = tmpLowerBounds.get(i);
            this.upperLimit_[i] = tmpUpperBounds.get(i);
        }

        this.solutionType_ = new IntSolutionType(this);
    }


    public void evaluate(Solution solution) throws JMException {
        double ffLOG = evaluateLOG(solution);
        solution.setObjective(0, ffLOG);

        double ffND = evaluateND(solution);
        solution.setObjective(1, ffND);

        double ffKLV = evaluateKLEV(solution);
        solution.setObjective(2, ffKLV);

        //solution.setFitness(ffLOG);
    }

    private double evaluateND (Solution solution) throws JMException {
        int sum = 0;
        int latticeDepth = 0;

        for (int i = 0; i < numberOfVariables_; i++) {
            if (upperLimit_[i] > 0) {
                sum += solution.getDecisionVariables()[i].getValue();
                latticeDepth = (int) upperLimit_[i];
            }
        }

        return sum/latticeDepth;
    }

    private double evaluateLOG (Solution solution) throws JMException {
        double sum = 0;

        for (int i = 0; i < numberOfVariables_; i++) {
            if (upperLimit_[i] != lowerLimit_[i]) {
                double value = solution.getDecisionVariables()[i].getValue();
                sum += ((value-lowerLimit_[i])/(upperLimit_[i]-lowerLimit_[i]));
            } else {
                sum++;
            }
        }

        return sum/numberOfVariables_;
    }

    private double evaluateKLEV (Solution solution) throws JMException {
        ArrayList<Integer> levelOfGeneralization = new ArrayList<Integer>();
        for (Variable var : solution.getDecisionVariables()) {
            levelOfGeneralization.add((int) var.getValue());
        }

        double kLev = kAnonymity.kAnonymityTest(levelOfGeneralization);

        return kLev;
    }


    public void validateSolution (Solution solution) throws JMException {
        ArrayList<Integer> chromosome = new ArrayList<Integer>();
        for (Variable var : solution.getDecisionVariables()) {
            chromosome.add((int) var.getValue());
        }

        boolean kAnonymized = this.kAnonymity.kAnonymityTest(chromosome, K_LEVEL);

        int numberOfIter = 0;
        while (!kAnonymized) {
            ArrayList<Integer> indexToChoose = new ArrayList<Integer>();
            for (int i = 0; i < chromosome.size(); i++) {
                if (upperLimit_[i] > chromosome.get(i)) {
                    indexToChoose.add(i);
                }
            }

            Collections.shuffle(indexToChoose);


            //Increase its value
            int randomIndex = indexToChoose.remove(0);
            chromosome.set(randomIndex, chromosome.get(randomIndex)+1);

            kAnonymized = this.kAnonymity.kAnonymityTest(chromosome, K_LEVEL);

            numberOfIter++;
        }

        for (int i = 0; i < chromosome.size(); i++) {
            solution.getDecisionVariables()[i].setValue(chromosome.get(i));
        }
    }


}
