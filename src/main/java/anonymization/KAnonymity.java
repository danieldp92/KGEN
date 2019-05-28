package anonymization;

import anonymization.generalization.exception.LevelNotValidException;
import anonymization.generalization.generator.GeneralizationGraphGenerator;
import anonymization.generalization.graph.GeneralizationTree;
import anonymization.generalization.type.DateGeneralization;
import anonymization.generalization.type.NumericGeneralization;
import anonymization.generalization.type.PlaceGeneralization;
import anonymization.generalization.type.StringGeneralization;
import anonymization.multithread.MultiThreadAnonymization;
import dataset.Attribute;
import dataset.Dataset;
import dataset.DatasetColumn;
import dataset.DatasetRow;
import dataset.type.Identifier;
import dataset.type.QuasiIdentifier;
import utils.DatasetUtils;

import java.util.*;

public class KAnonymity {
    private static final int MIN_K_LEVEL = 5;

    private PlaceGeneralization placeGeneralization;
    private DateGeneralization dateGeneralization;
    private NumericGeneralization numericGeneralization;
    private StringGeneralization stringGeneralization;

    private Dataset dataset;
    private ArrayList<Integer> lowerBounds;
    private ArrayList<Integer> upperBounds;
    private LinkedHashMap<Integer, ArrayList<DatasetColumn>> anonymizationMap;
    private LinkedHashMap<Integer, Object> quasiIdentifierMedianMap;
    private LinkedHashMap<String, Integer> kAnonymizedHistoryMap;

    private MultiThreadAnonymization multiThreadAnonymization;

    public KAnonymity(Dataset dataset) {
        this.placeGeneralization = new PlaceGeneralization();
        this.dateGeneralization = new DateGeneralization();
        this.numericGeneralization = new NumericGeneralization();
        this.stringGeneralization = new StringGeneralization();

        this.multiThreadAnonymization = new MultiThreadAnonymization(100);

        this.dataset = dataset;

        initMedianMap();

        try {
            initAnonymizationMap();
        } catch (LevelNotValidException e) {}

        this.lowerBounds = lowerBounds();
        this.upperBounds = upperBounds();
        this.kAnonymizedHistoryMap = new LinkedHashMap<String, Integer>();
    }

    //INIT
    private void initAnonymizationMap () throws LevelNotValidException {
        this.anonymizationMap = new LinkedHashMap<Integer, ArrayList<DatasetColumn>>();

        DatasetRow header = dataset.getHeader();
        ArrayList<Integer> upperBounds = upperBounds();
        int indexQuasiIdentifier = 0;

        for (int i = 0; i < header.size(); i++) {
            Attribute attribute = (Attribute) header.get(i);

            if (attribute.getType() instanceof QuasiIdentifier) {
                ArrayList<DatasetColumn> allGeneralizationColumns = new ArrayList<DatasetColumn>();

                for (int j = 0; j <= upperBounds.get(indexQuasiIdentifier); j++) {
                    DatasetColumn newDatasetColumn = anonymizeColumn(i, j);
                    ArrayList<Integer> hashColumn = DatasetUtils.getHashColumn(newDatasetColumn);

                    allGeneralizationColumns.add(newDatasetColumn);
                }

                indexQuasiIdentifier++;

                this.anonymizationMap.put(i, allGeneralizationColumns);
            } else {
                DatasetColumn identifiersAnonymized = new DatasetColumn();
                for (int j = 0; j < dataset.getDatasetSize(); j++) {
                    Attribute newAttribute = new Attribute(attribute.getName(), "*****");
                    newAttribute.setType(attribute.getType());
                    identifiersAnonymized.add(newAttribute);
                }

                ArrayList<DatasetColumn> columns = new ArrayList<DatasetColumn>();
                columns.add(identifiersAnonymized);

                this.anonymizationMap.put(i, columns);
            }
        }
    }

    private void initMedianMap () {
        this.quasiIdentifierMedianMap = new LinkedHashMap<Integer, Object>();

        for (int i = 0; i < dataset.getHeader().size(); i++) {
            Attribute attribute = (Attribute) dataset.getHeader().get(i);

            if (attribute.getType() instanceof QuasiIdentifier) {
                this.quasiIdentifierMedianMap.put(i, findMedian(dataset.getColumns().get(i)));
            }
        }
    }

