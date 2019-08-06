package approaches;

import anonymization.KAnonymity;
import main.experimentation.exceptions.OutOfTimeException;

import java.util.List;

public abstract class Algorithm {
    protected String name;
    protected KAnonymity kAnonymity;

    public KAnonymity getkAnonymity() {
        return kAnonymity;
    }

    public String getName() {
        return name;
    }

    abstract public List<List<Integer>> run() throws OutOfTimeException;
}
