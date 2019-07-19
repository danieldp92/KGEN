package main.experimentation.bean;

import java.util.List;

public class Result {
    private String datasetName;
    private int numberOfAttributes;
    private String algorithmName;
    private List<Integer> bottomNode;
    private List<Integer> topNode;
    private double executionTime;
    private List<Integer> solution;

    public Result(String datasetName, int numberOfAttributes, String algorithmName, List<Integer> bottomNode, List<Integer> topNode, double executionTime, List<Integer> solution) {
        this.datasetName = datasetName;
        this.numberOfAttributes = numberOfAttributes;
        this.algorithmName = algorithmName;
        this.bottomNode = bottomNode;
        this.topNode = topNode;
        this.executionTime = executionTime;
        this.solution = solution;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public int getNumberOfAttributes() {
        return numberOfAttributes;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public List<Integer> getBottomNode() {
        return bottomNode;
    }

    public List<Integer> getTopNode() {
        return topNode;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public List<Integer> getSolution() {
        return solution;
    }
}
