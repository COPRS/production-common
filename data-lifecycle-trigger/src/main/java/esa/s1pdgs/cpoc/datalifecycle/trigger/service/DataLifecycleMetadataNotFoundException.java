
package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

/**
 * Signaling that a requested product was not found in the data lifecycle metadata persistence.
 */
public class DataLifecycleMetadataNotFoundException extends Exception {

	private static final long serialVersionUID = 2676234765748567050L;

	// --------------------------------------------------------------------------

	public DataLifecycleMetadataNotFoundException(String string) {
		super(string);
	}

	public DataLifecycleMetadataNotFoundException(String string, Throwable e) {
		super(string, e);
	}

}
