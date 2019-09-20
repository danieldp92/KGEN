package anonymization;

import anonymization.exceptions.NoLowerBoundFound;
import anonymization.generalization.exception.LevelNotValidException;
import anonymization.generalization.generator.GeneralizationGraphGenerator;
import anonymization.generalization.graph.GeneralizationTree;
import anonymization.generalization.type.DateGeneralization;
import anonymization.generalization.type.NumericGeneralization;
import anonymization.generalization.type.PlaceGeneralization;
import anonymization.generalization.type.StringGeneralization;
import anonymization.generalization.utils.TreeUtils;
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
import jmetal.encodings.variable.Int;
import utils.DatasetUtils;
import utils.FileUtils;

import java.io.*;
import java.util.*;

public class KAnonymity {
    public static final int MIN_K_LEVEL = 2;

    private Dataset dataset;
    private List<String> placeCsv;
    public ArrayList<Integer> lowerBounds;
    public ArrayList<Integer> upperBounds;

    private PlaceGeneralization placeGeneralization;
    private DateGeneralization dateGeneralization;
    private NumericGeneralization numericGeneralization;

    private LinkedHashMap<DatasetColumn, Object> quasiIdentifierMedianMap;
    private List<Integer> quasiIdentifierIndex;
    private LinkedHashMap<Integer, ArrayList<DatasetColumn>> anonymizationMap;
    private GeneralizationMap generalizationMap;

    //AGGIUNTA. DA TESTARE
    private LinkedHashMap<Integer, AnonymizationReport> historyReports;

