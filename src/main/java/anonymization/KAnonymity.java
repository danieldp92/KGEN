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
import dataset.type.AttributeType;
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
    private LinkedHashMap<Integer, Integer> kAnonymizedHistoryMap;

    public KAnonymity(Dataset dataset) {
        this.placeGeneralization = new PlaceGeneralization();
        this.dateGeneralization = new DateGeneralization();
        this.numericGeneralization = new NumericGeneralization();

        this.dataset = dataset;

        try {
            initQuasiIdentifierIndex(this.dataset);
            initMedianMap();
            initAnonymizationMap();
            initGeneralizationMap(this.dataset);
        } catch (LevelNotValidException e) {}

        this.lowerBounds = lowerBounds();
        this.upperBounds = upperBounds();

        this.kAnonymizedHistoryMap = new LinkedHashMap<>();
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
                this.quasiIdentifierMedianMap.put(i, DatasetUtils.findMedian(dataset.getColumns().get(i)));
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

    //GET & SET
    public LinkedHashMap<Integer, Integer> getkAnonymizedHistoryMap() {
        return kAnonymizedHistoryMap;
    }


    //ANONYMIZATION METHODS
    /**
     * Run k-anonymity test with the level of generalizion of attributes.
     * @param levelOfAnonymization: the level of anonymization of all attributes of a given dataset
     * @return: the level of k-anonymization of dataset. 1 if it's not anonymized
     */
    public int kAnonymityTest (ArrayList<Integer> levelOfAnonymization) {
        int hash = levelOfAnonymization.toString().hashCode();

        Integer kValue = kAnonymizedHistoryMap.get(hash);
        if (kValue != null) {
            return kValue;
        }

        ArrayList<Integer> rowOccurrences = getRowsOccurrences(levelOfAnonymization);
        int kLevel = Collections.min(rowOccurrences);

        kAnonymizedHistoryMap.put(hash, kLevel);

        return kLevel;
    }

    public boolean kAnonymityTest (ArrayList<Integer> levelOfAnonymization, int kLevel) {
        if (kLevel == 1) {
            return true;
        }

        int kAnonLevel = kAnonymityTest(levelOfAnonymization);

        if (kAnonLevel <= kLevel) {
            return true;
        }

        return false;
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

    private ArrayList<Integer> getRowsOccurrences (ArrayList<Integer> levelOfAnonymization) {
        //SupportMaps
        //index -> qi attribute
        //supportMap -> supportmap of a log given by levelOfAnonymization variable
        List<SupportMap> supportMaps = new ArrayList<SupportMap>();
        for (int i = 0; i < levelOfAnonymization.size(); i++) {
            int qiIndex = this.quasiIdentifierIndex.get(i);
            int log = levelOfAnonymization.get(i);

            //Take a supportMap of a given LOG of qiIndex-attribute
            //Example: take the fisrt LOG of the third qi
            SupportMap tmpLOGSupportMap = generalizationMap.get(qiIndex).get(log);
            supportMaps.add(tmpLOGSupportMap);
        }

        //System.out.println("Get support maps time: " + (System.currentTimeMillis() - start));

        ArrayList<Integer> numberOfOccurrences = new ArrayList<>();
        for (int i = 0; i < dataset.getDatasetSize(); i++) {
            numberOfOccurrences.add(-1);
        }

        for (int i = 0; i < dataset.getDatasetSize(); i++) {
            if (numberOfOccurrences.get(i) < 0) {
                //start = System.currentTimeMillis();
                // List of all rows that shared with i-row the same attribute (Collection<Integer>), for all the attributes
                List<HashSet<Integer>> occurrences = new ArrayList<>();

                for (SupportMap qiSupportMap : supportMaps) {
                    //Search the i-row inside this specific attribute, and see which is the value that contain this row
                    for (Map.Entry<String, Collection<Integer>> entry : qiSupportMap.entrySet()) {
                        if (entry.getValue().contains(i)) {
                            occurrences.add(new HashSet<>(entry.getValue()));
                            break;
                        }
                    }
                }

                //System.out.println("Time to find the occurrences: " + (System.currentTimeMillis() - start));
                //start = System.currentTimeMillis();

                HashSet<Integer> commonRows = new HashSet<>(occurrences.get(0));
                for (int j = 1; j < occurrences.size(); j++) {
                    commonRows.retainAll(occurrences.get(j));
                }

                //System.out.println("Retain Time: " + (System.currentTimeMillis() - start));

                //Add the number of occurrences for aall rows in commonrows
                for (int indexRow : commonRows) {
                    numberOfOccurrences.set(indexRow, commonRows.size());
                }
                //numberOfOccurrences.add(commonRows.size());
            }
        }

        return numberOfOccurrences;
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
                        int maxValue = DatasetUtils.getMaxAttributNumber(dataset.getColumns().get(i));
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
                        upperBounds.add(DatasetUtils.getMaxAttributeStringLenght(dataset.getColumns().get(i)));
                        break;
                    default:
                        break;
                }
            }
        }

        return upperBounds;
    }



    private DatasetColumn anonymizeColumn (int indexColumn, int levelOfAnonymization) throws LevelNotValidException {
        DatasetColumn column = dataset.getColumns().get(indexColumn);
        if (((Attribute)column.get(0)).getType().type == AttributeType.TYPE_STRING) {
            this.stringGeneralization = new StringGeneralization(DatasetUtils.minLength(column), DatasetUtils.maxLength(column));
        }

        DatasetColumn datasetColumn = new DatasetColumn();

        Attribute firstAttribute = (Attribute)column.get(0);
        if (firstAttribute.getType() instanceof Identifier) {
            for (int i = 0; i < dataset.getColumns().size(); i++) {
                Attribute newAttribute = new Attribute(firstAttribute.getName(), "*****");
                newAttribute.setType(firstAttribute.getType());
                datasetColumn.add(newAttribute);
            }
        }

        else {
            for (Object attributeObj : column) {
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

}
