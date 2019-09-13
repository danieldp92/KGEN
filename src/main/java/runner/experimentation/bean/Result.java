package runner.experimentation.bean;

import anonymization.AnonymizationReport;

import java.text.DecimalFormat;
import java.util.List;

public class Result {
    private String datasetName;
    private int numberOfAttributes;
    private int numberOfAttributesAnalyzed;
    private int numberOfExperimentation;
    private String algorithmName;
    private double executionTime;
    private int latticeSize;
    private List<Integer> bottomNode;
    private List<Integer> topNode;
    private List<Integer> solution;
    private double logMetric;
    private int kValue;
    private int kValueWithSuppression;
    private double percentageOfSuppression;
    private List<Integer> rowToDelete;


    public Result () {}

    public Result(String datasetName, int numberOfExperimentation, int numberOfAttributes, String algorithmName,
                  Double executionTime, int latticeSize, List<Integer> bottomNode, List<Integer> topNode,
                  AnonymizationReport report) {
        DecimalFormat df = new DecimalFormat("#.###");
        DecimalFormat dfMetr = new DecimalFormat("#.########");

        this.datasetName = datasetName;
        this.numberOfExperimentation = numberOfExperimentation;
        this.numberOfAttributes = numberOfAttributes;
        this.algorithmName = algorithmName;
        this.executionTime = Double.parseDouble(df.format(executionTime));
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
            this.logMetric = Double.parseDouble(dfMetr.format(report.getLogMetric()));
            this.kValue = report.getkValue();
            this.kValueWithSuppression = report.getkValueWithSuppression();
            this.percentageOfSuppression = Double.parseDouble(dfMetr.format(report.getPercentageOfSuppression()));
            this.rowToDelete = report.getRowToDelete();
        }
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

    public double getExecutionTime() {
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

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public void setNumberOfAttributes(int numberOfAttributes) {
        this.numberOfAttributes = numberOfAttributes;
    }

    public void setNumberOfAttributesAnalyzed(int numberOfAttributesAnalyzed) {
        this.numberOfAttributesAnalyzed = numberOfAttributesAnalyzed;
    }

    public void setNumberOfExperimentation(int numberOfExperimentation) {
        this.numberOfExperimentation = numberOfExperimentation;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public void setExecutionTime(double executionTime) {
        DecimalFormat df = new DecimalFormat("#.###");
        this.executionTime = Double.parseDouble(df.format(executionTime));
    }

    public void setLatticeSize(int latticeSize) {
        this.latticeSize = latticeSize;
    }

    public void setBottomNode(List<Integer> bottomNode) {
        this.bottomNode = bottomNode;
    }

    public void setTopNode(List<Integer> topNode) {
        this.topNode = topNode;
    }

    public void setSolution(List<Integer> solution) {
        this.solution = solution;
    }

    public void setLogMetric(double logMetric) {
        DecimalFormat dfMetr = new DecimalFormat("#.########");
        this.logMetric = Double.parseDouble(dfMetr.format(logMetric));
    }

    public void setkValue(int kValue) {
        this.kValue = kValue;
    }

    public void setkValueWithSuppression(int kValueWithSuppression) {
        this.kValueWithSuppression = kValueWithSuppression;
    }

    public void setPercentageOfSuppression(double percentageOfSuppression) {
        DecimalFormat dfMetr = new DecimalFormat("#.########");
        this.percentageOfSuppression = Double.parseDouble(dfMetr.format(percentageOfSuppression));
    }

    public void setRowToDelete(List<Integer> rowToDelete) {
        this.rowToDelete = rowToDelete;
    }
}
