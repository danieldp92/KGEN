package anonymization.generalization.type;

import anonymization.generalization.exception.LevelNotValidException;

public interface IGeneralization {

    /**
     *
     * @param level: level of anonymization
     * @param value: value to anonymize
     * @return
     * @throws LevelNotValidException
     */
    public String generalize (int level, Object value) throws LevelNotValidException;
}
