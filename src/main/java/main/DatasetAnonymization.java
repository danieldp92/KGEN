package main;

import dataset.Attribute;
import dataset.Dataset;
import dataset.DatasetRow;
import utils.DatasetUtils;
import utils.XlsUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DatasetAnonymization {
    public static void main (String [] args) throws IOException {
        String datasetPath = System.getProperty("user.dir") + File.separator + "dataset" + File.separator +
                "F2_Dataset.xlsx";

        String anonymizedPath = System.getProperty("user.dir") + File.separator + "anonymized" + File.separator +
                "F2_Dataset_Anonymized.xlsx";

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = null;

        Dataset dataset = XlsUtils.readXlsx(datasetPath);
        DatasetRow attributeRow = (DatasetRow) dataset.get(0);
        ArrayList<Boolean> attributeIdentifiers = new ArrayList<Boolean>();


        System.out.println("Identifier/Quasi identifier attributes\n");
        for (Object attributeObj : attributeRow) {
            Attribute attribute = (Attribute) attributeObj;

            boolean repeat = false;
            do {
                repeat = false;

                System.out.print(attribute.getName() + " (I/QI): ");
                line = br.readLine();
                System.out.print("\n");

                if (line.toLowerCase().equals("i")) {
                    attributeIdentifiers.add(true);
                } else if (line.toLowerCase().equals("qi")) {
                    attributeIdentifiers.add(false);
                } else {
                    repeat = true;
                }
            } while (repeat);
        }

        DatasetUtils datasetUtils = new DatasetUtils();
        datasetUtils.setAttributeTypes(dataset, attributeIdentifiers);

        System.out.println();
    }
}
