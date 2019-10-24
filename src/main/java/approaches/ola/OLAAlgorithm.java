package approaches.ola;

import anonymization.KAnonymity;
import approaches.Algorithm;
import dataset.beans.Attribute;
import dataset.beans.Dataset;
import dataset.type.QuasiIdentifier;
import exception.TooNodeException;
import lattice.LatticeUtils;
import lattice.bean.Lattice;
import lattice.bean.Node;
import lattice.generator.LatticeGenerator;
import runner.experimentation.Experimentation;
import runner.experimentation.exceptions.LimitExceedException;

import java.util.ArrayList;
import java.util.List;

public class OLAAlgorithm extends Algorithm {
    private static final boolean TIME_EXIT_CONDITION = true;

    private LatticeUtils latticeUtils;
    private ArrayList<Node> results;
    private long start;

    public OLAAlgorithm (Dataset dataset, double suppressionTreshold) {
        this.results = new ArrayList<>();
        this.dataset = dataset;
        this.suppressionThreshold = suppressionTreshold;

        this.name = "OLA";

        this.kAnonymity = new KAnonymity(dataset);
    }

    @Override
    public List<List<Integer>> run() throws TooNodeException, LimitExceedException {
        this.start = System.currentTimeMillis();

        this.latticeUtils = new LatticeUtils(this.kAnonymity, suppressionThreshold);

        // Top and Bottom nodes
        ArrayList<Integer> topNode = kAnonymity.upperBounds;
        ArrayList<Integer> bottomNode = kAnonymity.lowerBounds;

        KMin(new Node(bottomNode), new Node(topNode));

        List<List<Integer>> solutions = new ArrayList<>();
        for (Node result : results) {
            solutions.add(result.getActualGeneralization());
        }

        return solutions;
    }

    public List<List<Integer>> run(ArrayList<Integer> bottomNode, ArrayList<Integer> topNode) throws TooNodeException, LimitExceedException {
        this.latticeUtils = new LatticeUtils(this.kAnonymity, suppressionThreshold);

        KMin(new Node(bottomNode), new Node(topNode));

        List<List<Integer>> solutions = new ArrayList<>();
        for (Node result : results) {
            solutions.add(result.getActualGeneralization());
        }

        return solutions;
    }

    public void KMin (Node bottomNode, Node topNode) throws TooNodeException, LimitExceedException {
        setChanged();
        notifyObservers(latticeUtils.taggedMap.size());

        // Time Exit condition
        if (TIME_EXIT_CONDITION && ((System.currentTimeMillis() - start) > Experimentation.MAX_EVALUATION_TIME)) {
            throw new LimitExceedException("Limit time of " + (Experimentation.MAX_EVALUATION_TIME/1000) + "s exceeded");
        }
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

    private ArrayList<Integer> get0LowerBound(int size) {
        ArrayList<Integer> lowerBound = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            lowerBound.add(0);
        }

        return lowerBound;
    }
}
