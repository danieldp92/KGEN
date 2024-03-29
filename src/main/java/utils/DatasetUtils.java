package utils;

import dataset.beans.Attribute;
import dataset.beans.Dataset;
import dataset.beans.DatasetColumn;
import dataset.beans.DatasetRow;
import dataset.type.AttributeType;
import dataset.type.Identifier;
import dataset.type.QuasiIdentifier;
import exception.DatasetNotFoundException;
import exception.IOPropertiesException;
import exception.InvalidCSVFile;

import java.io.*;
import java.util.*;

public class DatasetUtils {
    private static final int PROPERTY_FIELDS = 4;

    private static final String CSV_EXTENSION = "csv";
    private static final String XLXS_EXTENSION = "xlsx";
    private static final String XLS_EXTENSION = "xls";

    public static Dataset readAndInit (String datasetPath, String configPath, String nullValue, String separator) throws DatasetNotFoundException, IOPropertiesException {
        Dataset dataset = null;

        File datasetFile = new File(datasetPath);
        if (datasetFile.exists()) {
            String datasetName = "";

            String [] split = datasetFile.getName().split("\\.");
            for (int i = 0; i < split.length-1; i++) {
                datasetName += split[i];
            }

            String datasetExtension = FileUtils.getFileExtension(datasetFile);
            try {
                if (datasetExtension.equals(XLS_EXTENSION) || datasetExtension.equals(XLXS_EXTENSION)) {
                    dataset = DatasetUtils.readFromXls(datasetPath);
                } else if (datasetExtension.equals(CSV_EXTENSION)) {
                    dataset = DatasetUtils.readFromCSV(datasetPath, nullValue, separator);
                } else {
                    throw new DatasetNotFoundException();
                }
            } catch (IOException e) {
                throw new DatasetNotFoundException();
            }


            dataset.setName(datasetName);

            DatasetUtils.loadProperties(dataset, configPath);
        } else {
            throw new DatasetNotFoundException();
        }

        return dataset;
    }

    public static Dataset readFromCSV (String path, String nullValue, String separator) throws IOException {
        File csvFile = new File(path);
        if (!FileUtils.getFileExtension(csvFile).equals("csv")) {
            throw new InvalidCSVFile();
        }

        List<String> csv = FileUtils.loadFile(path);
        return readFromCSV(csv, nullValue, separator);
    }

    public static Dataset readFromCSV (List<String> csvText, String nullValue, String separator) {
        Dataset dataset = null;
        DatasetRow header = new DatasetRow();
        ArrayList<DatasetColumn> columns = new ArrayList<DatasetColumn>();

        // Header
        String [] headerAttributes = csvText.remove(0).split(separator);
        for (String headerAttribute : headerAttributes) {
            Attribute attribute = new Attribute(headerAttribute, null);
            header.add(attribute);
            columns.add(new DatasetColumn());
        }

        // Columns
        for (String line : csvText) {
            String [] values = line.split(separator);
            for (int i = 0; i < values.length; i++) {
                String value = values[i];

                // If the value red is the nullValue, then there is nothing in this cell
                if (value.equals(nullValue)) {
                    value = null;
                }

                Attribute attribute = new Attribute(((Attribute)header.get(i)).getName(), value);
                columns.get(i).add(attribute);
            }
        }

        dataset = new Dataset(header, columns);

        return dataset;
    }

    public static Dataset readFromXls (String path) throws IOException {
        Dataset dataset = XlsUtils.readXlsx(path);
        return dataset;
    }

    public static void loadProperties (Dataset dataset, String propertiesPath) throws IOPropertiesException {
        List<String> properties = null;

        // Load property file
        try {
            properties = FileUtils.loadFile(propertiesPath);
            properties.remove(0);
        } catch (IOException e) {
            throw new IOPropertiesException("Properties file NOT FOUND");
        }

        if (properties.isEmpty() || properties.get(0).split(":").length != PROPERTY_FIELDS) {
            throw new IOPropertiesException("ERROR IN THE CONFIG FILE\n" +
                    "SYNTAX EXPECTED: name : i/qi/sd : true/false");
        }


        for (String line : properties) {
            String [] split = line.split(":");

            String attributeName = split[0];

            int indexAttribute = 0;
            while (indexAttribute < dataset.getHeader().size() &&
                    !((Attribute)dataset.getHeader().get(indexAttribute)).getName().toLowerCase().equals(attributeName.toLowerCase())) {
                indexAttribute++;
            }

            if (indexAttribute >= dataset.getHeader().size()) {
                throw new IOPropertiesException("ATTRIBUTE NOT FOUND. CHECK PROPERTIES FILE");
            }

            //Header attribute
            setAttribute((Attribute)dataset.getHeader().get(indexAttribute), line);

            for (Object attributeObj : dataset.getColumns().get(indexAttribute)) {
                setAttribute((Attribute) attributeObj, line);
            }
        }
    }

