package fr.viveris.s1pdgs.jobgenerator.exception;

public class MetadataException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6031049176076706363L;
	
	public MetadataException(String message) {
		super(message);
	}
	
	public MetadataException(String message, Throwable e) {
		super(message, e);
	}

}
