package approaches.geneticalgorithm;

import anonymization.KAnonymity;
import approaches.geneticalgorithm.encoding.GeneralizationSolution;
import approaches.geneticalgorithm.utils.SolutionUtils;
import jmetal.core.*;
import jmetal.util.JMException;

import java.util.ArrayList;

public class AnonymizationAlgorithm extends Algorithm {
    private static final int MIN_K_LEVEL = 2;
    private static final int ffLOG_OBJECTIVE = 0;
    private static final int ffKLV_OBJECTIVE = 1;

    private Operator selection;
    private Operator crossover;
    private Operator mutation;
    private Operator horizontalMutation;

    private SolutionSet population;

    private KAnonymity kAnonymity;
    private int populationSize;
    private int maxEvaluations;
    private int evaluation;


    /**
     * Constructor
     *
     * @param problem The problem to be solved
     */
    public AnonymizationAlgorithm(Problem problem) throws JMException {
        super(problem);
    }

    public SolutionSet execute() throws JMException, ClassNotFoundException {
        init();

        long startTime = 0;


        this.population = new SolutionSet(populationSize);

        //Starting population
        startTime = System.currentTimeMillis();
        System.out.print("Population: " + evaluation);
        for (int i = 0; i < populationSize; i++) {
            GeneralizationSolution newSolution = new GeneralizationSolution(problem_);
            problem_.evaluate(newSolution);

            population.add(newSolution);
            System.out.print("\rPopulation: " + ++evaluation);
        }

        System.out.println("\nGeneration time: " + (double)(System.currentTimeMillis() - startTime)/1000 + "s\n");


        startTime = System.currentTimeMillis();
        evaluation = 0;
        System.out.print("Evaluation: " + evaluation);

        for (int i = 0; i < maxEvaluations; i++) {
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
                        //offsprings[k] = (GeneralizationSolution) mutation.execute(offsprings[k]);
                        offsprings[k] = (GeneralizationSolution) horizontalMutation.execute(offsprings[k]);
                    }





                    /*GeneralizationSolution minLatticeNode = tmpOffsprings[0];
                    GeneralizationSolution maxLatticeNode = tmpOffsprings[1];


                    //Mutation
                    boolean kAnonParent0 = false;
                    boolean kAnonParent1 = false;

                    if (parents[0].getObjective(ffKLV_OBJECTIVE) > 1) {
                        kAnonParent0 = true;
                    }

                    if (parents[1].getObjective(ffKLV_OBJECTIVE) > 1) {
                        kAnonParent1 = true;
                    }

                    if (kAnonParent0 && kAnonParent1) {
                        boolean kAnonMinLatticeNode = this.kAnonymity.kAnonymityTest(getSolutionValues(minLatticeNode), MIN_K_LEVEL);
                        if (kAnonMinLatticeNode) {
                            //MIN will be an offspring solution
                            offsprings.add(minLatticeNode);
                        } else {
                            //Generate a random value between min (not anonymized) and parents (anonymized).
                            //This nodes could be anonymized
                            offsprings.add((GeneralizationSolution) randomBetweenSolutions(minLatticeNode, parents[0]));
                            offsprings.add((GeneralizationSolution) randomBetweenSolutions(minLatticeNode, parents[1]));
                        }

                        //MUTATION: MAX
                        mutation.execute(maxLatticeNode);
                        //kAnonymizationMutation.execute(maxLatticeNode);
                        offsprings.add(maxLatticeNode);
                    } else if (!kAnonParent0 && !kAnonParent1) {
                        offsprings.add(maxLatticeNode);

                        boolean kAnonMaxLatticeNode = this.kAnonymity.kAnonymityTest(getSolutionValues(maxLatticeNode), MIN_K_LEVEL);
                        if (!kAnonMaxLatticeNode) {
                            offsprings.add((GeneralizationSolution) randomBetweenSolutions(parents[0], maxLatticeNode));
                            offsprings.add((GeneralizationSolution) randomBetweenSolutions(parents[1], maxLatticeNode));
                        }

                        //MUTATION: MIN
                        mutation.execute(minLatticeNode);
                        //kAnonymizationMutation.execute(minLatticeNode);
                        offsprings.add(minLatticeNode);
                    } else {
                        if (kAnonParent0) {
                            offsprings.add((GeneralizationSolution) randomBetweenSolutions(minLatticeNode, parents[0]));
                        } else {
                            offsprings.add((GeneralizationSolution) randomBetweenSolutions(minLatticeNode, parents[1]));
                        }

                        //MUTATION: MAX
                        mutation.execute(maxLatticeNode);
                        //kAnonymizationMutation.execute(maxLatticeNode);
                        offsprings.add(maxLatticeNode);
                    }*/


                    //Evaluation
                    for (Solution offspring : offsprings) {
                        problem_.evaluate(offspring);

                        offspringPopulation.add((GeneralizationSolution) offspring);
                    }

                    evaluation += 2;
                    System.out.print("\rEvaluation: " + evaluation);
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
        }

        for (int i = 0; i < population.size(); i++) {
            //population.get(i).setObjective(ffLOG_OBJECTIVE, 1 - population.get(i).getObjective(ffLOG_OBJECTIVE));
            if (population.get(i).getObjective(ffKLV_OBJECTIVE) == 1) {
                population.remove(i--);
            }
        }

        //SolutionUtils.printPopulation(population);

        System.out.println("\nExecution time: " + (double)(System.currentTimeMillis() - startTime)/1000 + "s");
        System.out.println("History size: " + kAnonymity.getkAnonymizedHistoryMap().size() + "\n");

        return population;
    }

    private void init () {
        //((AnonymizationProblem)problem_).initKAnonymity();
        this.kAnonymity = ((AnonymizationProblem) problem_).getkAnonymity();

        selection = operators_.get("selection");
        crossover = operators_.get("crossover");
        mutation = operators_.get("mutation");
        horizontalMutation = operators_.get("horizontalMutation");

        populationSize = ((Integer)getInputParameter("populationSize")).intValue();
        maxEvaluations = ((Integer)getInputParameter("maxEvaluations")).intValue();
        evaluation = 0;
    }





    private ArrayList<Integer> getSolutionValues (Solution solution) throws JMException {
        ArrayList<Integer> values = new ArrayList<Integer>();

        for (Variable var : solution.getDecisionVariables()) {
            values.add((int) var.getValue());
        }

        return values;
    }

    private Solution randomBetweenSolutions (Solution solution1, Solution solution2) throws JMException {
        GeneralizationSolution randomSolution = new GeneralizationSolution(solution1);

        for (int i = 0; i < solution1.getDecisionVariables().length; i++) {
            int val1 = (int) solution1.getDecisionVariables()[i].getValue();
            int val2 = (int) solution2.getDecisionVariables()[i].getValue();

            int random = (int) ((Math.random() * (val2 - val1 + 1)) + val1);
            randomSolution.getDecisionVariables()[i].setValue(random);
        }

        return randomSolution;
    }

    /**
     * This method deletes all equals solutions
     * @param solutions
     */
    private void reduction (ArrayList<GeneralizationSolution> solutions) {
        for (int i = 0; i < solutions.size()-1; i++) {
            for (int j = 0; j < solutions.size(); j++) {
                int k = 0;
                while (k < solutions.get(i).getDecisionVariables().length &&
                    solutions.get(i).getDecisionVariables()[k] == solutions.get(j).getDecisionVariables()[k]) {
                    k++;
                }

                if (k >= solutions.get(i).getDecisionVariables().length) {
                    solutions.remove(j--);
                }
            }
        }
    }
}
