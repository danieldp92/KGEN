package approaches.metaheuristics.geneticalgorithm.thread.ga_cycle;

import approaches.metaheuristics.geneticalgorithm.thread.GAThreadPoolExecutor;
import approaches.metaheuristics.utils.SolutionUtils;
import jmetal.core.Operator;
import jmetal.core.SolutionSet;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MultiThreadGenerationCycle extends GAThreadPoolExecutor {
    // Operators
    private Operator selection;
    private Operator crossover;
    private Operator horizontalMutation;
    private Operator mutation;

    private int numberOfEvaluations;

    public MultiThreadGenerationCycle(int maxNumberOfThreads, int numberOfEvaluations, Operator selection, Operator crossover,
                                      Operator horizontalMutation, Operator mutation) {
        super(maxNumberOfThreads);

        this.selection = selection;
        this.crossover = crossover;
        this.horizontalMutation = horizontalMutation;
        this.mutation = mutation;

        this.numberOfEvaluations = numberOfEvaluations;
    }

    @Override
    public SolutionSet execution(SolutionSet population) {
        this.solutions.clear();
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        for (int actualEvaluation = 0; actualEvaluation < population.size(); actualEvaluation++) {
            GenerationCycleThread generationCycleThread = new GenerationCycleThread(this, actualEvaluation, selection, crossover,
                    horizontalMutation, mutation);
            generationCycleThread.configure(clonePopulation(population));

            this.threadPoolExecutor.execute(generationCycleThread);
        }

        awaitTerminationAfterShutdown(this.threadPoolExecutor);

        return SolutionUtils.fromListToSolutionSet(solutions);
    }

    private SolutionSet clonePopulation (SolutionSet population) {
        SolutionSet clone = new SolutionSet(population.size());
        for (int i = 0; i < population.size(); i++) {
            clone.add(population.get(i));
        }

        return clone;
    }
}
