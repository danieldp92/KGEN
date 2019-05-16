package anonymization.generalization.type;

import anonymization.generalization.exception.LevelNotValidException;

public interface IGeneralization {
    public String generalize (int level, Object value) throws LevelNotValidException;
}
