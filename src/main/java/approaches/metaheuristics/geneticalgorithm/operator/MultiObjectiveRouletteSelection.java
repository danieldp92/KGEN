package approaches.metaheuristics.geneticalgorithm.operator;

import anonymization.KAnonymity;
import approaches.metaheuristics.geneticalgorithm.AnonymizationProblem;
import approaches.metaheuristics.geneticalgorithm.encoding.GeneralizationSolution;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.operators.selection.Selection;
import jmetal.util.JMException;

import java.util.*;

public class MultiObjectiveRouletteSelection extends Selection {
    private static final double MIN_NORMALIZATION = 0;
    private static final double MAX_NORMALIZATION = 0.9;

    public MultiObjectiveRouletteSelection(HashMap<String, Object> parameters) {
        super(parameters);
    }

    public Object execute(Object object) throws JMException {
        SolutionSet population = (SolutionSet) object;
        Solution solutionSelected = null;
        double random = 0;
        int index = 0;


        //Take all klv in the population, without repeat them
        Set<Double> klvList = new HashSet<Double>();
        for (int i = 0; i < population.size(); i++) {
            if (population.get(i).getObjective(AnonymizationProblem.ffKLV_OBJECTIVE) >= KAnonymity.MIN_K_LEVEL) {
                klvList.add(population.get(i).getObjective(AnonymizationProblem.ffKLV_OBJECTIVE));
            }
        }

        //If there is no other KLEV different to 1, then choose a solution with KLEV = 1
        if (klvList.isEmpty()) {
            klvList.add(1.0);
        }

        //KLV SELECTION
        ArrayList<Double> probabilitiesKLV = generateKLVProbabilities(klvList);

        //Choose the klv randomly, using probability prevously calculated
        random = Math.random();

        index = 0;
        while (index < probabilitiesKLV.size() && random > probabilitiesKLV.get(index)) {
            index++;
        }

        ArrayList<Double> klvArray = new ArrayList<Double>(klvList);
        double klvSelected = klvArray.get(index);


        //Take all solutions with the designed level of kanonymization
        ArrayList<Solution> candidateSolutions = new ArrayList<Solution>();
        for (int i = 0; i < population.size(); i++) {
            //if (population.get(i).getObjective(ffKLV_OBJECTIVE) == klvSelected) {
                candidateSolutions.add(population.get(i));
            //}
        }

        //LOG SELECTION
        ArrayList<Double> probabilitiesLOG = generateLOGProbabilities(candidateSolutions);

        //Choose the log randomly, using probability prevously calculated
        random = Math.random();

        index = 0;
        while (index < probabilitiesLOG.size() && random > probabilitiesLOG.get(index)) {
            index++;
        }

        solutionSelected = candidateSolutions.get(index);


        return solutionSelected;
    }

    private ArrayList<Double> generateKLVProbabilities (Set<Double> klvList) {
        ArrayList<Double> probabilitiesKLV = new ArrayList<Double>();
        double probabilityKVL = 0;

        //Calculate the probability to get a single klv
        double sumKLV = 0;
        for (double klv : klvList) {
            sumKLV += Math.log(klv);
            //sumKLV += klv;
        }

        for (double klv : klvList) {
            probabilityKVL += (Math.log(klv) / sumKLV);
            //probabilityKVL += (klv / sumKLV);
            probabilitiesKLV.add(probabilityKVL);
        }

        return probabilitiesKLV;
    }

    private ArrayList<Double> generateLOGProbabilities (ArrayList<Solution> candidateSolutions) {
        //Calculate the probability to get a single log
        double sumLOG = 0;
        int minPenalty = getMinPenalty(candidateSolutions);
        int maxPenalty = getMaxPenalty(candidateSolutions);

        for (Solution candidateSolution : candidateSolutions) {
            GeneralizationSolution generalizationSolution = (GeneralizationSolution) candidateSolution;
            double penaltyNormalized = normalize(minPenalty, maxPenalty, generalizationSolution.getPenalty());

            sumLOG += (1 - penaltyNormalized) * candidateSolution.getObjective(AnonymizationProblem.ffLOG_OBJECTIVE);
        }


        ArrayList<Double> probabilitiesLOG = new ArrayList<Double>();
        double probabilityLOG = 0;

        for (Solution candidateSolution : candidateSolutions) {
            GeneralizationSolution generalizationSolution = (GeneralizationSolution) candidateSolution;
            double penaltyNormalized = normalize(minPenalty, maxPenalty, generalizationSolution.getPenalty());

            probabilityLOG += (((1 - penaltyNormalized) * candidateSolution.getObjective(AnonymizationProblem.ffLOG_OBJECTIVE)) / sumLOG);
            probabilitiesLOG.add(probabilityLOG);
        }

        return probabilitiesLOG;
    }

    private int getMinPenalty (ArrayList<Solution> solutions) {
        int minPenalty = Integer.MAX_VALUE;

        for (Solution solution : solutions) {
            if (((GeneralizationSolution)solution).getPenalty() < minPenalty) {
                minPenalty = ((GeneralizationSolution)solution).getPenalty();
            }
        }

        return minPenalty;
    }

    private int getMaxPenalty (ArrayList<Solution> solutions) {
        int maxPenalty = -1;

        for (Solution solution : solutions) {
            if (((GeneralizationSolution)solution).getPenalty() > maxPenalty) {
                maxPenalty = ((GeneralizationSolution)solution).getPenalty();
            }
        }

        return maxPenalty;
    }

    private double normalize (int min, int max, int value) {
        if (max == min) {
            return 0;
        }

        double diff = MAX_NORMALIZATION - MIN_NORMALIZATION;
        double fract = (double)(value - min) / (max - min);

        return (diff * fract) + MIN_NORMALIZATION;
    }
}
