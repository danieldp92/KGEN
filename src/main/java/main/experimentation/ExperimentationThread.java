package main.experimentation;

import controller.LatticeController;
import dataset.beans.Dataset;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class ExperimentationThread extends Thread {
    private static final int OLA_ALGORITHM = 1;
    private static final int EXHAUSTIVE_ALGORITHM = 2;
    private static final int KGEN_ALGORITHM = 3;

    private ReentrantLock lock;
    private boolean unlock;
    private Dataset dataset;
    private LatticeController latticeController;

    private int approach;
    public ExperimentationThread (int approachType) {
        this.lock = new ReentrantLock();

        this.approach = approachType;
        this.unlock = false;
    }

    @Override
    public void run() {
        switch (approach) {
            case OLA_ALGORITHM:
                System.out.println("OLA");
                OLAExperimentation olaExperimentation = new OLAExperimentation();
                try {
                    dataset = olaExperimentation.initRandomDataset();
                } catch (IOException e) {}

                while (!unlock) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {}
                }


                try {
                    olaExperimentation.execute(latticeController);
                } catch (IOException e) {}
                break;
            case EXHAUSTIVE_ALGORITHM:
                break;
            case KGEN_ALGORITHM:

                break;
            default: break;
        }
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setLatticeController(LatticeController latticeController) {
        this.latticeController = latticeController;
    }

    public void unlockThread () {
        lock.lock();

        try {
            unlock = true;
        } finally {
            lock.unlock();
        }
    }
}
