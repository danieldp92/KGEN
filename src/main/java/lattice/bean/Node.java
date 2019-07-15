package lattice.bean;

import java.util.ArrayList;

public class Node {
    private int indexWidth;
    private int indexHeight;
    private ArrayList<Integer> actualGeneralization;
    private ArrayList<Integer> minGeneralization;
    private ArrayList<Integer> maxGeneralization;

    private ArrayList<Node> indegrees;
    private ArrayList<Node> outdegrees;

    public Node(ArrayList<Integer> actualGeneralization) {
        this.actualGeneralization = actualGeneralization;
    }

    public Node(ArrayList<Integer> actualGeneralization, ArrayList<Integer> minGeneralization, ArrayList<Integer> maxGeneralization) {
        this.indexWidth = 0;
        this.indexHeight = 0;
        this.actualGeneralization = actualGeneralization;
        this.minGeneralization = minGeneralization;
        this.maxGeneralization = maxGeneralization;
    }

    public Node(int indexWidth, int indexHeight, ArrayList<Integer> actualGeneralization, ArrayList<Integer> minGeneralization, ArrayList<Integer> maxGeneralization) {
        this.indexWidth = indexWidth;
        this.indexHeight = indexHeight;
        this.actualGeneralization = actualGeneralization;
        this.minGeneralization = minGeneralization;
        this.maxGeneralization = maxGeneralization;
    }

    public int getIndexWidth() {
        return indexWidth;
    }

    public int getIndexHeight() {
        return indexHeight;
    }

    public ArrayList<Integer> getActualGeneralization() {
        return actualGeneralization;
    }

    public void setActualGeneralization(ArrayList<Integer> actualGeneralization) {
        this.actualGeneralization = actualGeneralization;
    }

    public ArrayList<Integer> getMinGeneralization() {
        return minGeneralization;
    }

    public void setMinGeneralization(ArrayList<Integer> minGeneralization) {
        this.minGeneralization = minGeneralization;
    }

    public ArrayList<Integer> getMaxGeneralization() {
        return maxGeneralization;
    }

    public void setMaxGeneralization(ArrayList<Integer> maxGeneralization) {
        this.maxGeneralization = maxGeneralization;
    }

    public ArrayList<Node> getIndegrees() {
        return indegrees;
    }

    public void setIndegrees(ArrayList<Node> indegrees) {
        this.indegrees = indegrees;
    }

    public ArrayList<Node> getOutdegrees() {
        return outdegrees;
    }

    public void setOutdegrees(ArrayList<Node> outdegrees) {
        this.outdegrees = outdegrees;
    }

    @Override
    public boolean equals(Object obj) {
        Node node = (Node) obj;

        for (int i = 0; i < actualGeneralization.size(); i++) {
            if (actualGeneralization.get(i) != node.getActualGeneralization().get(i)) {
                return false;
            }
        }

        return true;
    }
}
