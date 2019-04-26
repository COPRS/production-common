package esa.s1pdgs.cpoc.appcatalog.server.sequence.db;

public interface SequenceDao {

	long getNextSequenceId(String key) throws SequenceException;

}
