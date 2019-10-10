package approaches.metaheuristics.geneticalgorithm.thread.evaluation;

import approaches.metaheuristics.geneticalgorithm.AnonymizationProblem;
import approaches.metaheuristics.geneticalgorithm.thread.GAMultiThread;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadEvaluation extends GAMultiThread {
    private AnonymizationProblem problem;

    public MultiThreadEvaluation(int maxNumberOfThreads, AnonymizationProblem problem) {
        super(maxNumberOfThreads);

        this.problem = problem;
    }

    @Override
    public SolutionSet parallelExecution(SolutionSet population) {
        threads = new ArrayList<>();

        // Set the size of the pool
        int numberOfThreads = 0;
        if (maxNumberOfThreads > population.size()) {
            numberOfThreads = population.size();
        } else {
            numberOfThreads = maxNumberOfThreads;
        }

        int indexSolution = 0;

        while (indexSolution < population.size() || !threads.isEmpty()) {
            // Add thread to the pool, if there are solutions to process
            if (threads.size() < numberOfThreads && indexSolution < population.size()) {
                // Create new thread
                EvaluationThread evaluationThread = new EvaluationThread(problem);
                evaluationThread.configure(indexSolution, population.get(indexSolution));
                evaluationThread.start();

                indexSolution++;

                threads.add(evaluationThread);
            } else {
                // Take the first thread that has stopped its execution
                if (!threads.isEmpty()) {
                    waitFreeThread();

                    Integer indexThread = getFreeIndexThread();
                    int index = ((EvaluationThread)threads.get(indexThread)).getIndex();
                    population.replace(index, (Solution) threads.get(indexThread).getReturnValue());

                    // Remove the thread
                    threads.remove(indexThread.intValue());
                }
            }
        }

        return population;
    }
}
