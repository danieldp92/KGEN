package geneticalgorithm.operator;

import geneticalgorithm.encoding.Chromosome;

public class Mutation {

    public Chromosome mutation (double probability, Chromosome chromosome) {
        double random = Math.random();

        Chromosome mutant = chromosome.copy();

        if (probability < random) {
            int randomIndex = (int) (Math.random() * chromosome.size());
            int randomValue = (int) (Math.random() * (chromosome.getUpperBound(randomIndex) + 1));

            mutant.set(randomIndex, randomValue);
        }

        return mutant;
    }
}
