package approaches.metaheuristics.geneticalgorithm;

import approaches.metaheuristics.geneticalgorithm.encoding.GeneralizationSolution;
import approaches.metaheuristics.geneticalgorithm.thread.evaluation.EvaluationThread;
import approaches.metaheuristics.geneticalgorithm.thread.evaluation.MultiThreadEvaluation;
import approaches.metaheuristics.geneticalgorithm.thread.ga_cycle.MultiThreadCycle;
import approaches.metaheuristics.utils.SolutionUtils;
import jmetal.core.*;
import jmetal.util.JMException;
import utils.ArrayUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AnonymizationAlgorithm extends Algorithm {
    private Operator selection;
    private Operator crossover;
    private Operator mutation;
    private Operator horizontalMutation;

    private int populationSize;
    private int maxEvaluations;
    private int maxNumberOfThreads;

    private KGENAlgorithm kgenAlgorithm;

    private List<EvaluationThread> evaluationThreads;

    /**
     * Constructor
     *
     * @param problem The problem to be solved
     */
    public AnonymizationAlgorithm(Problem problem) {
        super(problem);
    }


    // INIT ####################################################################################

    private void init () {
        selection = operators_.get("selection");
        crossover = operators_.get("crossover");
        mutation = operators_.get("mutation");
        horizontalMutation = operators_.get("horizontalMutation");

        populationSize = ((Integer)getInputParameter("populationSize")).intValue();
        maxEvaluations = ((Integer)getInputParameter("maxEvaluations")).intValue();
        maxNumberOfThreads = ((Integer)getInputParameter("maxNumberOfThreads")).intValue();

        ((AnonymizationProblem)problem_).getkAnonymity().cleanHistoryMap();

        // Init threads
        this.evaluationThreads = new ArrayList<>();
    }


    // GET & SET ###############################################################################

    public void setKgenAlgorithm(KGENAlgorithm kgenAlgorithm) {
        this.kgenAlgorithm = kgenAlgorithm;
    }


    // EXEC ####################################################################################

    public SolutionSet sexecute() throws JMException, ClassNotFoundException {
        init();

        List<List<Integer>> results = new ArrayList<>();
        SolutionSet population = new SolutionSet(populationSize);

        //Starting population
        for (int i = 0; i < populationSize; i++) {
            GeneralizationSolution newSolution = new GeneralizationSolution(problem_);

            EvaluationThread evaluationThread = new EvaluationThread((AnonymizationProblem) problem_);
            evaluationThread.configure(i, newSolution);
            evaluationThread.start();
            this.evaluationThreads.add(evaluationThread);

            //problem_.evaluate(newSolution);
            //population.add(newSolution);
        }



        int evaluation = 0;

        while (evaluation < maxEvaluations) {
            //SolutionUtils.printPopulation(population);
            ArrayList<GeneralizationSolution> union = new ArrayList<GeneralizationSolution>();
            for (int j = 0; j < populationSize; j++) {
                union.add((GeneralizationSolution) population.get(j));
            }

            ArrayList<GeneralizationSolution> offspringPopulation = new ArrayList<GeneralizationSolution>();

            for (int j = 0; j < populationSize/2; j++) {
                if (evaluation < maxEvaluations) {
                    GeneralizationSolution [] parents = new GeneralizationSolution[2];

                    //Selection
                    parents[0] = (GeneralizationSolution) selection.execute(population);
                    parents[1] = (GeneralizationSolution) selection.execute(population);

                    //Crossover
                    //ArrayList<GeneralizationSolution> offsprings = new ArrayList<GeneralizationSolution>();
                    GeneralizationSolution [] offsprings = (GeneralizationSolution[]) crossover.execute(parents);

                    //Mutation
                    for (int k = 0; k < offsprings.length; k++) {
                        offsprings[k] = (GeneralizationSolution) mutation.execute(offsprings[k]);
                        offsprings[k] = (GeneralizationSolution) horizontalMutation.execute(offsprings[k]);
                    }

                    //Evaluation
                    for (Solution offspring : offsprings) {
                        problem_.evaluate(offspring);

                        offspringPopulation.add((GeneralizationSolution) offspring);
                    }

                    evaluation += 2;

                    kgenAlgorithm.setChanged();
                    kgenAlgorithm.notifyObservers(evaluation);
                }
            }

            union.addAll(offspringPopulation);
            SolutionSet tmpUnion = new SolutionSet(union.size());
            for (int j = 0; j < union.size(); j++) {
                tmpUnion.add(union.get(j));
            }

            //Survival selection
            population.clear();
            for (int j = 0; j < populationSize; j++) {
                Solution survivalSolution = (Solution) selection.execute(tmpUnion);
                population.add(survivalSolution);
            }

            //Increase penalties
            for (int j = 0; j < population.size(); j++) {
                ((GeneralizationSolution)population.get(j)).increasePenalty();
            }

            //Insert best solutions in results
            saveBestSolutions(results, population);
        }

        for (int i = 0; i < population.size(); i++) {
            if (population.get(i).getObjective(AnonymizationProblem.ffKLV_OBJECTIVE) == 1) {
                population.remove(i--);
            }
        }

        Set<List<Integer>> tmpResults = new LinkedHashSet<>();
        for (List<Integer> result : results) {
            tmpResults.add(result);
        }

        population.clear();

        for (List<Integer> result : tmpResults) {
            GeneralizationSolution newSolution = new GeneralizationSolution(problem_);
            for (int i = 0; i < result.size(); i++) {
                newSolution.getDecisionVariables()[i].setValue(result.get(i));
            }

            problem_.evaluate(newSolution);

            population.add(newSolution);
        }

        return population;
    }


    public SolutionSet execute() throws JMException, ClassNotFoundException {
        init();

        // Threads
        MultiThreadEvaluation multiThreadEvaluation = new MultiThreadEvaluation(maxNumberOfThreads,
                (AnonymizationProblem) problem_);
        MultiThreadCycle multiThreadCycle = new MultiThreadCycle(maxNumberOfThreads, populationSize/2,
                selection, crossover, horizontalMutation, mutation);


        List<List<Integer>> results = new ArrayList<>();
        SolutionSet population = new SolutionSet(populationSize);

        //Starting population
        for (int i = 0; i < populationSize; i++) {
            GeneralizationSolution newSolution = new GeneralizationSolution(problem_);
            population.add(newSolution);
        }

        population = multiThreadEvaluation.parallelExecution(population);

        int evaluation = 0;
        while (evaluation < maxEvaluations) {
            // Generation cycle (selection - crossover - mutation are executed by the multithread)
            SolutionSet offsprings = multiThreadCycle.parallelExecution(population);
            offsprings = multiThreadEvaluation.parallelExecution(offsprings);

            offsprings.setCapacity(offsprings.size()+population.size());
            offsprings.union(population);

            evaluation += populationSize/2;

            kgenAlgorithm.setChanged();
            kgenAlgorithm.notifyObservers(evaluation);

            // Survival
            population.clear();
            for (int i = 0; i < populationSize; i++) {
                Solution survivalSolution = (Solution) selection.execute(offsprings);
                population.add(survivalSolution);
            }


            //Increase penalties
            for (int j = 0; j < population.size(); j++) {
                ((GeneralizationSolution)population.get(j)).increasePenalty();
            }

            //Insert best solutions in results
            saveBestSolutions(results, population);
        }

        for (int i = 0; i < population.size(); i++) {
            if (population.get(i).getObjective(AnonymizationProblem.ffKLV_OBJECTIVE) == 1) {
                population.remove(i--);
            }
        }

        Set<List<Integer>> tmpResults = new LinkedHashSet<>();
        for (List<Integer> result : results) {
            tmpResults.add(result);
        }

        population.clear();

        for (List<Integer> result : tmpResults) {
            GeneralizationSolution newSolution = new GeneralizationSolution(problem_);
            for (int i = 0; i < result.size(); i++) {
                newSolution.getDecisionVariables()[i].setValue(result.get(i));
            }

            population.add(newSolution);
        }

        population = multiThreadEvaluation.parallelExecution(population);

        return population;
    }



    private ArrayList<Integer> getSolutionValues (Solution solution) throws JMException {
        ArrayList<Integer> values = new ArrayList<Integer>();

        for (Variable var : solution.getDecisionVariables()) {
            values.add((int) var.getValue());
        }

        return values;
    }

    private void saveBestSolutions (List<List<Integer>> results, SolutionSet population) throws JMException {
        for (int i = 0; i < population.size(); i++) {
            if (population.get(i).getObjective(AnonymizationProblem.ffKLV_OBJECTIVE) > 1) {
                List<Integer> min = getSolutionValues(population.get(i));
                results.add(min);

                //Find the minimum node between actual solution and all nodes in results
                for (List<Integer> n : results) {
                    if (ArrayUtils.leq(n, min)) {
                        min = n;
                    }
                }

                for (int j = 0; j < results.size(); j++) {
                    if (!results.get(j).equals(min) &&
                            ArrayUtils.geq(results.get(j), min)) {
                        results.remove(j--);
                    }
                }
            }
        }
    }
}
