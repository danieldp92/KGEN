package dataset.beans;

import java.util.ArrayList;
import java.util.Date;

public class DatasetRow extends ArrayList{

    @Override
    public Object set(int index, Object element) {
        return super.set(index, element);
    }

    @Override
    public boolean add(Object o) {
        return super.add(o);
    }

    @Override
    public void add(int index, Object element) {
        super.add(index, element);
    }

    @Override
    public Object remove(int index) {
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        return super.remove(o);
    }
}
