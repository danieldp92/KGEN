package approaches.ola;

import anonymization.KAnonymity;
import approaches.Algorithm;
import controller.LatticeController;
import dataset.beans.Dataset;
import lattice.LatticeUtils;
import lattice.bean.Lattice;
import lattice.bean.Node;
import lattice.generator.LatticeGenerator;

import java.util.ArrayList;
import java.util.List;

public class OLAAlgorithm extends Algorithm {
    private static final int MIN_KLEV = 2;

    private LatticeUtils latticeUtils;
    private ArrayList<Node> results;

    public OLAAlgorithm (Dataset dataset) {
        this.results = new ArrayList<>();
        this.dataset = dataset;

        this.name = "OLA";
    }

    @Override
    public List<List<Integer>> run() {
        this.kAnonymity = new KAnonymity(dataset);
        this.latticeUtils = new LatticeUtils(this.kAnonymity);

        // Top and Bottom nodes
        ArrayList<Integer> topNode = kAnonymity.upperBounds();
        ArrayList<Integer> bottomNode = kAnonymity.lowerBounds();

        KMin(new Node(bottomNode), new Node(topNode));

        List<List<Integer>> solutions = new ArrayList<>();
        for (Node result : results) {
            solutions.add(result.getActualGeneralization());
        }

        return solutions;
    }

    public void KMin (Node bottomNode, Node topNode) {
        Lattice lattice = LatticeGenerator.generateOnlyNodes(bottomNode.getActualGeneralization(), topNode.getActualGeneralization());
        bottomNode = lattice.getNode1();
        topNode = lattice.getNode2();

        int heightN = this.latticeUtils.height(topNode, bottomNode.getIndexHeight());
        Node n = null;

        if (heightN > 1) {
            int height = heightN/2;

            for (int i = 0; i < latticeUtils.width(lattice, height); i++) {
                n = latticeUtils.node(lattice, height, i);

                if (latticeUtils.isTaggedKAnonymous(n)) {
                    KMin(bottomNode, n);
                } else if (latticeUtils.isTaggedNotKAnonymous(n)) {
                    KMin(n, topNode);
                } else if (latticeUtils.isKAnonymous(n)) {
                    latticeUtils.tagKAnonymous(n);
                    KMin(bottomNode, n);
                } else {
                    latticeUtils.tagNotKAnonymous(n);
                    KMin(n, topNode);
                }
            }
        } else {
            // This is a special case of a two node lattice
            if (latticeUtils.isTaggedNotKAnonymous(bottomNode)) {
                n = topNode;
            } else if (latticeUtils.isKAnonymous(bottomNode)) {
                latticeUtils.tagKAnonymous(bottomNode);
                n = bottomNode;
            } else {
                latticeUtils.tagNotKAnonymous(bottomNode);
                n = topNode;
            }

            if (!this.results.contains(n)) {

                this.results.add(n);
                latticeUtils.cleanUp(this.results, n);
            }
        }
    }
}
