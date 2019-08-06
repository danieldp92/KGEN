package main.experimentation;

import anonymization.KAnonymity;
import approaches.Algorithm;
import controller.LatticeController;
import dataset.beans.Dataset;
import dataset.generator.DatasetGenerator;
import exception.DatasetNotFoundException;
import exception.IOPropertiesException;
import main.experimentation.bean.Result;
import main.experimentation.exceptions.ControllerNotFoundException;
import utils.CsvUtils;
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

    public static final double MAX_EVALUATION_TIME = 600000;      //expressed in millisec (10 minutes)

    protected Dataset dataset;
    protected List<List<Integer>> solutions;
    protected double executionTime;
    protected boolean outOfTime;

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

    public void saveInfoExperimentation(String algorithmName, KAnonymity kAnonymity, int indexRun) {
        List<Object> results = new ArrayList<>();

        String datasetName = dataset.getName();
        int numberOfAttributes = dataset.getColumns().size();

        ArrayList<Integer> bottomNode = kAnonymity.lowerBounds();
        ArrayList<Integer> topNode = kAnonymity.upperBounds();

        //Lattice size
        int latticeSize = 1;
        for (int i = 0; i < topNode.size(); i++) {
            latticeSize *= (topNode.get(i) - bottomNode.get(i) + 1);
        }

        if (this.outOfTime) {
            Result result = new Result(datasetName, indexRun, numberOfAttributes, algorithmName,
                    null, latticeSize, bottomNode, topNode, null);
            results.add(result);
        } else {
            for (List<Integer> solution : solutions) {
                Result tmpResult = new Result(datasetName, indexRun, numberOfAttributes, algorithmName,
                        executionTime, latticeSize, bottomNode, topNode, solution);
                results.add(tmpResult);
            }
        }

        CsvUtils.appendClassAsCsv(results, RESULTS_FILE_PATH);
    }
}
