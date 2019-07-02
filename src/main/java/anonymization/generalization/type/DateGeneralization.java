package anonymization.generalization.type;

import anonymization.generalization.exception.LevelNotValidException;

import java.util.Calendar;
import java.util.Date;

public class DateGeneralization implements IGeneralization {

    public String generalize(int level, Object value) throws LevelNotValidException {
        String generalizedDate = "";

        if (level < 0) {
            throw new LevelNotValidException();
        }

        Date date = (Date) value;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        switch (level) {
            case 0:
                generalizedDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" +calendar.get(Calendar.YEAR);
                break;
            case 1:
                generalizedDate = (calendar.get(Calendar.MONTH) + 1) + "/" +calendar.get(Calendar.YEAR);
                break;
            case 2:
                generalizedDate = String.valueOf(calendar.get(Calendar.YEAR));
                break;
            default:
                int year = calendar.get(Calendar.YEAR);
                int multiple = (int) Math.pow(10, (level-2));

                int prev = findPrevious(year, multiple);
                int next = findNext(year, multiple);

                if (year % multiple == 0) {
                    next += multiple;
                }

                generalizedDate = prev + " - " + next;
                break;
        }

        return generalizedDate;
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
