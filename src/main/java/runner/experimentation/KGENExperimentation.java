package runner.experimentation;

import approaches.metaheuristics.geneticalgorithm.AnonymizationAlgorithm;
import approaches.metaheuristics.geneticalgorithm.AnonymizationProblem;
import approaches.metaheuristics.geneticalgorithm.AnonymizationSetting;
import approaches.metaheuristics.geneticalgorithm.KGENAlgorithm;
import approaches.metaheuristics.utils.SolutionUtils;
import exception.DatasetNotFoundException;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.util.JMException;
import runner.Main;
import runner.experimentation.thread.ExecutionThread;

import java.util.ArrayList;

public class KGENExperimentation extends Experimentation{
    private KGENAlgorithm kgenAlgorithm;

    public KGENExperimentation(String resultPath) {
        super(resultPath);
    }

    @Override
    public void execute(int numberOfRun, double suppressionTreshold) throws DatasetNotFoundException {
        if (this.dataset == null) {
            throw new DatasetNotFoundException();
        }

        this.kgenAlgorithm = new KGENAlgorithm(this.dataset, suppressionTreshold);

        for (int i = 1; i <= numberOfRun; i++) {
            ExecutionThread executionThread = new ExecutionThread(kgenAlgorithm, i);
            executionThread.start();

            long start = System.currentTimeMillis();
            while (executionThread.isAlive()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                    System.exit(0);
                }
            }

            this.solutions = executionThread.getSolutions();
            if (this.solutions != null) {
                this.executionTime = (double)(System.currentTimeMillis() - start) / 1000;
            }

            saveInfoExperimentation(this.kgenAlgorithm.getName(), this.kgenAlgorithm.getkAnonymity(), i);
        }
    }
}
