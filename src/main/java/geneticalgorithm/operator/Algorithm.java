package geneticalgorithm.operator;

import anonymization.DatasetAnonymization;
import anonymization.generalization.exception.LevelNotValidException;
import dataset.Dataset;
import geneticalgorithm.encoding.Chromosome;

import java.util.ArrayList;

public class Algorithm {
    private static final int POPULATION_SIZE = 100;
    private static final int MAX_GENERATION = 2000;
    private static final int K_LEVEL = 5;

    public ArrayList<Chromosome> population;
    private Selection selection;
    private Crossover crossover;
    private Mutation mutation;

    private Dataset dataset;
    private ArrayList<Integer> upperBounds;
    private DatasetAnonymization datasetAnonymization;


    public Algorithm (Dataset dataset, ArrayList<Integer> upperBounds) {
        this.dataset = dataset;
        this.upperBounds = upperBounds;
        this.datasetAnonymization = new DatasetAnonymization(dataset);

        //Operators
        this.selection = new Selection();
        this.crossover = new Crossover(datasetAnonymization);
        this.mutation = new Mutation();
    }

    private void generatePopulation (ArrayList<Integer> upperBounds) {
        this.population = new ArrayList<Chromosome>();

        //Set lowerBounds to 0
        ArrayList<Integer> lowerBounds = new ArrayList<Integer>();
        for (int i = 0; i < upperBounds.size(); i++)
            lowerBounds.add(0);


        System.out.println("Chromosomes generated : 0");
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Chromosome newChromosome = generateRandomChromosome(lowerBounds, upperBounds);
            this.population.add(newChromosome);

            System.out.println("Chromosomes generated : " + (i+1));
            double distanceFromMaxLatticeNode = 0;
            for (int j = 0; j < newChromosome.size(); j++) {
                distanceFromMaxLatticeNode += newChromosome.get(j)/newChromosome.getUpperBound(j);
            }
            distanceFromMaxLatticeNode /= newChromosome.size();
            System.out.println("\tDistance from max node: " + (1-distanceFromMaxLatticeNode));
            System.out.println();
        }
        System.out.println();
    }

    private Chromosome generateRandomChromosome (ArrayList<Integer> lowerBounds, ArrayList<Integer> upperBounds) {
        Chromosome newChromosome = new Chromosome(lowerBounds, upperBounds);
        boolean validChromosome = false;

        for (int j = 0; j < upperBounds.size(); j++) {
            int randomValue = (int) ((Math.random() * (upperBounds.get(j) - lowerBounds.get(j) + 1)) + lowerBounds.get(j));
            newChromosome.add(randomValue);
        }

        do {
            Dataset datasetAnonymized = this.datasetAnonymization.anonymize(newChromosome);
            validChromosome = this.datasetAnonymization.kAnonymityTest(datasetAnonymized, 5);

            if (!validChromosome) {
                try {
                    validateSolution(newChromosome);
                } catch (LevelNotValidException e) {
                    e.printStackTrace();
                }
            }

        } while (!validChromosome);

        return newChromosome;
    }

    public ArrayList<Chromosome> execute () {
        //Generate starting population
        long startTime = System.currentTimeMillis();

        generatePopulation(upperBounds);
        System.out.println("Population generation time: " + (System.currentTimeMillis() - startTime));


        startTime = System.currentTimeMillis();
        //Evaluate population
        for (Chromosome chromosome : population) {
            try {
                double ff = evaluate(chromosome);
                chromosome.setFitness(ff);
            } catch (LevelNotValidException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Population evaluation time: " + (System.currentTimeMillis() - startTime));

        int actualGeneration = 0;

        /*System.out.print("Iteration " + (actualGeneration+1));
        while (actualGeneration < MAX_GENERATION) {
            System.out.print("\rIteration " + (actualGeneration+1));


        }*/


        return null;
    }

    //STUB
    public double evaluate (Chromosome chromosome) throws LevelNotValidException {
        Dataset chromosomeDataset = datasetAnonymization.anonymize(chromosome);
        boolean kAnonymized = datasetAnonymization.kAnonymityTest(chromosomeDataset, K_LEVEL);

        if (!kAnonymized)
            return 0;

        return Math.random();
    }

    private void validateSolution (Chromosome chromosome) throws LevelNotValidException {
        ArrayList<Integer> upperBounds = chromosome.getUpperBounds();
        Dataset anonymizedDataset = null;
        boolean kAnonymized = false;

        while (!kAnonymized) {
            //Find the most distante value from the end of lattice
            int indexMinPercentage = 0;
            double minValueOfPercentage = 1;

            for (int i = 0; i < chromosome.size(); i++) {
                if (chromosome.get(i) != chromosome.getUpperBound(i)) {
                    double percentageOfAnonymization = (double)chromosome.get(i)/chromosome.getUpperBound(i);
                    if (percentageOfAnonymization < minValueOfPercentage) {
                        minValueOfPercentage = percentageOfAnonymization;
                        indexMinPercentage = i;
                    }
                }
            }

            //Increase its value
            chromosome.set(indexMinPercentage, chromosome.get(indexMinPercentage)+1);

            anonymizedDataset = this.datasetAnonymization.anonymize(chromosome);
            kAnonymized = this.datasetAnonymization.kAnonymityTest(anonymizedDataset, K_LEVEL);
        }
    }
}
