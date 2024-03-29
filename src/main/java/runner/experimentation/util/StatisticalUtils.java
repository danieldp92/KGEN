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
        if (stats != null) {
            List<Object> statsToConvert = new ArrayList<>();
            for (Stat stat : stats) {
                statsToConvert.add(stat);
            }

            CsvUtils.appendClassAsCsv(statsToConvert, csvPath);
        }
    }

    public static List<Stat> getStatsOfResults (List<Result> results) {
        if (results == null) {
            return null;
        }

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
            if (algorithmMap.get("OLA") != null && !algorithmMap.get("OLA").isEmpty()) {
                // Remove all results with null solution
                List<Result> tmpOlaResults = new ArrayList<>(algorithmMap.get("OLA"));
                for (int i = 0; i < tmpOlaResults.size(); i++) {
                    if (tmpOlaResults.get(i).getSolution() == null) {
                        tmpOlaResults.remove(i--);
                    }
                }

                optimalResults = tmpOlaResults;
            }

            else if (algorithmMap.get("EXHAUSTIVE") != null && !algorithmMap.get("EXHAUSTIVE").isEmpty()) {
                List<Result> tmpExhaustiveResults = new ArrayList<>(algorithmMap.get("OLA"));
                for (int i = 0; i < tmpExhaustiveResults.size(); i++) {
                    if (tmpExhaustiveResults.get(i).getSolution() == null) {
                        tmpExhaustiveResults.remove(i--);
                    }
                }
                optimalResults = tmpExhaustiveResults;
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

        if (results != null && !results.isEmpty()) {
            Result firstResult = results.get(0);
            int maxRun = 0;

            Double averageExecTime = null;
            Double averageAccuraces = null;
            Double averageLog = null;
            Double averageSuppression = null;

            List<Double> execTimes = new ArrayList<>();
            double weightedSum = 0;
            List<Double> weightedAccuraces = new ArrayList<>();
            List<Double> logs = new ArrayList<>();
            List<Double> suppressions = new ArrayList<>();

            for (Result result : results) {
                if (result.getExecutionTime() != null)
                    execTimes.add(result.getExecutionTime());

                /*if (result.getSolution() != null && optimalResults != null) {
                    accuraces.add(getAccuracy(optimalResults, result));
                }*/

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

            // Accuracy
            if (optimalResults != null && !optimalResults.isEmpty()) {
                int i = 1;
                for (Result optimalResult : optimalResults) {
                    double weight = 1 - optimalResult.getLogMetric();

                    weightedSum += weight;
                    double weightedAccuracy = weight * getAccuracy(results, optimalResult);

                    weightedAccuraces.add(weightedAccuracy);
                }
            }

            execTimes.removeAll(Collections.singletonList(null));
            weightedAccuraces.removeAll(Collections.singletonList(null));
            logs.removeAll(Collections.singletonList(null));
            suppressions.removeAll(Collections.singletonList(null));

            if (!execTimes.isEmpty()) {
                averageExecTime = ArrayUtils.doubleSum(execTimes) / execTimes.size();
            }

            if (!weightedAccuraces.isEmpty()) {
                // Weighted aritmetic mean
                averageAccuraces = ArrayUtils.doubleSum(weightedAccuraces) / weightedSum;
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

    /*private static Double getAccuracy (List<Result> exactResults, Result result) {
        Double accuracy = null;

        if (result != null && result.getSolution() != null && exactResults != null) {
            for (Result res : exactResults) {
                boolean sameStrategyPath = LatticeUtils.sameStrategyPath(res.getSolution(), result.getSolution());

                if (sameStrategyPath) {
                    double tmpAccuracy = 1 - (double)(ArrayUtils.sum(result.getSolution()) - ArrayUtils.sum(res.getSolution())) /
                            (ArrayUtils.sum(res.getTopNode()) - ArrayUtils.sum(res.getSolution()));

                    if (accuracy == null || tmpAccuracy > accuracy) {
                        accuracy = tmpAccuracy;
                    }
                }
            }
        }

        return accuracy;
    }*/

    private static Double getAccuracy (List<Result> results, Result optimalResult) {
        Double accuracy = new Double(0);

        if (optimalResult != null && optimalResult.getSolution() != null && results != null) {
            for (Result res : results) {
                boolean sameStrategyPath = LatticeUtils.sameStrategyPath(res.getSolution(), optimalResult.getSolution());
                if (sameStrategyPath) {
                    double tmpAccuracy = 1 - (double)(ArrayUtils.sum(res.getSolution()) - ArrayUtils.sum(optimalResult.getSolution())) /
                            (ArrayUtils.sum(optimalResult.getTopNode()) - ArrayUtils.sum(optimalResult.getSolution()));
                    if (tmpAccuracy > accuracy) {
                        accuracy = tmpAccuracy;
                    }
                }
            }
        }

        return accuracy;
    }

    private static Double getWeightedAccuracy (List<Result> results, Result optimalResult) {
        Double accuracy = new Double(0);

        if (optimalResult != null && optimalResult.getSolution() != null && results != null) {
            for (Result res : results) {
                boolean sameStrategyPath = LatticeUtils.sameStrategyPath(res.getSolution(), optimalResult.getSolution());
                if (sameStrategyPath) {
                    double tmpAccuracy = 1 - (double)(ArrayUtils.sum(res.getSolution()) - ArrayUtils.sum(optimalResult.getSolution())) /
                            (ArrayUtils.sum(optimalResult.getTopNode()) - ArrayUtils.sum(optimalResult.getSolution()));
                    if (tmpAccuracy > accuracy) {
                        accuracy = tmpAccuracy;
                    }
                }
            }
        }

        return accuracy;
    }
}
