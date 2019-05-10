package dataset;

import dataset.type.AttributeType;

public class Attribute {
    private String name;
    private Object value;
    private AttributeType type;

    public Attribute (String pName, Object pValue) {
        this.name = pName;
        this.value = pValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }
}
