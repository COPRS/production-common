package esa.s1pdgs.cpoc.preparation.worker.db;

import esa.s1pdgs.cpoc.preparation.worker.model.exception.SequenceException;

public interface SequenceDao {

	long getNextSequenceId(String key) throws SequenceException;
}
