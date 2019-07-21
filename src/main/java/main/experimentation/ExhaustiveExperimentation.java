package main.experimentation;

import approaches.exhaustive.ExhaustiveAlgorithm;
import dataset.beans.Dataset;
import dataset.generator.DatasetGenerator;
import exception.DatasetNotFoundException;
import main.experimentation.bean.Result;
import main.experimentation.exceptions.ControllerNotFoundException;
import utils.CsvUtils;
import utils.DatasetUtils;
import utils.FileUtils;
import utils.XlsUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExhaustiveExperimentation extends Experimentation{
    private ExhaustiveAlgorithm exhaustiveAlgorithm;

    @Override
    public void execute(int numberOfRun) throws DatasetNotFoundException, ControllerNotFoundException {
        System.out.println("EXHAUSTIVE 1");
        if (this.dataset == null) {
            throw new DatasetNotFoundException();
        }

        if (this.latticeController == null) {
            throw new ControllerNotFoundException();
        }

        this.exhaustiveAlgorithm = new ExhaustiveAlgorithm(dataset);

        long start = System.currentTimeMillis();

        this.solutions = this.exhaustiveAlgorithm.run();

        this.executionTime = (double)(System.currentTimeMillis()-start)/1000;

        saveInfoExperimentation(1);
    }

    @Override
    public void saveInfoExperimentation(int indexRun) {
        List<Object> results = new ArrayList<>();

        String datasetName = dataset.getName();
        int numberOfAttributes = dataset.getColumns().size();
        String algorithmName = "EXHAUSTIVE";

        ArrayList<Integer> bottomNode = this.exhaustiveAlgorithm.getkAnonymity().lowerBounds();
        ArrayList<Integer> topNode = this.exhaustiveAlgorithm.getkAnonymity().upperBounds();

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
}
