package runner.experimentation.thread;

import anonymization.AnonymizationReport;
import anonymization.KAnonymity;
import com.sun.org.apache.regexp.internal.RE;
import exception.DatasetNotFoundException;
import org.apache.commons.math3.stat.StatUtils;
import runner.Main;
import runner.experimentation.*;
import runner.experimentation.bean.Stat;
import runner.experimentation.bean.Result;
import runner.experimentation.type.AlgorithmType;
import runner.experimentation.util.StatisticalUtils;
import ui.cui.arguments.ExperimentationArguments;
import utils.FileUtils;
import runner.experimentation.util.ResultUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExperimentationThread extends Thread {
    public static final String RESULT_NAME = "result.csv";
    public static final String STAT_NAME = "stat.csv";

    private String datasetPath;
    private String configPath;
    private String resultPath;
    private int numberOfRuns;
    private double suppressionTreshold;


    public ExperimentationThread (ExperimentationArguments experimentationArguments) {
        this.datasetPath = experimentationArguments.getDatasetPath();
        this.configPath = experimentationArguments.getConfigPath();
        this.numberOfRuns = experimentationArguments.getNumberOfRuns();
        this.suppressionTreshold = experimentationArguments.getTreshold();

        if (experimentationArguments.getOutputPath() == null) {
            this.resultPath = FileUtils.getDirOfJAR();
        } else {
            this.resultPath = experimentationArguments.getOutputPath() + File.separator;
        }
    }

    @Override
    public void run() {
        List<Result> results = new ArrayList<>();
        List<Integer> algorithmTypes = new ArrayList<>();
        algorithmTypes.add(AlgorithmType.EXHAUSTIVE_ALGORITHM);
        algorithmTypes.add(AlgorithmType.OLA_ALGORITHM);
        algorithmTypes.add(AlgorithmType.KGEN_ALGORITHM);
        algorithmTypes.add(AlgorithmType.RANDOM_ALGORITHM);

        Experimentation experimentation = null;

        if (Main.SHOW_LOG_MESSAGE) System.out.println("\nCONFIG: " + configPath);

        for (int algorithmType : algorithmTypes) {
            switch (algorithmType) {
                case AlgorithmType.OLA_ALGORITHM:
                    experimentation = new OLAExperimentation(resultPath + RESULT_NAME);

                    break;
                case AlgorithmType.EXHAUSTIVE_ALGORITHM:
                    experimentation = new ExhaustiveExperimentation(resultPath + RESULT_NAME);

                    break;
                case AlgorithmType.KGEN_ALGORITHM:
                    experimentation = new KGENExperimentation(resultPath + RESULT_NAME);

                    break;
                case AlgorithmType.RANDOM_ALGORITHM:
                    experimentation = new RandomSearchExperimentation(resultPath + RESULT_NAME);

                    break;
                default: break;
            }


            //Initialize the dataset
            try {
                experimentation.initDataset(datasetPath, configPath);
            } catch (DatasetNotFoundException e) {
                System.out.println("Dataset not found");
                System.exit(0);
            }

            try {
                experimentation.execute(numberOfRuns, suppressionTreshold);
                results.addAll(experimentation.getResults());
            } catch (DatasetNotFoundException e) {
                System.out.println("Dataset not found");
                System.exit(0);
            }
        }

        List<Stat> stats = null;
        try {
            stats = StatisticalUtils.getStatsOfResults(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StatisticalUtils.saveStatsIntoCsv(stats, this.resultPath + STAT_NAME);

        System.exit(0);
    }

    private Result getStub () {
        String datasetName = "F2_DATASET";
        int numberOfAttributes = 57;
        int numberOfAttributesAnalyzed = 43;
        int numberOfExperimentation = 1;
        String algorithmName = "STUB";
        Double executionTime = 5.55;
        int latticeSize = 30;

        List<Integer> bottomNode = new ArrayList<>();
        bottomNode.add(0);
        bottomNode.add(0);
        bottomNode.add(0);

        List<Integer> topNode = new ArrayList<>();
        topNode.add(3);
        topNode.add(3);
        topNode.add(3);

        List<Integer> solution = new ArrayList<>();
        solution.add(2);
        solution.add(2);
        solution.add(2);

        // Report
        AnonymizationReport report = new AnonymizationReport();

        double logMetric = 0.5;
        int kValue = 2;
        int kValueWithSuppression = 2;
        double percentageOfSuppression = 0;
        List<Integer> rowToDelete = new ArrayList<>();

        report.setLevelOfAnonymization(solution);
        report.setLogMetric(logMetric);
        report.setkValue(kValue);
        report.setkValueWithSuppression(kValueWithSuppression);
        report.setPercentageOfSuppression(percentageOfSuppression);
        report.setRowToDelete(rowToDelete);

        return new Result(datasetName, numberOfExperimentation, numberOfAttributes, algorithmName, executionTime,
                latticeSize, bottomNode, topNode, report);
    }

}
