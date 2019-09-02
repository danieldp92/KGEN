package lattice;

import anonymization.KAnonymity;
import lattice.bean.Lattice;
import lattice.bean.Node;
import utils.ArrayUtils;

import java.util.*;

public class LatticeUtils {
    private KAnonymity kAnonymity;

    private LinkedHashMap<Integer, Boolean> taggedMap;

    public LatticeUtils (KAnonymity kAnonymity) {
        this.kAnonymity = kAnonymity;

        this.taggedMap = new LinkedHashMap<>();
    }


    /**
     * Computes the information loss for a particular node in the lattice. The particular information loss metric
     * used id Prec.
     * @param node
     * @return
     */
    public double infoLoss (Node node) {
        ArrayList<Integer> lowerLimit = node.getMinGeneralization();
        ArrayList<Integer> upperLimit = node.getMaxGeneralization();
        ArrayList<Integer> nodeLOG = node.getActualGeneralization();

        double sum = 0;
        int maxVariable = 0;

        for (int i = 0; i < nodeLOG.size(); i++) {
            if (upperLimit.get(i) != lowerLimit.get(i)) {
                double value = nodeLOG.get(i);
                sum += ((value - lowerLimit.get(i)) / (upperLimit.get(i) - lowerLimit.get(i)));
                maxVariable++;
            }
        }

        return sum/maxVariable;
    }

    /**
     * Returns the number of nodes at a particular height in the lattice. This is used mainly to traverse a level
     * in a lattice.
     * @param lattice
     * @param height
     * @return
     */
    public int width (Lattice lattice, int height) {
        int width = 0;

        for (Node node : lattice.getNodes()) {
            if (node.getIndexHeight() == height) {
                width++;
            }
        }

        return width;
    }

    /**
     * This function returns the height of a particular node in the particular (sub-) lattice.
     * @param node
     * @return
     */
    public int height (Node node, int startHeight) {
        return node.getIndexHeight() - startHeight;
    }

    /**
     * Determines whether the node is k-anonymous. This is the most time consuming function in the algorithm.
     * @param node
     * @return
     */
    public boolean isKAnonymous (Node node) {
        ArrayList<Integer> nodeLOG = node.getActualGeneralization();

        int kLev = this.kAnonymity.kAnonymityTest(nodeLOG);

        if (kLev >= KAnonymity.MIN_K_LEVEL) {
            return true;
        }

        return false;
    }

    /**
     * Determines whether a particular node has already been tagged as k-anonymous
     * @param node
     * @return
     */
    public boolean isTaggedKAnonymous (Node node) {
        int hashcodeNode = node.getActualGeneralization().toString().hashCode();

        Boolean taggedValue = taggedMap.get(hashcodeNode);

        if (taggedValue != null && taggedValue) {
            return true;
        }

        return false;
    }

    /**
     * Determines whether a particular node has already been tagged as not k-anonymous.
     * @param node
     * @return
     */
    public boolean isTaggedNotKAnonymous (Node node) {
        int hashcodeNode = node.getActualGeneralization().toString().hashCode();

        Boolean taggedValue = taggedMap.get(hashcodeNode);

        if (taggedValue != null && !taggedValue) {
            return true;
        }

        return false;
    }

    /**
     * Tag a particular node as k-anonymous. This will also tag as k-anonymous all other higher nodes in the
     * lattice along the same generalization strategies that pass through the node.
     * @param node
     */
    public void tagKAnonymous (Node node) {
        this.taggedMap.put(node.getActualGeneralization().toString().hashCode(), true);
    }

    /**
     * Tag a particular node as not k-anonymous. This will also tag as not k-anonymous all other lower nodes in
     * the lattice along the same generalization strategies that pass through the node.
     * @param node
     */
    public void tagNotKAnonymous (Node node) {
        this.taggedMap.put(node.getActualGeneralization().toString().hashCode(), false);
    }

    /**
     * This function is used to navigate a lattice. It returns the node at index in a particular height. The
     * index values start from the left.
     * @param lattice
     * @param height
     * @param index
     * @return
     */
    public Node node (Lattice lattice, int height, int index) {
        for (Node node : lattice.getNodes()) {
            if (node.getIndexHeight() == height && node.getIndexWidth() == index) {
                return node;
            }
        }

        return null;
    }

    /**
     * Removes all nodes in the solutions set that are generalizations of node (i.e., on the same
     * generalization strategies).
     * @param nodes
     * @param node
     */
    public void cleanUp (List<Node> nodes, Node node) {
        //Find the minimum node of the same strategy path of "node"
        Node min = node;
        for (Node n : nodes) {
            if (ArrayUtils.leq(n.getActualGeneralization(), min.getActualGeneralization())) {
                min = n;
            }
        }

        for (int i = 0; i < nodes.size(); i++) {
            if (!nodes.get(i).equals(min) &&
                    ArrayUtils.geq(nodes.get(i).getActualGeneralization(), min.getActualGeneralization())) {
                nodes.remove(i--);
            }
        }
    }


    public boolean areOfTheSameStrategyPath (Node node1, Node node2) {
        List<Integer> node1LOG = node1.getActualGeneralization();
        List<Integer> node2LOG = node2.getActualGeneralization();

        int i = 0;
        while (i < node1LOG.size() && node1LOG.get(i) >= node2LOG.get(i)) {
            i++;
        }

        //All LOG of node1 are greater then LOG of node2
        if (i >= node1LOG.size()) {
            return true;
        }


        i = 0;
        while (i < node1LOG.size() && node1LOG.get(i) <= node2LOG.get(i)) {
            i++;
        }

        //All LOG of node1 are greater then LOG of node2
        if (i >= node1LOG.size()) {
            return true;
        }

        return false;
    }

    private boolean greaterSameStrategy (Node node1, Node node2) {
        List<Integer> node1LOG = node1.getActualGeneralization();
        List<Integer> node2LOG = node2.getActualGeneralization();

        int i = 0;
        while (i < node1LOG.size() && node1LOG.get(i) >= node2LOG.get(i)) {
            i++;
        }

        //All LOG of node1 are greater then LOG of node2
        if (i >= node1LOG.size()) {
            return true;
        }

        return false;
    }


    // STATIC METHODS
    public static ArrayList<ArrayList<Node>> getLevelsNode (Lattice lattice) {
        int latticeHeight = 0;

        Node firstNode = lattice.getNodes().get(0);
        for (int i = 0; i < firstNode.getMaxGeneralization().size(); i++) {
            latticeHeight += (firstNode.getMaxGeneralization().get(i) - firstNode.getMinGeneralization().get(i));
        }

        ArrayList<ArrayList<Node>> levels = new ArrayList<ArrayList<Node>>();
        for (int i = 0; i <= latticeHeight; i++) {
            levels.add(new ArrayList<Node>());
        }


        for (Node node : lattice.getNodes()) {
            int height = 0;
            for (int i = 0; i < node.getActualGeneralization().size(); i++) {
                height += (node.getActualGeneralization().get(i) - node.getMinGeneralization().get(i));
            }

            levels.get(height).add(node);
        }

        return levels;
    }

    public static ArrayList<Node> orderLevel (ArrayList<Node> level, ArrayList<Node> prevLevel) {
        Set<Node> levelOrdered = new LinkedHashSet<Node>();

        for (Node levelNode : prevLevel) {
            ArrayList<Node> outgoingNodes = levelNode.getOutdegrees();
            levelOrdered.addAll(outgoingNodes);
        }

        return new ArrayList<Node>(levelOrdered);
    }
}
