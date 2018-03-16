package fr.viveris.s1pdgs.mdcatalog.model.exception;
/**
 * 
 * @author Olivier Bex-Chauvet
 *
 */
public class MetadataCreationException extends AbstractFileException {

	private static final long serialVersionUID = 1086920560648012203L;
	
	private static final String MESSAGE = "Metadata not created for %s: %s %s";

	public MetadataCreationException(String productName, String result, String status) {
		super(String.format(MESSAGE, productName, status, result), productName);
	}

}
