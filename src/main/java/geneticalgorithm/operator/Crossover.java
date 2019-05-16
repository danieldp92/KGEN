package geneticalgorithm.operator;

import anonymization.DatasetAnonymization;
import anonymization.generalization.exception.LevelNotValidException;
import dataset.Dataset;
import geneticalgorithm.encoding.Chromosome;

import java.util.ArrayList;

public class Crossover {
    private static final int K_LEVEL = 5;
    private DatasetAnonymization datasetAnonymization;

    public Crossover (DatasetAnonymization datasetAnonymization) {
        this.datasetAnonymization = datasetAnonymization;
    }

    public ArrayList<Chromosome> crossover (double probability, Chromosome parent1, Chromosome parent2) {
        ArrayList<Chromosome> offspring = new ArrayList<Chromosome>();
        offspring.add(parent1.copy());
        offspring.add(parent2.copy());

        double random = Math.random();

        if (probability < random) {
            int crossoverPoint = (int) (Math.random() * (parent1.size()-1)) + 1;

            Chromosome offspring1 = new Chromosome();
            Chromosome offspring2 = new Chromosome();

            for (int i = 0; i < parent1.size(); i++) {
                if (i < crossoverPoint) {
                    offspring1.add(parent1.get(i));
                    offspring2.add(parent2.get(i));
                } else {
                    offspring2.add(parent1.get(i));
                    offspring1.add(parent2.get(i));
                }
            }

            try {
                validateSolution(offspring1);
                validateSolution(offspring2);
            } catch (LevelNotValidException e) {
                System.out.println("Crossover validation function has a problem");
                e.printStackTrace();
            }

            offspring.set(0, offspring1);
            offspring.set(1, offspring2);
        }

        return offspring;
    }

    /**
     * This method provides a valid solution, checking if it's a k-anonymized dataset.
     * If it's not anonymized, modifies the solution increasing their values and adding a penality
     */
    private void validateSolution (Chromosome chromosome) throws LevelNotValidException {
        Dataset anonymizedDataset = this.datasetAnonymization.anonymize(chromosome);
        boolean kAnonymized = this.datasetAnonymization.kAnonymityTest(anonymizedDataset, K_LEVEL);

        while (!kAnonymized) {
            //Find the most distante value from the end of lattice
            int indexMinPercentage = 0;
            double minValueOfPercentage = 1;

            for (int i = 0; i < chromosome.size(); i++) {
                double percentageOfAnonymization = (double)chromosome.get(i)/chromosome.getUpperBound(i);
                if (percentageOfAnonymization < minValueOfPercentage) {
                    minValueOfPercentage = percentageOfAnonymization;
                    indexMinPercentage = i;
                }
            }

            //Increase its value
            chromosome.set(indexMinPercentage, chromosome.get(indexMinPercentage)+1);
            chromosome.increasePenality(1);

            anonymizedDataset = this.datasetAnonymization.anonymize(chromosome);
            kAnonymized = this.datasetAnonymization.kAnonymityTest(anonymizedDataset, K_LEVEL);
        }
    }
}
