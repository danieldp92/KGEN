package approaches.metaheuristics.geneticalgorithm.thread.evaluation;

import approaches.metaheuristics.geneticalgorithm.AnonymizationProblem;
import approaches.metaheuristics.geneticalgorithm.thread.GAThread;
import approaches.metaheuristics.geneticalgorithm.thread.GAThreadPoolExecutor;
import jmetal.core.Solution;
import jmetal.util.JMException;


public class EvaluationThread extends GAThread {
    private AnonymizationProblem problem;
    private int index;

    public EvaluationThread(GAThreadPoolExecutor gaThreadPoolExecutor, int index, AnonymizationProblem problem) {
        super(index, gaThreadPoolExecutor);

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
        try {
            problem.evaluate((Solution) returnValue);
        } catch (JMException e) {
            e.printStackTrace();
        }

        gaThreadPoolExecutor.setSolution((Solution) returnValue);
    }
}
