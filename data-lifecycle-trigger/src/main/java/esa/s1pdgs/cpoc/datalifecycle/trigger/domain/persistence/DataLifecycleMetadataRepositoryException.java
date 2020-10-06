
package esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence;

/**
 * Signaling that something went wrong interacting with the persistence.
 */
public class DataLifecycleMetadataRepositoryException extends Exception {

	private static final long serialVersionUID = -7218466810827809091L;
	
	// --------------------------------------------------------------------------

	public DataLifecycleMetadataRepositoryException(String string) {
		super(string);
	}

	public DataLifecycleMetadataRepositoryException(String string, Throwable e) {
		super(string, e);
	}

}
