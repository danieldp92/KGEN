package dataset;

import java.util.ArrayList;

public class Dataset {
    private DatasetRow header;
    private ArrayList<DatasetRow> data;

    public Dataset (DatasetRow pHeader, ArrayList<DatasetRow> pData) {
        this.header = pHeader;
        this.data = pData;
    }

    public DatasetRow getHeader() {
        return header;
    }

    public void setHeader(DatasetRow header) {
        this.header = header;
    }

    public ArrayList<DatasetRow> getData() {
        return data;
    }

    public void setData(ArrayList<DatasetRow> data) {
        this.data = data;
    }
}
