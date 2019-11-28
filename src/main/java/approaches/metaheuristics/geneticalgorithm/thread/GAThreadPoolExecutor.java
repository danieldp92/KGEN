package approaches.metaheuristics.geneticalgorithm.thread;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public abstract class GAThreadPoolExecutor {
    protected int maxNumberOfThreads;
    protected ThreadPoolExecutor threadPoolExecutor;
    private ReentrantLock lock;

    protected List<Solution> solutions;

    public GAThreadPoolExecutor(int maxNumberOfThreads) {
        this.maxNumberOfThreads = maxNumberOfThreads;

        this.lock = new ReentrantLock();

        this.solutions = new ArrayList<>();
    }

    public abstract SolutionSet execution(SolutionSet population);

    public void setSolution (Solution solution) {
        lock.lock();
        try {
            this.solutions.add(solution);
        } finally {
            lock.unlock();
        }

    }

    public void awaitTerminationAfterShutdown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
