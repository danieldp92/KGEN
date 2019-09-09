package runner.experimentation.thread;

import anonymization.AnonymizationReport;
import controller.LatticeController;
import exception.DatasetNotFoundException;
import org.omg.CORBA.RepositoryIdHelper;
import runner.Main;
import runner.experimentation.*;
import runner.experimentation.bean.Result;
import runner.experimentation.exceptions.ControllerNotFoundException;
import runner.experimentation.type.AlgorithmType;
import runner.experimentation.type.DatasetType;
import ui.cui.arguments.ExperimentationArguments;
import utils.CsvUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ExperimentationThread extends Thread {
    public static final String RESULT_NAME = "result.csv";

    private ReentrantLock lock;
    private boolean unlock;
    private LatticeController latticeController;

    private int datasetType;

    private String datasetPath;
    private String configPath;
    private String resultPath;
    private int numberOfRuns;
    private double suppressionTreshold;


    public ExperimentationThread (int datasetType, int numberOfRuns) {
        this.lock = new ReentrantLock();

        this.datasetType = datasetType;
        this.numberOfRuns = numberOfRuns;

        this.unlock = false;
    }

    public ExperimentationThread (ExperimentationArguments experimentationArguments) {
        String jarPath = null;
        try {
            jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replaceAll("/", "\\\\");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        this.datasetPath = experimentationArguments.getDatasetPath();
        this.configPath = experimentationArguments.getConfigPath();
        this.numberOfRuns = experimentationArguments.getNumberOfRuns();
        this.suppressionTreshold = experimentationArguments.getTreshold();

        if (experimentationArguments.getOutputPath() == null) {
            this.resultPath = new File(jarPath).getParent() + File.separator + RESULT_NAME;
        } else {
            this.resultPath = experimentationArguments.getOutputPath();
        }

        //this.resultPath = "C:\\Users\\20190482\\Documents\\GitHub\\KGEN\\out\\artifacts\\KGEN_jar" + File.separator + RESULT_NAME;

        this.lock = new ReentrantLock();
    }

    @Override
    public void run() {
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
                    experimentation = new OLAExperimentation(resultPath);

                    break;
                case AlgorithmType.EXHAUSTIVE_ALGORITHM:
                    experimentation = new ExhaustiveExperimentation(resultPath);

                    break;
                case AlgorithmType.KGEN_ALGORITHM:
                    experimentation = new KGENExperimentation(resultPath);

                    break;
                case AlgorithmType.RANDOM_ALGORITHM:
                    experimentation = new RandomSearchExperimentation(resultPath);

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
            } catch (DatasetNotFoundException | ControllerNotFoundException e) {
                if (e instanceof DatasetNotFoundException) {
                    System.out.println("Dataset not found");
                } else {
                    System.out.println("Controller not configured");
                }

                System.exit(0);
            }
        }











        /*File resultFile = new File(Experimentation.RESULTS_FILE_PATH);
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
            if (Main.SHOW_LOG_MESSAGE) System.out.println("\nCONFIG: " + configPaths.get(i));

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
                    experimentation.execute(numberOfRuns, SUPPRESSION_TRESHOLD);
                    //if (i == 2) System.exit(0);

                } catch (DatasetNotFoundException | ControllerNotFoundException e) {
                    if (e instanceof DatasetNotFoundException) {
                        System.out.println("Dataset not found");
                    } else {
                        System.out.println("Controller not configured");
                    }

                    System.exit(0);
                }
            }
        }*/

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


        List<List<Integer>> bestSolutions = new ArrayList<>();

        List<Integer> bestSol1 = new ArrayList<>();
        bestSol1.add(1);
        bestSol1.add(2);
        bestSol1.add(1);

        List<Integer> bestSol2 = new ArrayList<>();
        bestSol2.add(1);
        bestSol2.add(1);
        bestSol2.add(2);

        bestSolutions.add(bestSol1);
        bestSolutions.add(bestSol2);

        return new Result(datasetName, numberOfExperimentation, numberOfAttributes, algorithmName, executionTime,
                latticeSize, bottomNode, topNode, report, bestSolutions);
    }
}
