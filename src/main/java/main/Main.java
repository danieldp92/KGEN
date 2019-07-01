package main;

import anonymization.KAnonymity;
import dataset.beans.Attribute;
import dataset.beans.Dataset;
import dataset.beans.DatasetRow;
import approaches.geneticalgorithm.AnonymizationAlgorithm;
import approaches.geneticalgorithm.AnonymizationProblem;
import approaches.geneticalgorithm.AnonymizationSetting;
import dataset.database.DatasetMySQL;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import results.CSVResultGenerator;
import utils.DatasetUtils;
import utils.FileUtils;
import utils.XlsUtils;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    private static final boolean CREATE_DB = false;
    private static final int NUMBER_OF_EXPERIMENTATION = 20;

    private static final String datasetPath = System.getProperty("user.dir") + File.separator + "dataset" + File.separator + "F2_Dataset.xlsx";
    private static final String anonymizedPath = System.getProperty("user.dir") + File.separator + "anonymized" + File.separator + "F2_Dataset_Anonymized.xlsx";
    private static final String configPath = System.getProperty("user.dir") + File.separator + "config" + File.separator + "configIdentifier.txt";
    private static final String resultsPath = System.getProperty("user.dir") + File.separator + "results" + File.separator;

    //private static final String query = "SELECT Id, Straat, Huisnr, Latitude, PostCode, Plaats FROM dataset WHERE Huisnr <= 36;";
    private static final String query = "SELECT * FROM dataset;";

    public static void main (String [] args) throws IOException, JMException, ClassNotFoundException, SQLException {
        Dataset dataset = XlsUtils.readXlsx(datasetPath);
        DatasetUtils.loadProperties(dataset, configPath);

        DatasetMySQL datasetMySQL = new DatasetMySQL(dataset);

        if (CREATE_DB) {
            datasetMySQL.createDatabase("anon");
        }

        Dataset newDataset = datasetMySQL.selectQuery(query);

        AnonymizationProblem problem = new AnonymizationProblem(newDataset);
        AnonymizationSetting setting = new AnonymizationSetting(problem);
        AnonymizationAlgorithm algorithm = (AnonymizationAlgorithm) setting.configure();

        ArrayList<String> infoExec = new ArrayList<>();
        infoExec.add("QUERY");
        infoExec.add(query);
        infoExec.add("\n");

        for (int i = 1; i <= NUMBER_OF_EXPERIMENTATION; i++) {
            long startTime = System.currentTimeMillis();
            SolutionSet bestSolutions = algorithm.execute();

            if (bestSolutions.size() > 0) {
                infoExec.add("ITER " + i);
                infoExec.add("Execution time: " + ((double)(System.currentTimeMillis() - startTime)/1000) + " sec");
                infoExec.add("\n");

                ArrayList<String> csvTxt = CSVResultGenerator.csvResultGenerator(bestSolutions, newDataset.getHeader());
                FileUtils.saveFile(csvTxt, resultsPath + "result" + i + ".csv");
            }
        }

        FileUtils.saveFile(infoExec, resultsPath + "infoResult.txt");
    }

    private static ArrayList<Boolean> loadIdentifier (Dataset dataset) throws IOException {
        File configIdentifierFile = new File(configPath);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = null;

        DatasetRow header = dataset.getHeader();
        ArrayList<Boolean> attributeIdentifiers = new ArrayList<Boolean>();


        //Load identifiers attribute info
        if (!configIdentifierFile.exists()) {
            PrintWriter pw = new PrintWriter(configIdentifierFile);


            System.out.println("Identifier/Quasi identifier attributes\n");
            for (Object attributeObj : header) {
                Attribute attribute = (Attribute) attributeObj;

                boolean repeat = false;
                do {
                    repeat = false;

                    System.out.print(attribute.getName() + " (I/QI): ");
                    line = br.readLine();
                    System.out.print("\n");

                    if (line.toLowerCase().equals("i")) {
                        attributeIdentifiers.add(true);
                        pw.println(attribute.getName().toLowerCase() + ":" + "i");
                    } else if (line.toLowerCase().equals("qi")) {
                        attributeIdentifiers.add(false);
                        pw.println(attribute.getName().toLowerCase() + ":" + "qi");
                    } else {
                        repeat = true;
                    }
                } while (repeat);
            }

            pw.close();
        } else {
            BufferedReader brTxt = new BufferedReader(new FileReader(configIdentifierFile));

            while ((line = brTxt.readLine()) != null) {
                String [] split = line.split(":");

                if (split[1].toLowerCase().equals("i")) {
                    attributeIdentifiers.add(true);
                } else {
                    attributeIdentifiers.add(false);
                }
            }

            brTxt.close();
        }

        return attributeIdentifiers;
    }

    private static void prova (Dataset dataset) {
        KAnonymity kAnonymity = new KAnonymity(dataset);
        int lev = 0;

        ArrayList<Integer> sol1 = new ArrayList<>();
        sol1.add(0);
        sol1.add(3);
        sol1.add(1);
        lev = kAnonymity.kAnonymityTest(sol1);
        System.out.println(sol1);
        System.out.println("Lev: " + lev);

        ArrayList<Integer> sol2 = new ArrayList<>();
        sol2.add(0);
        sol2.add(4);
        sol2.add(1);
        lev = kAnonymity.kAnonymityTest(sol2);
        System.out.println(sol2);
        System.out.println("Lev: " + lev);

        ArrayList<Integer> sol3 = new ArrayList<>();
        sol3.add(0);
        sol3.add(5);
        sol3.add(1);
        lev = kAnonymity.kAnonymityTest(sol3);
        System.out.println(sol3);
        System.out.println("Lev: " + lev);

        ArrayList<Integer> sol4 = new ArrayList<>();
        sol4.add(0);
        sol4.add(6);
        sol4.add(1);
        lev = kAnonymity.kAnonymityTest(sol4);
        System.out.println(sol4);
        System.out.println("Lev: " + lev);

        ArrayList<Integer> sol5 = new ArrayList<>();
        sol5.add(0);
        sol5.add(7);
        sol5.add(1);
        lev = kAnonymity.kAnonymityTest(sol5);
        System.out.println(sol5);
        System.out.println("Lev: " + lev);

        ArrayList<Integer> sol6 = new ArrayList<>();
        sol6.add(0);
        sol6.add(7);
        sol6.add(2);
        lev = kAnonymity.kAnonymityTest(sol6);
        System.out.println(sol6);
        System.out.println("Lev: " + lev);

        ArrayList<Integer> sol7 = new ArrayList<>();
        sol7.add(1);
        sol7.add(7);
        sol7.add(2);
        lev = kAnonymity.kAnonymityTest(sol7);
        System.out.println(sol7);
        System.out.println("Lev: " + lev);

        ArrayList<Integer> sol8 = new ArrayList<>();
        sol8.add(2);
        sol8.add(7);
        sol8.add(2);
        lev = kAnonymity.kAnonymityTest(sol8);
        System.out.println(sol8);
        System.out.println("Lev: " + lev);
    }
}
