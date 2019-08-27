package runner.experimentation.bean;

import java.util.List;

public class Result {
    private String datasetName;
    private int numberOfExperimentation;
    private int numberOfAttributes;
    private String algorithmName;
    private Double executionTime;
    private int latticeSize;
    private List<Integer> bottomNode;
    private List<Integer> topNode;
    private List<Integer> solution;

    public Result(String datasetName, int numberOfExperimentation, int numberOfAttributes, String algorithmName, Double executionTime, int latticeSize, List<Integer> bottomNode, List<Integer> topNode, List<Integer> solution) {
        this.datasetName = datasetName;
        this.numberOfExperimentation = numberOfExperimentation;
        this.numberOfAttributes = numberOfAttributes;
        this.algorithmName = algorithmName;
        this.executionTime = executionTime;
        this.latticeSize = latticeSize;
        this.bottomNode = bottomNode;
        this.topNode = topNode;
        this.solution = solution;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public int getNumberOfExperimentation() {
        return numberOfExperimentation;
    }

    public int getNumberOfAttributes() {
        return numberOfAttributes;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public Double getExecutionTime() {
        return executionTime;
    }

    public int getLatticeSize() {
        return latticeSize;
    }

    public List<Integer> getBottomNode() {
        return bottomNode;
    }

    public List<Integer> getTopNode() {
        return topNode;
    }

    public List<Integer> getSolution() {
        return solution;
    }
}
