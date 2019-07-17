package gui;

import anonymization.KAnonymity;
import controller.LatticeController;
import dataset.beans.Dataset;
import gui.bean.TextEllipse;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import lattice.LatticeUtils;
import lattice.bean.Edge;
import lattice.bean.Lattice;
import lattice.bean.Node;
import lattice.generator.LatticeGenerator;
import main.experimentation.ExperimentationThread;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;

public class LatticeGui extends Application {
    public static final Dimension LATTICEGUI_SIZE = new Dimension(1280, 720);

    private static final int OLA_ALGORITHM = 1;
    private static final int EXHAUSTIVE_ALGORITHM = 2;
    private static final int KGEN_ALGORITHM = 3;

    private static ExperimentationThread experimentationThread;
    private static Lattice lattice;

    public static void run (String [] args) throws InterruptedException {

        experimentationThread = new ExperimentationThread(OLA_ALGORITHM);
        experimentationThread.start();

        Dataset dataset = null;
        while ((dataset = experimentationThread.getDataset()) == null) {
            Thread.sleep(10);
        }

        KAnonymity kAnonymity = new KAnonymity(dataset);

        ArrayList<Integer> min = new ArrayList<>();
        ArrayList<Integer> max = kAnonymity.upperBounds();

        for (int i : max) {
            min.add(0);
        }

        lattice = LatticeGenerator.generateWithMinMax(min, max);

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("K-Anonymous Project");

        URL mainGuiLocation = getClass().getResource("/main.fxml");
        FXMLLoader mainGuiLoader = new FXMLLoader(mainGuiLocation);
        Pane group = mainGuiLoader.load();

        LatticeController latticeController = mainGuiLoader.getController();
        //latticeController.drawLattice(lattice);

        Scene latticeguiScene = new Scene(group, LatticeGui.LATTICEGUI_SIZE.width, LatticeGui.LATTICEGUI_SIZE.height);

        primaryStage.setScene(latticeguiScene);
        primaryStage.show();

        mainGuiLoader.setController(latticeController);

        experimentationThread.setLatticeController(latticeController);
        experimentationThread.unlockThread();
    }
}
