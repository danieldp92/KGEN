package main;

import anonymization.DatasetAnonymization;
import anonymization.generalization.exception.LevelNotValidException;
import anonymization.generalization.generator.GeneralizationGraphGenerator;
import anonymization.generalization.graph.GeneralizationTree;
import dataset.Attribute;
import dataset.Dataset;
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

        Algorithm anonymizationAlgorithm = new Algorithm(dataset, upperBounds(dataset));
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

    private static int getMaxAttributeStringLenght (Dataset dataset, Attribute attribute) {
        //Detect attribute
        int i = 0;
        while (i < dataset.getHeader().size() &&
                !((Attribute)dataset.getHeader().get(i)).getName().equals(attribute.getName())) {
            i++;
        }

        int maxLenght = 0;

        if (i < dataset.getHeader().size()) {
            for (int j = 0; j < dataset.getData().size(); j++) {
                Attribute tmpAttribute = (Attribute)dataset.getData().get(j).get(i);
                String value = (String) tmpAttribute.getValue();
                if (value.length() > maxLenght) {
                    maxLenght = value.length();
                }
            }
        }


        return maxLenght;
    }

    private static int getMaxAttributNumber (Dataset dataset, Attribute attribute) {
        //Detect attribute
        int i = 0;
        while (i < dataset.getHeader().size() &&
                !((Attribute)dataset.getHeader().get(i)).getName().equals(attribute.getName())) {
            i++;
        }

        int maxNumber = 0;

        if (i < dataset.getHeader().size()) {
            for (int j = 0; j < dataset.getData().size(); j++) {
                Attribute tmpAttribute = (Attribute)dataset.getData().get(j).get(i);
                if (tmpAttribute.getValue() != null) {
                    int value = (Integer)tmpAttribute.getValue();

                    if (Math.abs(value) > maxNumber) {
                        maxNumber = Math.abs(value);
                    }
                }
            }
        }


        return maxNumber;
    }

    private static ArrayList<Integer> upperBounds (Dataset dataset) {
        ArrayList<Integer> upperBounds = new ArrayList<Integer>();

        GeneralizationTree generalizationTree = GeneralizationGraphGenerator.generatePlaceHierarchy();

        //Max level of anonymity of every attribute
        for (Object attributeObj : dataset.getHeader()) {
            Attribute attribute = (Attribute) attributeObj;

            if (attribute.getType() instanceof QuasiIdentifier) {
                QuasiIdentifier qiAttribute = (QuasiIdentifier) attribute.getType();

                switch (qiAttribute.type) {
                    case QuasiIdentifier.TYPE_PLACE:
                        upperBounds.add(generalizationTree.getHeight());
                        break;
                    case QuasiIdentifier.TYPE_NUMERIC:
                        int maxValue = getMaxAttributNumber(dataset, attribute);
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
                        upperBounds.add(getMaxAttributeStringLenght(dataset, attribute));
                        break;
                    default:
                        break;
                }
            }
        }

        return upperBounds;
    }

}
