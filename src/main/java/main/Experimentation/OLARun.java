package main.Experimentation;

import anonymization.KAnonymity;
import approaches.ola.OLAAlgorithm;
import dataset.beans.Dataset;
import exception.IOPropertiesException;
import lattice.bean.Lattice;
import utils.DatasetUtils;
import utils.XlsUtils;

import java.io.File;

public class OLARun {
    private static final String datasetPath = System.getProperty("user.dir") + File.separator + "dataset" + File.separator + "F2_Dataset.xlsx";
    private static final String anonymizedPath = System.getProperty("user.dir") + File.separator + "anonymized" + File.separator + "F2_Dataset_Anonymized.xlsx";
    private static final String configPath = System.getProperty("user.dir") + File.separator + "config" + File.separator + "configIdentifier.txt";

    public static void execute () throws IOPropertiesException {
        //Read the dataset not anonymized
        Dataset dataset = XlsUtils.readXlsx(datasetPath);
        DatasetUtils.loadProperties(dataset, configPath);

        OLAAlgorithm olaAlgorithm = new OLAAlgorithm();
        olaAlgorithm.execute(dataset);
    }
}
