package anonymization;

import anonymization.generalization.exception.LevelNotValidException;
import anonymization.generalization.generator.GeneralizationGraphGenerator;
import anonymization.generalization.graph.GeneralizationTree;
import anonymization.generalization.type.DateGeneralization;
import anonymization.generalization.type.NumericGeneralization;
import anonymization.generalization.type.PlaceGeneralization;
import anonymization.generalization.type.StringGeneralization;
import dataset.Attribute;
import dataset.Dataset;
import dataset.DatasetColumn;
import dataset.DatasetRow;
import dataset.type.QuasiIdentifier;
import geneticalgorithm.encoding.Chromosome;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DatasetAnonymization {
    private PlaceGeneralization placeGeneralization;
    private DateGeneralization dateGeneralization;
    private NumericGeneralization numericGeneralization;
    private StringGeneralization stringGeneralization;

    private Dataset dataset;
    private LinkedHashMap<Integer, ArrayList<DatasetColumn>> anonymizationMap;

    public DatasetAnonymization (Dataset dataset) {
        this.placeGeneralization = new PlaceGeneralization();
        this.dateGeneralization = new DateGeneralization();
        this.numericGeneralization = new NumericGeneralization();
        this.stringGeneralization = new StringGeneralization();

        this.dataset = dataset;
        try {
            initMap();
        } catch (LevelNotValidException e) {
            e.printStackTrace();
        }
    }

    private void initMap () throws LevelNotValidException {
        this.anonymizationMap = new LinkedHashMap<Integer, ArrayList<DatasetColumn>>();

        DatasetRow header = dataset.getHeader();
        ArrayList<Integer> upperBounds = upperBounds(dataset);
        int indexQuasiIdentifier = 0;

        for (int i = 0; i < header.size(); i++) {
            Attribute attribute = (Attribute) header.get(i);

            if (attribute.getType() instanceof QuasiIdentifier) {

                if (i == 17)
                    System.out.println();
                ArrayList<DatasetColumn> allGeneralizationColumns = new ArrayList<DatasetColumn>();
                for (int j = 0; j <= upperBounds.get(indexQuasiIdentifier); j++) {
                    allGeneralizationColumns.add(anonymizeColumn(i, j));
                }

                indexQuasiIdentifier++;

                this.anonymizationMap.put(i, allGeneralizationColumns);
            } else {
                DatasetColumn identifiersAnonymized = new DatasetColumn();
                for (int j = 0; j < dataset.getData().size(); j++) {
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

    private DatasetColumn anonymizeColumn (int indexColumn, int levelOfAnonymization) throws LevelNotValidException {
        DatasetColumn datasetColumn = new DatasetColumn();

        for (DatasetRow dataRow : dataset.getData()) {
            Attribute attribute = (Attribute) dataRow.get(indexColumn);
            Object valueAnonymized = null;

            if (attribute.getType() instanceof QuasiIdentifier) {
                QuasiIdentifier qiAttribute = (QuasiIdentifier) attribute.getType();

                switch (qiAttribute.type) {
                    case QuasiIdentifier.TYPE_PLACE:
                        valueAnonymized = placeGeneralization.generalize(levelOfAnonymization, attribute.getValue());
                        break;
                    case QuasiIdentifier.TYPE_NUMERIC:
                        valueAnonymized = numericGeneralization.generalize(levelOfAnonymization, attribute.getValue());
                        break;
                    case QuasiIdentifier.TYPE_DATE:
                        valueAnonymized = dateGeneralization.generalize(levelOfAnonymization, attribute.getValue());
                        break;
                    case QuasiIdentifier.TYPE_STRING:
                        valueAnonymized = stringGeneralization.generalize(levelOfAnonymization, attribute.getValue());
                        break;
                    default:
                        break;
                }
            } else {
                valueAnonymized = "*****";
            }

            Attribute attributeAnonymized = new Attribute(attribute.getName(), valueAnonymized);
            attributeAnonymized.setType(attribute.getType());
            datasetColumn.add(attributeAnonymized);
        }

        return datasetColumn;
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
                    Chromosome bounds = (Chromosome) levelOfAnonymization;
                    ArrayList<Integer> upperBounds = bounds.getUpperBounds();

                    System.out.println(ex.getMessage());
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

    /*public Dataset anonymize (ArrayList<Integer> levelOfAnonymization) throws LevelNotValidException {
        Dataset datasetAnonimyzed = null;

        DatasetRow header = dataset.getHeader();
        ArrayList<DatasetRow> dataAnonymized = new ArrayList<DatasetRow>();

        for (DatasetRow dataRow : dataset.getData()) {
            DatasetRow rowAnonymized = new DatasetRow();

            int indexIdentifier = 0;
            for (int i = 0; i < dataRow.size(); i++) {
                Attribute attribute = (Attribute) dataRow.get(i);
                Object valueAnonymized = null;

                if (attribute.getType() instanceof QuasiIdentifier) {
                    QuasiIdentifier qiAttribute = (QuasiIdentifier) attribute.getType();

                    switch (qiAttribute.type) {
                        case QuasiIdentifier.TYPE_PLACE:
                            valueAnonymized = placeGeneralization.generalize(levelOfAnonymization.get(indexIdentifier++), attribute.getValue());
                            break;
                        case QuasiIdentifier.TYPE_NUMERIC:
                            valueAnonymized = numericGeneralization.generalize(levelOfAnonymization.get(indexIdentifier++), attribute.getValue());
                            break;
                        case QuasiIdentifier.TYPE_DATE:
                            valueAnonymized = dateGeneralization.generalize(levelOfAnonymization.get(indexIdentifier++), attribute.getValue());
                            break;
                        case QuasiIdentifier.TYPE_STRING:
                            valueAnonymized = stringGeneralization.generalize(levelOfAnonymization.get(indexIdentifier++), attribute.getValue());
                            break;
                        default:
                            break;
                    }
                } else {
                    valueAnonymized = "*****";
                }

                Attribute attributeAnonymized = new Attribute(attribute.getName(), valueAnonymized);
                attributeAnonymized.setType(attribute.getType());
                rowAnonymized.add(attributeAnonymized);
            }
        }

        datasetAnonimyzed = new Dataset(header, dataAnonymized);

        return datasetAnonimyzed;
    }*/

    public boolean kAnonymityTest (Dataset dataset, int kLevel) {
        boolean kAnonymized = true;

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


        for (int i = 0; i < numberOfEqualsRows.length; i++) {
            if (numberOfEqualsRows[i] < kLevel) {
                kAnonymized = false;
                break;
            }
        }

        return kAnonymized;
    }

    private boolean equalsRows (Dataset dataset, int indexRow1, int indexRow2) {
        for (int i = 0; i < dataset.getColumns().size(); i++) {
            Attribute attribute1 = (Attribute) dataset.getColumns().get(i).get(indexRow1);
            Attribute attribute2 = (Attribute) dataset.getColumns().get(i).get(indexRow2);;

            if (attribute1.getValue() != null && attribute2.getValue() != null &&
                    !attribute1.getValue().equals(attribute2.getValue())) {
                return false;
            } else {
                if ((attribute1.getValue() == null || attribute2.getValue() == null) &&
                        !(attribute1.getValue() == null && attribute2.getValue() == null)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean equalsRows (DatasetRow row1, DatasetRow row2) {
        for (int i = 0; i < row1.size(); i++) {
            Attribute attribute1 = (Attribute) row1.get(i);
            Attribute attribute2 = (Attribute) row2.get(i);

            if (!attribute1.getValue().equals(attribute2.getValue())) {
                return false;
            }
        }

        return true;
    }

    private static ArrayList<Integer> upperBounds (Dataset dataset) {
        ArrayList<Integer> upperBounds = new ArrayList<Integer>();

        GeneralizationTree generalizationTree = GeneralizationGraphGenerator.generatePlaceHierarchy();

        //Max level of anonymity of every attribute
        for (Object attributeObj : dataset.getHeader()) {
            Attribute attribute = (Attribute) attributeObj;

            if (attribute.getType() instanceof QuasiIdentifier) {
                QuasiIdentifier qiAttribute = (QuasiIdentifier) attribute.getType();

                switch (qiAttribute.type) {
                    case QuasiIdentifier.TYPE_PLACE:
                        upperBounds.add(generalizationTree.getHeight());
                        break;
                    case QuasiIdentifier.TYPE_NUMERIC:
                        int maxValue = getMaxAttributNumber(dataset, attribute);
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
                        upperBounds.add(getMaxAttributeStringLenght(dataset, attribute));
                        break;
                    default:
                        break;
                }
            }
        }

        return upperBounds;
    }

    private static int getMaxAttributNumber(Dataset dataset, Attribute attribute) {
        //Detect attribute
        int i = 0;
        while (i < dataset.getHeader().size() &&
                !((Attribute)dataset.getHeader().get(i)).getName().equals(attribute.getName())) {
            i++;
        }

        int maxNumber = 0;

        if (i < dataset.getHeader().size()) {
            for (int j = 0; j < dataset.getData().size(); j++) {
                Attribute tmpAttribute = (Attribute)dataset.getData().get(j).get(i);
                if (tmpAttribute.getValue() != null) {
                    int value = (Integer)tmpAttribute.getValue();

                    if (Math.abs(value) > maxNumber) {
                        maxNumber = Math.abs(value);
                    }
                }
            }
        }


        return maxNumber;
    }

    private static int getMaxAttributeStringLenght(Dataset dataset, Attribute attribute) {
        //Detect attribute
        int i = 0;
        while (i < dataset.getHeader().size() &&
                !((Attribute)dataset.getHeader().get(i)).getName().equals(attribute.getName())) {
            i++;
        }

        int maxLenght = 0;

        if (i < dataset.getHeader().size()) {
            for (int j = 0; j < dataset.getData().size(); j++) {
                Attribute tmpAttribute = (Attribute)dataset.getData().get(j).get(i);
                String value = (String) tmpAttribute.getValue();
                if (value.length() > maxLenght) {
                    maxLenght = value.length();
                }
            }
        }


        return maxLenght;
    }
}
