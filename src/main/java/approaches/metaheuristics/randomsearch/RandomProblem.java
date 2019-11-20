package approaches.metaheuristics.randomsearch;

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

public class RandomProblem extends Problem {
    public static final int ffLOG_OBJECTIVE = 0;

    private KAnonymity kAnonymity;
    private double suppressionTreshold;
    private List<String> printableReports;

    public RandomProblem (KAnonymity kAnonymity, double suppressionTreshold) {
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

        this.printableReports = new ArrayList<>();
    }

    public KAnonymity getkAnonymity () {
        return kAnonymity;
    }

    public List<String> getPrintableReports() {
        return printableReports;
    }

    @Override
    public void evaluate(Solution solution) throws JMException {
        ArrayList<Integer> levelOfAnonymization = new ArrayList<>();
        for (Variable variable : solution.getDecisionVariables()) {
            levelOfAnonymization.add((int) variable.getValue());
        }

        AnonymizationReport report = null;
        try {
            report = this.kAnonymity.runKAnonymity(levelOfAnonymization, this.kAnonymity.MIN_K_LEVEL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        double ffLOG = report.getLogMetric();
        solution.setObjective(ffLOG_OBJECTIVE, ffLOG);

        printableReports.addAll(ReportUtils.getPrintableReport(report));
        printableReports.add("");
    }
}
