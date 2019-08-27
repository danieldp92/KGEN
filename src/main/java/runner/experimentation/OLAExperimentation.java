package runner.experimentation;

import approaches.ola.OLAAlgorithm;
import exception.DatasetNotFoundException;
import runner.experimentation.exceptions.ControllerNotFoundException;
import runner.experimentation.thread.ExecutionThread;

public class OLAExperimentation extends Experimentation {
    private OLAAlgorithm olaAlgorithm;

    @Override
    public void execute(int numberOfRun) throws DatasetNotFoundException, ControllerNotFoundException {
        System.out.println("\nOLA");
        if (this.dataset == null) {
            throw new DatasetNotFoundException();
        }

        /*if (this.latticeController == null) {
            throw new ControllerNotFoundException();
        }*/

        this.olaAlgorithm = new OLAAlgorithm(this.dataset);

        // Run the algorithm in a separate thread. In this way,
        // it's possible to handle the algorithm timeout
        ExecutionThread executionThread = new ExecutionThread(olaAlgorithm);
        executionThread.start();

        long start = System.currentTimeMillis();

        // Wait the thread OR the stop condition
        while (executionThread.isAlive() &&
                (System.currentTimeMillis() - start) < MAX_EVALUATION_TIME) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // The algorithm has a solution
        if ((System.currentTimeMillis() - start) < MAX_EVALUATION_TIME) {
            this.solutions = executionThread.getSolutions();
            this.executionTime = (double)(System.currentTimeMillis()-start)/1000;
        } else {
            executionThread.interrupt();
        }

        saveInfoExperimentation(this.olaAlgorithm.getName(),
                this.olaAlgorithm.getkAnonymity(), 1);
    }
}
