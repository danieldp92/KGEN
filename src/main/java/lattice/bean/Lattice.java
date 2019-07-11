package lattice.bean;

import java.util.ArrayList;

public class Lattice {
    private ArrayList<Node> nodes;
    private ArrayList<Edge> edges;
    private Node node1;
    private Node node2;

    public Lattice() {
        this.nodes = new ArrayList<Node>();
        this.edges = new ArrayList<Edge>();
        this.node1 = null;
        this.node2 = null;
    }

    public Lattice(ArrayList<Node> nodes, ArrayList<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
        this.node1 = null;
        this.node2 = null;
    }

    public Node getNode (ArrayList<Integer> generalization) {
        for (Node node : nodes) {
            boolean equals = false;

            int i = 0;
            while (i < node.getActualGeneralization().size() &&
                    node.getActualGeneralization().get(i) == generalization.get(i)) {
                i++;
            }

            if (i == node.getActualGeneralization().size()) {
                return node;
            }
        }

        return null;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void addNode (Node node) {
        this.nodes.add(node);
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void addEdge (Edge edge) {
        this.edges.add(edge);
    }

    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }

    public Node getNode1() {
        return node1;
    }

    public void setNode1(Node node1) {
        this.node1 = node1;
    }

    public Node getNode2() {
        return node2;
    }

    public void setNode2(Node node2) {
        this.node2 = node2;
    }
}
