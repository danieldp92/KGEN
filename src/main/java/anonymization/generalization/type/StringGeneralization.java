package anonymization.generalization.type;

import anonymization.generalization.exception.LevelNotValidException;

public class StringGeneralization implements IGeneralization {
    private int minLength;
    private int maxLength;

    public StringGeneralization(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public String generalize(int level, Object value) throws LevelNotValidException {
        if (level < 0) {
            throw new LevelNotValidException();
        }

        if (value == null)
            return null;


        String stringGeneralized = "";

        String stringValue = (String) value;


        //Number of elements to cut
        int numberToCut = stringValue.length() - maxLength + level;

        if (numberToCut > 0) {
            stringGeneralized = stringValue.substring(0, stringValue.length() - numberToCut);

            for (int i = stringGeneralized.length(); i < minLength; i++) {
                stringGeneralized += "*";
            }
        } else {
            stringGeneralized = stringValue;
        }

        return stringGeneralized;
    }
}
