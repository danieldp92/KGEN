package approaches.metaheuristics.utils;

import anonymization.KAnonymity;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.util.JMException;
import utils.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class SolutionUtils {

    public static List<Solution> fromSolutionSetToList(SolutionSet solutionSet) {
        List<Solution> solutions = new ArrayList<>();
        for (int i = 0; i < solutionSet.size(); i++) {
            solutions.add(solutionSet.get(i));
        }

        return solutions;
    }

    public static SolutionSet fromListToSolutionSet(List<Solution> solutions) {
        SolutionSet solutionSet = new SolutionSet(solutions.size());
        for (Solution solution : solutions) {
            solutionSet.add(solution);
        }

        return solutionSet;
    }

    public static List<Integer> getSolutionLowerBounds (Solution solution) {
        List<Integer> lowerBounds = new ArrayList<>();
        for (Variable variable : solution.getDecisionVariables()) {
            try {
                lowerBounds.add((int) variable.getLowerBound());
            } catch (JMException e) {}
        }

        return lowerBounds;
    }

    public static List<Integer> getSolutionUpperBounds (Solution solution) {
        List<Integer> upperBounds = new ArrayList<>();
        for (Variable variable : solution.getDecisionVariables()) {
            try {
                upperBounds.add((int) variable.getUpperBound());
            } catch (JMException e) {}
        }

        return upperBounds;
    }

    public static List<Integer> getSolutionValues (Solution solution) {
        List<Integer> values = new ArrayList<>();
        for (Variable variable : solution.getDecisionVariables()) {
            try {
                values.add((int) variable.getValue());
            } catch (JMException e) {}
        }

        return values;
    }

    public static void removeGreaterElements (List<List<Integer>> elements) {
        for (int i = 0; i < elements.size(); i++) {
            List<Integer> iResult = elements.get(i);
            for (int j = 0; j < elements.size(); j++) {
                if (i != j) {
                    List<Integer> jResult = elements.get(j);

                    if (ArrayUtils.geq(jResult, iResult)) {
                        if (j < i) {
                            i--;
                        }
                        elements.remove(j--);
                    }
                }
            }
        }
    }

    public static void removeInfeasibleSolutions (SolutionSet solutionSet, KAnonymity kAnonymity, int kMinLev, double suppressionThreshold) {
        for (int i = 0; i < solutionSet.size(); i++) {
            ArrayList<Integer> solution = new ArrayList<>(getSolutionValues(solutionSet.get(i)));
            boolean isKAnon = kAnonymity.isKAnonymous(solution, kMinLev, suppressionThreshold);

            if (!isKAnon) {
                solutionSet.remove(i--);
            }
        }
    }

    public static void removeSolutionsThreshold (List<List<Integer>> elements, KAnonymity kAnonymity, double threshold) {
        for (int i = 0; i < elements.size(); i++) {
            int hash = elements.get(i).toString().hashCode();
            if (kAnonymity.getHistoryReports().get(hash).getPercentageOfSuppression() > threshold) {
                elements.remove(i--);
            }
        }
    }

    public static void comparePopulations (SolutionSet population1, SolutionSet population2) {
        if (population1.size() > 0 && population1.size() == population2.size()) {
            System.out.println("Populations comparison\n");

            for (int i = 0; i < population1.size(); i++) {
                System.out.println("Solution " + (i+1));

                List<Integer> lowerBounds1 = getSolutionLowerBounds(population1.get(i));
                List<Integer> upperBounds1 = getSolutionUpperBounds(population1.get(i));
                List<Integer> solution1 = getSolutionValues(population1.get(i));
                double f1_1 = population1.get(i).getObjective(0);
                double f2_1 = population1.get(i).getObjective(1);

                List<Integer> lowerBounds2 = getSolutionLowerBounds(population2.get(i));
                List<Integer> upperBounds2 = getSolutionUpperBounds(population2.get(i));
                List<Integer> solution2 = getSolutionValues(population2.get(i));
                double f1_2 = population2.get(i).getObjective(0);
                double f2_2 = population2.get(i).getObjective(1);

                if (lowerBounds1.equals(lowerBounds2)) {
                    System.out.println("LB: v");
                } else {
                    System.out.println("LB: x");
                }

                if (upperBounds1.equals(upperBounds2)) {
                    System.out.println("UB: v");
                } else {
                    System.out.println("UB: x");
                }

                if (solution1.equals(solution2)) {
                    System.out.println("SOL: v");
                } else {
                    System.out.println("SOL: x");
                }

                if (f1_1 == f1_2) {
                    System.out.println("OB1: v");
                } else {
                    System.out.println("OB1: x");
                }

                if (f2_1 == f2_2) {
                    System.out.println("OB2: v");
                } else {
                    System.out.println("OB2: x");
                }

                System.out.println();
            }
        }
    }

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

    public static void printSolutions (List<List<Integer>> solutions) {
        if (solutions != null && !solutions.isEmpty()) {
            System.out.println("Print solutions\n");

            int i = 0;
            for (List<Integer> solution : solutions) {
                System.out.println("Solution " + ++i);
                System.out.println(solution.toString());
                System.out.println();
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
