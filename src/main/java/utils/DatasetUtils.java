package utils;

import dataset.Attribute;
import dataset.Dataset;
import dataset.DatasetRow;
import dataset.type.Identifier;
import dataset.type.QuasiIdentifier;

import java.util.ArrayList;

public class DatasetUtils {
    public void setAttributeTypes (Dataset dataset, ArrayList<Boolean> attributeIdentifiers) {
        for (int i = 0; i < dataset.getData().size(); i++) {

            DatasetRow datasetRow = dataset.getData().get(i);
            for (int j = 0; j < datasetRow.size(); j++) {
                Attribute attribute = (Attribute) datasetRow.get(j);
                if (attributeIdentifiers.get(j)) {
                    attribute.setType(new Identifier());
                } else {
                    if (attribute.getName().toLowerCase().equals("datumupdate")) {
                        attribute.setType(new QuasiIdentifier(QuasiIdentifier.TYPE_DATE));
                    } else if (attribute.getName().toLowerCase().equals("huisnr") ||
                            attribute.getName().toLowerCase().equals("huisnrtoe") ||
                            attribute.getName().toLowerCase().equals("latitude") ||
                            attribute.getName().toLowerCase().equals("longitude") ||
                            attribute.getName().toLowerCase().equals("xcoordinaat") ||
                            attribute.getName().toLowerCase().equals("ycoordinaat")) {
                        attribute.setType(new QuasiIdentifier(QuasiIdentifier.TYPE_NUMERIC));
                    } else if (attribute.getName().toLowerCase().equals("plaats") ||
                            attribute.getName().toLowerCase().equals("provincie")) {
                        attribute.setType(new QuasiIdentifier(QuasiIdentifier.TYPE_PLACE));
                    } else {
                        attribute.setType(new QuasiIdentifier(QuasiIdentifier.TYPE_STRING));
                    }
                }
            }
        }
    }
}
