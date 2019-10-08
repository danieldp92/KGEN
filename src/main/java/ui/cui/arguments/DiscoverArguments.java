package ui.cui.arguments;

public class DiscoverArguments extends Arguments {
    private double treshold;
    private String datasetPath;
    private String configPath;
    private String resultPath;

    public DiscoverArguments(double treshold, String datasetPath, String configPath, String resultPath, String outputPath) {
        this.treshold = treshold;
        this.datasetPath = datasetPath;
        this.configPath = configPath;
        this.resultPath = resultPath;
        this.outputPath = outputPath;
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

    public String getResultPath() {
        return resultPath;
    }
}
