package runner.experimentation;

import anonymization.AnonymizationReport;
import anonymization.KAnonymity;
import dataset.beans.Dataset;
import dataset.generator.DatasetGenerator;
import exception.DatasetNotFoundException;
import exception.IOPropertiesException;
import runner.experimentation.bean.Result;
import utils.DatasetUtils;
import runner.experimentation.util.ResultUtils;
import utils.FileUtils;
import utils.ObjectUtils;
import utils.XlsUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Experimentation {
    private static final double MAX_EVALUATION_TIME_MIN = 900;
    public static final double MAX_EVALUATION_TIME = MAX_EVALUATION_TIME_MIN * 60 * 1000;      //expressed in millisec

    public static final String CSV_EXTENSION = "csv";
    public static final String XLXS_EXTENSION = "xlsx";
    public static final String XLS_EXTENSION = "xls";

    private List<Result> results;

    protected Dataset dataset;
    protected List<List<Integer>> solutions;
    protected double executionTime;

    protected String resultPath;

    public Experimentation(String resultPath) {
        this.resultPath = resultPath;
        this.results = new ArrayList<>();
    }

    public void initDataset (String datasetPath, String configPath, String nullValue) throws DatasetNotFoundException {
        File datasetFile = new File(datasetPath);

        if (datasetFile.exists()) {
            String datasetName = "";

            String [] split = datasetFile.getName().split("\\.");
            for (int i = 0; i < split.length-1; i++) {
                datasetName += split[i];
            }

            String datasetExtension = FileUtils.getFileExtension(datasetFile);
            try {
                if (datasetExtension.equals(XLS_EXTENSION) || datasetExtension.equals(XLXS_EXTENSION)) {
                    this.dataset = DatasetUtils.readFromXls(datasetPath);
                } else if (datasetExtension.equals(CSV_EXTENSION)) {
                    this.dataset = DatasetUtils.readFromCSV(datasetPath, nullValue);
                } else {
                    throw new DatasetNotFoundException();
                }
            } catch (IOException e) {
                throw new DatasetNotFoundException();
            }


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

    public List<Result> getResults() {
        return results;
    }

    abstract public void execute(int numberOfRun, double suppressionTreshold) throws DatasetNotFoundException;

    public void saveInfoExperimentation(String algorithmName, KAnonymity kAnonymity, int indexRun) {
        String resultsObjectPath = resultPath.substring(0, resultPath.lastIndexOf(File.separator)+1) + algorithmName + "_results";

        List<Result> newResults = getResults(algorithmName, kAnonymity, indexRun);
        this.results.addAll(newResults);

        // Save result object in a tmp file
        if (this.results != null) {
            try {
                ObjectUtils.writerObject(this.results, resultsObjectPath);
            } catch (IOException e) {}
        }


        try {
            ResultUtils.saveResultsIntoCsv(newResults, resultPath);

            // Delete tmp file previously created
            File tmpFile = new File(resultsObjectPath);
            tmpFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Result> getResults (String algorithmName, KAnonymity kAnonymity, int indexRun) {
        List<Result> results = new ArrayList<>();

        String datasetName = dataset.getName();
        int numberOfAttributes = dataset.getColumns().size();

        if (kAnonymity == null) {
            Result result = new Result(datasetName, indexRun, numberOfAttributes, algorithmName,
                    null, null, null, null, null);
            results.add(result);
        } else {
            List<Integer> bottomNode = kAnonymity.lowerBounds;
            List<Integer> topNode = kAnonymity.upperBounds;

            //Lattice size
            Integer latticeSize = 1;
            for (int i = 0; i < topNode.size(); i++) {
                latticeSize *= (topNode.get(i) - bottomNode.get(i) + 1);
            }
            if (latticeSize < 0) {
                latticeSize = null;
            }

            if (this.solutions == null) {
                Result result = new Result(datasetName, indexRun, numberOfAttributes, algorithmName,
                        null, latticeSize, bottomNode, topNode, null);
                results.add(result);
            } else {
                for (int i = 0; i < solutions.size(); i++) {
                    List<Integer> solution = solutions.get(i);
                    executionTime = this.executionTime;

                    AnonymizationReport report = kAnonymity.getHistoryReports().get(solution.toString().hashCode());
                    Result tmpResult = new Result(datasetName, indexRun, numberOfAttributes, algorithmName,
                            executionTime, latticeSize, bottomNode, topNode, report);
                    results.add(tmpResult);
                }
            }
        }

        return results;
    }
}
