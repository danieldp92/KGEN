package anonymization.generalization.type;

import anonymization.generalization.exception.LevelNotValidException;

public class StringGeneralization implements IGeneralization {
    private int minLength;
    private int maxLength;

    public StringGeneralization(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public String generalize(int level, Object value) throws LevelNotValidException, StringIndexOutOfBoundsException {
        if (level < 0) {
            throw new LevelNotValidException();
        }

        if (value == null)
            return null;

        String stringGeneralized = "";

        String stringValue = (String) value;

        if (level == 0) {
            return stringValue;
        }


        if (stringValue.length() - level >= minLength) {
            stringGeneralized = stringValue.substring(0, stringValue.length() - level);
        } else {
            stringGeneralized = stringValue.substring(0, minLength);

            int numberOfLettersToAnonymise = minLength - maxLength + level;

            if (numberOfLettersToAnonymise > 0) {
                stringGeneralized = stringGeneralized.substring(0, stringGeneralized.length() - numberOfLettersToAnonymise);
                for (int i = 0; i > numberOfLettersToAnonymise; i++) {
                    stringGeneralized += "*";
                }
            }

        }




        /*if (stringValue.length() == 0) {
            for (int i = 0; i < this.maxLength; i++) {
                stringGeneralized += "*";
            }
        } else {*/
            //Number of elements to cut
            /*int numberToCut = stringValue.length() - maxLength + level;

            if (numberToCut > 0) {
                try {
                    stringGeneralized = stringValue.substring(0, stringValue.length() - numberToCut);
                } catch (StringIndexOutOfBoundsException ex) {
                    System.out.println();
                    System.out.println("String value: " + stringValue);
                    System.out.println("Number to cut: " + numberToCut);
                    System.out.println("Level: " + level);
                    System.out.println("Max lenght: " + maxLength);
                    System.out.println("Min lenght: " + minLength);
                    ex.printStackTrace();
                }

                for (int i = stringGeneralized.length(); i < minLength; i++) {
                    stringGeneralized += "*";
                }
            } else {
                stringGeneralized = stringValue;
            }
        //}*/

        return stringGeneralized;
    }
}
