package anonymization.generalization.type;

import anonymization.generalization.exception.LevelNotValidException;

public class NumericGeneralization implements IGeneralization {

    public String generalize(int level, Object value) throws LevelNotValidException {
        if (level < 0) {
            throw new LevelNotValidException();
        }


        String numericGeneralization = "";
        int numericValue = (Integer) value;

        if (level == 0) {
            numericGeneralization = String.valueOf(numericValue);
        } else {
            int multiple = (int) Math.pow(10, level);
            //Find the previous and the next number that are multiple of 10
            //Example: 25 -> 20/30
            int previous = findPrevious(numericValue, multiple);
            int next = findNext(numericValue, multiple);

            if (numericValue % multiple == 0) {
                next += multiple;
            }

            numericGeneralization = previous + " - " + next;
        }

        return numericGeneralization;
    }

    private int findPrevious (int value, int multiple) {
        int previous = value;
        int valueToDecrease = 1;
        int actualMultiple = 10;

        if (previous % multiple == 0)
            return value;


        while (previous % multiple != 0) {
            previous -= valueToDecrease;

            if (previous % actualMultiple == 0) {
                valueToDecrease *= 10;
                actualMultiple *= 10;
            }
        }

        return previous;
    }

    private int findNext (int value, int multiple) {
        int next = value;
        int valueToDecrease = 1;
        int actualMultiple = 10;

        if (next % multiple == 0)
            return value;


        while (next % multiple != 0) {
            next += valueToDecrease;

            if (next % actualMultiple == 0) {
                valueToDecrease *= 10;
                actualMultiple *= 10;
            }
        }

        return next;
    }
}
