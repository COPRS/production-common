package esa.s1pdgs.cpoc.appcatalog.services.mongodb;

public interface SequenceDao {

	long getNextSequenceId(String key) throws SequenceException;

}
