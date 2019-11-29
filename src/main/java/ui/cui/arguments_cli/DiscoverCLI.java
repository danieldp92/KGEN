package ui.cui.arguments_cli;

import anonymization.KAnonymity;
import approaches.ola.OLAAlgorithm;
import dataset.beans.Dataset;
import exception.DatasetNotFoundException;
import exception.IOPropertiesException;
import exception.TooNodeException;
import runner.experimentation.bean.ExactResult;
import runner.experimentation.bean.Result;
import runner.experimentation.exceptions.ArgumentException;
import runner.experimentation.exceptions.LimitExceedException;
import runner.experimentation.util.ResultUtils;
import ui.cui.AnonymizationCLI;
import ui.cui.arguments.DiscoverArguments;
import utils.CsvUtils;
import utils.DatasetUtils;
import utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static runner.experimentation.util.ResultUtils.loadResultsFromCsv;

public class DiscoverCLI {
    public static final String CSV_EXTENSION = "csv";
    public static final String XLXS_EXTENSION = "xlsx";
    public static final String XLS_EXTENSION = "xls";

    public static void showCLI (DiscoverArguments discoverArguments) throws IOException, DatasetNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String algorithm = null;
        int config = 0;
        Dataset dataset = null;

        // Validation of resultFile
        try {
            resultValidation(discoverArguments.getResultPath());
        } catch (ArgumentException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

        // Load results
        File resultFile = new File(discoverArguments.getResultPath());
        String extResult = FileUtils.getFileExtension(resultFile);

        List<Result> results = null;
        if (extResult.equals("csv")) {
            results = ResultUtils.loadResultsFromCsv(discoverArguments.getResultPath());
        } else {
            // TO DO
        }

        printQIALGTable(results);

        System.exit(0); // END FIRST TEST BLOCK
        // ##############################################################################

        // Read algorithm
        int choose = 0;
        System.out.println("Choose the metaheuristic to validate");
        System.out.println("1) KGEN");
        System.out.println("2) RANDOM");

        while (choose <= 0 || choose >= 3) {
            System.out.print("Choose: ");

            try {
                choose = Integer.parseInt(br.readLine());
                if (choose <= 0 || choose >= 3) {
                    System.out.println("The value must be 1 or 2");
                }
            } catch (NumberFormatException ex) {
                System.out.println("The value inserted is not a number");
            }
        }

        if (choose == 1) {
            algorithm = "KGEN";
        } else {
            algorithm = "RANDOM";
        }

        List<Integer> qiOfAlgorithm = ResultUtils.extractQIAnalyzedOfAlgorithm(results, algorithm);

        // Read config
        choose = 0;
        System.out.println("Choose the configuration to validate");
        for (int i = 1; i <= qiOfAlgorithm.size(); i++) {
            System.out.println(i + ") " + qiOfAlgorithm.get(i-1));
        }

        while (choose <= 0 || choose >= qiOfAlgorithm.size()+1) {
            System.out.print("Choose: ");

            try {
                choose = Integer.parseInt(br.readLine());
                if (choose <= 0 || choose >= 3) {
                    System.out.println("The value must be 1 or 2");
                }
            } catch (NumberFormatException ex) {
                System.out.println("The value inserted is not a number");
            }
        }

        // Read threshold
        double threshold = 0;
        System.out.println("Insert the threshold value");

        boolean validValue = false;
        while (!validValue) {
            System.out.print("Threshold value: ");

            try {
                threshold = Double.parseDouble(br.readLine());
                validValue = true;
            } catch (NumberFormatException ex) {
                System.out.println("The value inserted is not a number");
            }
        }

        config = qiOfAlgorithm.get(choose-1);

        List<Result> resultsToAnalyze = ResultUtils.extractResults(results, algorithm, config, false);

        ResultUtils.printResults(resultsToAnalyze);
        System.exit(0); // END SECOND TEST BLOCK

        // ##############################################################################

        dataset = DatasetUtils.readAndInit(discoverArguments.getDatasetPath(), discoverArguments.getConfigPath(), "?", ",");
        KAnonymity kAnonymity = new KAnonymity(dataset);

        OLAAlgorithm olaAlgorithm = new OLAAlgorithm(dataset, discoverArguments.getTreshold());
        List<Object> exactResult = new ArrayList<>();

        System.out.print("0% completed");
        for (int i = 0; i < resultsToAnalyze.size(); i++) {
            Result result = resultsToAnalyze.get(i);

            try {
                List<List<Integer>> exactSolutions = olaAlgorithm.run(new ArrayList<>(result.getBottomNode()), new ArrayList<>(result.getSolution()));

                for (List<Integer> exactSolution : exactSolutions) {
                    exactResult.add(new ExactResult(result.getDatasetName(), result.getNumberOfExperimentation(),
                            result.getAlgorithmName(), result.getSolution(), exactSolution));
                }
            } catch (TooNodeException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            } catch (LimitExceedException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }

            System.out.println("\r" + (((double)i/resultsToAnalyze.size()) * 100) + "% completed");
        }

        System.out.print("100% completed");

        CsvUtils.appendClassAsCsv(exactResult, discoverArguments.getOutputPath());
    }

    private static void resultValidation (String resultPath) throws ArgumentException {
        File resultFile = new File(resultPath);

        if (!resultFile.exists()) {
            throw new ArgumentException("ERROR! The result path inserted doesn't exist");
        }

        if (resultFile.isDirectory()) {
            throw new ArgumentException("ERROR! The result path inserted is not a file");
        }

        String ext = FileUtils.getFileExtension(resultFile);

        if (!Arrays.asList(AnonymizationCLI.VALID_DATASET_EXTENSIONS).contains(ext)) {
            throw new ArgumentException("ERROR! The extension of result file must be one of these: \"csv\", \"xls\" or \"xlsx\"");
        }
    }

    private static void printQIALGTable (List<Result> results) {
        List<String> algorithms = ResultUtils.extractAlgorithmAnalyzed(results);
        List<Integer> qiAnalyzed = ResultUtils.extractQIAnalyzed(results);

        System.out.print("\t");
        for (int i : qiAnalyzed) {
            System.out.print(i + "\t");
        }
        System.out.println();


        for (String algorithm : algorithms) {
            List<Integer> qiOfAlgorithm = ResultUtils.extractQIAnalyzedOfAlgorithm(results, algorithm);
            System.out.print(algorithm + "\t");

            for (int i : qiAnalyzed) {
                if (qiOfAlgorithm.contains(i)) {
                    System.out.print(i + "\t");
                } else {
                    System.out.print("-\t");
                }
            }
            System.out.println();
        }
    }
}
