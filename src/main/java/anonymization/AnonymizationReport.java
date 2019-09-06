package anonymization;

import java.util.List;

public class AnonymizationReport {
    private List<Integer> levelOfAnonymization;
    private double logMetric;
    private int kValue;
    private int kValueWithSuppression;
    private double percentageOfSuppression;
    private List<Integer> rowToDelete;

    public List<Integer> getLevelOfAnonymization() {
        return levelOfAnonymization;
    }

    public void setLevelOfAnonymization(List<Integer> levelOfAnonymization) {
        this.levelOfAnonymization = levelOfAnonymization;
    }

    public double getLogMetric() {
        return logMetric;
    }

    public void setLogMetric(double logMetric) {
        this.logMetric = logMetric;
    }

    public int getkValue() {
        return kValue;
    }

    public void setkValue(int kValue) {
        this.kValue = kValue;
    }

    public int getkValueWithSuppression() {
        return kValueWithSuppression;
    }

    public void setkValueWithSuppression(int kValueWithSuppression) {
        this.kValueWithSuppression = kValueWithSuppression;
    }

    public double getPercentageOfSuppression() {
        return percentageOfSuppression;
    }

    public void setPercentageOfSuppression(double percentageOfSuppression) {
        this.percentageOfSuppression = percentageOfSuppression;
    }

    public List<Integer> getRowToDelete() {
        return rowToDelete;
    }

    public void setRowToDelete(List<Integer> rowToDelete) {
        this.rowToDelete = rowToDelete;
    }
}
