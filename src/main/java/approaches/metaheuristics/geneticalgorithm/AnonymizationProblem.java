package approaches.metaheuristics.geneticalgorithm;

import anonymization.AnonymizationReport;
import anonymization.KAnonymity;
import anonymization.utils.ReportUtils;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.util.JMException;

import java.util.ArrayList;
import java.util.List;

public class AnonymizationProblem extends Problem {
    public static final int ffLOG_OBJECTIVE = 0;
    public static final int ffKLV_OBJECTIVE = 2;
    public static final int ffTH_OBJECTIVE = 1;

    private KAnonymity kAnonymity;
    private double suppressionTreshold;

    public AnonymizationProblem (KAnonymity kAnonymity, double suppressionTreshold) {
        //Dataset variables
        this.kAnonymity = kAnonymity;
        this.suppressionTreshold = suppressionTreshold;

        //Lower and Upper bounds
        ArrayList<Integer> lowerBounds = this.kAnonymity.lowerBounds(suppressionTreshold);
        ArrayList<Integer> upperBounds = this.kAnonymity.upperBounds();


        //Problem variables
        this.numberOfVariables_ = lowerBounds.size();
        this.numberOfObjectives_ = 1;
        this.numberOfConstraints_ = 0;

        this.problemName_ = "F2_Anonymization";


        //Init lower and upper bounds
        this.lowerLimit_ = new double[numberOfVariables_];
        this.upperLimit_ = new double[numberOfVariables_];

        for (int i = 0; i < numberOfVariables_; i++) {
            this.lowerLimit_[i] = lowerBounds.get(i);
            this.upperLimit_[i] = upperBounds.get(i);
        }

        this.solutionType_ = new IntSolutionType(this);
    }

    public KAnonymity getkAnonymity () {
        return kAnonymity;
    }

    //Evaluate methods
    public void evaluate(Solution solution) throws JMException {
        ArrayList<Integer> levelOfAnonymization = new ArrayList<>();
        for (Variable variable : solution.getDecisionVariables()) {
            levelOfAnonymization.add((int) variable.getValue());
        }

        AnonymizationReport report = null;
        try {
            //System.out.println("EV: Pointer address -> " + this.kAnonymity);
            report = this.kAnonymity.runKAnonymity(levelOfAnonymization, this.kAnonymity.MIN_K_LEVEL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        double ffLOG = report.getLogMetric();
        solution.setObjective(ffLOG_OBJECTIVE, 1-ffLOG);

        //double ffTH = report.getPercentageOfSuppression();
        //solution.setObjective(ffTH_OBJECTIVE, ffTH);

        /*
        double ffKLV = 0;
        if (report.getPercentageOfSuppression() <= suppressionTreshold) {
            ffKLV = report.getkValueWithSuppression();
        } else {
            ffKLV = report.getkValue();
        }

        solution.setObjective(1, ffKLV);
        */
    }
}
