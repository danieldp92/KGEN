package approaches;

import anonymization.KAnonymity;
import dataset.beans.Dataset;
import exception.TooNodeException;
import runner.experimentation.exceptions.LimitExceedException;

import java.util.List;
import java.util.Observable;

public abstract class Algorithm extends Observable {
    protected String name;
    protected Dataset dataset;
    protected KAnonymity kAnonymity;
    protected double suppressionThreshold;

    public KAnonymity getkAnonymity() {
        return kAnonymity;
    }

    public String getName() {
        return name;
    }

    public double getSuppressionThreshold() {
        return suppressionThreshold;
    }

    abstract public List<List<Integer>> run() throws TooNodeException, LimitExceedException;
}
