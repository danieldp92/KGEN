package anonymization.generalization.graph;

public class Node {
    private int id;
    private String value;

    public Node (int pId) {
        this.id = pId;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        Node nodeObj = (Node) obj;

        if (nodeObj.id == this.id)
            return true;

        return false;
    }
}
