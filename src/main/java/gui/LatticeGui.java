package gui;

import gui.bean.TextEllipse;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import lattice.LatticeUtils;
import lattice.bean.Edge;
import lattice.bean.Lattice;
import lattice.bean.Node;

import java.awt.*;
import java.util.ArrayList;

public class LatticeGui {
    public static final Dimension LATTICEGUI_SIZE = new Dimension(1280, 720);
    public static final double PADDING_PERCENTAGE = 0.7;

    public static ArrayList<TextEllipse> drawEllipses (Lattice lattice) {
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

            System.out.println("Level " + i);
            for (Node node : levels.get(i)) {
                System.out.println(node.getActualGeneralization());
            }
            System.out.println();
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

    public static ArrayList<Line> drawLines (Lattice lattice, ArrayList<Ellipse> ellipses) {
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

    private static Ellipse getEllipseFromNode (Node node, ArrayList<Ellipse> ellipses) {
        for (Ellipse ellipse : ellipses) {
            if (ellipse.getId().equals(node.getActualGeneralization().toString())) {
                return ellipse;
            }
        }

        return null;
    }
}
