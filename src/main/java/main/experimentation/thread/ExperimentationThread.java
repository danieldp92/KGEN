package main.experimentation.thread;

import controller.LatticeController;
import dataset.beans.Dataset;
import exception.DatasetNotFoundException;
import javafx.scene.paint.Color;
import jmetal.util.JMException;
import main.experimentation.ExhaustiveExperimentation;
import main.experimentation.Experimentation;
import main.experimentation.KGENExperimentation;
import main.experimentation.OLAExperimentation;
import main.experimentation.exceptions.ControllerNotFoundException;
import main.experimentation.type.AlgorithmType;
import main.experimentation.type.DatasetType;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class ExperimentationThread extends Thread {
    private static final String PROJECT_DIR = System.getProperty("user.dir") + File.separator;
    private static final String DATASET_DIR = PROJECT_DIR + "dataset" + File.separator;
    private static final String CONFIG_DIR = PROJECT_DIR + "config" + File.separator;

    private static final String F2_DATASET_PATH = DATASET_DIR + "F2_Dataset.xlsx";
    private static final String F2_CONFIG_PATH = CONFIG_DIR + "configIdentifier.txt";
    private static final String RANDOM_DATASET_PATH = DATASET_DIR + "configIdentifier.txt";
    private static final String RANDOM_CONFIG_PATH = CONFIG_DIR + "randomDatasetConfig.txt";

    private ReentrantLock lock;
    private boolean unlock;
    private LatticeController latticeController;

    private int algorithmType;
    private int datasetType;
    private int numberOfRuns;

    public ExperimentationThread (int algorithmType, int datasetType, int numberOfRuns) {
        this.lock = new ReentrantLock();

        this.algorithmType = algorithmType;
        this.datasetType = datasetType;
        this.numberOfRuns = numberOfRuns;

        this.unlock = false;
    }

    @Override
    public void run() {
        Experimentation experimentation = null;
        String datasetPath = null;
        String configPath = null;

        switch (datasetType) {
            case DatasetType.DATASET_F2:
                datasetPath = F2_DATASET_PATH;
                configPath = F2_CONFIG_PATH;
                break;
            case DatasetType.DATASET_RANDOM:
                datasetPath = RANDOM_DATASET_PATH;
                configPath = RANDOM_CONFIG_PATH;
                break;
            default:
                break;

        }

        switch (algorithmType) {
            case AlgorithmType.OLA_ALGORITHM:
                System.out.println("OLA");
                experimentation = new OLAExperimentation();

                break;
            case AlgorithmType.EXHAUSTIVE_ALGORITHM:
                System.out.println("EXHAUSTIVE");
                experimentation = new ExhaustiveExperimentation();

                break;
            case AlgorithmType.KGEN_ALGORITHM:
                System.out.println("KGEN");
                experimentation = new KGENExperimentation();

                break;
            default: break;
        }


        //Initaliza the dataset
        try {
            experimentation.initDataset(datasetPath, configPath);
        } catch (DatasetNotFoundException e) {
            if (datasetType == DatasetType.DATASET_RANDOM) {
                experimentation.initRandomDataset(datasetPath);
            } else {
                System.out.println("Dataset not found");
                System.exit(0);
            }
        }

        //Wait for a signal from GUI
        while (!unlock) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }

        experimentation.setController(latticeController);

        try {
            experimentation.execute(numberOfRuns);
        } catch (DatasetNotFoundException | ControllerNotFoundException e) {
            if (e instanceof DatasetNotFoundException) {
                System.out.println("Dataset not found");
            } else {
                System.out.println("Controller not configured");
            }

            System.exit(0);
        }
    }

    public void setLatticeController(LatticeController latticeController) {
        this.latticeController = latticeController;
    }

    public void unlockThread () {
        lock.lock();

        try {
            unlock = true;
        } finally {
            lock.unlock();
        }
    }
}
