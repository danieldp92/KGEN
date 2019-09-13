package utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

public class CsvUtils {
    private static final String SEPARATOR_TAG = ";";
    private static final String LIST_SEPARATOR_TAG = ",";

    public static void saveClassAsCsv(List<Object> objects, String path) {
        List<String> csv = convertObjectListIntoCSV(objects);

        try {
            FileUtils.saveFile((ArrayList<String>) csv, path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method add new results on a csv already created
     * @param objects
     * @param path
     */
    public static void appendClassAsCsv(List<Object> objects, String path) {
        boolean fileNotFound = false;

        List<String> csv = new ArrayList<>();
        try {
            csv = FileUtils.loadFile(path);
        } catch (IOException e) {
            fileNotFound = true;
        }

        List<String> newCsv = convertObjectListIntoCSV(objects);

        //Remove the header of this new csv, because it has already created in the csv loaded
        if (!fileNotFound) {
            newCsv.remove(0);
        }

        csv.addAll(newCsv);


        try {
            FileUtils.saveFile((ArrayList<String>) csv, path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static List<String> convertObjectListIntoCSV(List<Object> objects) {
        ArrayList<String> csv = new ArrayList<>();
        LinkedHashMap<String, Method> getMap = new LinkedHashMap<>();
        String line = "";

        if (!objects.isEmpty()) {
            //First element
            Object first = objects.get(0);

            //Initialize the getMap
            getMap = extractGetMethodsFromFields(first);

            Field [] fields = first.getClass().getDeclaredFields();

            //Header
            for (Field field : fields) {
                line += normalizeAttributeToSave(field.getName()) + SEPARATOR_TAG;
            }

            csv.add(line);

            //Data
            for (Object o : objects) {
                line = "";

                Field [] oFields = o.getClass().getDeclaredFields();
                for (Field oField : oFields) {
                    Method method = getMap.get(oField.getName());
                    try {
                        Object methodReturnValue = method.invoke(o);
                        if (methodReturnValue == null || methodReturnValue.toString().equals("-1") || methodReturnValue.toString().equals("-1.0")) {
                            line += "" + SEPARATOR_TAG;
                        } else {
                            int type = TypeUtils.objectInstanceOf(methodReturnValue);

                            switch (type) {
                                case TypeUtils.PRIMITIVE_TYPE:
                                    line += methodReturnValue.toString() + SEPARATOR_TAG;
                                    break;
                                case TypeUtils.ARRAY_TYPE:
                                    line += getArrayString(methodReturnValue) + SEPARATOR_TAG;
                                    break;
                                case TypeUtils.SET_TYPE:
                                    line += getArrayString(methodReturnValue) + SEPARATOR_TAG;
                                    break;
                                case TypeUtils.MAP_TYPE:
                                    line += getMapString(methodReturnValue) + SEPARATOR_TAG;
                                    break;
                                default:
                                    break;
                            }
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {}
                }

                csv.add(line);
            }
        }

        return csv;
    }

    private static LinkedHashMap<String, Method> extractGetMethodsFromFields(Object object) {
        LinkedHashMap<String, Method> getMap = new LinkedHashMap<>();

        Field[] fields = object.getClass().getDeclaredFields();
        Method [] methods = object.getClass().getDeclaredMethods();

        //Search, for each field, its get method
        for (Field field : fields) {
            String fieldName = field.getName().toLowerCase();

            int i = 0;
            while (i < methods.length && !methods[i].getName().toLowerCase().equals("get" + fieldName))
                i++;

            if (i < methods.length) {
                getMap.put(field.getName(), methods[i]);
            }
        }

        return getMap;
    }

    public static LinkedHashMap<String, Method> extractSetMethodsFromFields(Object object) {
        LinkedHashMap<String, Method> setMap = new LinkedHashMap<>();

        Field[] fields = object.getClass().getDeclaredFields();
        Method [] methods = object.getClass().getDeclaredMethods();

        //Search, for each field, its get method
        for (Field field : fields) {
            String fieldName = field.getName().toLowerCase();

            int i = 0;
            while (i < methods.length && !methods[i].getName().toLowerCase().equals("set" + fieldName))
                i++;

            if (i < methods.length) {
                setMap.put(field.getName(), methods[i]);
            }
        }

        return setMap;
    }

    private static String normalizeAttributeToSave(String attributeName) {
        String attributeNameNormalized = "";

        String[] split = attributeName.split("(?=\\p{Upper})");
        for (String s : split) {
            if (s.length() > 1) {
                attributeNameNormalized += s.substring(0, 1).toUpperCase() + s.substring(1) + " ";
            } else if (s.length() == 1) {
                attributeNameNormalized += s.toUpperCase() + " ";
            }
        }

        return attributeNameNormalized.substring(0, attributeNameNormalized.length()-1);
    }

    public static String normalizeAttributeToLoad (String attributeName) {
        String [] split = attributeName.split(" ");

        String attributeNormalized = "";
        for (int i = 0; i < split.length; i++) {
            if (i == 0) {
                attributeNormalized += split[i].toLowerCase();
            } else {
                attributeNormalized += split[i].substring(0, 1).toUpperCase() + split[i].substring(1);
            }
        }

        return attributeNormalized;
    }

    private static String getArrayString (Object o) {
        List list = (List) o;

        String toString = "";

        if (!list.isEmpty()) {
            // Get first element of array
            Object firstElement = list.get(0);

            int type = TypeUtils.objectInstanceOf(firstElement);

            switch (type) {
                case TypeUtils.PRIMITIVE_TYPE:
                    toString = list.toString();
                    break;
                case TypeUtils.ARRAY_TYPE:
                    toString = "[";
                    for (Object element : list) {
                        toString += getArrayString(element) + LIST_SEPARATOR_TAG;
                    }
                    toString = toString.substring(0, toString.length()-1) + "]";
                    break;
                case TypeUtils.SET_TYPE:
                    toString = "[";
                    for (Object element : list) {
                        toString += getArrayString(element) + LIST_SEPARATOR_TAG;
                    }
                    toString = toString.substring(0, toString.length()-1) + "]";
                    break;
                case TypeUtils.MAP_TYPE:
                    toString = "{";
                    for (Object element : list) {
                        Map mapElement = (Map) element;
                        toString += getMapString(element) + LIST_SEPARATOR_TAG;
                    }
                    toString = toString.substring(0, toString.length()-1) + "}";
                    break;
            }
        }

        return toString;
    }

    private static String getMapString (Object o) {
        String toString = "";
        Map map = (Map) o;

        if (!map.isEmpty()) {
            Set keySet = map.keySet();

            for (Object keyObject : keySet) {
                // Key
                toString += "key=";
                int keyType = TypeUtils.objectInstanceOf(keyObject);
                switch (keyType) {
                    case TypeUtils.PRIMITIVE_TYPE:
                        toString += keyObject.toString();
                        break;
                    case TypeUtils.ARRAY_TYPE:
                        toString = "[";
                        for (Object element : (List)keyObject) {
                            toString += getArrayString(element) + LIST_SEPARATOR_TAG;
                        }
                        toString = toString.substring(0, toString.length()-1) + "]";
                        break;
                    case TypeUtils.SET_TYPE:
                        toString = "[";
                        for (Object element : (List)keyObject) {
                            toString += getArrayString(element) + LIST_SEPARATOR_TAG;
                        }
                        toString = toString.substring(0, toString.length()-1) + "]";
                        break;
                    case TypeUtils.MAP_TYPE:
                        toString += getMapString(keyObject);
                        break;
                }

                toString += LIST_SEPARATOR_TAG + "value=";

                // Value
                Object valueObject = map.get(keyObject);
                int valueType = TypeUtils.objectInstanceOf(valueObject);
                switch (valueType) {
                    case TypeUtils.PRIMITIVE_TYPE:
                        toString += keyObject.toString();
                        break;
                    case TypeUtils.ARRAY_TYPE:
                        toString = "[";
                        for (Object element : (List)keyObject) {
                            toString += getArrayString(element) + LIST_SEPARATOR_TAG;
                        }
                        toString = toString.substring(0, toString.length()-1) + "]";
                        break;
                    case TypeUtils.SET_TYPE:
                        toString = "[";
                        for (Object element : (List)keyObject) {
                            toString += getArrayString(element) + LIST_SEPARATOR_TAG;
                        }
                        toString = toString.substring(0, toString.length()-1) + "]";
                        break;
                    case TypeUtils.MAP_TYPE:
                        toString += getMapString(keyObject);
                        break;
                }

                toString += LIST_SEPARATOR_TAG;
            }
        }

        return toString;
    }

    public static String replaceDoubleSEPARATOR (String line) {
        String newString = "";
        char [] lineChars = line.toCharArray();

        char prevChar = 0;
        for (int i = 0; i < lineChars.length; i++) {
            if (i != 0 && prevChar == lineChars[i] && String.valueOf(prevChar).equals(SEPARATOR_TAG)) {
                newString += " ";
            }

            prevChar = lineChars[i];
            newString += prevChar;
        }

        return newString;
    }

}
