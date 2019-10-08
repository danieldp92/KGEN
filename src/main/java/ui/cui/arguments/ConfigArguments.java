package ui.cui.arguments;

public class ConfigArguments extends Arguments {
    private String datasetPath;

    public ConfigArguments(String datasetPath, String outputPath) {
        this.datasetPath = datasetPath;
        this.outputPath = outputPath;
    }

    public String getDatasetPath() {
        return datasetPath;
    }
}
