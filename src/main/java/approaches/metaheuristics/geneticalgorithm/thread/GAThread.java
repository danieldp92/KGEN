package approaches.metaheuristics.geneticalgorithm.thread;

import java.util.concurrent.locks.ReentrantLock;

public abstract class GAThread extends Thread {
    protected ReentrantLock lock;
    protected boolean finish;

    protected Object returnValue;

    public GAThread() {
        this.lock = new ReentrantLock();
        setFinish(true);
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        lock.lock();

        try {
            this.finish = finish;
        } finally {
            lock.unlock();
        }
    }

    public Object getReturnValue() {
        return returnValue;
    }
}
