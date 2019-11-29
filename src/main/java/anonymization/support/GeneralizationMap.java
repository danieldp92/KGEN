package anonymization.support;

import java.util.LinkedHashMap;
import java.util.Map;

public class GeneralizationMap extends LinkedHashMap<Integer, LOGMap> {

    @Override
    public String toString() {
        String returnValue = "";
        returnValue += "QI Index\tLOG Map\n";
        for (Map.Entry<Integer, LOGMap> entry : this.entrySet()) {
            returnValue += entry.getKey() + "\t\tLog\tSupportMap\n";
            returnValue += entry.getValue().toString() + "\n";
        }

        return returnValue;
    }
}
