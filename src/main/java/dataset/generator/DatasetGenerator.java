package dataset.generator;

import dataset.beans.Attribute;
import dataset.beans.Dataset;
import dataset.beans.DatasetColumn;
import dataset.beans.DatasetRow;
import dataset.type.AttributeType;
import dataset.type.Identifier;
import dataset.type.QuasiIdentifier;
import utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatasetGenerator {
    private static final int MIN_AGE = 18;
    private static final int MAX_AGE = 99;
    private static final int MIN_POSTCODE = 1000;
    private static final int MAX_POSTCODE = 9999;


    /**
     * Generate random dataset with i Name and qi Age/Postcode/Gender
     * @return
     */
    public static Dataset generateRandomDataset (int numberOfRows) throws IOException {
        String nameFilePath = DatasetGenerator.class.getClassLoader().getResource("name.txt").getFile();
        List<String> nameList = FileUtils.loadFile(nameFilePath);

        DatasetRow header = new DatasetRow();
        ArrayList<DatasetColumn> columns = new ArrayList<>();

        //Name
        Attribute headerName = new Attribute("Name", null);
        headerName.setType(new Identifier(AttributeType.TYPE_STRING));
        headerName.setPrimaryKey(true);
        header.add(headerName);

        //Age
        Attribute headerAge = new Attribute("Age", null);
        headerAge.setType(new Identifier(AttributeType.TYPE_INT));
        headerAge.setPrimaryKey(false);
        header.add(headerAge);

        //PostCode
        Attribute headerPostcode = new Attribute("Postcode", null);
        headerPostcode.setType(new Identifier(AttributeType.TYPE_STRING));
        headerPostcode.setPrimaryKey(false);
        header.add(headerPostcode);

        //Gender
        Attribute headerGender = new Attribute("Gender", null);
        headerGender.setType(new Identifier(AttributeType.TYPE_STRING));
        headerGender.setPrimaryKey(false);
        header.add(headerGender);


        DatasetColumn nameColumn = new DatasetColumn();
        DatasetColumn ageColumn = new DatasetColumn();
        DatasetColumn postcodeColumn = new DatasetColumn();
        DatasetColumn genderColumn = new DatasetColumn();

        for (int i = 0; i < numberOfRows; i++) {
            Attribute tmpAttribute = null;

            //Name
            int randomIndex = (int) (Math.random() * nameList.size());
            tmpAttribute = new Attribute("Name", nameList.get(randomIndex));
            tmpAttribute.setType(new Identifier(AttributeType.TYPE_STRING));
            tmpAttribute.setPrimaryKey(true);

            nameColumn.add(tmpAttribute);

            //Age
            int randomAge = (int) (Math.random() * (MAX_AGE - MIN_AGE + 1)) + MIN_AGE;
            tmpAttribute = new Attribute("Age", randomAge);
            tmpAttribute.setType(new QuasiIdentifier(AttributeType.TYPE_INT));
            tmpAttribute.setPrimaryKey(false);

            ageColumn.add(tmpAttribute);

            //Postcode
            //Netherland -> postcode from 1000 to 9999
            int randomPostcodeNumber = (int) (Math.random() * (MAX_POSTCODE - MIN_POSTCODE + 1)) + MIN_POSTCODE;

            String randomPostCode = String.valueOf(randomPostcodeNumber);
            char randomAsciiCode = (char)((int)(Math.random() * (90 - 65 + 1)) + 65);
            randomPostCode += randomAsciiCode;
            randomAsciiCode = (char)((int)(Math.random() * (90 - 65 + 1)) + 65);
            randomPostCode += randomAsciiCode;

            tmpAttribute = new Attribute("Postcode", randomPostCode);
            tmpAttribute.setType(new QuasiIdentifier(AttributeType.TYPE_STRING));
            tmpAttribute.setPrimaryKey(false);

            postcodeColumn.add(tmpAttribute);

            //Gender
            double random = Math.random();
            String randomGender = null;
            if (random < 0.5) {
                randomGender = "M";
            } else {
                randomGender = "F";
            }

            tmpAttribute = new Attribute("Gender", randomGender);
            tmpAttribute.setType(new QuasiIdentifier(AttributeType.TYPE_STRING));
            tmpAttribute.setPrimaryKey(false);

            genderColumn.add(tmpAttribute);
        }

        columns.add(nameColumn);
        columns.add(ageColumn);
        columns.add(postcodeColumn);
        columns.add(genderColumn);

        Dataset dataset = new Dataset(header, columns);

        return dataset;
    }
}