    public KAnonymity(Dataset dataset) {
        this.dataset = dataset;
        try {
            this.placeCsv = FileUtils.loadFile(this.getClass().getClassLoader().getResourceAsStream("netherland_place_info.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.placeGeneralization = new PlaceGeneralization(this.placeCsv);
        this.dateGeneralization = new DateGeneralization();
        this.numericGeneralization = new NumericGeneralization();

        this.historyReports = new LinkedHashMap<>();

        // Init
        try {
            initMedianMap();
            initQuasiIdentifierIndex(this.dataset);
            initAnonymizationMap();
            initGeneralizationMap(this.dataset);
        } catch (LevelNotValidException e) {}
    }

    // INIT ####################################################################################
    private void initMedianMap () {
        this.quasiIdentifierMedianMap = new LinkedHashMap<DatasetColumn, Object>();

        for (int i = 0; i < dataset.getHeader().size(); i++) {
            Attribute attribute = (Attribute) dataset.getHeader().get(i);

            if (attribute.getType() instanceof QuasiIdentifier) {
                this.quasiIdentifierMedianMap.put(dataset.getColumns().get(i), DatasetUtils.findMedian(dataset.getColumns().get(i)));
            }
        }
    }

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

    /**
     * Generate the anonymization map. For each attribute, it will be generate all possible
     * anonymized columns, from 0 to their upper bounds
     * @throws LevelNotValidException
     */
    private void initAnonymizationMap () throws LevelNotValidException {
        this.anonymizationMap = new LinkedHashMap<Integer, ArrayList<DatasetColumn>>();

        DatasetRow header = dataset.getHeader();
        ArrayList<Integer> upperBounds = upperBounds();
        int indexQuasiIdentifier = 0;

        for (int i = 0; i < header.size(); i++) {
            Attribute attribute = (Attribute) header.get(i);
            DatasetColumn column = dataset.getColumns().get(i);
            ArrayList<DatasetColumn> allGeneralizationColumns = new ArrayList<DatasetColumn>();

            if (attribute.getType() instanceof QuasiIdentifier) {
                for (int j = 0; j <= upperBounds.get(indexQuasiIdentifier); j++) {
                    DatasetColumn newDatasetColumn = anonymizeColumn(column, j);
                    allGeneralizationColumns.add(newDatasetColumn);
                }

                indexQuasiIdentifier++;
            } else {
                DatasetColumn identifiersAnonymized = anonymizeColumn(column, 1);
                allGeneralizationColumns.add(identifiersAnonymized);
            }

            this.anonymizationMap.put(i, allGeneralizationColumns);
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



    // GET & SET ###############################################################################
    public LinkedHashMap<Integer, AnonymizationReport> getHistoryReports() {
        return historyReports;
    }

    public void cleanHistoryMap () {
        this.historyReports.clear();
    }



    // ANONYMIZATION METHODS ####################################################################
    /**
     * Run k-anonymity test with the level of generalizion of attributes.
     * @param levelOfAnonymization, the level of anonymization of all attributes of a given dataset
     * @param k, the kValue of the k-Anonimity algorithm
     * @return a report, containing all informations about a solution. See the class AnonymizationReport for more details
     * @throws Exception
     */
    public AnonymizationReport runKAnonymity (ArrayList<Integer> levelOfAnonymization, int k) throws Exception {
        boolean memorize = true;

        int hash = levelOfAnonymization.toString().hashCode();

        AnonymizationReport report = historyReports.get(hash);
        if (report != null) {
            return report;
        }


        //Variables report
        report = new AnonymizationReport();
        List<Integer> levelOfAnonymizationReport = new ArrayList<>(levelOfAnonymization);

        double logMetricReport = -1;
        try {
            logMetricReport = getLOG(levelOfAnonymization);
        } catch (NoLowerBoundFound ex) {
            memorize = false;
        }

        int kValueReport = 1;
        int kValueWithSuppressionReport = 1;
        double percentageOfSuppressionReport = 0;
        List<Integer> rowsToDeleteReport = new ArrayList<>();


        // if k == 1, it's not necessary to start kAnonymity algorithm, because the anonymous dataset is equals to the original one
        if (k > 1) {
            ArrayList<HashSet<Integer>> rowOccurrences = getRowsOccurrences(levelOfAnonymization);

            if (rowOccurrences.isEmpty()) {
                throw new Exception("Occurrences array is empty. Please, fix it");
            }

            // The min value of these occurrences is the k-value of the k-anonymization algorithm
            kValueReport = Integer.MAX_VALUE;
            for (HashSet<Integer> rowOccurrency : rowOccurrences) {
                if (rowOccurrency.size() < kValueReport) {
                    kValueReport = rowOccurrency.size();
                }
            }
            kValueWithSuppressionReport = kValueReport;


            // If kValue is lower than min k value, then apply suppression (if suppression is true)
            if (kValueReport < k) {
                HashSet<Integer> tmpRowsToDelete = new HashSet<>();

                //Set to null all rows with a number of equivalent rows less than k
                for (int i = 0; i < rowOccurrences.size(); i++) {
                    HashSet<Integer> rowOccurrency = rowOccurrences.get(i);
                    if (rowOccurrency != null && rowOccurrency.size() < k) {
                        for (int rowIndex : rowOccurrency) {
                            rowOccurrences.set(rowIndex, null);
                            tmpRowsToDelete.add(rowIndex);
                        }
                    }
                }

                rowsToDeleteReport = new ArrayList<>(tmpRowsToDelete);

                int numberOfRowsToDelete = rowsToDeleteReport.size();
                percentageOfSuppressionReport = ((double)numberOfRowsToDelete) / rowOccurrences.size();

                // Find kValueWithSuppression
                kValueWithSuppressionReport = Integer.MAX_VALUE;
                for (HashSet<Integer> rowOccurrency : rowOccurrences) {
                    if (rowOccurrency != null && rowOccurrency.size() < kValueWithSuppressionReport) {
                        kValueWithSuppressionReport = rowOccurrency.size();
                    }
                }
            }
        }


        report.setLevelOfAnonymization(levelOfAnonymizationReport);
        report.setLogMetric(logMetricReport);
        report.setkValue(kValueReport);
        report.setkValueWithSuppression(kValueWithSuppressionReport);
        report.setPercentageOfSuppression(percentageOfSuppressionReport);
        report.setRowToDelete(rowsToDeleteReport);


        // Add the report of this solution to the history
        if (memorize)
            this.historyReports.put(hash, report);

        return report;
    }

    public boolean isKAnonymous (ArrayList<Integer> levelOfAnonymization, int k, double suppressionThreshold) {
        AnonymizationReport report = null;
        try {
            report = runKAnonymity(levelOfAnonymization, k);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean isKAnonymous = false;

        if (report.getkValueWithSuppression() >= k && report.getPercentageOfSuppression() <= suppressionThreshold) {
            isKAnonymous = true;
        }

        return isKAnonymous;
    }



    // ANONYMIZATION BOUNDS ####################################################################
    public ArrayList<Integer> lowerBounds (double suppressionThreshold) {
        if (this.lowerBounds != null) {
            return this.lowerBounds;
        }

        if (this.upperBounds == null) {
            this.upperBounds = upperBounds();
        }

        ArrayList<Integer> lowerBounds = new ArrayList<Integer>();

        boolean isAnonymized = false;
        for (int i = 0; i < upperBounds.size(); i++) {
            ArrayList<Integer> tmpUB = new ArrayList<>(upperBounds);
            int maxLOGValueOfI = upperBounds.get(i);
            for (int j = 0; j <= maxLOGValueOfI; j++) {
                tmpUB.set(i, j);
                isAnonymized = isKAnonymous(tmpUB, MIN_K_LEVEL, suppressionThreshold);

                if (isAnonymized) {
                    lowerBounds.add(j);
                    break;
                }
            }
        }

        this.lowerBounds = lowerBounds;
        /*for (int i = 0; i < dataset.getHeader().size(); i++) {
            Attribute headerAttribute = (Attribute) dataset.getHeader().get(i);

            if (headerAttribute.getType() instanceof QuasiIdentifier) {
                ArrayList<DatasetColumn> anonymizationColumnsOfAttribute = this.anonymizationMap.get(i);

                for (int j = 0; j < anonymizationColumnsOfAttribute.size(); j++) {
                    DatasetRow header = new DatasetRow();
                    header.add(headerAttribute);

                    ArrayList<DatasetColumn> columns = new ArrayList<DatasetColumn>();
                    columns.add(anonymizationColumnsOfAttribute.get(j));

                    Dataset tmpDataset = new Dataset(header, columns);
                    isAnonymized = runKAnonymityOnDataset(tmpDataset, MIN_K_LEVEL);

                    if (isAnonymized) {
                        lowerBounds.add(j);
                        break;
                    }
                }
            }
        }*/

        return lowerBounds;
    }

    public ArrayList<Integer> upperBounds () {
        if (this.upperBounds != null) {
            return this.upperBounds;
        }

        ArrayList<Integer> upperBounds = new ArrayList<Integer>();

        GeneralizationTree generalizationTree = null;
        if (placeGeneralization != null) {
            generalizationTree = placeGeneralization.getPlaceHierarchy();
        } else {
            generalizationTree = GeneralizationGraphGenerator.generatePlaceHierarchy(this.placeCsv);
        }

        //Max level of anonymity of every attribute
        for (int i = 0; i < dataset.getHeader().size(); i++) {
            Attribute attribute = (Attribute) dataset.getHeader().get(i);
            if (attribute.getType() instanceof QuasiIdentifier) {
                QuasiIdentifier qiAttribute = (QuasiIdentifier) attribute.getType();

                int heightHierarchy = 0;
                switch (qiAttribute.type) {
                    case QuasiIdentifier.TYPE_PLACE:
                        upperBounds.add(generalizationTree.getHeight());
                        break;
                    case QuasiIdentifier.TYPE_INT:
                        int maxValue = (int) DatasetUtils.getMaxAttributINT(dataset.getColumns().get(i));

                        int tmpIntMax = maxValue;
                        while (tmpIntMax > 0) {
                            heightHierarchy++;
                            tmpIntMax = tmpIntMax/10;
                        }

                        upperBounds.add(heightHierarchy);
                        break;
                    case QuasiIdentifier.TYPE_DATE:
                        upperBounds.add(2);
                        break;
                    case QuasiIdentifier.TYPE_STRING:
                        upperBounds.add(DatasetUtils.getMaxAttributeStringLenght(dataset.getColumns().get(i)));
                        break;
                    case QuasiIdentifier.TYPE_DOUBLE:
                        double maxDoubleValue = DatasetUtils.getMaxAttributDOUBLE(dataset.getColumns().get(i));

                        heightHierarchy++;
                        int maxIntValue = (int) maxDoubleValue;

                        int tmpMax = maxIntValue;
                        while (tmpMax > 0) {
                            heightHierarchy++;
                            tmpMax = tmpMax/10;
                        }

                        upperBounds.add(heightHierarchy);
                    default:
                        break;
                }
            }
        }

        return upperBounds;
    }



    // PRIVATE METHODS #########################################################################
    private ArrayList<HashSet<Integer>> getRowsOccurrences (ArrayList<Integer> levelOfAnonymization) {
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
        ArrayList<HashSet<Integer>> totalCommonRows = new ArrayList<>();
        for (int i = 0; i < dataset.getDatasetSize(); i++) {
            numberOfOccurrences.add(-1);
            totalCommonRows.add(null);
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
                    totalCommonRows.set(indexRow, commonRows);
                }
                //numberOfOccurrences.add(commonRows.size());
            }
        }

        return totalCommonRows;
    }

    private DatasetColumn anonymizeColumn (DatasetColumn column, int levelOfAnonymization) throws LevelNotValidException {
        DatasetColumn datasetColumn = new DatasetColumn();

        Attribute firstAttribute = (Attribute)column.get(0);

        // Identifiers anonymization
        if (firstAttribute.getType() instanceof Identifier) {
            for (int i = 0; i < column.size(); i++) {
                Attribute newAttribute = new Attribute(firstAttribute.getName(), "*****");
                newAttribute.setType(firstAttribute.getType());
                datasetColumn.add(newAttribute);
            }
        }

        // Quasi Identifiers anonymization
        else {
            for (Object attributeObj : column) {
                Attribute attribute = (Attribute) attributeObj;
                Object valueAnonymized = null;

                Object valueToGeneralize = attribute.getValue();
                if (valueToGeneralize == null) {
                    valueToGeneralize = quasiIdentifierMedianMap.get(column);
                }

                switch (((QuasiIdentifier)attribute.getType()).type) {
                    case QuasiIdentifier.TYPE_PLACE:
                        valueAnonymized = placeGeneralization.generalize(levelOfAnonymization, valueToGeneralize);
                        break;
                    case QuasiIdentifier.TYPE_INT:
                        valueAnonymized = numericGeneralization.generalize(levelOfAnonymization, valueToGeneralize);
                        break;
                    case QuasiIdentifier.TYPE_DOUBLE:
                        valueAnonymized = numericGeneralization.generalize(levelOfAnonymization, valueToGeneralize);
                        break;
                    case QuasiIdentifier.TYPE_DATE:
                        valueAnonymized = dateGeneralization.generalize(levelOfAnonymization, valueToGeneralize);
                        break;
                    case QuasiIdentifier.TYPE_STRING:
                        StringGeneralization stringGeneralization = new StringGeneralization(DatasetUtils.minLength(column),
                                DatasetUtils.getMaxAttributeStringLenght(column));

                        try {
                            valueAnonymized = stringGeneralization.generalize(levelOfAnonymization, valueToGeneralize);
                        } catch (StringIndexOutOfBoundsException ex) {
                            DatasetColumn column1 = column;
                            System.out.println("MIN: " + DatasetUtils.minLength(column));
                            System.out.println("MAX: " + DatasetUtils.getMaxAttributeStringLenght(column));
                            System.out.println("LEV: " + levelOfAnonymization);
                            System.out.println("Value to generalize: " + valueToGeneralize);
                            System.out.println();
                        }
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

    private double getLOG (ArrayList<Integer> levelOfAnonymization) throws NoLowerBoundFound {
        if (lowerBounds == null) {
            throw new NoLowerBoundFound();
        }

        double sum = 0;
        int maxVariable = 0;

        for (int i = 0; i < levelOfAnonymization.size(); i++) {
            if (this.upperBounds.get(i) != this.lowerBounds.get(i)) {
                double value = levelOfAnonymization.get(i);
                sum += ((value - this.lowerBounds.get(i)) / (this.upperBounds.get(i) - this.lowerBounds.get(i)));
                maxVariable++;
            }
        }

        return sum/maxVariable;
    }

    /**
     * Run k-anonymity test on a given dataset.
     * @param dataset: the dataset to check
     * @param kLevel: the level of k-anonymity
     * @return: true, if the dataset is k-anonymized, false otherwise
     */
    private boolean runKAnonymityOnDataset (Dataset dataset, int kLevel) {
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
                    boolean equals = DatasetUtils.equalsRows(dataset, i, j);

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

    private void printLOGAttributes() {
        System.out.println("LOG\n");
        for (Map.Entry<Integer, LOGMap> entry : this.generalizationMap.entrySet()) {
            System.out.println("Name: " + ((Attribute)this.dataset.getHeader().get(entry.getKey())).getName());
            System.out.println("LOG size: " + (entry.getValue().size()-1));
            System.out.println();
        }
    }
}
