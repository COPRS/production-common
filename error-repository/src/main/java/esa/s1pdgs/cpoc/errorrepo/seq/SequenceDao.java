package esa.s1pdgs.cpoc.errorrepo.seq;

public interface SequenceDao {

	long getNextSequenceId(String key) throws SequenceException;

}
