package approaches;

import anonymization.KAnonymity;
import dataset.beans.Dataset;

import java.util.List;

public abstract class Algorithm {
    protected String name;
    protected Dataset dataset;
    protected KAnonymity kAnonymity;
    protected double suppressionTreshold;

    public KAnonymity getkAnonymity() {
        return kAnonymity;
    }

    public String getName() {
        return name;
    }

    abstract public List<List<Integer>> run();
}
