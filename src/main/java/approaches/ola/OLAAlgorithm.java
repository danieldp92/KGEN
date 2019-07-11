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

    public void execute (Dataset dataset) {
        ArrayList<Integer> bottomNode = kAnonymity.lowerBounds();
        ArrayList<Integer> topNode = kAnonymity.upperBounds();

        long start = System.currentTimeMillis();
        Lattice lattice = LatticeGenerator.generateOnlyNodes(bottomNode, topNode);
        System.out.println("Lattice execution time: " + ((double)System.currentTimeMillis()-start)/1000);

        start = System.currentTimeMillis();
        KMin(lattice.getNode1(), lattice.getNode2());
        System.out.println("Execution time: " + ((double)System.currentTimeMillis()-start)/1000);
    }

    public void KMin (Node bottomNode, Node topNode) {
        Lattice lattice = LatticeGenerator.generateOnlyNodes(bottomNode.getActualGeneralization(), topNode.getActualGeneralization());
        bottomNode = lattice.getNode1();
        topNode = lattice.getNode2();

        System.out.println("Lattice size: " + lattice.getNodes().size());
        int heightN = this.latticeUtils.height(topNode);
        Node n = null;

        if (heightN > 1) {
            int height = heightN/2;

            System.out.println("Analyzing height " + height);
            for (int i = 1; i <= latticeUtils.width(lattice, height); i++) {
                System.out.println("Analyzing width " + i);
                n = latticeUtils.node(lattice, height, i);
                Boolean isTagged = latticeUtils.isTaggedKAnonymous(n);

                if (isTagged == null) {
                    if (latticeUtils.isKAnonymous(n)) {
                        latticeUtils.tagKAnonymous(n);
                        KMin(bottomNode, n);
                    } else {
                        latticeUtils.tagNotKAnonymous(n);
                        KMin(n, topNode);
                    }
                } else {
                    if (isTagged) {
                        KMin(bottomNode, n);
                    } else {
                        KMin(n, topNode);
                    }
                }
            }
        } else {
            //This is a special case of a two node lattice
            if (latticeUtils.isTaggedKAnonymous(bottomNode)) {
                n = topNode;
            } else if (latticeUtils.isKAnonymous(n)) {
                latticeUtils.tagKAnonymous(bottomNode);
                n = bottomNode;
            } else {
                latticeUtils.tagNotKAnonymous(n);
                n = topNode;
            }

            this.result.add(n);
            latticeUtils.cleanUp(this.result, n);
        }
    }
}
