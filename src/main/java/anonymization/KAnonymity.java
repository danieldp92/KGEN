package anonymization;

import anonymization.generalization.exception.LevelNotValidException;
import anonymization.generalization.generator.GeneralizationGraphGenerator;
import anonymization.generalization.graph.GeneralizationTree;
import anonymization.generalization.type.DateGeneralization;
import anonymization.generalization.type.NumericGeneralization;
import anonymization.generalization.type.PlaceGeneralization;
import anonymization.generalization.type.StringGeneralization;
import anonymization.multithread.MultiThreadAnonymization;
import anonymization.support.GeneralizationMap;
import anonymization.support.LOGMap;
import anonymization.support.SupportMap;
import dataset.beans.Attribute;
import dataset.beans.Dataset;
import dataset.beans.DatasetColumn;
import dataset.beans.DatasetRow;
import dataset.type.Identifier;
import dataset.type.QuasiIdentifier;
import utils.DatasetUtils;

import java.util.*;

public class KAnonymity {
    private static final int MIN_K_LEVEL = 2;

    private PlaceGeneralization placeGeneralization;
    private DateGeneralization dateGeneralization;
    private NumericGeneralization numericGeneralization;
    private StringGeneralization stringGeneralization;

    private Dataset dataset;
    private ArrayList<Integer> lowerBounds;
    private ArrayList<Integer> upperBounds;


    private List<Integer> quasiIdentifierIndex;
    private GeneralizationMap generalizationMap;
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

        initQuasiIdentifierIndex(this.dataset);
        initMedianMap();

        try {
            initAnonymizationMap();
            initGeneralizationMap(this.dataset);
        } catch (LevelNotValidException e) {}

