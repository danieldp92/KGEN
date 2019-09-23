package lattice.generator;

import exception.TooNodeException;
import lattice.bean.Edge;
import lattice.bean.Lattice;
import lattice.bean.Node;
import utils.ArrayUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LatticeGenerator {
    public static Lattice generateWithNodes (ArrayList<Integer> gen1, ArrayList<Integer> gen2) {
        ArrayList<Integer> minLattice = ArrayUtils.min(gen1, gen2);
        ArrayList<Integer> maxLattice = ArrayUtils.max(gen1, gen2);

        Lattice lattice = generateWithMinMax(minLattice, maxLattice);

        Node node1 = getNode(lattice.getNodes(), gen1);
        Node node2 = getNode(lattice.getNodes(), gen2);

        lattice.setNode1(node1);
        lattice.setNode2(node2);

        return lattice;
    }

    public static Lattice generateOnlyNodes (ArrayList<Integer> minLattice, ArrayList<Integer> maxLattice) throws TooNodeException {
        Lattice lattice = null;

        try {
            lattice = new Lattice();

            ArrayList<Set<ArrayList<Integer>>> levels = getLevels(minLattice, maxLattice);

            ArrayList<Node> nodes = generateNodes(levels);

            Node node1 = getNode(nodes, minLattice);
            Node node2 = getNode(nodes, maxLattice);

            lattice.setNodes(nodes);
            lattice.setNode1(node1);
            lattice.setNode2(node2);
        } catch (OutOfMemoryError ex) {
            throw new TooNodeException();
        }



        return lattice;
    }

    public static Lattice generateWithMinMax (ArrayList<Integer> minLattice, ArrayList<Integer> maxLattice) {
        Lattice lattice = new Lattice();

        System.out.println("Generate Levels");
        ArrayList<Set<ArrayList<Integer>>> levels = getLevels(minLattice, maxLattice);

        System.out.println("Generate Nodes");
        ArrayList<Node> nodes = generateNodes(levels);
        System.out.println("Generate Edges");
        ArrayList<Edge> edges = generateEdges(levels, nodes);

        System.out.println("Generate Incoming and Outgoing");
        //Incoming and outgoing
        for (int i = 0; i < nodes.size(); i++) {
            ArrayList<Node> in = new ArrayList<Node>();
            ArrayList<Node> out = new ArrayList<Node>();

            Node node = nodes.get(i);

            for (Edge edge : edges) {
                if (node.equals(edge.getFrom())) {
                    out.add(edge.getTo());
                }

                else if (node.equals(edge.getTo())) {
                    in.add(edge.getFrom());
                }
            }

            nodes.get(i).setIndegrees(in);
            nodes.get(i).setOutdegrees(out);
        }

        Node node1 = getNode(nodes, minLattice);
        Node node2 = getNode(nodes, maxLattice);

        lattice.setNodes(nodes);
        lattice.setEdges(edges);
        lattice.setNode1(node1);
        lattice.setNode2(node2);

        return lattice;
    }

    //Generate private methods
    private static ArrayList<Node> generateNodes (ArrayList<Set<ArrayList<Integer>>> levels) {
        ArrayList<Node> nodes = new ArrayList<>();

        //First element of 0 level
        ArrayList<Integer> minLattice = levels.get(0).iterator().next();

        //First element of last level
        ArrayList<Integer> maxLattice = levels.get(levels.size()-1).iterator().next();

        //Generate nodes from lattice's levels
        int indexHeight = 0;
        for (Set<ArrayList<Integer>> level : levels) {
            int indexWidth = 0;
            for (ArrayList<Integer> generalizationCombination : level) {
                Node node = new Node(indexWidth++, indexHeight, generalizationCombination, minLattice, maxLattice);
                nodes.add(node);
            }

            indexHeight++;
        }

        return nodes;
    }

    private static ArrayList<Edge> generateEdges (ArrayList<Set<ArrayList<Integer>>> levels, ArrayList<Node> nodes) {
        ArrayList<Edge> edges = new ArrayList<>();

        //Generate edges
        for (int i = 1; i < levels.size(); i++) {
            Set<ArrayList<Integer>> prevLevel = levels.get(i-1);
            Set<ArrayList<Integer>> actualLevel = levels.get(i);

            for (ArrayList<Integer> prevCombination : prevLevel) {
                Node prevNode = getNode(nodes, prevCombination);

                for (ArrayList<Integer> actualCombination : actualLevel) {
                    Node actualNode = getNode(nodes, actualCombination);

                    if (sameStrategyPath(prevNode, actualNode)) {
                        Edge edge = new Edge(prevNode, actualNode);
                        edges.add(edge);
                    }
                }
            }
        }

        return edges;
    }

    private static Node getNode (ArrayList<Node> nodes, ArrayList<Integer> gen) {
        Node tmpNode = new Node(gen, null, null);

        for (Node n : nodes) {
            if (n.equals(tmpNode)) {
                return n;
            }
        }

        return null;
    }



    private static ArrayList<Set<ArrayList<Integer>>> getLevels (ArrayList<Integer> minVector, ArrayList<Integer> maxVector) {
        ArrayList<Set<ArrayList<Integer>>> levels = new ArrayList<Set<ArrayList<Integer>>>();

        int latticeHeight = 0;
        for (int i = 0; i < maxVector.size(); i++) {
            latticeHeight += (maxVector.get(i) - minVector.get(i));
        }

        //System.out.println("Height: " + latticeHeight);

        //Inizialize combination
        ArrayList<Integer> combination = new ArrayList<Integer>();
        for (int i = 0; i < maxVector.size(); i++) {
            combination.add(minVector.get(i));
        }

        Set<ArrayList<Integer>> prevLevel = new HashSet<ArrayList<Integer>>();
        Set<ArrayList<Integer>> actualLevel = new HashSet<ArrayList<Integer>>();

        ArrayList<Integer> actualCombination = null;

        for (int i = 0; i <= latticeHeight; i++) {
            //System.out.println("Level " + i);
            if (i == 0) {
                actualCombination = new ArrayList<Integer>(combination);
                actualLevel.add(actualCombination);

                levels.add(new HashSet<ArrayList<Integer>>(actualLevel));
            } else {
                prevLevel = actualLevel;
                actualLevel = new HashSet<ArrayList<Integer>>();

                for (ArrayList<Integer> prevCombination : prevLevel) {

                    int pos = 0;
                    while (pos < prevCombination.size()) {
                        if (prevCombination.get(pos) < maxVector.get(pos)) {
                            actualCombination = new ArrayList<Integer>(prevCombination);
                            actualCombination.set(pos, actualCombination.get(pos) + 1);

                            actualLevel.add(actualCombination);
                        }

                        pos++;
                    }
                }

                levels.add(new HashSet<ArrayList<Integer>>(actualLevel));
            }
        }

        return levels;
    }

    private static boolean sameStrategyPath (Node lowerNode, Node nextNode) {
        for (int i = 0; i < lowerNode.getActualGeneralization().size(); i++) {
            if ((nextNode.getActualGeneralization().get(i) - lowerNode.getActualGeneralization().get(i)) < 0) {
                return false;
            }
        }

        return true;
    }
}
