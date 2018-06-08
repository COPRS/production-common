package fr.viveris.s1pdgs.jobgenerator.exception;

public class MetadataException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6031049176076706363L;

	/**
	 * 
	 * @param message
	 */
	public MetadataException(final String message) {
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param e
	 */
	public MetadataException(final String message, final Throwable e) {
		super(message, e);
	}

}