    //GENERATION
    public void deleteNotKAnonymizedRows (Dataset dataset, ArrayList<Integer> indexRows) {
        for (int i = 0; i < dataset.getColumns().size(); i++) {
            Collection<Attribute> attributesToRemove = new ArrayList<Attribute>();
            for (int indexRow : indexRows) {
                attributesToRemove.add((Attribute) dataset.getColumns().get(i).get(indexRow));
            }

            dataset.getColumns().get(i).removeAll(attributesToRemove);
        }
    }

    //ANONYMIZATION METHODS
    public Dataset anonymize (ArrayList<Integer> levelOfAnonymization) {
        Dataset datasetAnonimyzed = null;

        DatasetRow header = dataset.getHeader();
        ArrayList<DatasetColumn> columnsAnonymized = new ArrayList<DatasetColumn>();

        int indexQuasiIdentifier = 0;
        for (int i = 0; i < header.size(); i++) {
            Attribute attribute = (Attribute) header.get(i);
            ArrayList<DatasetColumn> allGeneralizationOfAttribute = anonymizationMap.get(i);
            DatasetColumn columnAnonymized = null;

            if (attribute.getType() instanceof QuasiIdentifier) {
                try {
                    columnAnonymized = allGeneralizationOfAttribute.get(levelOfAnonymization.get(indexQuasiIdentifier++));
                } catch (IndexOutOfBoundsException ex) {
                    //Chromosome bounds = (Chromosome) levelOfAnonymization;
                    //ArrayList<Integer> upperBounds = bounds.getUpperBounds();

                    //System.out.println(ex.getMessage());
                    ex.printStackTrace();
                }

            } else {
                columnAnonymized = allGeneralizationOfAttribute.get(0);
            }

            columnsAnonymized.add(columnAnonymized);
        }

        datasetAnonimyzed = new Dataset(header, columnsAnonymized);

        return datasetAnonimyzed;
    }

    /**
     * Run k-anonymity test with the level of generalizion of attributes.
     * @param levelOfAnonymization: the level of anonymization of all attributes of a given dataset
     * @return: the level of k-anonymization of dataset. 1 if it's not anonymized
     */
    public int kAnonymityTest (ArrayList<Integer> levelOfAnonymization) {
        String key = "";
        for (int index : levelOfAnonymization) {
            key += index + "-";
        }

        Integer kValue = kAnonymizedHistoryMap.get(key);
        if (kValue != null) {
            return kValue;
        }

        int kLevelMin = 1;
        int kLevelMax = 2;

        while (kLevelMax < dataset.getDatasetSize() && kAnonymityTest(levelOfAnonymization, (kLevelMax))) {
            kLevelMin = kLevelMax;
            kLevelMax *= 2;
        }

        if (kLevelMin > dataset.getDatasetSize()) {
            kLevelMin = dataset.getDatasetSize();
        } else {
            //Set the upperBound to the max number of rows
            if (kLevelMax > dataset.getDatasetSize()) {
                kLevelMax = dataset.getDatasetSize();
            }

            int multiplier = 1;

            while (kLevelMin != kLevelMax) {
                multiplier = 1;

                while (kLevelMin < kLevelMax && kAnonymityTest(levelOfAnonymization, kLevelMin)) {
                    kLevelMin += multiplier;
                    multiplier *= 2;

                }

                multiplier /= 2;

                if (kLevelMax != kLevelMin) {
                    kLevelMax = kLevelMin;
                    kLevelMin -= multiplier;
                }
            }
        }

        kAnonymizedHistoryMap.put(key, kLevelMin);

        return kLevelMin;
    }

    /**
     * Run k-anonymity test with the level of generalizion of attributes.
     * Check if the generated dataset is kLevel-anonymized
     * @param levelOfAnonymization: the level of anonymization of all attributes of a given dataset
     * @param kLevel: the level of k-anonymity
     * @return: true, if the dataset is k-anonymized, false otherwise
     */
    public boolean kAnonymityTest (ArrayList<Integer> levelOfAnonymization, int kLevel) {
        boolean kAnonymized = true;

        String key = "";
        for (int index : levelOfAnonymization) {
            key += index + "-";
        }

        Integer value = this.kAnonymizedHistoryMap.get(key);

        if (value == null) {
            //I run kAnonymityTest only if I didn't do it before -> value == null
            Dataset anonymizedDataset = anonymize(levelOfAnonymization);
            anonymizedDataset = datasetReduction(anonymizedDataset);
            kAnonymized = kAnonymityTest(anonymizedDataset, kLevel);
        } else {
            if (value == 1)
                kAnonymized = false;
        }

        return kAnonymized;
    }

