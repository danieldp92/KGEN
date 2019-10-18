package ui.cui.arguments;

import runner.experimentation.type.AlgorithmType;

import java.util.ArrayList;
import java.util.List;

public class ExperimentationArguments extends Arguments {
    private int numberOfRuns;
    private double threshold;
    private String datasetPath;
    private String configPath;

    private List<Integer> algorithmsList;

    public ExperimentationArguments(int numberOfRuns, double threshold, String datasetPath, String configPath, String outputPath) {
        this(numberOfRuns, threshold, datasetPath, configPath, outputPath, null);

        this.algorithmsList = new ArrayList<>();
        this.algorithmsList.add(AlgorithmType.EXHAUSTIVE_ALGORITHM);
        this.algorithmsList.add(AlgorithmType.OLA_ALGORITHM);
        this.algorithmsList.add(AlgorithmType.KGEN_ALGORITHM);
        this.algorithmsList.add(AlgorithmType.RANDOM_ALGORITHM);
    }

    public ExperimentationArguments(int numberOfRuns, double threshold, String datasetPath, String configPath, String outputPath, List<Integer> algorithmsList) {
        this.numberOfRuns = numberOfRuns;
        this.threshold = threshold;
        this.datasetPath = datasetPath;
        this.configPath = configPath;
        this.outputPath = outputPath;
        this.algorithmsList = algorithmsList;
    }

    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    public double getThreshold() {
        return threshold;
    }

    public String getDatasetPath() {
        return datasetPath;
    }

    public String getConfigPath() {
        return configPath;
    }

    public List<Integer> getAlgorithmsList() {
        return algorithmsList;
    }
}
