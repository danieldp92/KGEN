package runner.experimentation;

import approaches.ola.OLAAlgorithm;
import exception.DatasetNotFoundException;
import runner.Main;
import runner.experimentation.thread.ExecutionThread;

public class OLAExperimentation extends Experimentation {
    private OLAAlgorithm olaAlgorithm;

    public OLAExperimentation(String resultPath) {
        super(resultPath);
    }

    @Override
    public void execute(int numberOfRun, double suppressionTreshold) throws DatasetNotFoundException {
        if (Main.SHOW_LOG_MESSAGE) System.out.println("\nOLA");
        if (this.dataset == null) {
            throw new DatasetNotFoundException();
        }

        this.olaAlgorithm = new OLAAlgorithm(this.dataset, suppressionTreshold);

        // Run the algorithm in a separate thread. In this way,
        // it's possible to handle the algorithm timeout
        ExecutionThread executionThread = new ExecutionThread(olaAlgorithm, 1);
        executionThread.start();

        long start = System.currentTimeMillis();

        // Wait the thread OR the stop condition
        while (executionThread.isAlive()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.solutions = executionThread.getSolutions();
        if (this.solutions != null) {
            this.executionTime = (double)(System.currentTimeMillis()-start)/1000;
        }

        saveInfoExperimentation(this.olaAlgorithm.getName(),
                this.olaAlgorithm.getkAnonymity(), 1);
    }
}