    /**
     * Run k-anonymity test on a given dataset.
     * @param dataset: the dataset to check
     * @param kLevel: the level of k-anonymity
     * @return: true, if the dataset is k-anonymized, false otherwise
     */
    public boolean kAnonymityTest (Dataset dataset, int kLevel) {
        if (kLevel == 1) {
            return false;
        }

        int [] numberOfEqualsRows = new int[dataset.getDatasetSize()];
        for (int i = 0; i < numberOfEqualsRows.length; i++) {
            numberOfEqualsRows[i] = 1;
        }

        for (int i = 0; i < dataset.getDatasetSize(); i++) {
            for (int j = 0; j < dataset.getDatasetSize(); j++) {
                if (i != j) {
                    boolean equals = equalsRows(dataset, i, j);

                    if (equals) {
                        numberOfEqualsRows[i] = numberOfEqualsRows[i] + 1;

                        if (numberOfEqualsRows[i] >= kLevel) {
                            break;
                        }
                    }
                }
            }
        }

        List tmpList = new ArrayList<Integer>();
        for (int i : numberOfEqualsRows)
            tmpList.add(i);

        Integer min = (Integer) Collections.min(tmpList);

        if (min < kLevel)
            return false;

        return true;
    }

    public int [] numberOfEqualsRow (Dataset dataset, int kLevel) {
        int [] numberOfEqualsRows = new int[dataset.getDatasetSize()];
        for (int i = 0; i < numberOfEqualsRows.length; i++) {
            numberOfEqualsRows[i] = 1;
        }

        for (int i = 0; i < dataset.getDatasetSize(); i++) {
            for (int j = i+1; j < dataset.getDatasetSize(); j++) {
                boolean equals = equalsRows(dataset, i, j);

                if (equals) {
                    numberOfEqualsRows[i] = numberOfEqualsRows[i] + 1;
                    numberOfEqualsRows[j] = numberOfEqualsRows[j] + 1;
                }
            }
        }

        return numberOfEqualsRows;
    }

    //ANONYMIZATION BOUNDS
    public ArrayList<Integer> lowerBounds () {
        ArrayList<Integer> lowerBounds = new ArrayList<Integer>();

        boolean isAnonymized = false;
        for (int i = 0; i < dataset.getHeader().size(); i++) {
            Attribute headerAttribute = (Attribute) dataset.getHeader().get(i);

            if (headerAttribute.getType() instanceof QuasiIdentifier) {
                ArrayList<DatasetColumn> anonymizationColumnsOfAttribute = this.anonymizationMap.get(i);

                for (int j = 0; j < anonymizationColumnsOfAttribute.size(); j++) {
                    DatasetRow header = new DatasetRow();
                    header.add(headerAttribute);

                    ArrayList<DatasetColumn> columns = new ArrayList<DatasetColumn>();
                    columns.add(anonymizationColumnsOfAttribute.get(j));

                    Dataset tmpDataset = new Dataset(header, columns);
                    isAnonymized = kAnonymityTest(tmpDataset, MIN_K_LEVEL);

                    if (isAnonymized) {
                        lowerBounds.add(j);
                        break;
                    }
                }
            }
        }

        return lowerBounds;
    }

    public ArrayList<Integer> upperBounds () {
        ArrayList<Integer> upperBounds = new ArrayList<Integer>();

        GeneralizationTree generalizationTree = GeneralizationGraphGenerator.generatePlaceHierarchy();

        //Max level of anonymity of every attribute
        for (int i = 0; i < dataset.getHeader().size(); i++) {
            Attribute attribute = (Attribute) dataset.getHeader().get(i);

            if (attribute.getType() instanceof QuasiIdentifier) {
                QuasiIdentifier qiAttribute = (QuasiIdentifier) attribute.getType();

                switch (qiAttribute.type) {
                    case QuasiIdentifier.TYPE_PLACE:
                        upperBounds.add(generalizationTree.getHeight());
                        break;
                    case QuasiIdentifier.TYPE_NUMERIC:
                        int maxValue = getMaxAttributNumber(dataset.getColumns().get(i));
                        int heightHierarchy = 0;

                        int tmpMax = maxValue;
                        while (tmpMax > 0) {
                            heightHierarchy++;
                            tmpMax = tmpMax/10;
                        }

                        upperBounds.add(heightHierarchy);
                        break;
                    case QuasiIdentifier.TYPE_DATE:
                        upperBounds.add(2);
                        break;
                    case QuasiIdentifier.TYPE_STRING:
                        upperBounds.add(getMaxAttributeStringLenght(dataset.getColumns().get(i)));
                        break;
                    default:
                        break;
                }
            }
        }

        return upperBounds;
    }

