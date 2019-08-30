package gui;

import controller.LatticeController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import runner.experimentation.thread.ExperimentationThread;
import runner.experimentation.type.DatasetType;

import java.awt.*;
import java.net.URL;

public class LatticeGui extends Application {
    public static final Dimension LATTICEGUI_SIZE = new Dimension(1280, 720);

    private static ExperimentationThread experimentationThread;

    public static void run (String [] args) {
        experimentationThread = new ExperimentationThread(DatasetType.DATASET_F2, 5);
        experimentationThread.start();

        //launch(args);

        experimentationThread.unlockThread();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("K-Anonymous Project");

        URL mainGuiLocation = getClass().getResource("/anon.fxml");
        FXMLLoader mainGuiLoader = new FXMLLoader(mainGuiLocation);
        Pane group = mainGuiLoader.load();

        LatticeController latticeController = mainGuiLoader.getController();

        Scene latticeguiScene = new Scene(group, LatticeGui.LATTICEGUI_SIZE.width, LatticeGui.LATTICEGUI_SIZE.height);

        primaryStage.setScene(latticeguiScene);
        primaryStage.show();

        mainGuiLoader.setController(latticeController);

        experimentationThread.setLatticeController(latticeController);
        experimentationThread.unlockThread();
    }
}
