package anonymization.support;

import java.util.LinkedHashMap;
import java.util.Map;

public class LOGMap extends LinkedHashMap<Integer, SupportMap> {
    @Override
    public String toString() {
        String returnValue = "";

        for (Map.Entry<Integer, SupportMap> entry : this.entrySet()) {
            returnValue += "\t\t" + entry.getKey() + "\tValue\t\tRows\n";
            returnValue += entry.getValue().toString() + "\n";
        }

        return returnValue;
    }
}
