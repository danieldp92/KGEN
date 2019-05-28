package approaches.geneticalgorithm;

import jmetal.core.*;
import jmetal.util.JMException;


public class AnonymizationAlgorithm extends Algorithm {
    private Operator selection;
    private Operator crossover;
    private Operator mutation;

    /**
     * Constructor
     *
     * @param problem The problem to be solved
     */
    public AnonymizationAlgorithm(Problem problem) throws JMException {
        super(problem);
    }

    public SolutionSet execute() throws JMException, ClassNotFoundException {
        selection = operators_.get("selection");
        crossover = operators_.get("crossover");
        mutation = operators_.get("mutation");

        int populationSize = ((Integer)getInputParameter("populationSize")).intValue();
        int maxEvaluations = ((Integer)getInputParameter("maxEvaluations")).intValue();

        SolutionSet population = new SolutionSet(populationSize);
        SolutionSet offspringPopulation;
        SolutionSet union = new SolutionSet(populationSize*2);
        Solution newSolution;


        //Starting population
        int evaluation = 0;
        System.out.print("Population: " + evaluation);
        for (int i = 0; i < populationSize; i++) {
            newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            population.add(newSolution);
            System.out.print("\rPopulation: " + ++evaluation);
        }

        System.out.println("Starting population\n");
        printPopulation(population);


        evaluation = 0;
        System.out.print("Evaluation: " + evaluation);
        for (int i = 0; i < maxEvaluations; i++) {
            for (int j = 0; j < populationSize; j++) {
                union.add(population.get(j));
            }

            offspringPopulation = new SolutionSet(populationSize);
            Solution [] parents = new Solution[2];

            for (int j = 0; j < populationSize/2; j++) {
                if (evaluation < maxEvaluations) {
                    //Get parents
                    parents[0] = (Solution) selection.execute(population);
                    parents[1] = (Solution) selection.execute(population);

                    //Crossover
                    Solution [] offspring = (Solution[]) crossover.execute(parents);

                    //Mutation
                    mutation.execute(offspring[0]);
                    mutation.execute(offspring[1]);

                    //Validate solution
                    ((AnonymizationProblem)problem_).validateSolution(offspring[0]);
                    ((AnonymizationProblem)problem_).validateSolution(offspring[1]);

                    problem_.evaluate(offspring[0]);
                    problem_.evaluate(offspring[1]);

                    offspringPopulation.add(offspring[0]);
                    offspringPopulation.add(offspring[1]);

                    evaluation += 2;
                    System.out.print("\rEvaluation: " + evaluation);
                }
            }

            for (int j = 0; j < offspringPopulation.size(); j++) {
                union.add(offspringPopulation.get(j));
            }

            orderByFitness(union);

            population = new SolutionSet(populationSize);
            for (int j = 0; j < populationSize; j++) {
                population.add(union.get(j));
            }

            union.clear();
        }


        System.out.println("\nFinal population\n");
        printPopulation(population);

        return population;
    }

    private void orderByFitness (SolutionSet solutionSet) {
        Solution tmp = null;

        for (int i = 0; i < solutionSet.size()-1; i++) {
            for (int j = i + 1; j < solutionSet.size(); j++) {
                if (solutionSet.get(i).getFitness() > solutionSet.get(j).getFitness()) {
                    tmp = solutionSet.get(i);
                    solutionSet.replace(i, solutionSet.get(j));
                    solutionSet.replace(j, tmp);
                }
            }
        }
    }

    private void printPopulation (SolutionSet population) {
        System.out.println("Print population\n");

        for (int i = 0; i < population.size(); i++) {
            System.out.println("Solution " + (i+1));
            printLL(population);
            printSolution(population.get(i));
            printUL(population);
            System.out.println("Fitness function LOG: " + population.get(i).getObjective(0));
            //System.out.println("Fitness function ND: " + population.get(i).getObjective(1));
            //System.out.println("Fitness function KLEV: " + population.get(i).getObjective(2));
            System.out.println();
        }
    }

    private void printLL (SolutionSet population) {
        System.out.print("[");
        for (int i = 0; i < population.get(i).getDecisionVariables().length; i++) {
            System.out.print(problem_.getLowerLimit(i));
            if (i < population.get(i).getDecisionVariables().length-1)
                System.out.print(", ");
            else {
                System.out.print("]");
            }
        }
        System.out.println();
    }

    private void printUL (SolutionSet population) {
        System.out.print("[");
        for (int i = 0; i < population.get(i).getDecisionVariables().length; i++) {
            System.out.print(problem_.getUpperLimit(i));
            if (i < population.get(i).getDecisionVariables().length-1)
                System.out.print(", ");
            else {
                System.out.print("]");
            }
        }
        System.out.println();
    }

    private void printSolution (Solution solution) {
        System.out.print("[");
        for (int i = 0; i < solution.getDecisionVariables().length; i++) {
            System.out.print(solution.getDecisionVariables()[i]);
            if (i < solution.getDecisionVariables().length-1)
                System.out.print(", ");
            else {
                System.out.print("]");
            }
        }
        System.out.println();
    }
}
