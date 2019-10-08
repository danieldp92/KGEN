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
import java.util.*;

public class ResultUtils {
    private static final String SEPARATOR_TAG = ";";
    private static final String LIST_SEPARATOR_TAG = ",";

    public static List<Result> loadResultsFromCsv (String csvPath) {
        List<String> csv = null;
        try {
            csv = FileUtils.loadFile(csvPath);
        } catch (IOException e) {}

        List<Result> results = loadResultsFromCsv(csv);

        return results;
    }

    public static List<Result> loadResultsFromCsv (List<String> csv) {
        Result result = new Result();

        List<Result> results = new ArrayList<>();

        if (csv == null || csv.isEmpty()) {
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

    /**
     * Extract all configurations analyzed, given the results
     * @param results
     * @return
     */
    public static List<Integer> extractQIAnalyzed (List<Result> results) {
        Set<Integer> qiAnalyzed = new LinkedHashSet<>();

        for (Result result : results) {
            qiAnalyzed.add(result.getNumberOfAttributesAnalyzed());
        }

        return new ArrayList<>(qiAnalyzed);
    }

    /**
     * Extract all algorithm analyzed, given the results
     * @param results
     * @return
     */
    public static List<String> extractAlgorithmAnalyzed (List<Result> results) {
        Set<String> algorithmAnalyzed = new LinkedHashSet<>();

        for (Result result : results) {
            algorithmAnalyzed.add(result.getAlgorithmName());
        }

        return new ArrayList<>(algorithmAnalyzed);
    }

    public static List<Integer> extractQIAnalyzedOfAlgorithm (List<Result> results, String algorithmName) {
        Set<Integer> qiAnalysed = new LinkedHashSet<>();

        for (Result result : results) {
            if (result.getAlgorithmName().equals(algorithmName)) {
                qiAnalysed.add(result.getNumberOfAttributesAnalyzed());
            }
        }

        return new ArrayList<>(qiAnalysed);
    }

    public static List<Result> extractResults (List<Result> results, String algorithmName, int qiConfig, boolean nullSolution) {
        List<Result> resultsToReturn = new ArrayList<>();

        for (Result result : results) {
            if (result.getAlgorithmName().equals(algorithmName) &&
                    result.getNumberOfAttributesAnalyzed() == qiConfig &&
                    (result.getSolution() != null || nullSolution)) {
                resultsToReturn.add(result);
            }
        }

        return resultsToReturn;
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
