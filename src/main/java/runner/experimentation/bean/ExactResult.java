package runner.experimentation.bean;

import java.util.List;

public class ExactResult {
    private String datasetName;
    private Integer numberOfAttributesAnalyzed;
    private int numberOfExperimentation;
    private String algorithmName;
    private List<Integer> solution;
    private List<Integer> exactSolution;

    public ExactResult(String datasetName, int numberOfExperimentation, String algorithmName, List<Integer> solution, List<Integer> exactSolution) {
        this.datasetName = datasetName;
        this.numberOfAttributesAnalyzed = solution.size();
        this.numberOfExperimentation = numberOfExperimentation;
        this.algorithmName = algorithmName;
        this.solution = solution;
        this.exactSolution = exactSolution;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public Integer getNumberOfAttributesAnalyzed() {
        return numberOfAttributesAnalyzed;
    }

    public int getNumberOfExperimentation() {
        return numberOfExperimentation;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public List<Integer> getSolution() {
        return solution;
    }

    public List<Integer> getExactSolution() {
        return exactSolution;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public void setNumberOfAttributesAnalyzed(Integer numberOfAttributesAnalyzed) {
        this.numberOfAttributesAnalyzed = numberOfAttributesAnalyzed;
    }

    public void setNumberOfExperimentation(int numberOfExperimentation) {
        this.numberOfExperimentation = numberOfExperimentation;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public void setSolution(List<Integer> solution) {
        this.solution = solution;
    }

    public void setExactSolution(List<Integer> exactSolution) {
        this.exactSolution = exactSolution;
    }
}
