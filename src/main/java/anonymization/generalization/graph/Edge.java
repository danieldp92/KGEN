package anonymization.generalization.graph;

public class Edge {
    private Node from;
    private Node to;

    public Edge (Node pFrom, Node pTo) {
        this.from = pFrom;
        this.to = pTo;
    }

    public Node getFrom() {
        return from;
    }

    public void setFrom(Node from) {
        this.from = from;
    }

    public Node getTo() {
        return to;
    }

    public void setTo(Node to) {
        this.to = to;
    }

    @Override
    public boolean equals(Object obj) {
        Edge edgeObj = (Edge) obj;

        if (edgeObj.getFrom().equals(this.from) && edgeObj.getTo().equals(this.to))
            return true;

        return false;
    }
}
