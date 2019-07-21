package main.experimentation;

import anonymization.KAnonymity;
import approaches.geneticalgorithm.AnonymizationAlgorithm;
import approaches.geneticalgorithm.AnonymizationProblem;
import approaches.geneticalgorithm.AnonymizationSetting;
import controller.LatticeController;
import dataset.beans.Dataset;
import dataset.generator.DatasetGenerator;
import exception.DatasetNotFoundException;
import exception.IOPropertiesException;
import javafx.application.Platform;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.util.JMException;
import lattice.bean.Lattice;
import lattice.bean.Node;
import lattice.generator.LatticeGenerator;
import main.experimentation.bean.Result;
import main.experimentation.exceptions.ControllerNotFoundException;
import utils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class KGENExperimentation extends Experimentation{
    private AnonymizationProblem anonymizationProblem;
    private AnonymizationSetting anonymizationSetting;
    private AnonymizationAlgorithm anonymizationAlgorithm;

    @Override
    public void execute(int numberOfRun) throws DatasetNotFoundException, ControllerNotFoundException {
        //Initialize the genetic algorithm
        this.anonymizationProblem = new AnonymizationProblem(dataset);
        this.anonymizationSetting = new AnonymizationSetting(this.anonymizationProblem, latticeController);
        try {
            this.anonymizationAlgorithm = (AnonymizationAlgorithm) this.anonymizationSetting.configure();
        } catch (JMException e) {
            e.printStackTrace();
        }

        for (int run = 1; run <= numberOfRun; run++) {
            System.out.println("KGEN " + run);
            long start = System.currentTimeMillis();

            SolutionSet bestSolutions = null;
            try {
                bestSolutions = this.anonymizationAlgorithm.execute();
            } catch (JMException | ClassNotFoundException e) {
                e.printStackTrace();
            }


            this.solutions = new ArrayList<>();
            for (int i = 0; i < bestSolutions.size(); i++) {
                this.solutions.add(getSolutionValues(bestSolutions.get(i)));
            }

            //Remove all k-anonymous solutions that are of the same strategy path (except for the minimal k-anonymous node)
            for (int i = 0; i < this.solutions.size(); i++) {
                List<Integer> iResult = this.solutions.get(i);
                for (int j = 0; j < this.solutions.size(); j++) {
                    List<Integer> jResult = this.solutions.get(j);
                    if (i != j) {
                        if (ArrayUtils.geq(jResult, iResult)) {
                            this.solutions.remove(j--);
                        }
                    }
                }
            }

            this.executionTime = (double)(System.currentTimeMillis()-start)/1000;

            saveInfoExperimentation(run);
        }

    }

    @Override
    public void saveInfoExperimentation(int indexRun) {
        List<Object> results = new ArrayList<>();

        String datasetName = dataset.getName();
        int numberOfAttributes = dataset.getColumns().size();
        String algorithmName = "KGEN";

        ArrayList<Integer> bottomNode = this.anonymizationProblem.getkAnonymity().lowerBounds();
        ArrayList<Integer> topNode = this.anonymizationProblem.getkAnonymity().upperBounds();

        //Lattice size
        int latticeSize = 1;
        for (int i = 0; i < topNode.size(); i++) {
            latticeSize *= (topNode.get(i) - bottomNode.get(i) + 1);
        }

        for (List<Integer> solution : solutions) {
            Result tmpResult = new Result(datasetName, indexRun, numberOfAttributes, algorithmName, executionTime,
                    latticeSize, bottomNode, topNode, solution);
            results.add(tmpResult);
        }

        CsvUtils.appendClassAsCsv(results, RESULTS_FILE_PATH);
    }


    private static ArrayList<Integer> getSolutionValues (Solution solution) {
        ArrayList<Integer> values = new ArrayList<Integer>();

        for (Variable var : solution.getDecisionVariables()) {
            try {
                values.add((int) var.getValue());
            } catch (JMException e) {
                e.printStackTrace();
            }
        }

        return values;
    }
}
