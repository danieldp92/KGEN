package approaches.metaheuristics.geneticalgorithm.thread.ga_cycle;

import approaches.metaheuristics.geneticalgorithm.encoding.GeneralizationSolution;
import approaches.metaheuristics.geneticalgorithm.thread.GAThread;
import jmetal.core.Operator;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class GenerationCycleThread extends GAThread {
    // Operators
    private Operator selection;
    private Operator crossover;
    private Operator horizontalMutation;
    private Operator mutation;

    private SolutionSet population;

    public GenerationCycleThread(Operator selection, Operator crossover, Operator horizontalMutation, Operator mutation) {
        super();

        this.selection = selection;
        this.crossover = crossover;
        this.horizontalMutation = horizontalMutation;
        this.mutation = mutation;
    }

    public void configure(SolutionSet population) {
        this.population = population;
    }

    @Override
    public void run() {
        setFinish(false);
        returnValue = null;

        try {
            GeneralizationSolution[] parents = new GeneralizationSolution[2];

            //Selection
            parents[0] = (GeneralizationSolution) selection.execute(population);
            parents[1] = (GeneralizationSolution) selection.execute(population);

            //Crossover
            //ArrayList<GeneralizationSolution> offsprings = new ArrayList<GeneralizationSolution>();
            GeneralizationSolution[] offsprings = (GeneralizationSolution[]) crossover.execute(parents);

            //Mutation
            for (int k = 0; k < offsprings.length; k++) {
                offsprings[k] = (GeneralizationSolution) mutation.execute(offsprings[k]);
                offsprings[k] = (GeneralizationSolution) horizontalMutation.execute(offsprings[k]);
            }

            returnValue = new ArrayList<>();
            for (GeneralizationSolution solution : offsprings) {
                ((List<Solution>)returnValue).add(solution);
            }
        } catch (JMException e) {}

        setFinish(true);
    }
}
