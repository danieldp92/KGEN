package anonymization.generalization.graph;

import java.util.ArrayList;

public class GeneralizationTree extends Graph {

    public GeneralizationTree(ArrayList<Node> pNodes, ArrayList<Edge> pEdges) {
        super(pNodes, pEdges);
    }

    public Node getParent (Node node) {
        for (Edge edge : this.edges) {
            if (edge.getTo().equals(node)) {
                return edge.getFrom();
            }
        }

        return null;
    }

    public ArrayList<Node> getChildren (Node node) {
        ArrayList<Node> children = new ArrayList<Node>();

        for (Edge edge : this.edges) {
            if (edge.getFrom().equals(node)) {
                children.add(edge.getTo());
            }
        }

        return children;
    }

    public int getHeight () {
        int height = 0;
        Node tmpNode = nodes.get(0);

        ArrayList<Node> children = getChildren(tmpNode);
        while (!children.isEmpty()) {
            tmpNode = children.get(0);
            children = getChildren(tmpNode);
            height++;
        }

        return height;
    }
}
