package anonymization.support;

import java.util.*;

public class SupportMap extends LinkedHashMap<String, Collection<Integer>> {
    public Collection<Integer> getRows (int index) {
        int i = 0;
        for (Map.Entry<String, Collection<Integer>> entry : entrySet()) {
            if (i++ == index) {
                Collection<Integer> cloneList = new ArrayList<>();
                for (int elem : entry.getValue()) {
                    cloneList.add(elem);
                }
                return cloneList;
            }
        }

        return null;
    }

    @Override
    public Object clone() {

        return super.clone();
    }
}
