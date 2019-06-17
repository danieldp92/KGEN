package approaches.geneticalgorithm.operator;

import approaches.geneticalgorithm.encoding.GeneralizationSolution;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.operators.selection.Selection;
import jmetal.util.JMException;

import java.util.*;

public class MultiObjectiveRouletteSelection extends Selection {
    private static final int ffLOG_OBJECTIVE = 0;
    private static final int ffKLV_OBJECTIVE = 1;

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
            klvList.add(population.get(i).getObjective(ffKLV_OBJECTIVE));
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
            if (population.get(i).getObjective(ffKLV_OBJECTIVE) == klvSelected) {
                candidateSolutions.add(population.get(i));
            }
        }

        orderByPenalty(candidateSolutions);

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
            sumKLV += Math.log(klv+1);
            //sumKLV += klv;
        }

        for (double klv : klvList) {
            probabilityKVL += (Math.log(klv+1) / sumKLV);
            //probabilityKVL += (klv / sumKLV);
            probabilitiesKLV.add(probabilityKVL);
        }

        return probabilitiesKLV;
    }

    private ArrayList<Double> generateLOGProbabilities (ArrayList<Solution> candidateSolutions) {
        //Calculate the probability to get a single log
        double sumLOG = 0;
        for (Solution candidateSolution : candidateSolutions) {
            sumLOG += candidateSolution.getObjective(ffLOG_OBJECTIVE);
        }

        ArrayList<Double> probabilitiesLOG = new ArrayList<Double>();
        double probabilityLOG = 0;

        for (Solution candidateSolution : candidateSolutions) {
            probabilityLOG += (candidateSolution.getObjective(ffLOG_OBJECTIVE) / sumLOG);
            probabilitiesLOG.add(probabilityLOG);
        }

        return probabilitiesLOG;
    }

    private void orderByPenalty (ArrayList<Solution> solutions) {
        for (int i = 0; i < solutions.size()-1; i++) {
            for (int j = i+1; j < solutions.size(); j++) {
                if (((GeneralizationSolution)solutions.get(i)).getPenalty() >
                        ((GeneralizationSolution)solutions.get(j)).getPenalty()) {
                    GeneralizationSolution tmp = (GeneralizationSolution) solutions.get(i);
                    solutions.set(i, solutions.get(j));
                    solutions.set(j, tmp);
                }
            }
        }
    }
}
