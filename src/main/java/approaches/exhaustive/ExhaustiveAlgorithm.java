package approaches.exhaustive;

import anonymization.KAnonymity;
import approaches.Algorithm;
import dataset.beans.Dataset;
import lattice.LatticeUtils;
import lattice.bean.Lattice;
import lattice.bean.Node;
import lattice.generator.LatticeGenerator;
import main.experimentation.Experimentation;
import main.experimentation.exceptions.OutOfTimeException;
import utils.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class ExhaustiveAlgorithm extends Algorithm {
    private static final int MIN_KLEV = 2;

    public ExhaustiveAlgorithm (Dataset dataset) {
        this.kAnonymity = new KAnonymity(dataset);
        this.name = "EXHAUSTIVE";
    }

    @Override
    public List<List<Integer>> run() throws OutOfTimeException {
        long startTime = System.currentTimeMillis();

        ArrayList<Integer> topNode = this.kAnonymity.upperBounds();
        ArrayList<Integer> bottomNode = new ArrayList<>();
        for (int i = 0; i < topNode.size(); i++) {
            bottomNode.add(0);
        }

        Lattice lattice = LatticeGenerator.generateOnlyNodes(bottomNode, topNode);

        List<List<Integer>> results = new ArrayList<>();

        System.out.println("Number of nodes: " + lattice.getNodes().size());

        int indexNode = 0;
        System.out.print("Analyzing node " + indexNode);

        while ((System.currentTimeMillis() - startTime) < Experimentation.MAX_EVALUATION_TIME &&
            indexNode < lattice.getNodes().size()) {

            System.out.print("\rAnalyzing node " + (indexNode + 1));

            Node node = lattice.getNodes().get(indexNode);

            if (this.kAnonymity.kAnonymityTest(node.getActualGeneralization(), MIN_KLEV)) {
                results.add(node.getActualGeneralization());
            }

            indexNode++;
        }

        if (indexNode < lattice.getNodes().size()) {
            throw new OutOfTimeException("Too many time");
        }

        System.out.println("\nAll nodes analyzed successfully");

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
