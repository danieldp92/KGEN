package lattice.generator;

import lattice.bean.Edge;
import lattice.bean.Lattice;
import lattice.bean.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LatticeGenerator {

    public static Lattice generateWithNodes (ArrayList<Integer> gen1, ArrayList<Integer> gen2) {
        ArrayList<Integer> minLattice = getMinLattice(gen1, gen2);
        ArrayList<Integer> maxLattice = getMaxLattice(gen1, gen2);

        Lattice lattice = generateWithMinMax(minLattice, maxLattice);

        Node node1 = new Node(gen1, minLattice, maxLattice);
        Node node2 = new Node(gen2, minLattice, maxLattice);

        for (Node node : lattice.getNodes()) {
            if (node.equals(node1)) {
                lattice.setNode1(node);
            }

            else if (node.equals(node2)) {
                lattice.setNode2(node);
            }
        }

        return lattice;
    }

    public static Lattice generateOnlyNodes (ArrayList<Integer> minLattice, ArrayList<Integer> maxLattice) {
        Lattice lattice = new Lattice();

        ArrayList<Set<ArrayList<Integer>>> levels = getLevels(minLattice, maxLattice);

        //Generate nodes from lattice's levels
        int indexHeight = 1;
        for (Set<ArrayList<Integer>> level : levels) {
            int indexWidth = 1;
            for (ArrayList<Integer> generalizationCombination : level) {
                Node node = new Node(indexWidth++, indexHeight, generalizationCombination, minLattice, maxLattice);
                lattice.addNode(node);
            }

            indexHeight++;
        }

        Node node1 = new Node(minLattice, minLattice, maxLattice);
        Node node2 = new Node(maxLattice, minLattice, maxLattice);
        for (Node node : lattice.getNodes()) {
            if (node.equals(node1)) {
                lattice.setNode1(node);
            }

            else if (node.equals(node2)) {
                lattice.setNode2(node);
            }
        }

        return lattice;
    }

    public static Lattice generateWithMinMax (ArrayList<Integer> minLattice, ArrayList<Integer> maxLattice) {
        Lattice lattice = new Lattice();

        ArrayList<Set<ArrayList<Integer>>> levels = getLevels(minLattice, maxLattice);

        //Generate nodes from lattice's levels
        int indexHeight = 0;
        for (Set<ArrayList<Integer>> level : levels) {
            int indexWidth = 0;
            for (ArrayList<Integer> generalizationCombination : level) {
                Node node = new Node(indexWidth++, indexHeight, generalizationCombination, minLattice, maxLattice);
                lattice.addNode(node);
            }

            indexHeight++;
        }

        //Generate edges
        for (int i = 1; i < levels.size(); i++) {
            System.out.println(i);
            Set<ArrayList<Integer>> prevLevel = levels.get(i-1);
            Set<ArrayList<Integer>> actualLevel = levels.get(i);

            for (ArrayList<Integer> prevCombination : prevLevel) {
                Node prevNode = lattice.getNode(prevCombination);

                for (ArrayList<Integer> actualCombination : actualLevel) {
                    Node actualNode = lattice.getNode(actualCombination);

                    if (sameStrategyPath(prevNode, actualNode)) {
                        Edge edge = new Edge(prevNode, actualNode);
                        lattice.addEdge(edge);
                    }
                }
            }
        }

        //Incoming and outgoing
        for (int i = 0; i < lattice.getNodes().size(); i++) {
            ArrayList<Node> in = new ArrayList<Node>();
            ArrayList<Node> out = new ArrayList<Node>();

            Node node = lattice.getNodes().get(i);

            for (Edge edge : lattice.getEdges()) {
                if (node.equals(edge.getFrom())) {
                    out.add(edge.getTo());
                }

                else if (node.equals(edge.getTo())) {
                    in.add(edge.getFrom());
                }
            }

            lattice.getNodes().get(i).setIndegrees(in);
            lattice.getNodes().get(i).setOutdegrees(out);
        }

        return lattice;
    }

    private static ArrayList<Set<ArrayList<Integer>>> getLevels (ArrayList<Integer> minVector, ArrayList<Integer> maxVector) {
        ArrayList<Set<ArrayList<Integer>>> levels = new ArrayList<Set<ArrayList<Integer>>>();

        int latticeHeight = 0;
        for (int i = 0; i < maxVector.size(); i++) {
            latticeHeight += (maxVector.get(i) - minVector.get(i));
        }

        //Inizialize combination
        ArrayList<Integer> combination = new ArrayList<Integer>();
        for (int i = 0; i < maxVector.size(); i++) {
            combination.add(minVector.get(i));
        }

        Set<ArrayList<Integer>> prevLevel = new HashSet<ArrayList<Integer>>();
        Set<ArrayList<Integer>> actualLevel = new HashSet<ArrayList<Integer>>();

        ArrayList<Integer> actualCombination = null;

        for (int i = 0; i <= latticeHeight; i++) {
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

    private static ArrayList<Integer> getMinLattice (ArrayList<Integer> gen1, ArrayList<Integer> gen2) {
        ArrayList<Integer> min = new ArrayList<>();

        for (int i = 0; i < gen1.size(); i++) {
            if (gen1.get(i) < gen2.get(i)) {
                min.add(gen1.get(i));
            } else {
                min.add(gen2.get(i));
            }
        }

        return min;
    }

    private static ArrayList<Integer> getMaxLattice (ArrayList<Integer> gen1, ArrayList<Integer> gen2) {
        ArrayList<Integer> max = new ArrayList<>();

        for (int i = 0; i < gen1.size(); i++) {
            if (gen1.get(i) > gen2.get(i)) {
                max.add(gen1.get(i));
            } else {
                max.add(gen2.get(i));
            }
        }

        return max;
    }
}
