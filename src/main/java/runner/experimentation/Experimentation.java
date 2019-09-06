package runner.experimentation;

import anonymization.AnonymizationReport;
import anonymization.KAnonymity;
import controller.LatticeController;
import dataset.beans.Dataset;
import dataset.generator.DatasetGenerator;
import exception.DatasetNotFoundException;
import exception.IOPropertiesException;
import runner.experimentation.bean.Result;
import runner.experimentation.exceptions.ControllerNotFoundException;
import utils.CsvUtils;
import utils.DatasetUtils;
import utils.XlsUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Experimentation {
    private static final double MAX_EVALUATION_TIME_MIN = 2;
    public static final double MAX_EVALUATION_TIME = MAX_EVALUATION_TIME_MIN * 60 * 1000;      //expressed in millisec (10 minutes)

    protected Dataset dataset;
    protected List<List<Integer>> solutions;
    protected double executionTime;

    protected String resultPath;

    protected LatticeController latticeController;

    public Experimentation(String resultPath) {
        this.resultPath = resultPath;
    }

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

    abstract public void execute(int numberOfRun, double suppressionTreshold) throws DatasetNotFoundException, ControllerNotFoundException;

    public void saveInfoExperimentation(String algorithmName, KAnonymity kAnonymity, int indexRun, List<List<List<Integer>>> allExactSolutions) {
        List<Object> results = new ArrayList<>();

        String datasetName = dataset.getName();
        int numberOfAttributes = dataset.getColumns().size();

        if (kAnonymity == null) {
            Result result = new Result(datasetName, indexRun, numberOfAttributes, algorithmName,
                    null, -1, null, null, null, null);
            results.add(result);
        } else {
            ArrayList<Integer> bottomNode = kAnonymity.lowerBounds();
            ArrayList<Integer> topNode = kAnonymity.upperBounds();

            //Lattice size
            int latticeSize = 1;
            for (int i = 0; i < topNode.size(); i++) {
                latticeSize *= (topNode.get(i) - bottomNode.get(i) + 1);
            }

            if (this.solutions == null) {
                Result result = new Result(datasetName, indexRun, numberOfAttributes, algorithmName,
                        null, latticeSize, bottomNode, topNode, null, null);
                results.add(result);
            } else {
                for (int i = 0; i < solutions.size(); i++) {
                    List<Integer> solution = solutions.get(i);
                    List<List<Integer>> bestExactSolutions = null;
                    if (allExactSolutions != null) {
                        bestExactSolutions = allExactSolutions.get(i);
                    }

                    AnonymizationReport report = kAnonymity.getHistoryReports().get(solution.toString().hashCode());
                    Result tmpResult = new Result(datasetName, indexRun, numberOfAttributes, algorithmName,
                            executionTime, latticeSize, bottomNode, topNode, report, bestExactSolutions);
                    results.add(tmpResult);
                }
            }
        }

        CsvUtils.appendClassAsCsv(results, resultPath);
    }


}
