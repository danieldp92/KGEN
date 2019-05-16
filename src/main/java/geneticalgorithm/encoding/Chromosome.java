package geneticalgorithm.encoding;

import java.util.ArrayList;

public class Chromosome extends ArrayList<Integer> {
    private ArrayList<Integer> lowerBounds;
    private ArrayList<Integer> upperBounds;
    private double fitness;
    private int penality;
    private int latticeHeight;


    public Chromosome () {
        this.lowerBounds = new ArrayList<Integer>();
        this.upperBounds = new ArrayList<Integer>();
        this.fitness = 0;

        this.latticeHeight = 0;
    }

    public Chromosome (ArrayList<Integer> lowerBounds, ArrayList<Integer> upperBounds) {
        this.lowerBounds = lowerBounds;
        this.upperBounds = upperBounds;
        this.fitness = 0;

        this.latticeHeight = 0;
        for (int upperBound : this.upperBounds) {
            this.latticeHeight += upperBound;
        }
    }

    public int getLowerBound (int index) {
        return lowerBounds.get(index);
    }

    public ArrayList<Integer> getLowerBounds() {
        return lowerBounds;
    }

    public void setLowerBounds(ArrayList<Integer> lowerBounds) {
        this.lowerBounds = lowerBounds;
    }

    public int getUpperBound (int index) {
        return upperBounds.get(index);
    }

    public ArrayList<Integer> getUpperBounds() {
        return upperBounds;
    }

    public void setUpperBounds(ArrayList<Integer> upperBounds) {
        this.upperBounds = upperBounds;
        for (int upperBound : this.upperBounds) {
            this.latticeHeight += upperBound;
        }
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public int getPenality() {
        return penality;
    }

    public void increasePenality (int penality) {
        this.penality += penality;
    }

    public void setPenality(int penality) {
        this.penality = penality;
    }

    public int getLatticeHeight() {
        return latticeHeight;
    }

    public Chromosome copy () {
        Chromosome newChromosome = new Chromosome();
        for (Integer i : this) {
            newChromosome.add(i);
        }

        return newChromosome;
    }
}
