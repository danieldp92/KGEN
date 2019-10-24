package approaches.exhaustive;

import anonymization.KAnonymity;
import approaches.Algorithm;
import dataset.beans.Attribute;
import dataset.beans.Dataset;
import dataset.type.QuasiIdentifier;
import exception.TooNodeException;
import lattice.bean.Lattice;
import lattice.bean.Node;
import lattice.generator.LatticeGenerator;
import runner.experimentation.Experimentation;
import runner.experimentation.exceptions.LimitExceedException;
import utils.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class ExhaustiveAlgorithm extends Algorithm {
    public ExhaustiveAlgorithm (Dataset dataset, double suppressionTreshold) {
        this.dataset = dataset;
        this.name = "EXHAUSTIVE";
        this.suppressionThreshold = suppressionTreshold;

        this.kAnonymity = new KAnonymity(dataset);
    }

    @Override
    public List<List<Integer>> run() throws TooNodeException, LimitExceedException {
        List<List<Integer>> results = new ArrayList<>();

        long start = System.currentTimeMillis();

        ArrayList<Integer> topNode = this.kAnonymity.upperBounds;
        ArrayList<Integer> bottomNode = this.kAnonymity.lowerBounds;

        Lattice lattice = LatticeGenerator.generateOnlyNodes(bottomNode, topNode);

        int indexNode = 0;

        // Apply kAnonymity on every node in the lattice
        while (indexNode < lattice.getNodes().size()) {
            // Time exit condition
            if ((System.currentTimeMillis() - start) > Experimentation.MAX_EVALUATION_TIME) {
                throw new LimitExceedException("Limit time of " + (Experimentation.MAX_EVALUATION_TIME/1000) + "s exceeded");
            }

            Node node = lattice.getNodes().get(indexNode);
            boolean kAnon = this.kAnonymity.isKAnonymous(node.getActualGeneralization(), KAnonymity.MIN_K_LEVEL, this.suppressionThreshold);

            if (kAnon) {
                results.add(node.getActualGeneralization());
            }

            indexNode++;

            setChanged();
            notifyObservers(indexNode);
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
