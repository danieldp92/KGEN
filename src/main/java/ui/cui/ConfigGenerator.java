package ui.cui;

import dataset.beans.Attribute;
import dataset.beans.Dataset;
import dataset.beans.DatasetRow;
import utils.DatasetUtils;
import utils.FileUtils;
import utils.XlsUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ConfigGenerator {
    private static final String SEPARATOR_TAG = ":";

    public static void generateConfigFileFromCLI (String datasetPath, String outputPath) {
        System.out.println("CONFIG FILE GENERATION");
        ArrayList<String> configFileTxt = new ArrayList<>();
        configFileTxt.add("Name:IDType:DateType:PK");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int choose = -1;

        // Read dataset
        Dataset dataset = XlsUtils.readXlsx(datasetPath);

        DatasetRow header = dataset.getHeader();
        for (Object attributeObject : header) {
            Attribute attribute = (Attribute) attributeObject;

            // Name
            String name = attribute.getName();
            System.out.println("\nName: " + name);

            // I/QI
            String idType = null;
            System.out.println("Identifiers / Quasi-Identifiers?");
            System.out.println("1) Identifiers");
            System.out.println("2) Quasi-Identifiers");

            do {
                System.out.print("Choose: ");
                try {
                    choose = Integer.parseInt(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    System.out.println("The value must be a number!");
                }

                if (choose < 1 || choose > 2) {
                    System.out.println("The value must be 1 or 2");
                }
            } while (choose < 1 || choose > 2);

            if (choose == 1) {
                idType = "i";
            } else {
                idType = "qi";
            }

            // Data type
            String dataType = null;
            System.out.println("Choose the data type of the attribute:");
            System.out.println("1) INT");
            System.out.println("2) DOUBLE");
            System.out.println("3) STRING");
            System.out.println("4) DATE");
            System.out.println("5) PLACE");

            do {
                System.out.print("Choose: ");
                try {
                    choose = Integer.parseInt(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    System.out.println("The value must be a number!");
                }

                if (choose < 1 || choose > 5) {
                    System.out.println("The value must be included between 1 and 5");
                }
            } while (choose < 1 || choose > 5);

            switch (choose) {
                case 1:
                    dataType = "int";
                    break;
                case 2:
                    dataType = "double";
                    break;
                case 3:
                    dataType = "string";
                    break;
                case 4:
                    dataType = "date";
                    break;
                case 5:
                    dataType = "place";
                    break;
            }

            configFileTxt.add(name.toLowerCase() + SEPARATOR_TAG + idType + SEPARATOR_TAG + dataType + SEPARATOR_TAG + "false");
        }

        try {
            FileUtils.saveFile(configFileTxt, outputPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
