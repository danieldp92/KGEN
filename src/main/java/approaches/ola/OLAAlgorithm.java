package approaches.ola;

import anonymization.KAnonymity;
import controller.LatticeController;
import dataset.beans.Dataset;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import lattice.LatticeUtils;
import lattice.bean.Lattice;
import lattice.bean.Node;
import lattice.generator.LatticeGenerator;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OLAAlgorithm {
    private KAnonymity kAnonymity;
    private LatticeUtils latticeUtils;
    private ArrayList<Node> result;
    private Lattice lattice;

    private List<Node> prevs;
    private Node prev;

    private LatticeController latticeController;

    public OLAAlgorithm (Dataset dataset, LatticeController controller) {
        this.kAnonymity = new KAnonymity(dataset);
        this.latticeUtils = new LatticeUtils(this.kAnonymity);
        this.result = new ArrayList<>();

        this.latticeController = controller;

        this.prevs = new ArrayList<>();
    }

    public void execute () {
        System.out.println("Start");

        long start = System.currentTimeMillis();

        // Top and Bottom nodes
        ArrayList<Integer> topNode = kAnonymity.upperBounds();
        ArrayList<Integer> bottomNode = new ArrayList<>();
        for (int i = 0; i < topNode.size(); i++) {
            bottomNode.add(0);
        }

        lattice = LatticeGenerator.generateWithMinMax(bottomNode, topNode);
        System.out.println("Lattice generation time: " + ((double)(System.currentTimeMillis()-start))/1000);

        //Draw lattice
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                latticeController.drawLattice(lattice);
            }
        });

        try {
            KMin(new Node(bottomNode), new Node(topNode));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Solution");
        for (Node n : result) {
            System.out.println(n.getActualGeneralization());
        }

        System.out.println("Execution time: " + ((double)System.currentTimeMillis()-start)/1000);
    }

    public void KMin (Node bottomNode, Node topNode) throws InterruptedException {
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

                if (prev != null) {
                    latticeController.changeColor(prev, Color.WHITE);
                }
                latticeController.changeColor(n, Color.LIGHTGREY);

                prev = n;
                Thread.sleep(1000);

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

                for (Node prev : this.prevs) {
                    latticeController.changeColor(prev, Color.WHITE);
                }

                for (Node r : this.result) {
                    latticeController.changeColor(r, Color.BROWN);
                }

                prevs = new ArrayList<>(result);
            }
        }
    }
}
