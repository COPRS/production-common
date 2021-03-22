
package esa.s1pdgs.cpoc.datalifecycle.client.error;

/**
 * Signaling that something went wrong in the data lifecycle trigger.
 */
public class DataLifecycleTriggerInternalServerErrorException extends Exception {

	private static final long serialVersionUID = -5343969326183198146L;

	// --------------------------------------------------------------------------

	public DataLifecycleTriggerInternalServerErrorException(String string) {
		super(string);
	}

	public DataLifecycleTriggerInternalServerErrorException(String string, Throwable e) {
		super(string, e);
	}

}
