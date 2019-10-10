package approaches.metaheuristics.geneticalgorithm.thread;

import jmetal.core.SolutionSet;

import java.util.ArrayList;
import java.util.List;

public abstract class GAMultiThread {
    protected List<GAThread> threads;
    protected int maxNumberOfThreads;

    public GAMultiThread(int maxNumberOfThreads) {
        this.maxNumberOfThreads = maxNumberOfThreads;
    }

    public abstract SolutionSet parallelExecution (SolutionSet population);

    protected void waitFreeThread () {
        if (threads.isEmpty()) {
            return;
        }

        boolean freeThread = false;

        while (!freeThread) {
            Integer threadFreeIndex = getFreeIndexThread();

            if (threadFreeIndex != null) {
                freeThread = true;
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {}
            }
        }
    }

    protected Integer getFreeIndexThread() {
        Integer threadFreeIndex = null;

        int index = 0;
        while (index < threads.size() && threads.get(index).isAlive()) {
            index++;
        }

        if (index < threads.size()) {
            threadFreeIndex = index;
        }

        return threadFreeIndex;
    }

    protected void startThread(int index) {
        while (threads.get(index).isAlive()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {}
        }

        threads.get(index).start();
    }
}
