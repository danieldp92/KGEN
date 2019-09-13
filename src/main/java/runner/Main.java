package runner;

import runner.experimentation.exceptions.ArgumentException;
import ui.cui.AnonymizationCLI;
import utils.ArrayUtils;
import utils.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static final boolean SHOW_LOG_MESSAGE = false;
    public static final boolean EXACT_METAHEURISTIC_VERIFICATION = false;

    public static void main (String [] args) throws IOException, ArgumentException {
        AnonymizationCLI anonymizationCLI = new AnonymizationCLI();
        anonymizationCLI.run(args);

        //ResultUtils.loadResultsFromCsv("C:\\Users\\20190482\\Documents\\GitHub\\KGEN\\results\\Accuracy\\resultsWithTreshold_0.005.csv");
        //Run GUI
        //LatticeGui.run(args);
    }

    public static void accuracy() throws IOException {
        List<String> csv = FileUtils.loadFile("C:\\Users\\20190482\\Documents\\GitHub\\KGEN\\results\\resultsWithoutTreshold.csv");
        List<String> newCsv = new ArrayList<>();
        newCsv.add(csv.remove(0) + "accuracy;");

        List<List<Integer>> exactSolutions = new ArrayList<>();
        int lastSize = -1;
        for (String s : csv) {
            String [] split = s.split(";");
            int size = Integer.parseInt(split[2]);
            String algName = split[4];
            String bottom = split[7];
            String top = split[8];
            String sol = "";

            List<Integer> bottomNode = fromString(bottom);
            List<Integer> topNode = fromString(top);
            List<Integer> solution = null;

            double accuracy = -1;

            try {
                sol = split[9];
                solution= fromString(sol);

                if (algName.equals("OLA")) {
                    if (size != lastSize) {
                        exactSolutions.clear();
                        lastSize = size;
                    }
                    exactSolutions.add(solution);
                    accuracy = 1;
                } else if (!algName.equals("EXHAUSTIVE")) {
                    // Calculate accuracy
                    for (List<Integer> exactSolution : exactSolutions) {
                        if (areOfTheSameStrategyPath(exactSolution, solution)) {
                            double tmpAccuracy = 1 - (double)(ArrayUtils.sum(solution) - ArrayUtils.sum(exactSolution)) / (ArrayUtils.sum(topNode) - ArrayUtils.sum(exactSolution));
                            if (tmpAccuracy > accuracy) {
                                accuracy = tmpAccuracy;
                            }
                        }
                    }
                } else {
                    accuracy = 1;
                }
            } catch (ArrayIndexOutOfBoundsException ex) {}

            String acc = "";
            if (accuracy != -1) {
                acc = String.valueOf(accuracy);
            }

            newCsv.add(s + acc + ";");
        }

        FileUtils.saveFile((ArrayList<String>) newCsv, "C:\\Users\\20190482\\Documents\\GitHub\\KGEN\\results\\-resultsWithoutTreshold.csv");
    }

    public static List<Integer> fromString (String line) {
        String tmpLine = line.substring(1, line.length()-1);
        tmpLine = tmpLine.replaceAll(",", "");
        String [] split = tmpLine.split(" ");

        List<Integer> array = new ArrayList<>();
        for (String s : split) {
            array.add(Integer.parseInt(s));
        }

        return array;
    }

    public static boolean areOfTheSameStrategyPath (List<Integer> node1LOG, List<Integer> node2LOG) {
        int i = 0;
        while (i < node1LOG.size() && node1LOG.get(i) >= node2LOG.get(i)) {
            i++;
        }

        //All LOG of node1 are greater then LOG of node2
        if (i >= node1LOG.size()) {
            return true;
        }


        i = 0;
        while (i < node1LOG.size() && node1LOG.get(i) <= node2LOG.get(i)) {
            i++;
        }

        //All LOG of node1 are greater then LOG of node2
        if (i >= node1LOG.size()) {
            return true;
        }

        return false;
    }
}
