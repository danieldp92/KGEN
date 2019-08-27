package runner.experimentation.thread;

import controller.LatticeController;
import exception.DatasetNotFoundException;
import runner.experimentation.*;
import runner.experimentation.exceptions.ControllerNotFoundException;
import runner.experimentation.type.AlgorithmType;
import runner.experimentation.type.DatasetType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ExperimentationThread extends Thread {
    private static final String PROJECT_DIR = System.getProperty("user.dir") + File.separator;
    private static final String DATASET_DIR = PROJECT_DIR + "dataset" + File.separator;
    private static final String CONFIG_DIR = PROJECT_DIR + "config" + File.separator;

    private static final String F2_DATASET_PATH = DATASET_DIR + "F2_Dataset.xlsx";
    private static final String F2_CONFIG_NAME = "F2Identifier";
    private static final String RANDOM_DATASET_PATH = DATASET_DIR + "RandomDataset.xlsx";
    private static final String RANDOM_CONFIG_NAME = CONFIG_DIR + "randomDatasetConfig";

    private ReentrantLock lock;
    private boolean unlock;
    private LatticeController latticeController;

    private int datasetType;
    private int numberOfRuns;

    public ExperimentationThread (int datasetType, int numberOfRuns) {
        this.lock = new ReentrantLock();

        this.datasetType = datasetType;
        this.numberOfRuns = numberOfRuns;

        this.unlock = false;
    }

    @Override
    public void run() {
        File resultFile = new File(Experimentation.RESULTS_FILE_PATH);
        if (resultFile.exists()) {
            resultFile.delete();
        }

        Experimentation experimentation = null;
        String datasetPath = null;

        List<Integer> algorithmTypes = new ArrayList<>();
        algorithmTypes.add(AlgorithmType.EXHAUSTIVE_ALGORITHM);
        algorithmTypes.add(AlgorithmType.OLA_ALGORITHM);
        algorithmTypes.add(AlgorithmType.KGEN_ALGORITHM);
        algorithmTypes.add(AlgorithmType.RANDOM_ALGORITHM);

        List<String> configPaths = new ArrayList<>();
        switch (datasetType) {
            case DatasetType.DATASET_F2:
                datasetPath = F2_DATASET_PATH;
                configPaths.add(CONFIG_DIR + F2_CONFIG_NAME + "3.txt");
                configPaths.add(CONFIG_DIR + F2_CONFIG_NAME + "5.txt");
                configPaths.add(CONFIG_DIR + F2_CONFIG_NAME + "10.txt");
                configPaths.add(CONFIG_DIR + F2_CONFIG_NAME + "15.txt");

                break;
            case DatasetType.DATASET_RANDOM:
                datasetPath = RANDOM_DATASET_PATH;
                configPaths.add(CONFIG_DIR + RANDOM_CONFIG_NAME + "3.txt");
                configPaths.add(CONFIG_DIR + RANDOM_CONFIG_NAME + "5.txt");
                configPaths.add(CONFIG_DIR + RANDOM_CONFIG_NAME + "10.txt");
                configPaths.add(CONFIG_DIR + RANDOM_CONFIG_NAME + "15.txt");

                break;
            default:
                break;

        }

        int numberOfConfigurations = configPaths.size();


        for (int i = 0; i < numberOfConfigurations; i++) {
            System.out.println("\nCONFIG: " + configPaths.get(i));

            for (int algorithmType : algorithmTypes) {
                switch (algorithmType) {
                    case AlgorithmType.OLA_ALGORITHM:
                        experimentation = new OLAExperimentation();

                        break;
                    case AlgorithmType.EXHAUSTIVE_ALGORITHM:
                        experimentation = new ExhaustiveExperimentation();

                        break;
                    case AlgorithmType.KGEN_ALGORITHM:
                        experimentation = new KGENExperimentation();

                        break;
                    case AlgorithmType.RANDOM_ALGORITHM:
                        experimentation = new RandomSearchExperimentation();

                        break;
                    default: break;
                }


                //Initalize the dataset
                try {
                    experimentation.initDataset(datasetPath, configPaths.get(i));
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
        }

        System.exit(0);
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
