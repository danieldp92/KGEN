package anonymization.multithread;

import dataset.beans.Dataset;

import java.util.ArrayList;

public class MultiThreadAnonymization {

    private int maxThread;

    public MultiThreadAnonymization (int maxThreads) {

        this.maxThread = maxThreads;
    }


    public int [] numberOfEqualsRow (Dataset dataset, int kLevel) {
        //Variables
        int numberOfRows = dataset.getColumns().get(0).size();
        int [] equalsRows = new int[numberOfRows];
        ArrayList<AnonymizationThread> threads = new ArrayList<AnonymizationThread>();
        boolean finish = false;

        int nextRowToAnalyze = 0;

        AnonymizationThread tmpThread = null;


        if (numberOfRows < maxThread)
            maxThread = numberOfRows;

        //Create threads
        for (int i = 0; i < maxThread; i++) {
            tmpThread = new AnonymizationThread(dataset, nextRowToAnalyze++, kLevel);
            threads.add(tmpThread);
        }

        //Start threads
        for (int i = 0; i < threads.size(); i++) {
            threads.get(i).start();
        }


        while (!finish) {
            if (isThreadsRunning(threads)) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {}
            } else {
                //If there aren't any rows to analyse, just delete the thread from the pool
                if (nextRowToAnalyze == numberOfRows) {
                    //Exit condition
                    if (threads.isEmpty()) {
                        finish = true;
                    }

                    else {
                        int indexToRemove = getIndexOfFinishThread(threads);

                        if (indexToRemove >= 0) {
                            add(equalsRows, threads.get(indexToRemove).getNumberOfEqualsRows());
                            threads.remove(indexToRemove);
                        }
                    }

                }

                else {
                    int indexOfFinishThread = getIndexOfFinishThread(threads);
                    add(equalsRows, threads.get(indexOfFinishThread).getNumberOfEqualsRows());

                    tmpThread = new AnonymizationThread(dataset, nextRowToAnalyze++, kLevel);
                    threads.set(indexOfFinishThread, tmpThread);
                    threads.get(indexOfFinishThread).start();
                }

            }
        }

        return equalsRows;
    }

    private int getIndexOfFinishThread (ArrayList<AnonymizationThread> threads) {
        int i = 0;
        while (i < threads.size() && threads.get(i).isAlive()) {
            i++;
        }

        if (i < threads.size()) {
            return i;
        }

        return -1;
    }

    private boolean isThreadsRunning (ArrayList<AnonymizationThread> threads) {
        if (threads.isEmpty()) {
            return false;
        }


        int indexOfDeadThread = getIndexOfFinishThread(threads);

        if (indexOfDeadThread == -1) {
            return true;
        }

        return false;
    }

    private int [] collapseWithSum (ArrayList<int[]> equalsRows) {
        int [] sum = new int[equalsRows.get(0).length];

        int tmpSum = 0;
        for (int i = 0; i < equalsRows.get(0).length; i++) {
            for (int j = 0; j < equalsRows.get(0).length; j++) {
                tmpSum += equalsRows.get(j)[i];
            }

            sum[i] = tmpSum;
            tmpSum = 0;
        }

        return sum;
    }

    private void add (int [] sum, int [] vectorToSum) {
        for (int i = 0; i < sum.length; i++) {
            sum[i] += vectorToSum[i];
        }
    }
}
