package anonymization.generalization.utils;

import anonymization.generalization.graph.GeneralizationTree;
import anonymization.generalization.graph.Node;
import lattice.LatticeUtils;

import java.util.ArrayList;
import java.util.List;

public class TreeUtils {
    public static void printTree (GeneralizationTree generalizationTree) {
        System.out.println("GENERALIZATION TREE INFO\n");

        for (Node n : generalizationTree.getNodes()) {
            System.out.println(n.getValue());
        }


    }
}
