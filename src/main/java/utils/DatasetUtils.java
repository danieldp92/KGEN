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
            System.out.println(line);
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

    public static ArrayList<Integer> getHashColumn (DatasetColumn datasetColumn) {
        ArrayList<Integer> hashColumn = new ArrayList<Integer>();

        for (Object attributeObj : datasetColumn) {
            Attribute attribute = (Attribute) attributeObj;
            if (attribute.getValue() == null) {
                hashColumn.add(null);
            } else {
                hashColumn.add(attribute.getValue().hashCode());
            }
        }

        return hashColumn;
    }

    private static void setAttribute (Attribute attribute, String propertyLine) throws IOPropertiesException {
        Object value = attribute.getValue();
        Object realValue = null;

        String [] split = propertyLine.split(":");

        if (split.length != PROPERTY_FIELDS) {
            throw new IOPropertiesException("NUMBER OF PROPERTY FILEDS WRONG");
        }

        String attributeName = split[0];
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

                if (value != null) {
                    realValue = value.toString();
                }

                break;
            case "string":
                type = AttributeType.TYPE_STRING;

                if (value != null)
                    realValue = value;
                break;
            case "place":
                type = AttributeType.TYPE_PLACE;
                if (value != null)
                    realValue = value;
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
