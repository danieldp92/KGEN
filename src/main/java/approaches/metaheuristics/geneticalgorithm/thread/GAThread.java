package approaches.metaheuristics.geneticalgorithm.thread;

import java.util.concurrent.locks.ReentrantLock;

public abstract class GAThread implements Runnable {
    protected ReentrantLock lock;
    protected GAThreadPoolExecutor gaThreadPoolExecutor;
    protected int index;

    protected Object returnValue;

    public GAThread(int index, GAThreadPoolExecutor gaThreadPoolExecutor) {
        this.lock = new ReentrantLock();
        this.index = index;
        this.gaThreadPoolExecutor = gaThreadPoolExecutor;
    }

    public Object getReturnValue() {
        return returnValue;
    }
}
