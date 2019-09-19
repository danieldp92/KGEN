package runner.experimentation.util;

import runner.experimentation.bean.Result;
import utils.ArrayUtils;
import utils.CsvUtils;
import utils.FileUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ResultUtils {
    private static final String SEPARATOR_TAG = ";";
    private static final String LIST_SEPARATOR_TAG = ",";

    public static List<Result> loadResultsFromCsv (String csvPath) {
        List<String> csv = null;
        try {
            csv = FileUtils.loadFile(csvPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Result> results = loadResultsFromCsv(csv);

        return results;
    }

    public static List<Result> loadResultsFromCsv (List<String> csv) {
        Result result = new Result();

        List<Result> results = new ArrayList<>();

        if (csv.isEmpty()) {
            return null;
        }

        String header = csv.remove(0);
        String [] splitHeader = header.split(SEPARATOR_TAG);
        for (int i = 0; i < splitHeader.length; i++) {
            splitHeader[i] = CsvUtils.normalizeAttributeToLoad(splitHeader[i]);
        }

        Field[] fields = result.getClass().getDeclaredFields();

        LinkedHashMap<String, Field> fieldMap = new LinkedHashMap<>();
        for (Field f : fields) {
            fieldMap.put(f.getName(), f);
        }

        LinkedHashMap<String, Method> setMap = CsvUtils.extractSetMethodsFromFields(result);

        for (String line : csv) {
            result = new Result();

            String [] split = line.split(SEPARATOR_TAG);

            for (int i = 0; i < split.length; i++) {
                String name = splitHeader[i];
                Method method = setMap.get(name);

                String stringValue = split[i];
                Object value = null;

                if (!stringValue.equals("-")) {
                    if (fieldMap.get(name).getType().isAssignableFrom(Integer.TYPE) ||
                            fieldMap.get(name).getType().isAssignableFrom(Integer.class) ) {
                        value = Integer.valueOf(stringValue);
                    } else if (fieldMap.get(name).getType().isAssignableFrom(Double.TYPE) ||
                            fieldMap.get(name).getType().isAssignableFrom(Double.class)) {
                        try {
                            value = Double.valueOf(stringValue);
                        } catch (NumberFormatException ex) {
                            NumberFormat formatter = new DecimalFormat("#0.00");
                            try {
                                value = formatter.parse(stringValue).doubleValue();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (fieldMap.get(name).getType().isAssignableFrom(String.class)) {
                        value = stringValue;
                    } else if (fieldMap.get(name).getType().isAssignableFrom(List.class)) {
                        if (!stringValue.equals("[]")) {
                            value = ArrayUtils.fromString(stringValue);
                        }
                    }
                }

                try {
                    method.invoke(result, value);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            results.add(result);
        }

        return results;
    }

    public static void saveResultsIntoCsv(List<Result> results, String csvPath) {
        if (!results.isEmpty()) {
            List<String> csv = null;
            List<Result> oldResults = null;

            boolean fileNotFound = false;
            try {
                csv = FileUtils.loadFile(csvPath);
                oldResults = loadResultsFromCsv(csv);
            } catch (IOException e) {
                fileNotFound = true;
            }

            if (!fileNotFound) {
                String algorithmName = ((Result)results.get(0)).getAlgorithmName();
                String oldAlgorithmName = oldResults.get(oldResults.size()-1).getAlgorithmName();

                // Increase the number of experiment
                if (oldAlgorithmName.equals(algorithmName)) {
                    int newNumberOfExperimentation = oldResults.get(oldResults.size()-1).getNumberOfExperimentation() + 1;

                    for (int i = 0; i < results.size(); i++) {
                        ((Result)results.get(i)).setNumberOfExperimentation(newNumberOfExperimentation);
                    }
                }
            }

            List<Object> resultsToSend = new ArrayList<>();
            for (Result result : results) {
                resultsToSend.add(result);
            }

            CsvUtils.appendClassAsCsv(resultsToSend, csvPath);
        }
    }

    public static void printResults (List<Result> results) {
        System.out.println("\nRESULT INFO\n");

        List<Object> resultsObj = new ArrayList<>(results);
        List<String> csv = CsvUtils.convertObjectListIntoCSV(resultsObj);

        for (String line : csv) {
            line = line.replaceAll(SEPARATOR_TAG, "\n");
            System.out.println(line);
            System.out.println();
        }
    }
}
