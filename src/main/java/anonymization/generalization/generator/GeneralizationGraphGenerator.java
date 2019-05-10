package anonymization.generalization.generator;

import anonymization.generalization.graph.GeneralizationTree;
import dataset.Dataset;
import utils.XlsUtils;

import java.io.File;
import java.util.ArrayList;

import static utils.XlsUtils.readXlsx;

public class GeneralizationGraphGenerator {
    private static final String PLACEINFO_XLS_PATH = System.getProperty("user.dir") + File.separator + "dataset" + File.separator +
            "netherland_place_info.xls";

    public static GeneralizationTree generatePlaceHierarchy (ArrayList<String> places) {
        GeneralizationTree placeTree = null;

        Dataset placeDataset = XlsUtils.readXlsx(PLACEINFO_XLS_PATH);

        return placeTree;
    }
}
