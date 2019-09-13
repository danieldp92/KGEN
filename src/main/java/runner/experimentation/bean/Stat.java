package runner.experimentation.bean;

import java.text.DecimalFormat;

public class Stat {
    private String datasetName;
    private String numberOfQuasiIdentifiers;
    private String algorithmName;
    private double averageExecutionTime;
    private double averageAccuracy;
    private double averageLOG;
    private double averageSuppression;

    public Stat(String datasetName, String numberOfQuasiIdentifiers, String algorithmName, double averageExecutionTime, double averageAccuracy, double averageLOG, double averageSuppression) {
        DecimalFormat df = new DecimalFormat("#.###");
        DecimalFormat dfMetr = new DecimalFormat("#.########");

        this.datasetName = datasetName;
        this.numberOfQuasiIdentifiers = numberOfQuasiIdentifiers;
        this.algorithmName = algorithmName;
        this.averageExecutionTime = Double.parseDouble(df.format(averageExecutionTime));
        this.averageAccuracy = Double.parseDouble(dfMetr.format(averageAccuracy));
        this.averageLOG = Double.parseDouble(dfMetr.format(averageLOG));
        this.averageSuppression = Double.parseDouble(dfMetr.format(averageSuppression));
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getNumberOfQuasiIdentifiers() {
        return numberOfQuasiIdentifiers;
    }

    public void setNumberOfQuasiIdentifiers(String numberOfQuasiIdentifiers) {
        this.numberOfQuasiIdentifiers = numberOfQuasiIdentifiers;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public double getAverageExecutionTime() {
        return averageExecutionTime;
    }

    public void setAverageExecutionTime(double averageExecutionTime) {
        this.averageExecutionTime = averageExecutionTime;
    }

    public double getAverageAccuracy() {
        return averageAccuracy;
    }

    public void setAverageAccuracy(double averageAccuracy) {
        this.averageAccuracy = averageAccuracy;
    }

    public double getAverageLOG() {
        return averageLOG;
    }

    public void setAverageLOG(double averageLOG) {
        this.averageLOG = averageLOG;
    }

    public double getAverageSuppression() {
        return averageSuppression;
    }

    public void setAverageSuppression(double averageSuppression) {
        this.averageSuppression = averageSuppression;
    }
}
