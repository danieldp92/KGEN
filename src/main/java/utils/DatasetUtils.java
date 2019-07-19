package utils;

import dataset.beans.Attribute;
import dataset.beans.Dataset;
import dataset.beans.DatasetColumn;
import dataset.type.AttributeType;
import dataset.type.Identifier;
import dataset.type.QuasiIdentifier;
import exception.IOPropertiesException;

import java.io.IOException;
import java.util.*;

public class DatasetUtils {
    private static final int PROPERTY_FIELDS = 4;


    public static void loadProperties (Dataset dataset, String propertiesPath) throws IOPropertiesException {
        List<String> properties = null;

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
            String attributeType = split[1];
            String valueType = split[2];
            boolean primaryKey = Boolean.parseBoolean(split[3]);

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

    public static int getMaxAttributNumber(DatasetColumn column) {
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
                median = "*****";
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

        return minLength;
    }

    public static int maxLength (DatasetColumn column) {
        int maxLength = Integer.MIN_VALUE;

        for (Object attributeObj : column) {
            Attribute attribute = (Attribute) attributeObj;

            if (attribute.getValue() instanceof String) {
                if (((String) attribute.getValue()).length() > maxLength) {
                    maxLength = ((String) attribute.getValue()).length();
                }
            }
        }

        return maxLength;
    }

    public static void printDataset (Dataset dataset) {
        System.out.println("DATASET INFO\n");

        //Header
        for (Object attributeObj : dataset.getHeader()) {
            Attribute attribute = (Attribute) attributeObj;
            System.out.print(attribute.getName() + "\t\t\t\t");
        }
        System.out.println();

        //Data
        for (int i = 0; i < dataset.getDatasetSize(); i++) {
            for (int j = 0; j < dataset.getColumns().size(); j++) {
                Attribute attribute = (Attribute) dataset.getColumns().get(j).get(i);
                if (attribute.getValue() != null)
                    System.out.print(attribute.getValue() + "\t\t\t");
                else
                    System.out.print("null\t\t\t");
            }
            System.out.println();
        }
    }


    private static void setAttribute (Attribute attribute, String propertyLine) throws IOPropertiesException {
        Object value = attribute.getValue();
        Object realValue = value;

        String [] split = propertyLine.split(":");

        if (split.length != PROPERTY_FIELDS) {
            throw new IOPropertiesException("NUMBER OF PROPERTY FILEDS WRONG");
        }

        String attributeType = split[1];
        String valueType = split[2];
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
