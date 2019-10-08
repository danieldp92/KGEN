package approaches.metaheuristics.geneticalgorithm;

import anonymization.KAnonymity;
import approaches.metaheuristics.geneticalgorithm.encoding.GeneralizationSolution;
import exception.TooNodeException;
import jmetal.core.*;
import jmetal.util.JMException;
import runner.experimentation.exceptions.LimitExceedException;
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

    private KGENAlgorithm kgenAlgorithm;

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

        ((AnonymizationProblem)problem_).getkAnonymity().cleanHistoryMap();
    }


    // GET & SET ###############################################################################

    public void setKgenAlgorithm(KGENAlgorithm kgenAlgorithm) {
        this.kgenAlgorithm = kgenAlgorithm;
    }


    // EXEC ####################################################################################

    public SolutionSet execute() throws JMException, ClassNotFoundException {
        init();

        List<List<Integer>> results = new ArrayList<>();
        SolutionSet population = new SolutionSet(populationSize);

        //Starting population
        for (int i = 0; i < populationSize; i++) {
            GeneralizationSolution newSolution = new GeneralizationSolution(problem_);
            problem_.evaluate(newSolution);

            population.add(newSolution);
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