    public static int getMaxAttributINT(DatasetColumn column) {
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

    public static double getMaxAttributDOUBLE(DatasetColumn column) {
        double maxNumber = 0;

        for (Object attributeObj : column) {
            Attribute attribute = (Attribute) attributeObj;
            if (attribute.getValue() != null) {
                Double number = (Double) attribute.getValue();

                if (Math.abs(number) > maxNumber) {
                    maxNumber = Math.abs(number);
                }
            }
        }

        return maxNumber;
    }

    public static int getMaxAttributeStringLenght(DatasetColumn columns) {
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

    public static Object findMedian (DatasetColumn column) {
        Object median = null;
        ArrayList<Object> arrayOfNotNull = new ArrayList<Object>();

        for (Object attributeObj : column) {
            Attribute attribute = (Attribute) attributeObj;

            if (attribute.getValue() != null) {
                arrayOfNotNull.add(attribute.getValue());
            }
        }

        if (!arrayOfNotNull.isEmpty()) {
            median = arrayOfNotNull.get(arrayOfNotNull.size()/2);
        } else {
            QuasiIdentifier qiAttribute = (QuasiIdentifier) ((Attribute)column.get(0)).getType();

            if (qiAttribute.type == QuasiIdentifier.TYPE_INT) {
                median = 0;
            } else {
                median = "";
            }
        }

        return median;
    }

    public static int minLength (DatasetColumn column) {
        int minLength = Integer.MAX_VALUE;

        for (Object attributeObj : column) {
            Attribute attribute = (Attribute) attributeObj;

            if (attribute.getValue() instanceof String) {
                if (((String) attribute.getValue()).length() < minLength) {
                    minLength = ((String) attribute.getValue()).length();
                }
            }
        }

        if (minLength == Integer.MAX_VALUE) {
            minLength = 0;
        }

        return minLength;
    }

    public static boolean equalsRows (Dataset dataset, int indexRow1, int indexRow2) {
        int i = 0;
        boolean equalsAttribute = true;

        while (i < dataset.getHeader().size() && equalsAttribute) {
            Attribute attribute1 = (Attribute) dataset.getColumns().get(i).get(indexRow1);
            Attribute attribute2 = (Attribute) dataset.getColumns().get(i).get(indexRow2);;

            equalsAttribute = DatasetUtils.equalsAttribute(attribute1, attribute2);
            i++;
        }

        return equalsAttribute;
    }


    private static boolean equalsAttribute (Attribute attribute1, Attribute attribute2) {
        boolean equals = false;

        if (attribute1.getValue() == null && attribute2.getValue() == null) {
            equals = true;
        }

        else if ((attribute1.getValue() == null && attribute2.getValue() != null) ||
                (attribute1.getValue() != null && attribute2.getValue() == null)) {
            equals = false;
        }

        else {
            if (attribute1.getValue().equals(attribute2.getValue())) {
                equals = true;
            }
        }

        return equals;
    }

    private static void setAttribute (Attribute attribute, String propertyLine) throws IOPropertiesException {
        Object value = attribute.getValue();
        Object realValue = value;

        String [] split = propertyLine.split(":");

        if (split.length != PROPERTY_FIELDS) {
            throw new IOPropertiesException("NUMBER OF PROPERTY FILES WRONG");
        }

        String attributeType = split[1];
        String valueType = split[2].toLowerCase();
        boolean primaryKey = Boolean.parseBoolean(split[3]);

        int type = -1;

        switch (valueType) {
            case "int":
                type = AttributeType.TYPE_INT;

                if (value != null) {
                    if (value instanceof Double) {
                        //Convert double in int
                        realValue = ((Double) value).intValue();
                    } else if (value instanceof Date) {
                        realValue = null;
                    } else {
                        realValue = Integer.parseInt((String) value);
                    }
                }

                break;
            case "date":
                type = AttributeType.TYPE_DATE;
                break;
            case "string":
                type = AttributeType.TYPE_STRING;
                break;
            case "place":
                type = AttributeType.TYPE_PLACE;
                break;
            case "double":
                type = AttributeType.TYPE_DOUBLE;
                if (value != null) {
                    try {
                        realValue = Double.parseDouble((String) value);
                    } catch (NumberFormatException ex) {
                        realValue = null;
                    }
                }

                break;
            default:
                throw new IOPropertiesException("TYPE OF DATA IN PROPERTY FILE NOT VALID");
        }


        //Set type
        if (attributeType.toLowerCase().equals("i")) {
            attribute.setType(new Identifier(type));
        } else {
            attribute.setType(new QuasiIdentifier(type));
        }

        //Set pk
        attribute.setPrimaryKey(primaryKey);

        //Cast value, using type just obtained
        attribute.setValue(realValue);
    }
}
