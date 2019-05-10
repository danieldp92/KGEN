package anonymization;

import anonymization.generalization.graph.GeneralizationTree;
import dataset.Attribute;
import dataset.type.QuasiIdentifier;

import java.util.ArrayList;

public class DatasetAnonymization {
    private GeneralizationTree placeHierarchy;

    public DatasetAnonymization () {

    }

    private void initGeneralizationHierarchies () {

    }
    public ArrayList<ArrayList<String>> getDatasetAnonymized (ArrayList<ArrayList<String>> dataset) {
        return null;
    }

    private String generalization (Attribute attribute) {
        String generalizedValue = "*****";

        if (attribute.getType() instanceof QuasiIdentifier) {
            String value = (String) attribute.getValue();
            QuasiIdentifier attributeType = (QuasiIdentifier) attribute.getType();

            switch (attributeType.type) {
                case QuasiIdentifier.TYPE_DATE:
                    break;
                case QuasiIdentifier.TYPE_NUMERIC:
                    break;
                case QuasiIdentifier.TYPE_PLACE:
                    break;
                case QuasiIdentifier.TYPE_STRING:
                    break;
                default:
                    break;
            }

        }
        return generalizedValue;
    }

    private String dateGeneralization (int levelOfGeneralization, String value) {
        String dateGeneralized = "";



        return dateGeneralized;
    }










    private void supression () {

    }

    private void perturbation () {

    }
}
