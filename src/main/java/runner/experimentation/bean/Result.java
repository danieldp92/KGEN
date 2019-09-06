package runner.experimentation.bean;

import anonymization.AnonymizationReport;

import java.util.List;

public class Result {
    private String datasetName;
    private int numberOfAttributes;
    private int numberOfAttributesAnalyzed;
    private int numberOfExperimentation;
    private String algorithmName;
    private Double executionTime;
    private int latticeSize;
    private List<Integer> bottomNode;
    private List<Integer> topNode;
    private List<Integer> solution;
    private double logMetric;
    private int kValue;
    private int kValueWithSuppression;
    private double percentageOfSuppression;
    private List<Integer> rowToDelete;
    private List<List<Integer>> bestSolutions;


    public Result(String datasetName, int numberOfExperimentation, int numberOfAttributes, String algorithmName,
                  Double executionTime, int latticeSize, List<Integer> bottomNode, List<Integer> topNode,
                  AnonymizationReport report, List<List<Integer>> bestSolutions) {
        this.datasetName = datasetName;
        this.numberOfExperimentation = numberOfExperimentation;
        this.numberOfAttributes = numberOfAttributes;
        this.algorithmName = algorithmName;
        this.executionTime = executionTime;
        this.latticeSize = latticeSize;
        this.bottomNode = bottomNode;
        this.topNode = topNode;

        if (this.bottomNode == null) {
            this.numberOfAttributesAnalyzed = -1;
        } else {
            this.numberOfAttributesAnalyzed = this.bottomNode.size();
        }

        if (report == null) {
            this.solution = null;
            this.logMetric = -1;
            this.kValue = -1;
            this.kValueWithSuppression = -1;
            this.percentageOfSuppression = -1;
            this.rowToDelete = null;
        } else {
            this.solution = report.getLevelOfAnonymization();
            this.logMetric = report.getLogMetric();
            this.kValue = report.getkValue();
            this.kValueWithSuppression = report.getkValueWithSuppression();
            this.percentageOfSuppression = report.getPercentageOfSuppression();
            this.rowToDelete = report.getRowToDelete();
        }

        this.bestSolutions = bestSolutions;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public int getNumberOfAttributes() {
        return numberOfAttributes;
    }

    public int getNumberOfAttributesAnalyzed() {
        return numberOfAttributesAnalyzed;
    }

    public int getNumberOfExperimentation() {
        return numberOfExperimentation;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public Double getExecutionTime() {
        return executionTime;
    }

    public int getLatticeSize() {
        return latticeSize;
    }

    public List<Integer> getBottomNode() {
        return bottomNode;
    }

    public List<Integer> getTopNode() {
        return topNode;
    }

    public List<Integer> getSolution() {
        return solution;
    }

    public double getLogMetric() {
        return logMetric;
    }

    public int getkValue() {
        return kValue;
    }

    public int getkValueWithSuppression() {
        return kValueWithSuppression;
    }

    public double getPercentageOfSuppression() {
        return percentageOfSuppression;
    }

    public List<Integer> getRowToDelete() {
        return rowToDelete;
    }

    public List<List<Integer>> getBestSolutions() {
        return bestSolutions;
    }
}
