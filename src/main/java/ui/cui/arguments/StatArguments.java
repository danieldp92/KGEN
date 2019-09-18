package ui.cui.arguments;

public class StatArguments extends Arguments {
    private String resultPath;
    private String outputPath;

    public StatArguments(String resultPath, String outputPath) {
        this.resultPath = resultPath;
        this.outputPath = outputPath;
    }

    public String getResultPath() {
        return resultPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
}
