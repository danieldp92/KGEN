package utils;

import dataset.Attribute;
import dataset.Dataset;
import dataset.DatasetRow;
import org.apache.commons.compress.compressors.FileNameUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Attr;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class XlsUtils {

    public static Dataset readXlsx(String path) {
        Dataset dataset = null;
        DatasetRow header = new DatasetRow();
        ArrayList<DatasetRow> data = new ArrayList<DatasetRow>();

        File excelFile = new File(path);

        try {
            FileInputStream excelIS = new FileInputStream(excelFile);

            String fileExtension = FileUtils.getFileExtension(excelFile);

            Workbook workbook = null;
            if (fileExtension.equals("xlsx")) {
                workbook = new XSSFWorkbook(excelIS);
            } else {
                workbook = new HSSFWorkbook(excelIS);
            }

            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            boolean attributeRow = true;

            while (iterator.hasNext()) {
                Row currentRow = iterator.next();

                if (attributeRow) {
                    Iterator<Cell> cellIterator = currentRow.iterator();

                    while (cellIterator.hasNext()) {
                        Cell currentCell = cellIterator.next();
                        String value = currentCell.toString();

                        header.add(new Attribute(value, null));
                    }

                    attributeRow = false;
                }

                else {
                    DatasetRow datasetRow = new DatasetRow();
                    for (int i = 0; i < header.size(); i++) {
                        Cell cell = currentRow.getCell(i);
                        String value = "";

                        if (cell != null) {
                            value = cell.toString();
                        }

                        Attribute headerAttribute = (Attribute) header.get(i);
                        datasetRow.add(new Attribute(headerAttribute.getName(), value));
                    }

                    data.add(datasetRow);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataset = new Dataset(header, data);

        return dataset;
    }

    public static void writeXlsx(String path, ArrayList<ArrayList<String>> data, ArrayList<CellType> attributeTypes) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Zoekresultaten");

        if (!data.isEmpty()) {
            ArrayList<String> columns = data.get(0);

            // Create attribute Row
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i));
            }

            for (int i = 1; i < data.size(); i++) {
                Row dataRow = sheet.createRow(i);

                for (int j = 0; j < data.get(i).size(); j++) {
                    Cell cell = dataRow.createCell(j);
                    cell.setCellValue(data.get(i).get(j));
                }
            }

            // Write the output to a file
            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream(path);
                workbook.write(fileOut);
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static ArrayList<CellType> getDatasetAttributeType (String path) {
        ArrayList<CellType> attributeTypes = new ArrayList<CellType>();

        try {
            FileInputStream excelFile = new FileInputStream(new File(path));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            //Jump to 2 row
            if (iterator.hasNext())
                iterator.next();


            if (iterator.hasNext()) {
                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();

                while (cellIterator.hasNext()) {
                    Cell currentCell = cellIterator.next();
                    attributeTypes.add(currentCell.getCellType());
                }

            } else {
                return null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return attributeTypes;
    }
}
