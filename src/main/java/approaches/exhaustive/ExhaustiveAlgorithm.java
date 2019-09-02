package approaches.exhaustive;

import anonymization.KAnonymity;
import approaches.Algorithm;
import dataset.beans.Dataset;
import lattice.bean.Lattice;
import lattice.bean.Node;
import lattice.generator.LatticeGenerator;
import utils.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class ExhaustiveAlgorithm extends Algorithm {

    public ExhaustiveAlgorithm (Dataset dataset) {
        this.dataset = dataset;
        this.name = "EXHAUSTIVE";
    }

    @Override
    public List<List<Integer>> run() {
        this.kAnonymity = new KAnonymity(dataset);

        ArrayList<Integer> topNode = this.kAnonymity.upperBounds();
        ArrayList<Integer> bottomNode = this.kAnonymity.lowerBounds();

        Lattice lattice = LatticeGenerator.generateOnlyNodes(bottomNode, topNode);

        List<List<Integer>> results = new ArrayList<>();

        int indexNode = 0;

        // Apply kAnonymity on every node in the lattice
        while (indexNode < lattice.getNodes().size()) {

            Node node = lattice.getNodes().get(indexNode);
            boolean kAnon = this.kAnonymity.kAnonymityTest(node.getActualGeneralization(), KAnonymity.MIN_K_LEVEL);

            System.out.println("Node " + (indexNode + 1));
            System.out.println("LOG: " + node.getActualGeneralization());
            System.out.println("KANON: " + kAnon);
            System.out.println("SUPPRESSION PERCENTAGE: " + this.kAnonymity.suppressionPercentage(node.getActualGeneralization(), KAnonymity.MIN_K_LEVEL));

            // If the node is kAnonymized, add it to the results
            if (kAnon) {
                results.add(node.getActualGeneralization());
            }

            indexNode++;
        }

        // Remove from results every kAnonymized node that is greater of an
        // other kAnonymized node on the same strategy path
        for (int i = 0; i < results.size(); i++) {
            List<Integer> iResult = results.get(i);
            for (int j = 0; j < results.size(); j++) {
                List<Integer> jResult = results.get(j);
                if (i != j) {
                    if (ArrayUtils.geq(jResult, iResult)) {
                        results.remove(j--);
                    }
                }
            }
        }

        return results;
    }
}
