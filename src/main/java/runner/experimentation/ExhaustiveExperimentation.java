package runner.experimentation;

import approaches.exhaustive.ExhaustiveAlgorithm;
import exception.DatasetNotFoundException;
import runner.Main;
import runner.experimentation.exceptions.ControllerNotFoundException;
import runner.experimentation.thread.ExecutionThread;

public class ExhaustiveExperimentation extends Experimentation{
    private ExhaustiveAlgorithm exhaustiveAlgorithm;

    public ExhaustiveExperimentation(String resultPath) {
        super(resultPath);
    }

    @Override
    public void execute(int numberOfRun, double suppressionTreshold) throws DatasetNotFoundException, ControllerNotFoundException {
        if (Main.SHOW_LOG_MESSAGE) System.out.println("\nEXHAUSTIVE");
        if (this.dataset == null) {
            throw new DatasetNotFoundException();
        }

        /*if (this.latticeController == null) {
            throw new ControllerNotFoundException();
        }*/

        this.exhaustiveAlgorithm = new ExhaustiveAlgorithm(dataset, suppressionTreshold);

        long start = System.currentTimeMillis();

        ExecutionThread executionThread = new ExecutionThread(exhaustiveAlgorithm);
        executionThread.start();

        while (executionThread.isAlive() &&
                (System.currentTimeMillis() - start) < MAX_EVALUATION_TIME) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if ((System.currentTimeMillis() - start) < MAX_EVALUATION_TIME) {
            this.solutions = executionThread.getSolutions();
            this.executionTime = (double)(System.currentTimeMillis()-start)/1000;
        } else {
            if (Main.SHOW_LOG_MESSAGE) System.out.println("Expired time");
            executionThread.stop();
        }

        saveInfoExperimentation(this.exhaustiveAlgorithm.getName(),
                this.exhaustiveAlgorithm.getkAnonymity(), 1, null);
    }

}
