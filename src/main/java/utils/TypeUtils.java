package utils;

import java.util.*;

public class TypeUtils {
    public static final int PRIMITIVE_TYPE = 0;
    public static final int ARRAY_TYPE = 1;
    public static final int SET_TYPE = 2;
    public static final int MAP_TYPE = 3;
    public static final int UNKNOWN_TYPE = 4;

    public static int objectInstanceOf (Object o) {
        int type = UNKNOWN_TYPE;

        if (o instanceof Integer || o instanceof Double || o instanceof Float ||
                o instanceof String || o instanceof Object) {
            type = PRIMITIVE_TYPE;
        } else if (o instanceof ArrayList || o instanceof LinkedList || o instanceof List) {
            type = ARRAY_TYPE;
        } else if (o instanceof Set || o instanceof HashSet || o instanceof LinkedHashSet) {
            type = SET_TYPE;
        } else if (o instanceof Map || o instanceof HashMap || o instanceof LinkedHashMap) {
            type = MAP_TYPE;
        } else {
            type = UNKNOWN_TYPE;
        }

        return type;
    }
}
