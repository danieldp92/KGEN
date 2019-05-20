package geneticalgorithm.operator;

import anonymization.KAnonymity;
import anonymization.generalization.exception.LevelNotValidException;
import dataset.Dataset;
import geneticalgorithm.encoding.Chromosome;

import java.util.ArrayList;
import java.util.Collections;

public class Algorithm {
    private static final int POPULATION_SIZE = 100;
    private static final int MAX_GENERATION = 2000;
    private static final int K_LEVEL = 5;

    public ArrayList<Chromosome> population;
    private Selection selection;
    private Crossover crossover;
    private Mutation mutation;

    private Dataset dataset;
    private ArrayList<Integer> lowerBounds;
    private ArrayList<Integer> upperBounds;
    private KAnonymity kAnonymity;


    public Algorithm (Dataset dataset) {
        this.dataset = dataset;
        this.upperBounds = upperBounds;
        this.kAnonymity = new KAnonymity(dataset);

        //Operators
        this.selection = new Selection();
        this.crossover = new Crossover(kAnonymity);
        this.mutation = new Mutation();

        //UpperBounds and lowerBounds of solution
        this.lowerBounds = kAnonymity.lowerBounds();
        this.upperBounds = kAnonymity.upperBounds();
    }

    private void generatePopulation () {
        this.population = new ArrayList<Chromosome>();

        long startTime;

        for (int i = 0; i < POPULATION_SIZE; i++) {
            System.out.println("Chromosome : " + (i+1));
            startTime = System.currentTimeMillis();
            Chromosome newChromosome = generateRandomChromosome(lowerBounds, upperBounds);
            this.population.add(newChromosome);

            System.out.println("\tGeneration time: " + (System.currentTimeMillis() - startTime));
            System.out.println("\tLowerbound array");
            System.out.println("\t\t" + lowerBounds.toString());
            System.out.println("\tFinal chromosome array");
            System.out.println("\t\t" + newChromosome.toString());
            System.out.println("\tUpperbound array");
            System.out.println("\t\t" + upperBounds.toString());
            double distanceFromMaxLatticeNode = 0;
            for (int j = 0; j < newChromosome.size(); j++) {
                if (newChromosome.getUpperBound(j) > 0) {
                    distanceFromMaxLatticeNode += newChromosome.get(j)/newChromosome.getUpperBound(j);
                } else {
                    distanceFromMaxLatticeNode++;
                }
            }
            distanceFromMaxLatticeNode /= newChromosome.size();
            System.out.println("\tDistance from max node: " + (1-distanceFromMaxLatticeNode));
            System.out.println();
        }
        System.out.println();
    }

    private Chromosome generateRandomChromosome (ArrayList<Integer> lowerBounds, ArrayList<Integer> upperBounds) {
        Chromosome newChromosome = new Chromosome(lowerBounds, upperBounds);

        for (int j = 0; j < upperBounds.size(); j++) {
            int randomValue = (int) ((Math.random() * (upperBounds.get(j) - lowerBounds.get(j) + 1)) + lowerBounds.get(j));
            newChromosome.add(randomValue);
        }

        System.out.println("\tStarting chromosome array");
        System.out.println("\t\t" + newChromosome.toString());

        try {
            validateSolution(newChromosome);
        } catch (LevelNotValidException e) {}

        return newChromosome;
    }

    public ArrayList<Chromosome> execute () {
        //Generate starting population
        long startTime = System.currentTimeMillis();

        generatePopulation();
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
        boolean kAnonymized = kAnonymity.kAnonymityTest(chromosome, K_LEVEL);

        if (!kAnonymized)
            return 0;

        return Math.random();
    }

    private void validateSolution (Chromosome chromosome) throws LevelNotValidException {
        ArrayList<Integer> upperBounds = chromosome.getUpperBounds();
        boolean kAnonymized = this.kAnonymity.kAnonymityTest(chromosome, K_LEVEL);

        int numberOfIter = 0;
        while (!kAnonymized) {
            ArrayList<Integer> indexToChoose = new ArrayList<Integer>();
            for (int i = 0; i < chromosome.size(); i++) {
                if (chromosome.getUpperBound(i) > chromosome.get(i)) {
                    indexToChoose.add(i);
                }
            }

            Collections.shuffle(indexToChoose);


            //Find the most distante value from the end of lattice
            /*int indexMinPercentage = 0;
            double minValueOfPercentage = 1;

            for (int i = 0; i < chromosome.size(); i++) {
                if (chromosome.get(i) != chromosome.getUpperBound(i)) {
                    double percentageOfAnonymization = (double)chromosome.get(i)/chromosome.getUpperBound(i);
                    if (percentageOfAnonymization < minValueOfPercentage) {
                        minValueOfPercentage = percentageOfAnonymization;
                        indexMinPercentage = i;
                    }
                }
            }*/

            //Increase its value
            int randomIndex = indexToChoose.remove(0);
            chromosome.set(randomIndex, chromosome.get(randomIndex)+1);

            kAnonymized = this.kAnonymity.kAnonymityTest(chromosome, K_LEVEL);

            numberOfIter++;
        }

        System.out.println("\tNumber of validation: " + numberOfIter);
    }
}
