package runner.experimentation;

import approaches.exhaustive.ExhaustiveAlgorithm;
import exception.DatasetNotFoundException;
import runner.Main;
import runner.experimentation.thread.ExecutionThread;

public class ExhaustiveExperimentation extends Experimentation{
    private ExhaustiveAlgorithm exhaustiveAlgorithm;

    public ExhaustiveExperimentation(String resultPath) {
        super(resultPath);
    }

    @Override
    public void execute(int numberOfRun, double suppressionTreshold) throws DatasetNotFoundException {
        if (Main.SHOW_LOG_MESSAGE) System.out.println("\nEXHAUSTIVE");
        if (this.dataset == null) {
            throw new DatasetNotFoundException();
        }

        this.exhaustiveAlgorithm = new ExhaustiveAlgorithm(dataset, suppressionTreshold);
        this.kAnonymity = this.exhaustiveAlgorithm.getkAnonymity();

        long start = System.currentTimeMillis();

        ExecutionThread executionThread = new ExecutionThread(exhaustiveAlgorithm, 1);
        executionThread.start();

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

        saveInfoExperimentation(this.exhaustiveAlgorithm.getName(),
                this.exhaustiveAlgorithm.getkAnonymity(), 1);
    }

}