    private DatasetColumn anonymizeColumn (int indexColumn, int levelOfAnonymization) throws LevelNotValidException {
        DatasetColumn datasetColumn = new DatasetColumn();

        Attribute firstAttribute = (Attribute)dataset.getColumns().get(indexColumn).get(0);
        if (firstAttribute.getType() instanceof Identifier) {
            for (int i = 0; i < dataset.getColumns().size(); i++) {
                Attribute newAttribute = new Attribute(firstAttribute.getName(), "*****");
                newAttribute.setType(firstAttribute.getType());
                datasetColumn.add(newAttribute);
            }
        }

        else {
            for (Object attributeObj : dataset.getColumns().get(indexColumn)) {
                Attribute attribute = (Attribute) attributeObj;
                Object valueAnonymized = null;

                Object valueToGeneralize = attribute.getValue();
                if (valueToGeneralize == null) {
                    valueToGeneralize = quasiIdentifierMedianMap.get(indexColumn);
                }

                switch (((QuasiIdentifier)attribute.getType()).type) {
                    case QuasiIdentifier.TYPE_PLACE:
                        valueAnonymized = placeGeneralization.generalize(levelOfAnonymization, valueToGeneralize);
                        break;
                    case QuasiIdentifier.TYPE_NUMERIC:
                        valueAnonymized = numericGeneralization.generalize(levelOfAnonymization, valueToGeneralize);
                        break;
                    case QuasiIdentifier.TYPE_DATE:
                        valueAnonymized = dateGeneralization.generalize(levelOfAnonymization, valueToGeneralize);
                        break;
                    case QuasiIdentifier.TYPE_STRING:
                        valueAnonymized = stringGeneralization.generalize(levelOfAnonymization, valueToGeneralize);
                        break;
                    default:
                        break;
                }

                Attribute newAttribute = new Attribute(attribute.getName(), valueAnonymized);
                newAttribute.setType(attribute.getType());
                datasetColumn.add(newAttribute);
            }
        }

        return datasetColumn;
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

    private static int getMaxAttributNumber(DatasetColumn column) {
        int maxNumber = 0;

        for (Object attributeObj : column) {
            Attribute attribute = (Attribute) attributeObj;
            if (attribute.getValue() != null) {
                Integer number = (Integer) attribute.getValue();

                if (Math.abs(number) > maxNumber) {
                    maxNumber = Math.abs(number);
                }
            }
        }

        return maxNumber;
    }

    private static int getMaxAttributeStringLenght(DatasetColumn columns) {
        int maxLenght = 0;

        for (Object attributeObj : columns) {
            Attribute attribute = (Attribute) attributeObj;

            if (attribute.getValue() != null) {
                String value = (String) attribute.getValue();

                if (value.length() > maxLenght) {
                    maxLenght = value.length();
                }
            }
        }


        return maxLenght;
    }

    private Object findMedian (DatasetColumn column) {
        Object median = null;
        ArrayList<Object> arrayOfNotNull = new ArrayList<Object>();

        for (Object attributeObj : column) {
            Attribute attribute = (Attribute) attributeObj;

            if (attribute.getValue() != null) {
                arrayOfNotNull.add(attribute.getValue());
            }
        }

        if (!arrayOfNotNull.isEmpty()) {
            median = arrayOfNotNull.get(arrayOfNotNull.size()/2);
        } else {
            QuasiIdentifier qiAttribute = (QuasiIdentifier) ((Attribute)column.get(0)).getType();

            if (qiAttribute.type == QuasiIdentifier.TYPE_NUMERIC) {
                median = 0;
            } else {
                median = "*****";
            }
        }

        return median;
    }

    private Dataset datasetReduction (Dataset dataset) {
        DatasetRow newHeader = new DatasetRow();
        ArrayList<DatasetColumn> newColumns = new ArrayList<DatasetColumn>();

        int indexIdentifier = 0;
        for (int i = 0; i < dataset.getColumns().size(); i++) {
            Attribute attribute = (Attribute) dataset.getColumns().get(i).get(0);
            if (attribute.getType() instanceof QuasiIdentifier) {
                if (upperBounds.get(indexIdentifier) > lowerBounds.get(indexIdentifier)) {
                    newHeader.add(dataset.getHeader().get(i));
                    newColumns.add(dataset.getColumns().get(i));
                }
                indexIdentifier++;
            }
        }

        Dataset newDataset = new Dataset(newHeader, newColumns);

        return newDataset;
    }
}
