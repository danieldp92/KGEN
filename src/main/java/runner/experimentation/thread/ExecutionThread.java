package runner.experimentation.thread;

import approaches.Algorithm;
import exception.TooNodeException;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutionThread extends Thread{
    private ReentrantLock lock;

    private Algorithm algorithm;
    private List<List<Integer>> solutions;
    private double suppressionTreshold;

    public ExecutionThread (Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public void run() {
        try {
            solutions = algorithm.run();
        } catch (TooNodeException e) {
            System.out.println("Too many node to process. The algorithm has been stopped");
        }
    }

    public List<List<Integer>> getSolutions() {
        return solutions;
    }
}
