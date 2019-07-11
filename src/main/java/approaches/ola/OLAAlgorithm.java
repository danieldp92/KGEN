package approaches.ola;

import anonymization.KAnonymity;
import dataset.beans.Dataset;
import lattice.LatticeUtils;
import lattice.bean.Lattice;
import lattice.bean.Node;
import lattice.generator.LatticeGenerator;

import java.util.ArrayList;

public class OLAAlgorithm {

    public void execute (Dataset dataset) {
        KAnonymity kAnonymity = new KAnonymity(dataset);

        ArrayList<Integer> bottomNode = kAnonymity.lowerBounds();
        ArrayList<Integer> topNode = kAnonymity.upperBounds();

        LatticeUtils latticeUtils = new LatticeUtils(kAnonymity);

        Lattice lattice = LatticeGenerator.generateWithMinMax(bottomNode, topNode);
        
    }
}
