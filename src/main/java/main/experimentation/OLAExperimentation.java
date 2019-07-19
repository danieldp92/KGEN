package main.experimentation;

import approaches.ola.OLAAlgorithm;
import controller.LatticeController;
import dataset.beans.Dataset;
import dataset.generator.DatasetGenerator;
import exception.DatasetNotFoundException;
import exception.IOPropertiesException;
import main.experimentation.exceptions.ControllerNotFoundException;
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
        if (this.dataset == null) {
            throw new DatasetNotFoundException();
        }

        if (this.latticeController == null) {
            throw new ControllerNotFoundException();
        }

        this.olaAlgorithm = new OLAAlgorithm(this.dataset, this.latticeController);

        for (int indexRun = 1; indexRun <= numberOfRun; indexRun++) {
            long start = System.currentTimeMillis();

            this.solutions = olaAlgorithm.run();

            this.executionTime = (double)(System.currentTimeMillis()-start)/1000;

            saveInfoExperimentation(indexRun);
        }

    }

    @Override
    public void saveInfoExperimentation(int indexRun) {
        ArrayList<String> infoExperimentation = new ArrayList<>();
        infoExperimentation.add("OLA RESULTS\n");

        ArrayList<Integer> lowerBounds = this.olaAlgorithm.getkAnonymity().lowerBounds();
        ArrayList<Integer> upperBounds = this.olaAlgorithm.getkAnonymity().upperBounds();

        infoExperimentation.add("Lower Bound: " + lowerBounds);
        infoExperimentation.add("Upper Bound: " + upperBounds);

        //Lattice size
        int latticeSize = 1;
        for (int i = 0; i < upperBounds.size(); i++) {
            latticeSize *= (upperBounds.get(i) - lowerBounds.get(i) + 1);
        }

        infoExperimentation.add("Lattice size: " + latticeSize);
        infoExperimentation.add("Execution time: " + this.executionTime + "\n");

        infoExperimentation.add("Solutions");
        for (List<Integer> solution : solutions) {
            infoExperimentation.add(solution.toString());
        }

        try {
            FileUtils.saveFile(infoExperimentation, RESULTS_DIR + "olaExp.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
