package approaches.exhaustive;

import anonymization.KAnonymity;
import dataset.beans.Dataset;
import lattice.LatticeUtils;
import lattice.bean.Lattice;
import lattice.bean.Node;
import lattice.generator.LatticeGenerator;
import utils.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class ExhaustiveAlgorithm {
    private KAnonymity kAnonymity;
    private LatticeUtils latticeUtils;

    public ExhaustiveAlgorithm (Dataset dataset) {
        this.kAnonymity = new KAnonymity(dataset);
        this.latticeUtils = new LatticeUtils(this.kAnonymity);
    }

    public void execute () {
        long start = System.currentTimeMillis();

        ArrayList<Integer> topNode = this.kAnonymity.upperBounds();
        ArrayList<Integer> bottomNode = new ArrayList<>();
        for (int i = 0; i < topNode.size(); i++) {
            bottomNode.add(0);
        }

        Lattice lattice = LatticeGenerator.generateOnlyNodes(bottomNode, topNode);

        System.out.println("Size of the problem: " + lattice.getNodes().size() + " nodes to evaluate");


        List<Node> result = new ArrayList<>();
        for (Node node : lattice.getNodes()) {
            System.out.println("Analysing " + node.getActualGeneralization());
            if (this.latticeUtils.isKAnonymous(node)) {
                result.add(node);
            }
        }

        for (int i = 0; i < result.size(); i++) {
            Node actualNode = result.get(i);
            for (int j = 0; j < result.size(); j++) {
                if (i != j) {
                    if (ArrayUtils.geq(result.get(j).getActualGeneralization(), actualNode.getActualGeneralization())) {
                        result.remove(j--);
                    }
                }
            }
        }

        System.out.println();
        System.out.println("Solution\n");
        for (Node node : result) {
            System.out.println(node.getActualGeneralization());
        }
        System.out.println("Evaluation time: " + (double)(System.currentTimeMillis()-start)/1000);
    }
}
