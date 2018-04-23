package fr.viveris.s1pdgs.jobgenerator.exception;

import java.util.List;

/**
 * Generic exception concerning the metadata
 * @author Cyrielle Gailliard
 *
 */
public class MetadataMissingException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 6588566901653710376L;
	
	private static final String MESSAGE = "Missing metadata %s";
	
	private List<String> missingMetadata;

	/**
	 * Constructor
	 * @param message
	 */
	public MetadataMissingException(List<String> missingData) {
		super(String.format(MESSAGE, missingData));
		this.missingMetadata = missingData;
	}

	/**
	 * @return the missingMetadata
	 */
	public List<String> getMissingMetadata() {
		return missingMetadata;
	}

}
