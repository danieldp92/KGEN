package main.experimentation;

import approaches.ola.OLAAlgorithm;
import controller.LatticeController;
import dataset.beans.Dataset;
import dataset.generator.DatasetGenerator;
import exception.IOPropertiesException;
import utils.DatasetUtils;
import utils.XlsUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OLAExperimentation {
    private static final String PROJECT_DIR = System.getProperty("user.dir") + File.separator;
    private static final String DATASET_FOLDER_DIR = PROJECT_DIR + "dataset" + File.separator;
    private static final String CONFIG_FOLDER_DIR = PROJECT_DIR + "config" + File.separator;

    private static final String datasetPath = DATASET_FOLDER_DIR + "F2_Dataset.xlsx";
    private static final String randomDatasetPath = DATASET_FOLDER_DIR + "RandomDataset.xlsx";
    private static final String configIdentifierPath = CONFIG_FOLDER_DIR + "configIdentifier.txt";
    private static final String randomDatasetConfigPath = CONFIG_FOLDER_DIR + "randomDatasetConfig.txt";

    private static final boolean RANDOM_TEST = true;

    private Dataset dataset;

    public OLAExperimentation() {
        try {
            initRandomDataset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Dataset initRandomDataset () throws IOException {
        File randomDatasetFile = new File(randomDatasetPath);

        if (randomDatasetFile.exists()) {
            dataset = XlsUtils.readXlsx(randomDatasetPath);
            DatasetUtils.loadProperties(dataset, randomDatasetConfigPath);
        } else {
            dataset = DatasetGenerator.generateRandomDataset(20000);
            XlsUtils.writeXlsx(randomDatasetPath, dataset);
        }

        return dataset;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void execute(LatticeController controller) throws IOException {
        OLAAlgorithm olaAlgorithm = new OLAAlgorithm(dataset, controller);
        olaAlgorithm.execute();
    }
}
