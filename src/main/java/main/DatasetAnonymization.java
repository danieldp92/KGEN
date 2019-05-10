package main;

import anonymization.generalization.generator.GeneralizationGraphGenerator;
import anonymization.generalization.graph.GeneralizationTree;
import anonymization.generalization.graph.Node;
import dataset.Attribute;
import dataset.Dataset;
import dataset.DatasetRow;
import dataset.type.Identifier;
import dataset.type.QuasiIdentifier;
import utils.DatasetUtils;
import utils.XlsUtils;

import java.io.*;
import java.util.ArrayList;

public class DatasetAnonymization {
    private static final String datasetPath = System.getProperty("user.dir") + File.separator + "dataset" + File.separator + "F2_Dataset.xlsx";
    private static final String anonymizedPath = System.getProperty("user.dir") + File.separator + "anonymized" + File.separator + "F2_Dataset_Anonymized.xlsx";
    private static final String configPath = System.getProperty("user.dir") + File.separator + "config" + File.separator + "configIdentifier.txt";


    public static void main (String [] args) throws IOException {
        DatasetUtils datasetUtils = new DatasetUtils();
        Dataset dataset = XlsUtils.readXlsx(datasetPath);

        //Load and set identifiers in our dataset
        ArrayList<Boolean> attributeIdentifiers = loadIdentifier(dataset);
        datasetUtils.setAttributeTypes(dataset, attributeIdentifiers);

        System.out.println();

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
