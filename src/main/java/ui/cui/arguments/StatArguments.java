package ui.cui.arguments;

public class StatArguments extends Arguments {
    private String resultPath;

    public StatArguments(String resultPath, String outputPath) {
        this.resultPath = resultPath;
        this.outputPath = outputPath;
    }

    public String getResultPath() {
        return resultPath;
    }
}
