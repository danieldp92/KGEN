package ui.cui.arguments_cli;

import runner.experimentation.thread.ExperimentationRunner;
import runner.experimentation.type.AlgorithmType;
import ui.cui.arguments.Arguments;
import ui.cui.arguments.ExperimentationArguments;
import ui.cui.utils.InputValidator;
import utils.FileUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NoArgumentCLI {
    public static final String [] DATASET_EXT_ALLOWED = {"csv", "xlsx", "xls"};
    public static final String [] CONFIG_EXT_ALLOWED = {"txt"};
    public static final String exitCLI = "q";
    public static final String SEPARATOR_TAG = "-";

    public static BufferedReader br;

    public static void showCLI() throws IOException {
        br = new BufferedReader(new InputStreamReader(System.in));


        String input = null;
        int choice = -1;

        System.out.println("Choose the task to run: (q for quit)");
        System.out.println("1) Experimentation");
        System.out.println("2) Dataset statistic info");

        choice = InputValidator.getChoice(1, 2, exitCLI);

        switch (choice) {
            case 1:
                // Experimentation
                ExperimentationArguments arguments = null;

                System.out.println("Anonymization tool\n");

                System.out.println("Choose the information source (q for quit): ");
                System.out.println("1) From file");
                System.out.println("2) Manually");

                choice = InputValidator.getChoice(1, 2, exitCLI);

                boolean invalidInput = false;

                switch (choice) {
                    case 1:
                        String configARGSPath = null;

                        do {
                            System.out.print("Argument file path: ");
                            configARGSPath = br.readLine();

                            if (!FileUtils.exist(configARGSPath) ||
                                    !Arrays.asList(CONFIG_EXT_ALLOWED).contains(FileUtils.getFileExtension(configARGSPath))) {
                                invalidInput = true;
                            } else {
                                invalidInput = false;
                            }

                        } while (invalidInput);

                        arguments = getArgumentsFromFile(configARGSPath);

                        break;
                    case 2:
                        // Variables
                        String datasetPath = null;
                        String configPath = null;
                        int numberOfRuns = 1;
                        double threshold = -1;

                        System.out.println("\nInsert the following informations:");

                        // Dataset
                        do {
                            System.out.print("Dataset Path: ");
                            datasetPath = br.readLine();

                            if (!FileUtils.exist(datasetPath)) {
                                System.out.println("\nThe file doesn't exist. Please, insert a valid input\n");
                                invalidInput = true;
                            } else if (!Arrays.asList(DATASET_EXT_ALLOWED).contains(FileUtils.getFileExtension(datasetPath))) {
                                System.out.println("The dataset must have one of the following extensions:\n\t- csv;\n\t- xlsx;\n\t- xls;\n");
                                invalidInput = true;
                            } else {
                                invalidInput = false;
                            }

                        } while (invalidInput);

                        // Config file
                        do {
                            System.out.print("Config Path: ");
                            configPath = br.readLine();

                            if (!FileUtils.exist(configPath)) {
                                System.out.println("\nThe file doesn't exist. Please, insert a valid input\n");
                                invalidInput = true;
                            } else if (!Arrays.asList(CONFIG_EXT_ALLOWED).contains(FileUtils.getFileExtension(configPath))) {
                                System.out.println("The dataset must have one of the following extensions:\n\t- txt;\n");
                                invalidInput = true;
                            } else {
                                invalidInput = false;
                            }

                        } while (invalidInput);

                        // Algorithms
                        System.out.println("\nChoose the algorithm (or more than one) to run:");
                        System.out.println("1) EXHAUSTIVE");
                        System.out.println("2) OLA");
                        System.out.println("3) KGEN");
                        System.out.println("4) RANDOM");
                        System.out.println("(If you want to choose two or more algorithms, use this syntax: 1-3-4)\n");

                        List<Integer> choices = InputValidator.getMultipleChoices(1, 4, exitCLI, SEPARATOR_TAG);
                        List<Integer> algorithmTypeList = new ArrayList<>();

                        for (int c : choices) {
                            switch (c) {
                                case 1:
                                    algorithmTypeList.add(AlgorithmType.EXHAUSTIVE_ALGORITHM);
                                    break;
                                case 2:
                                    algorithmTypeList.add(AlgorithmType.OLA_ALGORITHM);
                                    break;
                                case 3:
                                    algorithmTypeList.add(AlgorithmType.KGEN_ALGORITHM);
                                    break;
                                case 4:
                                    algorithmTypeList.add(AlgorithmType.RANDOM_ALGORITHM);
                                    break;
                                default:
                                    break;
                            }
                        }
                        System.out.println();


                        // Number of runs
                        if (algorithmTypeList.contains(AlgorithmType.KGEN_ALGORITHM) ||
                                algorithmTypeList.contains(AlgorithmType.RANDOM_ALGORITHM)) {

                            do {
                                System.out.print("Number of runs: ");
                                try {
                                    numberOfRuns = Integer.parseInt(br.readLine());
                                    invalidInput = false;
                                } catch (NumberFormatException ex) {
                                    System.out.println("\nInvalid input\n");
                                    invalidInput = true;
                                }
                            } while (invalidInput);
                        }


                        // Threshold
                        do {
                            System.out.print("Threshold: ");
                            try {
                                threshold = Double.parseDouble(br.readLine());
                                invalidInput = false;
                            } catch (NumberFormatException ex) {
                                System.out.println("\nInvalid input\n");
                                invalidInput = true;
                            }
                        } while (invalidInput);


                        arguments = new ExperimentationArguments(numberOfRuns, threshold, datasetPath, configPath, FileUtils.getDirOfJAR(), algorithmTypeList);
                        saveArguments(arguments, FileUtils.getDirOfJAR() + "args.txt");

                        break;
                    default:
                        break;
                }

                ExperimentationRunner experimentationRunner = new ExperimentationRunner(arguments);
                experimentationRunner.start();

                break;
            case 2:
                // Dataset statistic info
                System.out.println("Dataset info\n");

                System.out.println("Insert the dataset path: ");


                break;
            default:
                break;
        }


    }

    private static ExperimentationArguments getArgumentsFromFile(String path) throws IOException {
        List<String> config = FileUtils.loadFile(path);

        // Variables
        String datasetPath = null;
        String configPath = null;
        String outputPath = null;
        int numberOfRuns = 1;
        double threshold = -1;
        List<Integer> algorithms= new ArrayList<>();


        for (String line : config) {
            String [] split = line.split(":");
            switch (split[0]) {
                case "datasetPath":
                    datasetPath = line.substring(split[0].length()+1);
                    break;
                case "configPath":
                    configPath = line.substring(split[0].length()+1);
                    break;
                case "numberOfRuns":
                    numberOfRuns = Integer.parseInt(split[1]);
                    break;
                case "threshold":
                    threshold = Double.parseDouble(split[1]);
                    break;
                case "algorithmList":
                    String [] algorithmList = split[1].replaceAll(" ", "").split(",");
                    for (String algorithm : algorithmList) {
                        switch (algorithm) {
                            case "EXHAUSTIVE":
                                algorithms.add(AlgorithmType.EXHAUSTIVE_ALGORITHM);
                                break;
                            case "OLA":
                                algorithms.add(AlgorithmType.OLA_ALGORITHM);
                                break;
                            case "KGEN":
                                algorithms.add(AlgorithmType.KGEN_ALGORITHM);
                                break;
                            case "RANDOM":
                                algorithms.add(AlgorithmType.RANDOM_ALGORITHM);
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                case "outputPath":
                    outputPath = line.substring(split[0].length()+1);
                    break;
            }
        }

        return new ExperimentationArguments(numberOfRuns, threshold, datasetPath, configPath,
                outputPath, algorithms);
    }

    private static void saveArguments(ExperimentationArguments experimentationArguments, String savePath) throws FileNotFoundException {
        ArrayList<String> txt = new ArrayList<>();
        txt.add("datasetPath:" + experimentationArguments.getDatasetPath());
        txt.add("configPath:" + experimentationArguments.getConfigPath());
        txt.add("outputPath:" + experimentationArguments.getOutputPath());
        txt.add("numberOfRuns:" + experimentationArguments.getNumberOfRuns());
        txt.add("threshold:" + experimentationArguments.getThreshold());

        String algorithmList = "algorithmList:";
        for (int i = 0; i < experimentationArguments.getAlgorithmsList().size(); i++) {
            switch (experimentationArguments.getAlgorithmsList().get(i)) {
                case AlgorithmType.EXHAUSTIVE_ALGORITHM:
                    algorithmList += "EXHAUSTIVE";
                    break;
                case AlgorithmType.OLA_ALGORITHM:
                    algorithmList += "OLA";
                    break;
                case AlgorithmType.KGEN_ALGORITHM:
                    algorithmList += "KGEN";
                    break;
                case AlgorithmType.RANDOM_ALGORITHM:
                    algorithmList += "RANDOM";
                    break;
            }

            if (i < experimentationArguments.getAlgorithmsList().size()-1) {
                algorithmList += ", ";
            }
        }

        txt.add(algorithmList);

        FileUtils.saveFile(txt, savePath);
    }
}
