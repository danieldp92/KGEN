package main.Experimentation;

import approaches.geneticalgorithm.AnonymizationAlgorithm;
import approaches.geneticalgorithm.AnonymizationProblem;
import approaches.geneticalgorithm.AnonymizationSetting;
import dataset.beans.Dataset;
import exception.IOPropertiesException;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import results.CSVResultGenerator;
import utils.DatasetUtils;
import utils.FileUtils;
import utils.XlsUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MultipleKGENRun {
    private static final String datasetPath = System.getProperty("user.dir") + File.separator + "dataset" + File.separator + "F2_Dataset.xlsx";
    private static final String anonymizedPath = System.getProperty("user.dir") + File.separator + "anonymized" + File.separator + "F2_Dataset_Anonymized.xlsx";
    private static final String configPath = System.getProperty("user.dir") + File.separator + "config" + File.separator + "configIdentifier.txt";
    private static final String resultsPath = System.getProperty("user.dir") + File.separator + "results" + File.separator;


    public static void execute (int numberOfRun) throws IOPropertiesException, JMException, ClassNotFoundException, FileNotFoundException {
        //Read the dataset not anonymized
        Dataset dataset = XlsUtils.readXlsx(datasetPath);
        DatasetUtils.loadProperties(dataset, configPath);

        //Initialize the genetic algorithm
        AnonymizationProblem problem = new AnonymizationProblem(dataset);
        AnonymizationSetting setting = new AnonymizationSetting(problem);
        AnonymizationAlgorithm algorithm = (AnonymizationAlgorithm) setting.configure();

        File expFolder = createExpFolder();

        ArrayList<String> infoExec = new ArrayList<String>();

        long start = System.currentTimeMillis();

        for (int i = 1; i <= numberOfRun; i++) {
            System.out.println("ITER " + i);
            long startTime = System.currentTimeMillis();
            SolutionSet bestSolutions = algorithm.execute(); //GA RUN

            if (bestSolutions.size() > 0) {
                infoExec.add("ITER " + i);
                infoExec.add("Execution time: " + ((double)(System.currentTimeMillis() - startTime)/1000) + " sec\n");

                ArrayList<String> csvTxt = CSVResultGenerator.csvResultGenerator(bestSolutions, dataset.getHeader());
                String savePath = expFolder.getAbsolutePath() + File.separator + "csv" + File.separator + "result" + i + ".csv";
                FileUtils.saveFile(csvTxt, savePath);
            }
        }

        infoExec.add("Total execution time: " + ((double)(System.currentTimeMillis() - start)/1000) + " sec");

        FileUtils.saveFile(infoExec, expFolder.getAbsolutePath() + File.separator + "infoResult.txt");
    }

    private static File createExpFolder () {
        File [] expFolders = new File(resultsPath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    String folderName = pathname.getName();

                    String [] split = folderName.split("_");
                    if (split.length == 2 && split[0].toLowerCase().equals("exp")) {
                        try {
                            int expNumber = Integer.parseInt(split[1]);
                            return true;
                        } catch (NumberFormatException ex) {}
                    }
                }

                return false;
            }
        });

        int maxNumber = -1;
        for (File file : expFolders) {
            String [] split = file.getName().split("_");
            if (split.length == 2) {
                int number = Integer.parseInt(split[1]);
                if (number > maxNumber) {
                    maxNumber = number;
                }
            }
        }

        int newExpNumber = maxNumber + 1;

        //Exp folder
        File newExp = new File(resultsPath + "Exp_" + newExpNumber);
        newExp.mkdir();

        //csv folder
        File csvFolder = new File(newExp.getAbsolutePath() + File.separator + "csv");
        csvFolder.mkdir();

        //csv folder
        File plotsFolder = new File(newExp.getAbsolutePath() + File.separator + "plota");
        plotsFolder.mkdir();

        return newExp;
    }
}
