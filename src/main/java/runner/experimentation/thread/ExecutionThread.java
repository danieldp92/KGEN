package runner.experimentation.thread;

import approaches.Algorithm;
import approaches.metaheuristics.geneticalgorithm.KGENAlgorithm;
import approaches.metaheuristics.randomsearch.RandomAlgorithm;
import exception.TooNodeException;
import me.tongfei.progressbar.ProgressBar;
import runner.experimentation.exceptions.LimitExceedException;
import runner.experimentation.exceptions.OverflowException;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutionThread extends Thread implements Observer {
    private Algorithm algorithm;
    private List<List<Integer>> solutions;
    private int numberOfNodeAnalyzed;
    private int initialMax;
    private ProgressBar pb;
    private int indexRun;

    private boolean overflow;

    public ExecutionThread (Algorithm algorithm, int indexRun) {
        this.algorithm = algorithm;
        this.algorithm.addObserver(this);
        this.numberOfNodeAnalyzed = 0;
        this.indexRun = indexRun;

        if (algorithm instanceof KGENAlgorithm) {
            this.initialMax = ((KGENAlgorithm) algorithm).getMaxEvaluations();
        } else if (algorithm instanceof RandomAlgorithm) {
            this.initialMax = ((RandomAlgorithm) algorithm).getMaxEvaluations();
        } else {
            try {
                this.initialMax = height(algorithm.getkAnonymity().lowerBounds(algorithm.getSuppressionThreshold()), algorithm.getkAnonymity().upperBounds());
            } catch (OverflowException e) {
                System.out.println(algorithm.getName() +  ": Too many nodes. Impossible to memorize them");
                overflow = true;
            }
        }
    }

    @Override
    public void run() {
        if (!overflow) {
            this.pb = new ProgressBar(algorithm.getName() + " " + indexRun, initialMax);

            try {
                solutions = algorithm.run();
                pb.close();
            } catch (TooNodeException | LimitExceedException e) {
                pb.stepTo(initialMax);
                pb.close();
                System.out.println("\n" + e.getMessage());
            }

            pb.close();
        }
    }

    public List<List<Integer>> getSolutions() {
        return solutions;
    }


    @Override
    public void update(Observable o, Object arg) {
        int i = (int) arg;
        this.pb.stepTo(i);
    }

    private int height(ArrayList<Integer> bottomNode, ArrayList<Integer> topNode) throws OverflowException {
        int count = 1;

        for (int i = 0; i < bottomNode.size(); i++) {
            count *= topNode.get(i) - bottomNode.get(i) + 1;

            if (count < 0) {
                throw new OverflowException();
            }
        }

        return count;
    }
}
