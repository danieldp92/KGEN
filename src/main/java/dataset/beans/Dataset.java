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
