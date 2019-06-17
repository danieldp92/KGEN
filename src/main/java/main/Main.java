package main;

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
    private static final boolean CREATE_DB = true;
    private static final String datasetPath = System.getProperty("user.dir") + File.separator + "dataset" + File.separator + "F2_Dataset.xlsx";
    private static final String anonymizedPath = System.getProperty("user.dir") + File.separator + "anonymized" + File.separator + "F2_Dataset_Anonymized.xlsx";
    private static final String configPath = System.getProperty("user.dir") + File.separator + "config" + File.separator + "configIdentifier.txt";
    private static final String resultsPath = System.getProperty("user.dir") + File.separator + "results" + File.separator + "results5.csv";


    public static void main (String [] args) throws IOException, JMException, ClassNotFoundException, SQLException {
        DatasetMySQL datasetMySQL = new DatasetMySQL();
        Dataset dataset = XlsUtils.readXlsx(datasetPath);
        DatasetUtils.loadProperties(dataset, configPath);

        if (CREATE_DB) {
            datasetMySQL.createDatabase(dataset, "anon");
        }

        /*
        //Load and set identifiers in our dataset
        ArrayList<Boolean> attributeIdentifiers = loadIdentifier(dataset);
        datasetUtils.setAttributeTypes(dataset, attributeIdentifiers);

        AnonymizationProblem problem = new AnonymizationProblem(dataset);
        AnonymizationSetting setting = new AnonymizationSetting(problem);
        AnonymizationAlgorithm algorithm = (AnonymizationAlgorithm) setting.configure();

        SolutionSet bestSolutions = algorithm.execute();
        if (bestSolutions.size() > 0) {
            ArrayList<String> csvTxt = CSVResultGenerator.csvResultGenerator(bestSolutions, dataset.getHeader());
            FileUtils.saveFile(csvTxt, resultsPath);
        }*/
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
}
