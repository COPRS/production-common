package esa.s1pdgs.cpoc.appcatalog.server.services.mongodb;

public interface SequenceDao {

	long getNextSequenceId(String key) throws SequenceException;

}
