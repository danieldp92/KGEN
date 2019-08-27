package runner.experimentation.thread;

import approaches.Algorithm;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutionThread extends Thread{
    private ReentrantLock lock;

    private Algorithm algorithm;
    private List<List<Integer>> solutions;

    public ExecutionThread (Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public void run() {
        solutions = algorithm.run();
    }

    public List<List<Integer>> getSolutions() {
        return solutions;
    }
}
