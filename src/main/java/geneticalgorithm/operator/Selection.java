package geneticalgorithm.operator;

import geneticalgorithm.encoding.Chromosome;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

public class Selection {
    private static final int TOURNAMENT_SIZE = 8;
    private static final double TOURNAMENT_PROB = 0.75;

    public ArrayList<Chromosome> selection (ArrayList<Chromosome> population) {
        ArrayList<Chromosome> chromosomeSelected = new ArrayList<Chromosome>();

        for (int i = 0; i < population.size(); i++) {
            ArrayList<Chromosome> randomChromosomes = getRandomChromosomes(population, TOURNAMENT_SIZE);
            randomChromosomes = getChromosomesSortedByFitness(randomChromosomes);

            double random = Math.random();

            double actualTournamentProb = TOURNAMENT_PROB;

            int j = 0;
            while (random > actualTournamentProb) {
                j++;
                random -= actualTournamentProb;
                actualTournamentProb = TOURNAMENT_PROB * Math.pow((1-TOURNAMENT_PROB), j);
            }

            chromosomeSelected.add(randomChromosomes.get(j));
        }

        return chromosomeSelected;
    }


    //Sorted from lowest value of FF to greatest one
    private ArrayList<Chromosome> getChromosomesSortedByFitness (ArrayList<Chromosome> chromosomes) {
        ArrayList<Chromosome> tmpChromosomes = new ArrayList<Chromosome>();
        tmpChromosomes.addAll(chromosomes);

        ArrayList<Chromosome> sortedPopulation = new ArrayList<Chromosome>();

        int posMaxChromosome;
        double minFF;

        while (!tmpChromosomes.isEmpty()) {
            posMaxChromosome = -1;
            minFF = 0;
            for (int i = 0; i < tmpChromosomes.size(); i++) {
                if (tmpChromosomes.get(i).getFitness() < minFF) {
                    minFF = tmpChromosomes.get(i).getFitness();
                    posMaxChromosome = i;
                }
            }
            sortedPopulation.add(tmpChromosomes.remove(posMaxChromosome));
        }

        return sortedPopulation;
    }

    private ArrayList<Chromosome> getRandomChromosomes (ArrayList<Chromosome> chromosomes, int numberOfRandomChromosome) {
        ArrayList<Chromosome> clonePopulation = (ArrayList<Chromosome>) chromosomes.clone();
        ArrayList<Chromosome> randomChromosomes = new ArrayList<Chromosome>();

        Collections.shuffle(clonePopulation);

        for (int i = 0; i < numberOfRandomChromosome; i++) {
            randomChromosomes.add(clonePopulation.remove(0));
        }

        return randomChromosomes;
    }

}
