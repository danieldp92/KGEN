package controller;

import dataset.beans.Dataset;
import ui.gui.bean.TextEllipse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import lattice.LatticeUtils;
import lattice.bean.Edge;
import lattice.bean.Lattice;
import lattice.bean.Node;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class LatticeController implements Initializable {
    public static final Dimension LATTICEGUI_SIZE = new Dimension(1280, 720);
    public static final double PADDING_PERCENTAGE = 0.7;

    private static final int OLA_ALGORITHM = 1;
    private static final int EXHAUSTIVE_ALGORITHM = 2;
    private static final int KGEN_ALGORITHM = 3;

    @FXML Pane latticePane;

    private Stage stage;
    private Dataset dataset;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void changeColor (Node node, final Color color) {
        String genText = node.getActualGeneralization().toString();

        for (javafx.scene.Node n : latticePane.getChildren()) {
            if (n instanceof TextEllipse) {
                final TextEllipse textEllipse = (TextEllipse) n;
                if (textEllipse.getText().getText().equals(genText)) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            textEllipse.getBubble().setFill(color);
                        }
                    });
                }
            }
        }
    }


    public void drawLattice (Lattice lattice) {
        ArrayList<TextEllipse> textEllipses = drawEllipses(lattice);

        ArrayList<Ellipse> ellipses = new ArrayList<>();
        for (TextEllipse textEllipse : textEllipses) {
            ellipses.add(textEllipse.getBubble());
        }

        ArrayList<Line> lines = drawLines(lattice, ellipses);

        this.latticePane.getChildren().addAll(lines);
        this.latticePane.getChildren().addAll(textEllipses);
    }

    private ArrayList<TextEllipse> drawEllipses (Lattice lattice) {
        ArrayList<TextEllipse> ellipses = new ArrayList<TextEllipse>();

        ArrayList<ArrayList<Node>> levels = LatticeUtils.getLevelsNode(lattice);
        for (int i = 1; i < levels.size(); i++) {
            levels.set(i, LatticeUtils.orderLevel(levels.get(i), levels.get(i-1)));
        }

        int maxNumberOfNodesInALevel = 0;
        int latticeHeight = levels.size();

        for (int i = 0; i < levels.size(); i++) {
            if (levels.get(i).size() > maxNumberOfNodesInALevel) {
                maxNumberOfNodesInALevel = levels.get(i).size();
            }
        }

        double widthNode = (double)LATTICEGUI_SIZE.width / maxNumberOfNodesInALevel;
        double heightNode = (double)LATTICEGUI_SIZE.height / latticeHeight;

        double ellipseWidth = widthNode - (widthNode * PADDING_PERCENTAGE);
        double ellipseHeight = heightNode - (heightNode * PADDING_PERCENTAGE);

        //Ellipse
        for (int i = 0; i < levels.size(); i++) {
            double yCenter = LATTICEGUI_SIZE.height - (i * heightNode) - (heightNode / 2);
            double xCenter = 0;

            for (int j = 0; j < levels.get(i).size(); j++) {
                double actualWithNode = (double)LATTICEGUI_SIZE.width / (levels.get(i).size() + 1);

                xCenter = ((j+1) * actualWithNode);
                TextEllipse textEllipse = new TextEllipse(levels.get(i).get(j).getActualGeneralization().toString(),
                        xCenter, yCenter, ellipseWidth, ellipseHeight);


                ellipses.add(textEllipse);
            }
        }

        return ellipses;
    }

    private ArrayList<Line> drawLines (Lattice lattice, ArrayList<Ellipse> ellipses) {
        ArrayList<Line> lines = new ArrayList<Line>();
        for (Edge edge : lattice.getEdges()) {
            Node from = edge.getFrom();
            Node to = edge.getTo();

            Ellipse ellipseFrom = getEllipseFromNode(from, ellipses);
            Ellipse ellipseTo = getEllipseFromNode(to, ellipses);

            Line line = new Line();
            line.setStartX(ellipseFrom.getCenterX());
            line.setStartY(ellipseFrom.getCenterY());
            line.setEndX(ellipseTo.getCenterX());
            line.setEndY(ellipseTo.getCenterY());

            lines.add(line);
        }

        return lines;
    }

    private Ellipse getEllipseFromNode (Node node, ArrayList<Ellipse> ellipses) {
        for (Ellipse ellipse : ellipses) {
            if (ellipse.getId().equals(node.getActualGeneralization().toString())) {
                return ellipse;
            }
        }

        return null;
    }
}
