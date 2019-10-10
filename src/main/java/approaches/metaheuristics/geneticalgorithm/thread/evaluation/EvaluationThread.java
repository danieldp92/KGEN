package approaches.metaheuristics.geneticalgorithm.thread.evaluation;

import approaches.metaheuristics.geneticalgorithm.AnonymizationProblem;
import approaches.metaheuristics.geneticalgorithm.thread.GAThread;
import jmetal.core.Solution;
import jmetal.util.JMException;

import java.util.concurrent.locks.ReentrantLock;

public class EvaluationThread extends GAThread {
    private AnonymizationProblem problem;
    private int index;

    public EvaluationThread(AnonymizationProblem problem) {
        super();

        this.problem = problem;
    }

    // Get & Set
    public int getIndex() {
        return index;
    }

    // Configure
    public void configure(int index, Solution solution) {
        this.index = index;
        this.returnValue = solution;
    }

    // RUN
    @Override
    public void run() {
        setFinish(false);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            problem.evaluate((Solution) returnValue);
        } catch (JMException e) {
            e.printStackTrace();
        }

        setFinish(true);
    }
}
