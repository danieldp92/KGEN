package main.experimentation;

import approaches.ola.OLAAlgorithm;
import controller.LatticeController;
import dataset.beans.Dataset;
import dataset.generator.DatasetGenerator;
import exception.DatasetNotFoundException;
import exception.IOPropertiesException;
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

public class OLAExperimentation extends Experimentation {
    private OLAAlgorithm olaAlgorithm;

    @Override
    public void execute(int numberOfRun) throws DatasetNotFoundException, ControllerNotFoundException {
        System.out.println("OLA 1");
        if (this.dataset == null) {
            throw new DatasetNotFoundException();
        }

        /*if (this.latticeController == null) {
            throw new ControllerNotFoundException();
        }*/

        this.olaAlgorithm = new OLAAlgorithm(this.dataset, this.latticeController);

        long start = System.currentTimeMillis();

        try {
            this.solutions = olaAlgorithm.run();
            this.executionTime = (double)(System.currentTimeMillis()-start)/1000;
        } catch (OutOfTimeException e) {
            this.outOfTime = true;
        }

        saveInfoExperimentation(this.olaAlgorithm.getName(),
                this.olaAlgorithm.getkAnonymity(), 1);
    }
}
