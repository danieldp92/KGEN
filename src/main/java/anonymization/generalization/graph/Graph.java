package anonymization.generalization.graph;

import java.util.ArrayList;

public class Graph {
    protected ArrayList<Node> nodes;
    protected ArrayList<Edge> edges;

    public Graph(ArrayList<Node> pNodes, ArrayList<Edge> pEdges) {
        this.nodes = pNodes;
        this.edges = pEdges;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public Node getNode (int index) {
        return nodes.get(index);
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }
}
