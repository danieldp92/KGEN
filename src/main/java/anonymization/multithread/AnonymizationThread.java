package anonymization.multithread;

import dataset.Attribute;
import dataset.Dataset;

public class AnonymizationThread extends Thread {
    private Dataset dataset;
    private int indexRow;
    private int kLevel;
    private int [] numberOfEqualsRows;

    public AnonymizationThread (Dataset dataset, int indexRow, int kLevel) {
        this.dataset = dataset;
        this.indexRow = indexRow;
        this.kLevel = kLevel;

        this.numberOfEqualsRows = new int[dataset.getDatasetSize()];
    }

    public int getIndexRow() {
        return indexRow;
    }

    public int[] getNumberOfEqualsRows() {
        return numberOfEqualsRows;
    }

    @Override
    public void run() {
        numberOfEqualsRows[indexRow] = 1;

        for (int j = indexRow+1; j < dataset.getDatasetSize(); j++) {
            boolean equals = equalsRows(dataset, indexRow, j);

            if (equals) {
                numberOfEqualsRows[indexRow] = numberOfEqualsRows[indexRow] + 1;
                numberOfEqualsRows[j] = numberOfEqualsRows[j] + 1;
            }
        }
    }

    private boolean equalsRows (Dataset dataset, int indexRow1, int indexRow2) {
        int i = 0;
        boolean equalsAttribute = true;

        while (i < dataset.getHeader().size() && equalsAttribute) {
            Attribute attribute1 = (Attribute) dataset.getColumns().get(i).get(indexRow1);
            Attribute attribute2 = (Attribute) dataset.getColumns().get(i).get(indexRow2);;

            equalsAttribute = equalsAttribute(attribute1, attribute2);
            i++;
        }

        return equalsAttribute;
    }

    private boolean equalsAttribute (Attribute attribute1, Attribute attribute2) {
        boolean equals = false;

        if (attribute1.getValue() == null && attribute2.getValue() == null) {
            equals = true;
        }

        else if ((attribute1.getValue() == null && attribute2.getValue() != null) ||
                (attribute1.getValue() != null && attribute2.getValue() == null)) {
            equals = false;
        }

        else {
            if (attribute1.getValue().equals(attribute2.getValue())) {
                equals = true;
            }
        }

        return equals;
    }
}
