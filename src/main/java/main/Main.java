package main;

import anonymization.KAnonymity;
import anonymization.generalization.exception.LevelNotValidException;
import anonymization.generalization.generator.GeneralizationGraphGenerator;
import anonymization.generalization.graph.GeneralizationTree;
import dataset.Attribute;
import dataset.Dataset;
import dataset.DatasetColumn;
import dataset.DatasetRow;
import dataset.type.QuasiIdentifier;
import geneticalgorithm.operator.Algorithm;
import utils.DatasetUtils;
import utils.XlsUtils;

import java.io.*;
import java.util.ArrayList;

public class Main {
    private static final String datasetPath = System.getProperty("user.dir") + File.separator + "dataset" + File.separator + "F2_Dataset.xlsx";
    private static final String anonymizedPath = System.getProperty("user.dir") + File.separator + "anonymized" + File.separator + "F2_Dataset_Anonymized.xlsx";
    private static final String configPath = System.getProperty("user.dir") + File.separator + "config" + File.separator + "configIdentifier.txt";


    public static void main (String [] args) throws IOException, LevelNotValidException {
        DatasetUtils datasetUtils = new DatasetUtils();
        Dataset dataset = XlsUtils.readXlsx(datasetPath);
        dataset.getData();

        //Load and set identifiers in our dataset
        ArrayList<Boolean> attributeIdentifiers = loadIdentifier(dataset);
        datasetUtils.setAttributeTypes(dataset, attributeIdentifiers);

        generateCompleteAnonymizedDataset(dataset);

        Algorithm anonymizationAlgorithm = new Algorithm(dataset);
        anonymizationAlgorithm.execute();
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

    private static int getMaxAttributNumber(DatasetColumn column) {
        int maxNumber = 0;

        for (Object attributeObj : column) {
            Attribute attribute = (Attribute) attributeObj;
            if (attribute.getValue() != null) {
                Integer number = (Integer) attribute.getValue();

                if (Math.abs(number) > maxNumber) {
                    maxNumber = Math.abs(number);
                }
            }
        }

        return maxNumber;
    }

    private static int getMaxAttributeStringLenght(DatasetColumn columns) {
        int maxLenght = 0;

        for (Object attributeObj : columns) {
            Attribute attribute = (Attribute) attributeObj;

            if (attribute.getValue() != null) {
                String value = (String) attribute.getValue();

                if (value.length() > maxLenght) {
                    maxLenght = value.length();
                }
            }
        }


        return maxLenght;
    }

    private static ArrayList<Integer> upperBounds (Dataset dataset) {
        ArrayList<Integer> upperBounds = new ArrayList<Integer>();

        GeneralizationTree generalizationTree = GeneralizationGraphGenerator.generatePlaceHierarchy();

        //Max level of anonymity of every attribute
        for (int i = 0; i < dataset.getHeader().size(); i++) {
            Attribute attribute = (Attribute) dataset.getHeader().get(i);

            if (attribute.getType() instanceof QuasiIdentifier) {
                QuasiIdentifier qiAttribute = (QuasiIdentifier) attribute.getType();

                switch (qiAttribute.type) {
                    case QuasiIdentifier.TYPE_PLACE:
                        upperBounds.add(generalizationTree.getHeight());
                        break;
                    case QuasiIdentifier.TYPE_NUMERIC:
                        int maxValue = getMaxAttributNumber(dataset.getColumns().get(i));
                        int heightHierarchy = 0;

                        int tmpMax = maxValue;
                        while (tmpMax > 0) {
                            heightHierarchy++;
                            tmpMax = tmpMax/10;
                        }

                        upperBounds.add(heightHierarchy);
                        break;
                    case QuasiIdentifier.TYPE_DATE:
                        upperBounds.add(2);
                        break;
                    case QuasiIdentifier.TYPE_STRING:
                        upperBounds.add(getMaxAttributeStringLenght(dataset.getColumns().get(i)));
                        break;
                    default:
                        break;
                }
            }
        }

        return upperBounds;
    }

    private static void generateCompleteAnonymizedDataset (Dataset dataset) {
        KAnonymity kAnonymity = new KAnonymity(dataset);
        ArrayList<Integer> upperBounds = upperBounds(dataset);

        long startTime = System.currentTimeMillis();
        Dataset anon = kAnonymity.anonymize(upperBounds);
        System.out.println("Anonymization time: " + (System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        int [] kAnonymityRows = kAnonymity.numberOfEqualsRow(anon, 5);

        ArrayList<Integer> indexRowsToRemove = new ArrayList<Integer>();
        for (int i = 0; i < kAnonymityRows.length; i++) {
            if (kAnonymityRows[i] < 5) {
                indexRowsToRemove.add(i);
            }
        }

        boolean isAnonymized = false;
        if (indexRowsToRemove.isEmpty()) {
            isAnonymized = true;
        }

        System.out.println("K-Anonymization test time: " + (System.currentTimeMillis() - startTime));
        System.out.println("Results: " + isAnonymized);
        System.out.println("Number of rows not k-anonymized: " + indexRowsToRemove.size());


        System.out.println("Transformation of dataset anonymized in a k-anonymized dataset");
        kAnonymity.deleteNotKAnonymizedRows(dataset, indexRowsToRemove);

        //Anonymization of new dataset
        startTime = System.currentTimeMillis();
        anon = kAnonymity.anonymize(upperBounds);
        System.out.println("Anonymization time: " + (System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        kAnonymityRows = kAnonymity.numberOfEqualsRow(anon, 5);
        indexRowsToRemove = new ArrayList<Integer>();
        for (int i = 0; i < kAnonymityRows.length; i++) {
            if (kAnonymityRows[i] < 5) {
                indexRowsToRemove.add(i);
            }
        }

        isAnonymized = false;
        if (indexRowsToRemove.isEmpty()) {
            isAnonymized = true;
        }

        System.out.println("K-Anonymization test time: " + (System.currentTimeMillis() - startTime));
        System.out.println("Results: " + isAnonymized);

        System.out.println("Number of rows not k-anonymized: " + indexRowsToRemove.size());
    }

}
