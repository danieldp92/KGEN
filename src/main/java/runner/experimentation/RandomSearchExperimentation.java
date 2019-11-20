package runner.experimentation;

import approaches.metaheuristics.geneticalgorithm.AnonymizationProblem;
import approaches.metaheuristics.randomsearch.RandomAlgorithm;
import approaches.metaheuristics.randomsearch.RandomSearchSetting;
import approaches.metaheuristics.utils.SolutionUtils;
import approaches.ola.OLAAlgorithm;
import exception.DatasetNotFoundException;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.metaheuristics.randomSearch.RandomSearch;
import jmetal.util.JMException;
import runner.Main;
import runner.experimentation.thread.ExecutionThread;

import java.util.ArrayList;
import java.util.List;

public class RandomSearchExperimentation extends Experimentation {
    private RandomAlgorithm randomAlgorithm;

    public RandomSearchExperimentation(String resultPath) {
        super(resultPath);
    }

    @Override
    public void execute(int numberOfRun, double suppressionTreshold) throws DatasetNotFoundException {
        if (this.dataset == null) {
            throw new DatasetNotFoundException();
        }

        this.randomAlgorithm = new RandomAlgorithm(this.dataset, suppressionTreshold);
        this.kAnonymity = this.randomAlgorithm.getkAnonymity();

        for (int i = 1; i <= numberOfRun; i++) {
            ExecutionThread executionThread = new ExecutionThread(randomAlgorithm, i);
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

            saveInfoExperimentation(this.randomAlgorithm.getName(), this.randomAlgorithm.getkAnonymity(), i);
        }

        /*this.suppressionThreshold = suppressionThreshold;

        if (Main.SHOW_LOG_MESSAGE) System.out.println("\nRandom Search");

        for (int run = 1; run <= numberOfRun; run++) {
            this.anonymizationProblem = new AnonymizationProblem(dataset, this.suppressionThreshold);
            this.randomSearchSetting = new RandomSearchSetting(anonymizationProblem);
            RandomSearch randomSearch = null;
            try {
                randomSearch = (RandomSearch) randomSearchSetting.configure();
            } catch (JMException e) {
                e.printStackTrace();
            }

            if (Main.SHOW_LOG_MESSAGE) System.out.println("Random Search " + run);

            long start = System.currentTimeMillis();

            SolutionSet randomSearchSolutions = null;

            try {
                randomSearchSolutions = randomSearch.execute();
                SolutionUtils.removeAllInvalidSolutions(randomSearchSolutions);
            } catch (JMException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (randomSearchSolutions.size() > 0) {
                this.solutions = new ArrayList<>();
                for (int i = 0; i < randomSearchSolutions.size(); i++) {
                    Solution solution = randomSearchSolutions.get(i);
                    List<Integer> arraySolution = getSolutionValues(solution);
                    this.solutions.add(arraySolution);
                }

                //Remove all k-anonymous solutions that are of the same strategy path (except for the minimal k-anonymous node)
                SolutionUtils.removeGreaterElements(this.solutions);

                this.executionTime = (double)(System.currentTimeMillis()-start)/1000;
            }

            saveInfoExperimentation("RANDOM", anonymizationProblem.getkAnonymity(), run);
        }*/
    }
}
