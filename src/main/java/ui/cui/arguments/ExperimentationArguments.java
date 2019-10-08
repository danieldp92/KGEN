package ui.cui.arguments;

public class ExperimentationArguments extends Arguments {
    private int numberOfRuns;
    private double treshold;
    private String datasetPath;
    private String configPath;

    public ExperimentationArguments(int numberOfRuns, double treshold, String datasetPath, String configPath, String outputPath) {
        this.numberOfRuns = numberOfRuns;
        this.treshold = treshold;
        this.datasetPath = datasetPath;
        this.configPath = configPath;
        this.outputPath = outputPath;
    }

    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    public double getTreshold() {
        return treshold;
    }

    public String getDatasetPath() {
        return datasetPath;
    }

    public String getConfigPath() {
        return configPath;
    }
}
