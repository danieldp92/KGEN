package dataset.beans;

import java.util.ArrayList;

public class Dataset {
    private DatasetRow header;
    private ArrayList<DatasetRow> data;
    private ArrayList<DatasetColumn> columns;

    /*public Dataset (DatasetRow pHeader, ArrayList<DatasetRow> pData) {
        this.header = pHeader;
        this.data = pData;
    }*/

    public Dataset (DatasetRow pHeader, ArrayList<DatasetColumn> pColumns) {
        this.header = pHeader;
        this.columns = pColumns;
    }

    public DatasetRow getHeader() {
        return header;
    }

    public Attribute getHeaderAttribute (String attributeName) {
        Attribute headerAttribute = null;

        int index = 0;
        while (index < header.size() && !((Attribute)header.get(index)).getName().equals(attributeName)) {
            index++;
        }

        if (index < header.size()) {
            headerAttribute = (Attribute) header.get(index);
        }

        return headerAttribute;
    }

    public void setHeader(DatasetRow header) {
        this.header = header;
    }

    public ArrayList<DatasetRow> getData() {
        if (data == null) {
            data = new ArrayList<DatasetRow>();

            for (int i = 0; i < columns.get(0).size(); i++) {
                DatasetRow datasetRow = new DatasetRow();

                for (int j = 0; j < columns.size(); j++) {
                    datasetRow.add(columns.get(j).get(i));
                }

                data.add(datasetRow);
            }
        }

        return data;
    }

    public DatasetRow getRow (int index) {
        if (index >= getDatasetSize()) {
            return null;
        }

        if (data != null) {
            return data.get(index);
        }

        DatasetRow row = new DatasetRow();
        for (int i = 0; i < columns.size(); i++) {
            row.add(columns.get(i).get(index));
        }

        return row;
    }

    public void setData(ArrayList<DatasetRow> data) {
        this.data = data;
    }

    public ArrayList<DatasetColumn> getColumns() {
        return columns;
    }

    public void setColumns(ArrayList<DatasetColumn> columns) {
        this.columns = columns;
    }

    public int getDatasetSize () {
        if (!columns.isEmpty()) {
            return columns.get(0).size();
        }

        return 0;
    }
}
