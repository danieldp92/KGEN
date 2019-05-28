package anonymization.generalization.type;

import anonymization.generalization.exception.LevelNotValidException;

public class StringGeneralization implements IGeneralization {
    public String generalize(int level, Object value) throws LevelNotValidException {
        if (level < 0) {
            throw new LevelNotValidException();
        }

        String stringGeneralized = "";

        String stringValue = (String) value;

        if (stringValue.length() - level <= 0) {
            for (int i = 0; i < level; i++) {
                stringGeneralized += "*";
            }
        } else {
            stringGeneralized = stringValue.substring(0, stringValue.length() - level);
            for (int i = 0; i < level; i++) {
                stringGeneralized += "*";
            }
        }


        return stringGeneralized;
    }
}
