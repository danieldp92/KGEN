package approaches.geneticalgorithm.utils;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;

public class SolutionUtils {

    public static void printPopulation (SolutionSet population) {
        System.out.println("Print population\n");

        try {
            for (int i = 0; i < population.size(); i++) {
                System.out.println("Solution " + (i+1));
                printLL(population);
                printSolution(population.get(i));
                printUL(population);

                for (int j = 0; j < population.get(i).getNumberOfObjectives(); j++) {
                    System.out.println("Objective " + (j+1) + ": " + population.get(i).getObjective(j));
                }

                System.out.println();
            }
        } catch (JMException ex) {
            ex.printStackTrace();
        }
    }

    private static void printLL (SolutionSet population) throws JMException {
        System.out.print("[");
        for (int i = 0; i < population.get(0).getDecisionVariables().length; i++) {
            System.out.print(population.get(0).getDecisionVariables()[i].getLowerBound());
            if (i < population.get(0).getDecisionVariables().length-1)
                System.out.print(", ");
            else {
                System.out.print("]");
            }
        }
        System.out.println();
    }

    private static void printUL (SolutionSet population) throws JMException {
        System.out.print("[");
        for (int i = 0; i < population.get(0).getDecisionVariables().length; i++) {
            System.out.print(population.get(0).getDecisionVariables()[i].getUpperBound());
            if (i < population.get(0).getDecisionVariables().length-1)
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
