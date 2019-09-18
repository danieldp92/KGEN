package ui.cui;

import exception.DatasetNotFoundException;
import runner.experimentation.*;
import runner.experimentation.bean.Result;
import runner.experimentation.bean.Stat;
import runner.experimentation.exceptions.ArgumentException;
import runner.experimentation.thread.ExperimentationRunner;
import runner.experimentation.type.AlgorithmType;
import runner.experimentation.util.ResultUtils;
import runner.experimentation.util.StatisticalUtils;
import ui.UI;
import ui.cui.arguments.*;
import utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnonymizationCLI implements UI {
    public static final String [] EXPERIMENTATION_ARGUMENTS = {"-e", "--experimentation"};
    public static final String [] ALGORITHM_ARGUMENTS = {"-a", "--algorithm"};
    public static final String [] CONFIG_ARGUMENTS = {"-c", "--config"};
    public static final String [] SAVE_ARGUMENTS = {"-o"};
    public static final String [] STAT_ARGUMENTS = {"-s", "--stat"};
    public static final String [] HELP_ARGUMENTS = {"-h", "--help"};

    private static final String [] VALID_DATASET_EXTENSIONS = {"csv", "xlsx", "xls"};
    private static final String [] ALGORITHM_NAMES = {"EXHAUSTIVE", "OLA", "KGEN", "RANDOM"};
    private static final String [] CONFIG_ID_TYPE = {"i", "qi"};
    private static final String [] CONFIG_DATE_TYPE = {"int", "double", "string", "date", "place"};
    private static final String [] CONFIG_PK = {"true", "false"};

    private static final String SEPARATOR_TAG = ":";

    public static final String RESULT_NAME = "result.csv";
    public static final String STAT_NAME = "stat.csv";

    @Override
    public void run(String [] args) throws ArgumentException {
        Arguments arguments = argumentValidation(args);

        if (arguments instanceof ExperimentationArguments) {
            ExperimentationRunner experimentationRunner = new ExperimentationRunner((ExperimentationArguments) arguments);
            experimentationRunner.start();
        }

        else if (arguments instanceof AlgorithmArguments) {
            Experimentation experimentation = null;
            AlgorithmArguments algorithmArguments = (AlgorithmArguments) arguments;

            // If the path is null, use the directory of the jar file
            if (algorithmArguments.getOutputPath() == null) {
                String outputDir = FileUtils.getDirOfJAR();
                algorithmArguments.setOutputPath(outputDir);
            }

            switch (algorithmArguments.getAlgorithmType()) {
                case AlgorithmType.EXHAUSTIVE_ALGORITHM:
                    experimentation = new ExhaustiveExperimentation(algorithmArguments.getOutputPath() + RESULT_NAME);
                    break;
                case AlgorithmType.OLA_ALGORITHM:
                    experimentation = new OLAExperimentation(algorithmArguments.getOutputPath() + RESULT_NAME);
                    break;
                case AlgorithmType.KGEN_ALGORITHM:
                    experimentation = new KGENExperimentation(algorithmArguments.getOutputPath() + RESULT_NAME);
                    break;
                case AlgorithmType.RANDOM_ALGORITHM:
                    experimentation = new RandomSearchExperimentation(algorithmArguments.getOutputPath() + RESULT_NAME);
                    break;
            }

            try {
                experimentation.initDataset(algorithmArguments.getDatasetPath(), algorithmArguments.getConfigPath());
                experimentation.execute(1, algorithmArguments.getTreshold());
            } catch (DatasetNotFoundException  e) {
                e.printStackTrace();
            }

            List<Result> results = ResultUtils.loadResultsFromCsv(algorithmArguments.getOutputPath() + RESULT_NAME);
            List<Stat> stats = StatisticalUtils.getStatsOfResults(results);
            StatisticalUtils.saveStatsIntoCsv(stats,algorithmArguments.getOutputPath() + STAT_NAME);
        }

        else if (arguments instanceof ConfigArguments) {
            ConfigArguments configArguments = (ConfigArguments) arguments;
            ConfigGenerator.generateConfigFileFromCLI(configArguments.getDatasetPath(), configArguments.getOutputPath());
        }

        else if (arguments instanceof StatArguments) {
            StatArguments statArguments = (StatArguments) arguments;
            List<Result> results = ResultUtils.loadResultsFromCsv(statArguments.getResultPath());

            // If the path is null, use the directory of the jar file
            if (statArguments.getOutputPath() == null) {
                String outputDir = FileUtils.getDirOfJAR();
                statArguments.setOutputPath(outputDir);
            }

            List<Stat> stats = StatisticalUtils.getStatsOfResults(results);

            File statFile = new File(statArguments.getOutputPath() + STAT_NAME);
            if (statFile.exists()) {
                statFile.delete();
            }

            StatisticalUtils.saveStatsIntoCsv(stats, statArguments.getOutputPath() + STAT_NAME);
        }

        else {
            showHelpMenu();
        }
    }

    /**
     * This method check if the command inserted is valid
     * @param args, the arguments passed as arguments
     * @return the argument choosen
     * @throws ArgumentException
     */
    public Arguments argumentValidation(String [] args) throws ArgumentException {
        Arguments arguments = null;

        if (args.length <= 0) {
            throw new ArgumentException("ERROR! Please insert an option. To see all valid options, please use the command -h or --help");
        }

        //Extract the output from arguments
        String outputPath = extractOutputFromArguments(args);
        if (outputPath != null) {
            args = removeOArgs(args);

            // Check if the path is a directory
            File outputDir = new File(outputPath);
            if (!outputDir.exists()) {
                throw new ArgumentException("ERROR! The outputPath doesn't exist. Please, insert a valid path");
            } else if (outputDir.isFile()) {
                throw new ArgumentException("ERROR! The outputPath must be a directory");
            }
        }

        // Check the option

        // Experimentation command -> -e numberOfRuns <datasetPath> <configFilePath>
        // Algorithm command -> -a algorithmName <datasetPath> <configFilePath>
        if (Arrays.asList(EXPERIMENTATION_ARGUMENTS).contains(args[0]) || Arrays.asList(ALGORITHM_ARGUMENTS).contains(args[0])) {
            int numberOfExperimentation = 0;
            int algorithmType = -1;
            double treshold = 0;
            String datasetPath = null;
            String configFilePath = null;

            if (args.length != 5) {
                if (Arrays.asList(EXPERIMENTATION_ARGUMENTS).contains(args[0])) {
                    throw new ArgumentException("ERROR! When you want to start an experimentation, please use this format:\n" +
                            "-e numberOfRuns treshold <datasetPath> <configFilePath>");
                } else {
                    throw new ArgumentException("ERROR! When you want to start an algorithm, please use this format:\n" +
                            "-a algorithmName treshold <datasetPath> <configFilePath>");
                }
            }

            // -e -> args[1] -> Number
            if (Arrays.asList(EXPERIMENTATION_ARGUMENTS).contains(args[0])) {
                try {
                    numberOfExperimentation = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex) {
                    throw new ArgumentException("ERROR: Invalid argument. The number of experimentation must be a number!!!");
                }
            }

            // -a -> args[1] -> Algorithm name
            else {
                String algorithmName = args[1].toUpperCase();
                if (!Arrays.asList(ALGORITHM_NAMES).contains(algorithmName)) {
                    throw new ArgumentException("ERROR! Please, insert only one of the follow valid algorithm:\n" +
                            "\t- EXHAUSTIVE\n\t- OLA\n\t- KGEN\n\t- RANDOM");
                }

                switch (algorithmName) {
                    case "EXHAUSTIVE":
                        algorithmType = AlgorithmType.EXHAUSTIVE_ALGORITHM;
                        break;
                    case "OLA":
                        algorithmType = AlgorithmType.OLA_ALGORITHM;
                        break;
                    case "KGEN":
                        algorithmType = AlgorithmType.KGEN_ALGORITHM;
                        break;
                    case "RANDOM":
                        algorithmType = AlgorithmType.RANDOM_ALGORITHM;
                        break;
                }
            }

            // args[2] -> Treshold
            try {
                treshold = Double.parseDouble(args[2]);
                if (treshold < 0 || treshold > 1) {
                    throw new ArgumentException("ERROR: Invalid treshold. The treshold must be expressed as a number between 0 and 1!!!");
                }
            } catch (NumberFormatException ex) {
                throw new ArgumentException("ERROR: Invalid argument. The treshold must be expressed as a number between 0 and 1!!!");
            }

            // args[3] -> Dataset Path
            datasetPath = args[3];
            File datasetFile = new File(datasetPath);
            if (!datasetFile.exists()) {
                throw new ArgumentException("ERROR! No dataset found in the path " + datasetPath);
            } else if (!Arrays.asList(VALID_DATASET_EXTENSIONS).contains(FileUtils.getFileExtension(datasetFile))) {
                throw new ArgumentException("ERROR! The only extension accepted for the dataset are the follows:\n " +
                        "\t- csv\n\t- xlsx\n\t- xls");
            }

            // args[4] -> Config Path
            configFilePath = args[4];
            File configFile = new File(configFilePath);
            if (!configFile.exists()) {
                throw new ArgumentException("ERROR! No config file found in the path " + configFilePath);
            } else if (!FileUtils.getFileExtension(configFile).equals("txt")) {
                throw new ArgumentException("ERROR! Config file must be a txt file");
            } else {
                try {
                    if (!isConfigFileValid(configFilePath)) {
                        throw new ArgumentException("ERROR! The config file passed is not well formed");
                    }
                } catch (IOException e) {
                    throw new ArgumentException("ERROR! Impossible to load config file");
                }
            }


            if (Arrays.asList(EXPERIMENTATION_ARGUMENTS).contains(args[0])) {
                arguments = new ExperimentationArguments(numberOfExperimentation, treshold, datasetPath, configFilePath, outputPath);
            } else {
                arguments = new AlgorithmArguments(algorithmType, treshold, datasetPath, configFilePath, outputPath);
            }
        }

        // Config command -> -c <datasetPath>
        else if (Arrays.asList(CONFIG_ARGUMENTS).contains(args[0])) {
            if (args.length != 2) {
                throw new ArgumentException("ERROR! When you want to start an experimentation, please use this format:\n" +
                        "-c <datasetPath>");
            }

            // args[1] -> Dataset Path
            String datasetPath = args[1];
            File datasetFile = new File(datasetPath);
            if (!datasetFile.exists()) {
                throw new ArgumentException("ERROR! No dataset found in the path " + datasetPath);
            } else if (!Arrays.asList(VALID_DATASET_EXTENSIONS).contains(FileUtils.getFileExtension(datasetFile))) {
                throw new ArgumentException("ERROR! The only extension accepted for the dataset are the follows:\n " +
                        "\t- csv\n\t- xlsx\n\t- xls");
            }

            arguments = new ConfigArguments(datasetPath, outputPath);
        }

        // Stat command -> -s
        else if (Arrays.asList(STAT_ARGUMENTS).contains(args[0])) {
            if (args.length != 2) {
                throw new ArgumentException("ERROR! When you want to start stat generator, please use this format:\n" +
                        "-s <resultPath>");
            }

            String resultPath = args[1];
            File resultFile = new File(resultPath);
            if (!resultFile.exists()) {
                throw new ArgumentException("ERROR! No result found in the path " + resultPath);
            } else if (!Arrays.asList(VALID_DATASET_EXTENSIONS).contains(FileUtils.getFileExtension(resultFile))) {
                throw new ArgumentException("ERROR! The only extension accepted for result file are the follows:\n " +
                        "\t- csv\n\t- xlsx\n\t- xls");
            }

            arguments = new StatArguments(resultPath, outputPath);
        }

        // Help command -> -h
        else if (Arrays.asList(HELP_ARGUMENTS).contains(args[0])) {
            if (args.length != 1) {
                throw new ArgumentException("ERROR! When you want to start an experimentation, please use this format:\n" +
                        "\t-h");
            }

            if (outputPath != null) {
                throw new ArgumentException("ERROR! Help argument cannot have an output! Please use this format:\n" +
                        "\t-h");
            }

            arguments = new HelpArguments();
        }

        else {
            throw new ArgumentException("Invalid option. To see all valid options, please use the command -h or --help");
        }

        return arguments;
    }


    private String extractOutputFromArguments (String [] args) {
        String outputFile = null;

        for (int i = 0; i < args.length; i++) {
            if (Arrays.asList(SAVE_ARGUMENTS).contains(args[i]) && (i+1) < args.length) {
                outputFile = args[i+1];
                break;
            }
        }

        return outputFile;
    }

    private String [] removeOArgs (String [] args) {
        List<String> tmpArgs = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (Arrays.asList(SAVE_ARGUMENTS).contains(args[i]) && (i+1) < args.length) {
                i++;
            } else {
                tmpArgs.add(args[i]);
            }
        }

        String [] newArgs = new String[tmpArgs.size()];
        for (int i = 0; i < tmpArgs.size(); i++) {
            newArgs[i] = tmpArgs.get(i);
        }

        return newArgs;
    }

    private static boolean isConfigFileValid (String configFilePath) throws IOException {
        List<String> configTxt = FileUtils.loadFile(configFilePath);

        if (configTxt.isEmpty()) {
            return false;
        } else {
            // Remove the header and check if there are other attributes
            configTxt.remove(0);
            if (configTxt.isEmpty()) {
                return false;
            }
        }

        for (String line : configTxt) {
            String [] split = line.split(SEPARATOR_TAG);
            if (split.length != 4) {
                return false;
            }

            // Check on split[1]
            String idType = split[1];
            if (!Arrays.asList(CONFIG_ID_TYPE).contains(idType)) {
                return false;
            }

            // Check on split[2]
            String dateType = split[2];
            if (!Arrays.asList(CONFIG_DATE_TYPE).contains(dateType)) {
                return false;
            }

            // Check on split[3]
            String pk = split[3];
            if (!Arrays.asList(CONFIG_PK).contains(pk)) {
                return false;
            }
        }

        return true;
    }

    private static void showHelpMenu () {
        System.out.println("\tOptions\t\t\t\tDescription");

        System.out.println("\t-a --algorithm" +
                "\t\t\tChoose an algorithm for the anonymization of a dataset.\n" +
                "\t\t\t\t\tIt's possible to choose 4 algorithms: EXHAUSTIVE, OLA, KGEN, RANDOM\n" +
                "\t\t\t\t\tCommand line format: -a <algorithmName> <treshold> <datasetPath> <configPath>\n");

        System.out.println("\t-c --config" +
                "\t\t\tGenerate a config file, that it's necessary to start the algorithm.\n" +
                "\t\t\t\t\tCommand line format: -c <datasetPath>\n");
        System.out.println("\t-e --experimentation" +
                "\t\tRun the entire experimentation, comparing all the algorithms\n" +
                "\t\t\t\t\tavailable in the tool\n" +
                "\t\t\t\t\tCommand line format: -e <numberOfRuns> <treshold> <datasetPath> <configPath>\n");
        System.out.println("\t-s --stat" +
                "\t\t\tRun the stat generator. From results generated by an experimentation,\n " +
                "\t\t\t\t\tit's possible to extract all relevant statistical informations\n" +
                "\t\t\t\t\tCommand line format: -s <resultPath>\n");
        System.out.println("\t-o" +
                "\t\t\t\tAllow you to choose the destination folder for the output\n" +
                "\t\t\t\t\tExample of usage: -o <outputPath>\n");
        System.out.println("\t-h --help" +
                "\t\t\tShow help menu");
    }
}
