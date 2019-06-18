package approaches.geneticalgorithm.utils;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;

import java.util.ArrayList;

public class SolutionUtils {

    public static void printPopulation (SolutionSet population) {
        if (population.size() > 0) {
            System.out.println("Print population\n");

            try {
                for (int i = 0; i < population.size(); i++) {
                    System.out.println("Solution " + (i+1));
                    printLL(population.get(i));
                    printSolution(population.get(i));
                    printUL(population.get(i));

                    for (int j = 0; j < population.get(i).getNumberOfObjectives(); j++) {
                        if (j == 0) {
                            System.out.println("Objective " + (j+1) + ": " + (1 - population.get(i).getObjective(j)));
                        } else {
                            System.out.println("Objective " + (j+1) + ": " + population.get(i).getObjective(j));
                        }

                    }

                    System.out.println();
                }
            } catch (JMException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void printSolutions (ArrayList<Solution> solutions) {
        if (!solutions.isEmpty()) {
            System.out.println("Print solutions\n");

            try {
                int i = 0;
                for (Solution solution : solutions) {
                    System.out.println("Solution " + ++i);
                    printLL(solution);
                    printSolution(solution);
                    printUL(solution);

                    for (int j = 0; j < solution.getNumberOfObjectives(); j++) {
                        System.out.println("Objective " + (j+1) + ": " + solution.getObjective(j));
                    }

                    System.out.println();
                }
            } catch (JMException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void printLL (Solution solution) throws JMException {
        System.out.print("[");
        for (int i = 0; i < solution.getDecisionVariables().length; i++) {
            System.out.print((int)solution.getDecisionVariables()[i].getLowerBound());
            if (i < solution.getDecisionVariables().length-1)
                System.out.print(", ");
            else {
                System.out.print("]");
            }
        }
        System.out.println();
    }

    private static void printUL (Solution solution) throws JMException {
        System.out.print("[");
        for (int i = 0; i < solution.getDecisionVariables().length; i++) {
            System.out.print((int)solution.getDecisionVariables()[i].getUpperBound());
            if (i < solution.getDecisionVariables().length-1)
                System.out.print(", ");
            else {
                System.out.print("]");
            }
        }
        System.out.println();
    }

    private static void printSolution (Solution solution) {
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
