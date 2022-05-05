
package esa.s1pdgs.cpoc.datalifecycle.worker.service.error;

/**
 * Signaling that the trigger thinks the client request is somehow invalid (wrong syntax, arguments, ...)
 */
public class DataLifecycleTriggerBadRequestException extends Exception {

	private static final long serialVersionUID = -859447506097294646L;

	// --------------------------------------------------------------------------

	public DataLifecycleTriggerBadRequestException(String string) {
		super(string);
	}

	public DataLifecycleTriggerBadRequestException(String string, Throwable e) {
		super(string, e);
	}

}
