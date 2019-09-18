package runner.experimentation.bean;

import java.text.DecimalFormat;

public class Stat {
    private String datasetName;
    private String numberOfQuasiIdentifiers;
    private String algorithmName;
    private Integer numberOfRuns;
    private Double averageExecutionTime;
    private Double averageAccuracy;
    private Double averageLOG;
    private Double averageSuppression;

    public Stat(String datasetName, String numberOfQuasiIdentifiers, String algorithmName, Integer numberOfRuns, Double averageExecutionTime, Double averageAccuracy, Double averageLOG, Double averageSuppression) {
        DecimalFormat df = new DecimalFormat("#.###");
        DecimalFormat dfMetr = new DecimalFormat("#.########");

        this.datasetName = datasetName;
        this.numberOfQuasiIdentifiers = numberOfQuasiIdentifiers;
        this.algorithmName = algorithmName;
        this.numberOfRuns = numberOfRuns;

        this.averageExecutionTime = averageExecutionTime;
        if (this.averageExecutionTime != null) {
            this.averageExecutionTime = Double.parseDouble(df.format(averageExecutionTime));
        }

        this.averageAccuracy = averageAccuracy;
        if (this.averageAccuracy != null) {
            this.averageAccuracy = Double.parseDouble(dfMetr.format(averageAccuracy));
            ;
        }

        this.averageLOG = averageLOG;
        if (this.averageLOG != null) {
            this.averageLOG = Double.parseDouble(dfMetr.format(averageLOG));
        }

        this.averageSuppression = averageSuppression;
        if (this.averageSuppression != null) {
            this.averageSuppression = Double.parseDouble(dfMetr.format(averageSuppression));
        }
    }

    public String getDatasetName() {
        return datasetName;
    }

    public String getNumberOfQuasiIdentifiers() {
        return numberOfQuasiIdentifiers;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public Integer getNumberOfRuns() {
        return numberOfRuns;
    }

    public Double getAverageExecutionTime() {
        return averageExecutionTime;
    }

    public Double getAverageAccuracy() {
        return averageAccuracy;
    }

    public Double getAverageLOG() {
        return averageLOG;
    }

    public Double getAverageSuppression() {
        return averageSuppression;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public void setNumberOfQuasiIdentifiers(String numberOfQuasiIdentifiers) {
        this.numberOfQuasiIdentifiers = numberOfQuasiIdentifiers;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public void setNumberOfRuns(Integer numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
    }

    public void setAverageExecutionTime(Double averageExecutionTime) {
        this.averageExecutionTime = averageExecutionTime;
    }

    public void setAverageAccuracy(Double averageAccuracy) {
        this.averageAccuracy = averageAccuracy;
    }

    public void setAverageLOG(Double averageLOG) {
        this.averageLOG = averageLOG;
    }

    public void setAverageSuppression(Double averageSuppression) {
        this.averageSuppression = averageSuppression;
    }
}
