package runner.experimentation.util;

import lattice.LatticeUtils;
import runner.experimentation.bean.Result;
import runner.experimentation.bean.Stat;
import utils.ArrayUtils;
import utils.CsvUtils;
import utils.FileUtils;

import java.io.IOException;
import java.util.*;

public class StatisticalUtils {
    public static void saveStatsIntoCsv (List<Stat> stats, String csvPath) {
        List<Object> statsToConvert = new ArrayList<>();
        for (Stat stat : stats) {
            statsToConvert.add(stat);
        }

        CsvUtils.appendClassAsCsv(statsToConvert, csvPath);
    }

    public static List<Stat> getStatsOfResults (List<Result> results) {
        List<Stat> stats = new ArrayList<>();

        /*
        Map composition
        -------------------------------------------------------
        |number of qi|           Algorithms Map               |
        |            |Algorithm name|Results of that algorithm|
        -------------------------------------------------------
         */
        LinkedHashMap<Integer, LinkedHashMap<String, List<Result>>> configResultMap = initConfigMap(results);

        // Iterate on config
        for (Map.Entry<Integer, LinkedHashMap<String, List<Result>>> entry : configResultMap.entrySet()) {
            LinkedHashMap<String, List<Result>> algorithmMap = entry.getValue();

            // Get the optimal solution
            List<Result> optimalResults = null;
            if (!algorithmMap.get("OLA").isEmpty()) {
                optimalResults = algorithmMap.get("OLA");
            }

            else if (!algorithmMap.get("EXHAUSTIVE").isEmpty()) {
                optimalResults = algorithmMap.get("EXHAUSTIVE");
            }

            for (Map.Entry<String, List<Result>> algorithmEntry : algorithmMap.entrySet()) {
                Stat stat = getStatOfResults(algorithmEntry.getValue(), optimalResults);
                stats.add(stat);
            }
        }

        return stats;
    }

    public static Stat getStatOfResults (List<Result> results, List<Result> optimalResults) {
        Stat stat = null;

        if (!results.isEmpty()) {
            Result firstResult = results.get(0);

            int maxRun = 0;

            Double averageExecTime = null;
            Double averageAccuraces = null;
            Double averageLog = null;
            Double averageSuppression = null;

            List<Double> execTimes = new ArrayList<>();
            List<Double> accuraces = new ArrayList<>();
            List<Double> logs = new ArrayList<>();
            List<Double> suppressions = new ArrayList<>();

            for (Result result : results) {
                if (result.getExecutionTime() != null)
                    execTimes.add(result.getExecutionTime());

                if (result.getSolution() != null && optimalResults != null) {
                    accuraces.add(getAccuracy(optimalResults, result));
                }

                if (result.getLogMetric() != null) {
                    logs.add(result.getLogMetric());
                }

                if (result.getPercentageOfSuppression() != null) {
                    suppressions.add(result.getPercentageOfSuppression());
                }

                if (result.getNumberOfExperimentation() > maxRun) {
                    maxRun = result.getNumberOfExperimentation();
                }
            }

            if (!execTimes.isEmpty()) {
                averageExecTime = ArrayUtils.doubleSum(execTimes) / execTimes.size();
            }

            if (!accuraces.isEmpty()) {
                averageAccuraces = ArrayUtils.doubleSum(accuraces) / accuraces.size();
            }

            if (!logs.isEmpty()) {
                averageLog = ArrayUtils.doubleSum(logs) / logs.size();
            }

            if (!suppressions.isEmpty()) {
                averageSuppression = ArrayUtils.doubleSum(suppressions) / suppressions.size();
            }

            stat = new Stat(firstResult.getDatasetName(), String.valueOf(firstResult.getNumberOfAttributesAnalyzed()),
                    firstResult.getAlgorithmName(), maxRun, averageExecTime, averageAccuraces, averageLog, averageSuppression);
        }


        return stat;
    }

    /**
     * Map composition
     *
     *-------------------------------------------------------
     *|number of qi|           Algorithms Map               |
     *|            |Algorithm name|Results of that algorithm|
     *-------------------------------------------------------
     *
     * @param results
     * @return
     */
    private static LinkedHashMap<Integer, LinkedHashMap<String, List<Result>>> initConfigMap (List<Result> results) {
        LinkedHashMap<Integer, LinkedHashMap<String, List<Result>>> configResultMap = new LinkedHashMap<>();

        //Extract all config qi and all algorithm names
        Set<Integer> numberOfQiList = new LinkedHashSet<>();
        Set<String> algorithmNames = new LinkedHashSet<>();
        for (Result result: results) {
            numberOfQiList.add(result.getNumberOfAttributesAnalyzed());
            algorithmNames.add(result.getAlgorithmName());
        }

        // Initialize the configMap
        for (int numberOfQI : numberOfQiList) {
            LinkedHashMap<String, List<Result>> algorithmMap = new LinkedHashMap<>();
            for (String algorithmName : algorithmNames) {
                algorithmMap.put(algorithmName, new ArrayList<>());
            }

            configResultMap.put(numberOfQI, algorithmMap);
        }

        List<Result> resultsClone = new ArrayList<>(results);
        for (Result result : resultsClone) {
            int numberOfQI = result.getNumberOfAttributesAnalyzed();
            String algorithmName = result.getAlgorithmName();

            configResultMap.get(numberOfQI).get(algorithmName).add(result);
        }

        return configResultMap;
    }

    private static double getAccuracy (List<Result> exactResults, Result result) {
        double accuracy = -1;

        if (result.getSolution() != null) {
            for (Result res : exactResults) {
                boolean sameStrategyPath = LatticeUtils.sameStrategyPath(res.getSolution(), result.getSolution());

                if (sameStrategyPath) {
                    double tmpAccuracy = 1 - (double)(ArrayUtils.sum(result.getSolution()) - ArrayUtils.sum(res.getSolution())) /
                            (ArrayUtils.sum(res.getTopNode()) - ArrayUtils.sum(res.getSolution()));

                    if (tmpAccuracy > accuracy) {
                        accuracy = tmpAccuracy;
                    }
                }
            }
        }

        return accuracy;
    }
}
