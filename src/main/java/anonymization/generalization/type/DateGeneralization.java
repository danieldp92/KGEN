package anonymization.generalization.type;

import anonymization.generalization.exception.LevelNotValidException;

import java.util.Calendar;
import java.util.Date;

public class DateGeneralization implements IGeneralization {

    public String generalize(int level, Object value) throws LevelNotValidException {
        String generalizedDate = "";

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
                throw new LevelNotValidException();
        }

        return generalizedDate;
    }
}
