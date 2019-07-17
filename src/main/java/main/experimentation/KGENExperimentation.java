package main.experimentation;

import anonymization.KAnonymity;
import approaches.geneticalgorithm.AnonymizationAlgorithm;
import approaches.geneticalgorithm.AnonymizationProblem;
import approaches.geneticalgorithm.AnonymizationSetting;
import dataset.beans.Dataset;
import dataset.generator.DatasetGenerator;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.util.JMException;
import lattice.bean.Lattice;
import lattice.bean.Node;
import lattice.generator.LatticeGenerator;
import utils.ArrayUtils;
import utils.DatasetUtils;
import utils.XlsUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class KGENExperimentation {
    private static final String PROJECT_DIR = System.getProperty("user.dir") + File.separator;
    private static final String DATASET_FOLDER_DIR = PROJECT_DIR + "dataset" + File.separator;
    private static final String CONFIG_FOLDER_DIR = PROJECT_DIR + "config" + File.separator;

    private static final String datasetPath = DATASET_FOLDER_DIR + "F2_Dataset.xlsx";
    private static final String randomDatasetPath = DATASET_FOLDER_DIR + "RandomDataset.xlsx";
    private static final String configIdentifierPath = CONFIG_FOLDER_DIR + "configIdentifier.txt";
    private static final String randomDatasetConfigPath = CONFIG_FOLDER_DIR + "randomDatasetConfig.txt";

    private static final boolean RANDOM_TEST = true;

    public static void execute() throws IOException, JMException, ClassNotFoundException {
        Dataset dataset = null;

        if (RANDOM_TEST) {
            File randomDatasetFile = new File(randomDatasetPath);

            if (randomDatasetFile.exists()) {
                dataset = XlsUtils.readXlsx(randomDatasetPath);
                DatasetUtils.loadProperties(dataset, randomDatasetConfigPath);
            } else {
                dataset = DatasetGenerator.generateRandomDataset(20000);
                XlsUtils.writeXlsx(randomDatasetPath, dataset);
            }

            //Initialize the genetic algorithm
            AnonymizationProblem problem = new AnonymizationProblem(dataset);
            AnonymizationSetting setting = new AnonymizationSetting(problem);
            AnonymizationAlgorithm algorithm = (AnonymizationAlgorithm) setting.configure();

            long start = System.currentTimeMillis();

            SolutionSet bestSolutions = algorithm.execute();

            KAnonymity kAnonymity = problem.getkAnonymity();
            Lattice lattice = LatticeGenerator.generateOnlyNodes(kAnonymity.lowerBounds(), kAnonymity.upperBounds());

            Set<Node> tmpResults = new LinkedHashSet<>();
            for (int i = 0; i < bestSolutions.size(); i++) {
                tmpResults.add(lattice.getNode(getSolutionValues(bestSolutions.get(i))));
            }

            List<Node> results = new ArrayList<>(tmpResults);
            for (int i = 0; i < results.size(); i++) {
                Node iNode = results.get(i);
                for (int j = 0; j < results.size(); j++) {
                    Node jNode = results.get(j);
                    if (i != j) {
                        if (ArrayUtils.geq(jNode.getActualGeneralization(), iNode.getActualGeneralization())) {
                            results.remove(j--);
                        }
                    }
                }
            }

            System.out.println("Solution");
            for (Node n : results) {
                System.out.println(n.getActualGeneralization());
            }

            System.out.println("Execution time: " + (double)(System.currentTimeMillis()-start)/1000);


        } else {}
    }

    private static ArrayList<Integer> getSolutionValues (Solution solution) throws JMException {
        ArrayList<Integer> values = new ArrayList<Integer>();

        for (Variable var : solution.getDecisionVariables()) {
            values.add((int) var.getValue());
        }

        return values;
    }
}
