package main.experimentation;

import approaches.exhaustive.ExhaustiveAlgorithm;
import dataset.beans.Dataset;
import dataset.generator.DatasetGenerator;
import exception.DatasetNotFoundException;
import main.experimentation.bean.Result;
import main.experimentation.exceptions.ControllerNotFoundException;
import main.experimentation.exceptions.OutOfTimeException;
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

        /*if (this.latticeController == null) {
            throw new ControllerNotFoundException();
        }*/

        this.exhaustiveAlgorithm = new ExhaustiveAlgorithm(dataset);

        long start = System.currentTimeMillis();

        try {
            this.solutions = this.exhaustiveAlgorithm.run();

            this.executionTime = (double)(System.currentTimeMillis()-start)/1000;
        } catch (OutOfTimeException e) {
            //Save Nan on csv
            this.outOfTime = true;
        }

        saveInfoExperimentation(this.exhaustiveAlgorithm.getName(),
                this.exhaustiveAlgorithm.getkAnonymity(), 1);
    }

}