        this.lowerBounds = lowerBounds();
        this.upperBounds = upperBounds();
        this.kAnonymizedHistoryMap = new LinkedHashMap<String, Integer>();
    }

    //INIT
    private void initQuasiIdentifierIndex (Dataset dataset) {
        this.quasiIdentifierIndex = new ArrayList<Integer>();

        DatasetRow header = dataset.getHeader();

        for (int i = 0; i < header.size(); i++) {
            Attribute attribute = (Attribute) header.get(i);
            if (attribute.getType() instanceof QuasiIdentifier) {
                this.quasiIdentifierIndex.add(i);
            }
        }
    }

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

    /**
     * Generate generalization map
     * Example
     * Quasi Id Attribute Index | LOG Map
     * 0                          LOG | Attribute that are differents
     *                            0     Attribute Value | List<Integer> rows
     *                                  01/02/2018        <1, 5, 23, 246, 1577>
     *                                  01/02/2018        <4, 6, 21, 300>
     * @param dataset
     */
    private void initGeneralizationMap (Dataset dataset) {
        DatasetRow header = dataset.getHeader();

        //GeneralizationMap
        this.generalizationMap = new GeneralizationMap();
        for (int indexHeader = 0; indexHeader < header.size(); indexHeader++) {
            Attribute headerAttribute = (Attribute) header.get(indexHeader);

            if (headerAttribute.getType() instanceof QuasiIdentifier) {
                ArrayList<DatasetColumn> anonymizedColumns = anonymizationMap.get(indexHeader);

                //LOGMap
                LOGMap tmpLOGMap = new LOGMap();
                for (int indexLOG = 0; indexLOG < anonymizedColumns.size(); indexLOG++) {
                    DatasetColumn anonymizedColumn = anonymizedColumns.get(indexLOG);

                    //Get all different values
                    Set<String> attributeValues = new HashSet<String>();
                    for (Object attributeObj : anonymizedColumn) {
                        Attribute attribute = (Attribute) attributeObj;
                        String value = null;
                        if (attribute.getValue() instanceof String) {
                            value = (String) attribute.getValue();
                        } else {
                            value = String.valueOf(attribute.getValue());

                        }
                        attributeValues.add(value);
                    }

                    //SupportMap
                    SupportMap tmpSupportMap = new SupportMap();
                    for (String attributeValue : attributeValues) {
                        //For each value, check in which rows it's available
                        Collection<Integer> rows = new ArrayList<Integer>();
                        for (int i = 0; i < anonymizedColumn.size(); i++) {
                            Attribute attribute = (Attribute) anonymizedColumn.get(i);
                            if ((attributeValue == null && attribute.getValue() == null) ||
                                    ((attributeValue != null && attribute.getValue() != null && attributeValue.equals(attribute.getValue())))) {
                                rows.add(i);
                            }
                        }

                        tmpSupportMap.put(attributeValue, rows);
                    }

                    tmpLOGMap.put(indexLOG, tmpSupportMap);
                }

                this.generalizationMap.put(indexHeader, tmpLOGMap);
            }
        }
    }


    //ANONYMIZATION METHODS
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

        if (!kAnonymityTest(levelOfAnonymization, 2)) {
            kAnonymizedHistoryMap.put(key, 1);
            return 1;
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

    public boolean kAnonymityTest (ArrayList<Integer> levelOfAnonymization, int kLevel) {
        if (kLevel == 1) {
            return true;
        }

        boolean kAnonymized = true;

        String key = "";
        for (int index : levelOfAnonymization) {
            key += index + "-";
        }

        Integer value = this.kAnonymizedHistoryMap.get(key);

        if (value == null) {
            int minNumberOfRows = dataset.getDatasetSize() + 1;

            //SupportMaps
            //index -> qi attribute
            //supportMap -> supportmap of a log given by levelOfAnonymization variable
            List<SupportMap> supportMaps = new ArrayList<SupportMap>();
            for (int i = 0; i < levelOfAnonymization.size(); i++) {
                int qiIndex = this.quasiIdentifierIndex.get(i);
                int log = levelOfAnonymization.get(i);

                //Take a supportMap of a given LOG of qiIndex-attribute
                SupportMap tmpLOGSupportMap = generalizationMap.get(qiIndex).get(log);
                supportMaps.add(tmpLOGSupportMap);
            }

            List<Integer> maxVector = new ArrayList<Integer>();
            for (int i = 0; i < supportMaps.size(); i++) {
                maxVector.add(supportMaps.get(i).size()-1);
            }

            List<List<Integer>> allCombinations = getAllCombinations(maxVector);

            for (List<Integer> combination : allCombinations) {
                List<Collection<Integer>> rowsOfEveryLOG = new ArrayList<Collection<Integer>>();
                for (int i = 0; i < combination.size(); i++) {
                    rowsOfEveryLOG.add(supportMaps.get(i).getRows(combination.get(i)));
                }

                for (int i = 1; i < rowsOfEveryLOG.size(); i++) {
                    rowsOfEveryLOG.get(0).retainAll(rowsOfEveryLOG.get(i));
                }

                if (rowsOfEveryLOG.get(0).size() > 0 && rowsOfEveryLOG.get(0).size() < minNumberOfRows) {
                    minNumberOfRows = rowsOfEveryLOG.get(0).size();
                }
            }

            if (minNumberOfRows == dataset.getDatasetSize() + 1) {
                minNumberOfRows = 1;
            }

            if (minNumberOfRows < kLevel) {
                kAnonymized = false;
            }

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
                    case QuasiIdentifier.TYPE_INT:
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
                    case QuasiIdentifier.TYPE_INT:
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

            if (qiAttribute.type == QuasiIdentifier.TYPE_INT) {
                median = 0;
            } else {
                median = "*****";
            }
        }

        return median;
    }

    private List<List<Integer>> getAllCombinations (List<Integer> maxVector) {
        List<List<Integer>> allCombinations = new ArrayList<List<Integer>>();

        int actualPos = maxVector.size()-1;

        //Inizialize combination
        List<Integer> combination = new ArrayList<Integer>();
        for (int i = 0; i < maxVector.size(); i++) {
            combination.add(0);
        }

        allCombinations.add(new ArrayList<Integer>(combination));

        while (actualPos >= 0) {
            actualPos = maxVector.size()-1;
            while (actualPos >= 0 && combination.get(actualPos) == maxVector.get(actualPos)) {
                combination.set(actualPos, 0);
                actualPos--;
            }

            if (actualPos >= 0) {
                combination.set(actualPos, combination.get(actualPos)+1);
                allCombinations.add(new ArrayList<Integer>(combination));
            }
        }

        return allCombinations;
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

    /**
     * Run k-anonymity test with the level of generalizion of attributes.
     * Check if the generated dataset is kLevel-anonymized
     * @param levelOfAnonymization: the level of anonymization of all attributes of a given dataset
     * @param kLevel: the level of k-anonymity
     * @return: true, if the dataset is k-anonymized, false otherwise
     */
    /*public boolean kAnonymityTest (ArrayList<Integer> levelOfAnonymization, int kLevel) {
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
    }*/
}
