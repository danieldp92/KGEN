package main.Experimentation;

import anonymization.KAnonymity;
import approaches.ola.OLAAlgorithm;
import dataset.beans.Dataset;
import dataset.generator.DatasetGenerator;
import exception.IOPropertiesException;
import lattice.bean.Lattice;
import utils.DatasetUtils;
import utils.FileUtils;
import utils.XlsUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

public class OLARun {
    private static final String PROJECT_DIR = System.getProperty("user.dir") + File.separator;
    private static final String DATASET_FOLDER_DIR = PROJECT_DIR + "dataset" + File.separator;
    private static final String CONFIG_FOLDER_DIR = PROJECT_DIR + "config" + File.separator;

    private static final String datasetPath = DATASET_FOLDER_DIR + "F2_Dataset.xlsx";
    private static final String randomDatasetPath = DATASET_FOLDER_DIR + "RandomDataset.xlsx";
    private static final String configIdentifierPath = CONFIG_FOLDER_DIR + "configIdentifier.txt";
    private static final String randomDatasetConfigPath = CONFIG_FOLDER_DIR + "randomDatasetConfig.txt";

    private static final boolean RANDOM_TEST = true;

    public static void execute() throws IOException {
        Dataset dataset = null;

        if (RANDOM_TEST) {
            File randomDatasetFile = new File(randomDatasetPath);

            if (randomDatasetFile.exists()) {
                dataset = XlsUtils.readXlsx(randomDatasetPath);
                DatasetUtils.loadProperties(dataset, randomDatasetConfigPath);
            } else {
                dataset = DatasetGenerator.generateRandomDataset(20000);
                XlsUtils.writeXlsx(randomDatasetPath, dataset);
            }

            OLAAlgorithm olaAlgorithm = new OLAAlgorithm(dataset);
            olaAlgorithm.execute(dataset);

        } else {
            //Read the dataset not anonymized
            dataset = XlsUtils.readXlsx(datasetPath);
            DatasetUtils.loadProperties(dataset, configIdentifierPath);

            //OLAAlgorithm olaAlgorithm = new OLAAlgorithm(dataset);
            //olaAlgorithm.execute(dataset);
        }
    }
}
