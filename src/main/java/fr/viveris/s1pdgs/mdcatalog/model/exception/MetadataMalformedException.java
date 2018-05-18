package fr.viveris.s1pdgs.mdcatalog.model.exception;

/**
 * @author Olivier Bex-Chauvet
 *
 */
public class MetadataMalformedException extends AbstractFileException {

	private static final long serialVersionUID = 2939784030412076416L;
	
	private static final String MESSAGE = "Metadata malformed %s, missing field %s";

	/**
	 * @param productName
	 */
	public MetadataMalformedException(String productName, String missingField) {
		super(String.format(MESSAGE, productName, missingField), productName);
	}

}
