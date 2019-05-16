package anonymization.generalization.type;

import anonymization.generalization.exception.LevelNotValidException;
import anonymization.generalization.generator.GeneralizationGraphGenerator;
import anonymization.generalization.graph.GeneralizationTree;
import anonymization.generalization.graph.Node;

import java.io.File;

public class PlaceGeneralization implements IGeneralization{
    private GeneralizationTree placeHierarchy;

    public PlaceGeneralization() {
        this.placeHierarchy = GeneralizationGraphGenerator.generatePlaceHierarchy();
    }

    public String generalize(int level, Object value) throws LevelNotValidException {
        String place = (String) value;
        String generalizedPlace = null;

        switch (level) {
            case 0:
                generalizedPlace = place;
                break;
            case 1:
                for (Node node : placeHierarchy.getNodes()) {
                    if (node.getValue().toLowerCase().equals(place.toLowerCase())) {
                        generalizedPlace = placeHierarchy.getParent(node).getValue();
                        break;
                    }
                }
                break;
            case 2 :
                generalizedPlace = placeHierarchy.getNode(0).getValue();
                break;
            default:
                throw new LevelNotValidException();
        }

        return generalizedPlace;
    }
}
