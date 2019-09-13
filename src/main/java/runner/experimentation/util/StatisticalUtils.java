package runner.experimentation.util;

import lattice.LatticeUtils;
import runner.experimentation.bean.Result;
import runner.experimentation.bean.Stat;
import utils.ArrayUtils;
import utils.CsvUtils;
import utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StatisticalUtils {
    private static final int EXEC_TIME_TYPE = 0;
    private static final int ACCURACY_TYPE = 1;
    private static final int LOG_TYPE = 2;
    private static final int SUPPRESSION_TYPE = 3;


    public static void saveStatsIntoCsv (List<Stat> stats, String csvPath) {
        List<Object> statsToConvert = new ArrayList<>();
        for (Stat stat : stats) {
            statsToConvert.add(stat);
        }

        CsvUtils.appendClassAsCsv(statsToConvert, csvPath);
    }

    public static List<Stat> getStatsOfResults (List<Result> results, List<Result> optimalResults) {
        List<Result> resultsClone = new ArrayList<>(results);
        List<Stat> stats = new ArrayList<>();

        List<Result> actualResults = new ArrayList<>();
        Result prevResult = null;

        while (!resultsClone.isEmpty()) {
            Result actualResult = resultsClone.remove(0);

            // If the actual result algorithm is different from the previous one, then generate a util for the previous algorithm
            if ((prevResult != null && !prevResult.getAlgorithmName().equals(actualResult.getAlgorithmName())) || resultsClone.isEmpty()) {
                // ActualResult is the last element of the array
                if (resultsClone.isEmpty()) {
                    actualResults.add(actualResult);
                    prevResult = actualResult;
                }

                double averageExecTime = getExecTimeMean(actualResults);
                double averageAccuracy = getAccuracyMean(optimalResults, actualResults);
                double averageLog = getLogMean(actualResults);
                double averageSuppression = getSuppressionMean(actualResults);

                Stat stat = new Stat(prevResult.getDatasetName(), String.valueOf(prevResult.getNumberOfAttributesAnalyzed()),
                        prevResult.getAlgorithmName(), averageExecTime, averageAccuracy, averageLog, averageSuppression);

                stats.add(stat);

                actualResults.clear();
            }

            prevResult = actualResult;
            actualResults.add(actualResult);
        }

        return stats;
    }

    public static List<Stat> getStatsOfResults (List<Result> results) throws Exception {
        List<Result> resultsClone = new ArrayList<>(results);
        if (results == null || results.isEmpty()) {
            return null;
        }

        List<Result> optimalResults = new ArrayList<>();
        for (Result res : results) {
            if (res.getAlgorithmName().equals("OLA")) {
                optimalResults.add(res);
            }
        }


        // Try with EXHAUSTIVE ALGORITHM
        if (optimalResults.isEmpty()) {
            for (Result res : results) {
                if (res.getAlgorithmName().equals("EXHAUSTIVE")) {
                    optimalResults.add(res);
                }
            }
        }

        if (optimalResults.isEmpty()) {
            throw new Exception("There are no optimal solution. Impossible to calculate accuracy");
        }

        List<Stat> stats = getStatsOfResults(results, optimalResults);

        return stats;
    }

    public static double getExecTimeMean (List<Result> results) {
        return getMean(results, EXEC_TIME_TYPE);
    }

    public static double getAccuracyMean (List<Result> optimalResults, List<Result> results) {
        double mean = 0;
        int counter = 0;

        for (Result result : results) {
            if (result.getSolution() != null) {
                double accuracy = getAccuracy(optimalResults, result);
                if (accuracy != -1) {
                    mean += accuracy;
                    counter++;
                }
            }
        }

        if (counter == 0) {
            mean = -1;
        }

        return mean/counter;
    }

    public static double getLogMean (List<Result> results) {
        return getMean(results, LOG_TYPE);
    }

    public static double getSuppressionMean (List<Result> results) {
        return getMean(results, SUPPRESSION_TYPE);
    }

    private static double getMean(List<Result> results, int statType) {
        double mean = 0;
        int counter = 0;

        for (Result result : results) {
            double metric = 0;

            switch (statType) {
                case EXEC_TIME_TYPE:
                    metric = result.getExecutionTime();
                    break;
                case ACCURACY_TYPE:
                    if (result.getSolution() != null) {
                        if (result.getAlgorithmName().equals("EXHAUSTIVE") || result.getAlgorithmName().equals("OLA")) {
                            mean++;
                            counter++;
                        } else {
                            double accuracy = getAccuracy(results, result);
                            if (accuracy != -1) {
                                mean += accuracy;
                                counter++;
                            }
                        }
                    }
                    break;
                case LOG_TYPE:
                    metric = result.getLogMetric();
                    break;
                case SUPPRESSION_TYPE:
                    metric = result.getPercentageOfSuppression();
                    break;
            }

            if (metric != -1) {
                counter++;
                mean += metric;
            }
        }

        if (counter == 0) {
            mean = -1;
        }

        return mean/counter;
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
