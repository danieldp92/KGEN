package approaches.ola;

import anonymization.KAnonymity;
import dataset.beans.Dataset;
import lattice.LatticeUtils;
import lattice.bean.Lattice;
import lattice.bean.Node;
import lattice.generator.LatticeGenerator;

import java.util.ArrayList;

public class OLAAlgorithm {
    private KAnonymity kAnonymity;
    private LatticeUtils latticeUtils;
    private ArrayList<Node> result;
    private int k;

    public OLAAlgorithm (Dataset dataset) {
        this.kAnonymity = new KAnonymity(dataset);
        this.latticeUtils = new LatticeUtils(this.kAnonymity);
        this.result = new ArrayList<>();

        k = 0;
    }

    public void execute () {
        long start = System.currentTimeMillis();

        ArrayList<Integer> topNode = kAnonymity.upperBounds();
        ArrayList<Integer> bottomNode = new ArrayList<>();
        for (int i = 0; i < topNode.size(); i++) {
            bottomNode.add(0);
        }

        Lattice lattice = LatticeGenerator.generateOnlyNodes(bottomNode, topNode);

        KMin(lattice.getNode1(), lattice.getNode2());

        System.out.println("Solution");
        for (Node n : result) {
            System.out.println(n.getActualGeneralization());
        }

        System.out.println("Execution time: " + ((double)System.currentTimeMillis()-start)/1000);
    }

    public void KMin (Node bottomNode, Node topNode) {
        Lattice lattice = LatticeGenerator.generateOnlyNodes(bottomNode.getActualGeneralization(), topNode.getActualGeneralization());
        bottomNode = lattice.getNode1();
        topNode = lattice.getNode2();

        System.out.println("Lattice size: " + lattice.getNodes().size());
        int heightN = this.latticeUtils.height(topNode, bottomNode.getIndexHeight());
        Node n = null;

        if (heightN > 1) {
            int height = heightN/2;

            System.out.println("Analyzing height " + height);
            for (int i = 0; i < latticeUtils.width(lattice, height); i++) {
                System.out.println("Analyzing width " + i);
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
            //This is a special case of a two node lattice
            if (latticeUtils.isTaggedNotKAnonymous(bottomNode)) {
                n = topNode;
            } else if (latticeUtils.isKAnonymous(bottomNode)) {
                latticeUtils.tagKAnonymous(bottomNode);
                n = bottomNode;
            } else {
                latticeUtils.tagNotKAnonymous(bottomNode);
                n = topNode;
            }

            if (!this.result.contains(n)) {
                this.result.add(n);
                System.out.println(n.getActualGeneralization() + " added");
                latticeUtils.cleanUp(this.result, n);
            }
        }
    }
}
