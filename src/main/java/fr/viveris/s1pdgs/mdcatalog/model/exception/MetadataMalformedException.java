package fr.viveris.s1pdgs.mdcatalog.model.exception;

/**
 * @author Olivier Bex-Chauvet
 *
 */
public class MetadataMalformedException extends AbstractFileException {

	private static final long serialVersionUID = 2939784030412076416L;
	
	private static final String MESSAGE = "Metadata malformed %s";

	/**
	 * @param productName
	 */
	public MetadataMalformedException(String productName) {
		super(String.format(MESSAGE, productName), productName);
	}

}
