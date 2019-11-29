package approaches.metaheuristics.geneticalgorithm.thread.evaluation;

import approaches.metaheuristics.geneticalgorithm.AnonymizationProblem;
import approaches.metaheuristics.geneticalgorithm.thread.GAThreadPoolExecutor;
import approaches.metaheuristics.utils.SolutionUtils;
import jmetal.core.SolutionSet;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MultiThreadEvaluation extends GAThreadPoolExecutor {
    private AnonymizationProblem problem;

    public MultiThreadEvaluation(int maxNumberOfThreads, AnonymizationProblem problem) {
        super(maxNumberOfThreads);

        this.problem = problem;
    }

    @Override
    public SolutionSet execution(SolutionSet population) {
        this.solutions.clear();
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        for (int indexSolution = 0; indexSolution < population.size(); indexSolution++) {
            EvaluationThread evaluationThread = new EvaluationThread(this, indexSolution, problem);
            evaluationThread.configure(indexSolution, population.get(indexSolution));

            this.threadPoolExecutor.execute(evaluationThread);
        }

        awaitTerminationAfterShutdown(this.threadPoolExecutor);

        population.clear();
        for (int i = 0; i < solutions.size(); i++) {
            population.add(solutions.get(i));
        }

        return population;
    }
}
