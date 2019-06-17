package anonymization.support;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class SupportMap extends LinkedHashMap<String, Collection<Integer>> {
    public Collection<Integer> getRows (int index) {
        int i = 0;
        for (Map.Entry<String, Collection<Integer>> entry : entrySet()) {
            if (i++ == index) {
                return entry.getValue();
            }
        }

        return null;
    }

}
