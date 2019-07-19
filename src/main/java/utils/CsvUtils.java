package utils;

import main.experimentation.bean.Result;

import java.io.FileNotFoundException;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CsvUtils {
    private static final String SEPARATOR_TAG = ";";

    public static void saveClassAsCsv(List<Object> objects, String path) {
        ArrayList<String> csv = new ArrayList<>();
        LinkedHashMap<String, Method> getMap = new LinkedHashMap<>();
        String line = "";

        if (!objects.isEmpty()) {
            //First element
            Object first = objects.get(0);
            System.out.println(first.getClass());

            //Initialize the getMap
            getMap = extractGetMethodsFromFields(first);

            Field [] fields = first.getClass().getDeclaredFields();

            //Header
            for (Field field : fields) {
                line += normalizeAttribute(field.getName()) + SEPARATOR_TAG;
            }

            csv.add(line);

            //Data
            for (Object o : objects) {
                line = "";

                Field [] oFields = o.getClass().getDeclaredFields();
                for (Field oField : oFields) {
                    Method method = getMap.get(oField.getName());
                    try {
                        line += method.invoke(o).toString() + SEPARATOR_TAG;
                    } catch (IllegalAccessException | InvocationTargetException e) {}
                }

                csv.add(line);
            }
        }

        for (String s : csv) {
            System.out.println(s);
        }

        try {
            FileUtils.saveFile(csv, path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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

    private static String normalizeAttribute (String attributeName) {
        String attributeNameNormalized = "";

        String[] split = attributeName.split("(?=\\p{Upper})");
        for (String s : split) {
            if (s.length() > 1) {
                attributeNameNormalized += s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() + " ";
            }
        }

        return attributeNameNormalized.substring(0, attributeNameNormalized.length()-1);
    }
}
