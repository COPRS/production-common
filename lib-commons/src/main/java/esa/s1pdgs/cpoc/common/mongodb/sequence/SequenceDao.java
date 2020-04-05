package esa.s1pdgs.cpoc.common.mongodb.sequence;

public interface SequenceDao {

	long getNextSequenceId(String key) throws SequenceException;

}
