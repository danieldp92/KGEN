package runner.experimentation.thread;

import exception.DatasetNotFoundException;
import runner.Main;
import runner.experimentation.*;
import runner.experimentation.bean.Stat;
import runner.experimentation.bean.Result;
import runner.experimentation.type.AlgorithmType;
import runner.experimentation.util.StatisticalUtils;
import ui.cui.arguments.ExperimentationArguments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExperimentationRunner {
    public static final String RESULT_NAME = "result.csv";
    public static final String STAT_NAME = "stat.csv";

    private String datasetPath;
    private String configPath;
    private String resultPath;
    private int numberOfRuns;
    private double suppressionTreshold;
    private List<Integer> algorithmTypes;


    public ExperimentationRunner(ExperimentationArguments experimentationArguments) {
        this.datasetPath = experimentationArguments.getDatasetPath();
        this.configPath = experimentationArguments.getConfigPath();
        this.numberOfRuns = experimentationArguments.getNumberOfRuns();
        this.suppressionTreshold = experimentationArguments.getThreshold();
        this.resultPath = experimentationArguments.getOutputPath();
        this.algorithmTypes = experimentationArguments.getAlgorithmsList();

        char [] tmp = this.resultPath.toCharArray();
        String lastCh = String.valueOf(tmp[tmp.length-1]);

        if (!lastCh.equals(File.separator)) {
            this.resultPath += File.separator;
        }
    }

    public void start() {
        List<Result> results = new ArrayList<>();

        Experimentation experimentation = null;

        if (Main.SHOW_LOG_MESSAGE) System.out.println("\nCONFIG: " + configPath);
        File configFile = new File(configPath);

        if (algorithmTypes != null) {
            for (int algorithmType : algorithmTypes) {

                switch (algorithmType) {
                    case AlgorithmType.OLA_ALGORITHM:
                        experimentation = new OLAExperimentation(resultPath + configFile.getName() + RESULT_NAME);

                        break;
                    case AlgorithmType.EXHAUSTIVE_ALGORITHM:
                        experimentation = new ExhaustiveExperimentation(resultPath + configFile.getName() + RESULT_NAME);

                        break;
                    case AlgorithmType.KGEN_ALGORITHM:
                        experimentation = new KGENExperimentation(resultPath + configFile.getName() + RESULT_NAME);

                        break;
                    case AlgorithmType.RANDOM_ALGORITHM:
                        experimentation = new RandomSearchExperimentation(resultPath + configFile.getName() + RESULT_NAME);

                        break;
                    default: break;
                }


                //Initialize the dataset
                try {
                    experimentation.initDataset(datasetPath, configPath, "?");
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

            List<Stat> stats = StatisticalUtils.getStatsOfResults(results);
            StatisticalUtils.saveStatsIntoCsv(stats, this.resultPath + configFile.getName() + STAT_NAME);
        }
    }
}
