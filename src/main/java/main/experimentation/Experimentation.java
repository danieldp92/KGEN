package main.experimentation;

import controller.LatticeController;
import dataset.beans.Dataset;
import dataset.generator.DatasetGenerator;
import exception.DatasetNotFoundException;
import exception.IOPropertiesException;
import main.experimentation.exceptions.ControllerNotFoundException;
import utils.DatasetUtils;
import utils.XlsUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Experimentation {
    protected static final String PROJECT_DIR = System.getProperty("user.dir") + File.separator;
    protected static final String RESULTS_DIR = PROJECT_DIR + "results" + File.separator;
    protected static final String RESULTS_FILE_PATH = RESULTS_DIR + "result.csv";

    protected Dataset dataset;
    protected List<List<Integer>> solutions;
    protected double executionTime;

    protected LatticeController latticeController;


    public void initDataset (String datasetPath, String configPath) throws DatasetNotFoundException {
        File datasetFile = new File(datasetPath);

        if (datasetFile.exists()) {
            String datasetName = datasetFile.getName().split("\\.")[0];

            this.dataset = XlsUtils.readXlsx(datasetPath);
            this.dataset.setName(datasetName);
            try {
                DatasetUtils.loadProperties(this.dataset, configPath);
            } catch (IOPropertiesException e) {
                System.out.println("Dataset not found");
                e.printStackTrace();
            }
        } else {
            throw new DatasetNotFoundException();
        }
    }

    public void initRandomDataset(String savePath) {
        try {
            this.dataset = DatasetGenerator.generateRandomDataset(20000);
        } catch (IOException e) {
            System.out.println("List of all names not found. Please, insert it in resource folder");
            e.printStackTrace();
        }

        XlsUtils.writeXlsx(savePath, this.dataset);
    }

    public void setController (LatticeController controller) {
        this.latticeController = controller;
    }

    abstract public void execute(int numberOfRun) throws DatasetNotFoundException, ControllerNotFoundException;

    abstract public void saveInfoExperimentation(int indexRun);
}
