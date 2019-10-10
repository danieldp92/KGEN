package approaches.metaheuristics.geneticalgorithm.thread.ga_cycle;

import approaches.metaheuristics.geneticalgorithm.encoding.GeneralizationSolution;
import approaches.metaheuristics.geneticalgorithm.thread.GAMultiThread;
import approaches.metaheuristics.utils.SolutionUtils;
import jmetal.core.Operator;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class MultiThreadCycle extends GAMultiThread {
    // Operators
    private Operator selection;
    private Operator crossover;
    private Operator horizontalMutation;
    private Operator mutation;

    private int numberOfEvaluations;

    public MultiThreadCycle(int maxNumberOfThreads, int numberOfEvaluations, Operator selection, Operator crossover,
                            Operator horizontalMutation, Operator mutation) {
        super(maxNumberOfThreads);

        this.selection = selection;
        this.crossover = crossover;
        this.horizontalMutation = horizontalMutation;
        this.mutation = mutation;

        this.numberOfEvaluations = numberOfEvaluations;
    }

    @Override
    public SolutionSet parallelExecution(SolutionSet population) {
        List<Solution> offsprings = new ArrayList<>();
        threads = new ArrayList<>();

        // Set the size of the pool
        int numberOfThreads = 0;
        if (maxNumberOfThreads > numberOfEvaluations) {
            numberOfThreads = numberOfEvaluations;
        } else {
            numberOfThreads = maxNumberOfThreads;
        }

        int actualEvaluation = 0;

        while (actualEvaluation < numberOfEvaluations || !threads.isEmpty()) {
            // Add thread to the pool, if there are solutions to process
            if (threads.size() < numberOfThreads && actualEvaluation < numberOfEvaluations) {
                GenerationCycleThread generationCycleThread = new GenerationCycleThread(selection, crossover,
                        horizontalMutation, mutation);
                generationCycleThread.configure(clonePopulation(population));
                generationCycleThread.start();

                actualEvaluation++;

                threads.add(generationCycleThread);
            } else {
                // Take the first thread that has stopped its execution
                if (!threads.isEmpty()) {
                    waitFreeThread();

                    Integer indexThread = getFreeIndexThread();
                    offsprings.addAll((List<Solution>)threads.get(indexThread).getReturnValue());

                    // Remove the thread
                    threads.remove(indexThread.intValue());
                }
            }
        }

        return SolutionUtils.fromListToSolutionSet(offsprings);
    }

    private SolutionSet clonePopulation (SolutionSet population) {
        SolutionSet clone = new SolutionSet(population.size());
        for (int i = 0; i < population.size(); i++) {
            clone.add(population.get(i));
        }

        return clone;
    }
}
