package ui.cui.arguments;

import runner.experimentation.type.AlgorithmType;

public class AlgorithmArguments extends Arguments {
    private int algorithmType;
    private double treshold;
    private String datasetPath;
    private String configPath;
    private String outputPath;

    public AlgorithmArguments(int algorithmType, double treshold, String datasetPath, String configPath, String outputPath) {
        this.algorithmType = algorithmType;
        this.treshold = treshold;
        this.datasetPath = datasetPath;
        this.configPath = configPath;
        this.outputPath = outputPath;
    }

    public int getAlgorithmType() {
        return algorithmType;
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

    public String getOutputPath() {
        return outputPath;
    }
}
