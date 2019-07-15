package main.gui_experimentation;

import anonymization.KAnonymity;
import approaches.ola.OLAAlgorithm;
import dataset.beans.Dataset;
import dataset.generator.DatasetGenerator;
import exception.IOPropertiesException;
import gui.LatticeGui;
import gui.bean.TextEllipse;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import lattice.bean.Lattice;
import lattice.generator.LatticeGenerator;
import utils.DatasetUtils;
import utils.XlsUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class OLARunGui extends Application {
    private static final String PROJECT_DIR = System.getProperty("user.dir") + File.separator;
    private static final String DATASET_FOLDER_DIR = PROJECT_DIR + "dataset" + File.separator;
    private static final String CONFIG_FOLDER_DIR = PROJECT_DIR + "config" + File.separator;

    private static final String datasetPath = DATASET_FOLDER_DIR + "F2_Dataset.xlsx";
    private static final String randomDatasetPath = DATASET_FOLDER_DIR + "RandomDataset.xlsx";
    private static final String configIdentifierPath = CONFIG_FOLDER_DIR + "configIdentifier.txt";
    private static final String randomDatasetConfigPath = CONFIG_FOLDER_DIR + "randomDatasetConfig.txt";

    private static final boolean RANDOM_TEST = true;

    private static Lattice lattice;

    public static void execute (String [] args) throws IOException {
        Dataset dataset = null;

        if (RANDOM_TEST) {
            //Dataset load
            File randomDatasetFile = new File(randomDatasetPath);

            if (randomDatasetFile.exists()) {
                dataset = XlsUtils.readXlsx(randomDatasetPath);
                DatasetUtils.loadProperties(dataset, randomDatasetConfigPath);
            } else {
                dataset = DatasetGenerator.generateRandomDataset(20000);
                XlsUtils.writeXlsx(randomDatasetPath, dataset);
            }

            //Lattice generator
            KAnonymity kAnonymity = new KAnonymity(dataset);
            ArrayList<Integer> bottomNode = new ArrayList<>();
            ArrayList<Integer> topNode = kAnonymity.upperBounds();
            for (int i : topNode) {
                bottomNode.add(0);
            }

            lattice = LatticeGenerator.generateWithMinMax(bottomNode, topNode);

            //OLAAlgorithm olaAlgorithm = new OLAAlgorithm(dataset);
            //olaAlgorithm.execute();

        }

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("K-Anonymous Project");

        URL mainGuiLocation = getClass().getResource("/main.fxml");
        FXMLLoader mainGuiLoader = new FXMLLoader(mainGuiLocation);
        Pane group = mainGuiLoader.load(mainGuiLocation);

        ArrayList<TextEllipse> textEllipses = LatticeGui.drawEllipses(lattice);
        ArrayList<Ellipse> ellipses = new ArrayList<Ellipse>();
        for (TextEllipse textEllipse : textEllipses) {
            ellipses.add(textEllipse.getBubble());
        }

        ArrayList<Line> lines = LatticeGui.drawLines(lattice, ellipses);
        group.getChildren().addAll(lines);
        group.getChildren().addAll(textEllipses);


        Scene latticeguiScene = new Scene(group, LatticeGui.LATTICEGUI_SIZE.width, LatticeGui.LATTICEGUI_SIZE.height);


        primaryStage.setScene(latticeguiScene);
        primaryStage.show();
    }
}
