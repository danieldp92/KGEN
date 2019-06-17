package dataset.type;

public class AttributeType {
    public static final int TYPE_STRING = 1;
    public static final int TYPE_DATE = 2;
    public static final int TYPE_INT = 3;
    public static final int TYPE_PLACE = 4;
    public static final int TYPE_DOUBLE = 5;

    public int type;

    public AttributeType (int type) { this.type = type; }
}
