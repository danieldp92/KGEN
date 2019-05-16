package utils;

import anonymization.generalization.exception.LevelNotValidException;
import anonymization.generalization.type.DateGeneralization;
import anonymization.generalization.type.NumericGeneralization;
import anonymization.generalization.type.PlaceGeneralization;
import anonymization.generalization.type.StringGeneralization;
import dataset.Attribute;
import dataset.Dataset;
import dataset.DatasetRow;
import dataset.type.Identifier;
import dataset.type.QuasiIdentifier;

import java.util.ArrayList;
import java.util.Date;

public class DatasetUtils {

    public void setAttributeTypes (Dataset dataset, ArrayList<Boolean> attributeIdentifiers) {
        for (int j = 0; j < dataset.getHeader().size(); j++) {
            Attribute attribute = (Attribute) dataset.getHeader().get(j);
            setAttributeType(attribute, attributeIdentifiers.get(j));
        }

        for (int i = 0; i < dataset.getData().size(); i++) {

            DatasetRow datasetRow = dataset.getData().get(i);
            for (int j = 0; j < datasetRow.size(); j++) {
                Attribute attribute = (Attribute) datasetRow.get(j);
                setAttributeType(attribute, attributeIdentifiers.get(j));
            }
        }
    }

    private void setAttributeType (Attribute attribute, boolean identifier) {
        if (identifier) {
            attribute.setType(new Identifier());
        } else {
            if (attribute.getName().toLowerCase().equals("datumupdate")) {
                attribute.setType(new QuasiIdentifier(QuasiIdentifier.TYPE_DATE));
                if (attribute.getValue() != null && !(attribute.getValue() instanceof Date)) {
                    attribute.setValue((Date)attribute.getValue());
                }
            } else if (attribute.getName().toLowerCase().equals("huisnr") ||
                    attribute.getName().toLowerCase().equals("huisnrtoe") ||
                    attribute.getName().toLowerCase().equals("latitude") ||
                    attribute.getName().toLowerCase().equals("longitude") ||
                    attribute.getName().toLowerCase().equals("xcoordinaat") ||
                    attribute.getName().toLowerCase().equals("ycoordinaat")) {
                attribute.setType(new QuasiIdentifier(QuasiIdentifier.TYPE_NUMERIC));
                if (attribute.getValue() != null && attribute.getValue() instanceof String) {
                    if (((QuasiIdentifier)attribute.getType()).type == QuasiIdentifier.TYPE_NUMERIC) {
                        if (attribute.getValue().equals("")) {
                            attribute.setValue(null);
                        } else {
                            attribute.setValue(Integer.parseInt((String)attribute.getValue()));
                        }
                    }

                }
            } else if (attribute.getName().toLowerCase().equals("plaats") ||
                    attribute.getName().toLowerCase().equals("provincie")) {
                attribute.setType(new QuasiIdentifier(QuasiIdentifier.TYPE_PLACE));
            } else {
                if (attribute.getName().toLowerCase().equals("postcode") && attribute.getValue() != null) {
                    attribute.setValue(((String)attribute.getValue()).replaceAll(" ", ""));
                }
                attribute.setType(new QuasiIdentifier(QuasiIdentifier.TYPE_STRING));
            }
        }
    }
}
