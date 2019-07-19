package approaches;

import anonymization.KAnonymity;

import java.util.List;

public abstract class Algorithm {
    protected KAnonymity kAnonymity;

    public KAnonymity getkAnonymity() {
        return kAnonymity;
    }

    abstract public List<List<Integer>> run();
}
