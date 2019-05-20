package anonymization.generalization.generator;

import anonymization.generalization.graph.Edge;
import anonymization.generalization.graph.GeneralizationTree;
import anonymization.generalization.graph.Node;
import dataset.Attribute;
import dataset.Dataset;
import dataset.DatasetRow;
import utils.XlsUtils;

import java.io.File;
import java.util.*;

import static utils.XlsUtils.readXlsx;

public class GeneralizationGraphGenerator {
    private static final String PLACEINFO_XLS_PATH = System.getProperty("user.dir") + File.separator + "dataset" + File.separator +
            "netherland_place_info.xls";

    public static GeneralizationTree generatePlaceHierarchy () {
        GeneralizationTree placeTree = null;
        LinkedHashMap<String, String> tmpPlaceMap = new LinkedHashMap<String, String>();
        LinkedHashMap<String, ArrayList<String>> placeMap = new LinkedHashMap<String, ArrayList<String>>();

        Dataset placeDataset = XlsUtils.readXlsx(PLACEINFO_XLS_PATH);

        String plaat = null;
        String provincie = null;

        //Map city - province
        for (DatasetRow row : placeDataset.getData()) {
            for (Object attributeObj : row) {
                Attribute attribute = (Attribute) attributeObj;

                if (attribute.getValue() != null) {
                    if (attribute.getName().toLowerCase().equals("plaats")) {
                        plaat = ((String) attribute.getValue()).toLowerCase();
                    }

                    else if (attribute.getName().toLowerCase().equals("provincie")) {
                        provincie = ((String) attribute.getValue()).toLowerCase();
                    }
                }
            }

            if (plaat != null && provincie != null) {
                tmpPlaceMap.put(plaat, provincie);
            }

            plaat = null;
            provincie = null;
        }

        //Take all regions in netherland
        Set<String> regions = new HashSet<String>();
        for (Map.Entry<String, String> entry : tmpPlaceMap.entrySet()) {
            regions.add(entry.getValue());
        }

        //Reverse: Map province - array of cities
        for (String region : regions) {
            ArrayList<String> cities = new ArrayList<String>();

            for (Map.Entry<String, String> entry : tmpPlaceMap.entrySet()) {
                if (entry.getValue().equals(region)) {
                    cities.add(entry.getKey());
                }
            }

            placeMap.put(region, cities);
        }


        //Node generation
        int i = 0;
        ArrayList<Node> placeNodes = new ArrayList<Node>();
        ArrayList<Edge> placeEdges = new ArrayList<Edge>();


        //Create root (State) - Level 0
        Node root = new Node(i++);
        root.setValue("Netherland");
        placeNodes.add(root);

        Edge edge = null;
        for (Map.Entry<String, ArrayList<String>> entry : placeMap.entrySet()) {
            //Region Node
            Node regionNode = new Node(i++);
            regionNode.setValue(entry.getKey());

            edge = new Edge(root, regionNode);

            placeNodes.add(regionNode);
            placeEdges.add(edge);

            //Cities nodes
            for (String city : entry.getValue()) {
                Node cityNode = new Node(i++);
                cityNode.setValue(city);

                edge = new Edge(regionNode, cityNode);

                placeNodes.add(cityNode);
                placeEdges.add(edge);
            }
        }

        placeTree = new GeneralizationTree(placeNodes, placeEdges);

        return placeTree;
    }


}
