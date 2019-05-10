package dataset.type;

public class QuasiIdentifier extends AttributeType {
    public static final int TYPE_STRING = 1;
    public static final int TYPE_DATE = 2;
    public static final int TYPE_NUMERIC = 3;
    public static final int TYPE_PLACE = 4;


    public int type;

    public QuasiIdentifier (int quasiIdentifierType) {
        this.type = quasiIdentifierType;
    }
}
