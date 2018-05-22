package fr.viveris.s1pdgs.mdcatalog.model.exception;

public class MetadataNotPresentException extends AbstractFileException {

	private static final long serialVersionUID = -616528427720024929L;
	
	private static final String MESSAGE = "Metadata %s is not present";

	public MetadataNotPresentException(String productName) {
		super(String.format(MESSAGE, productName), productName);
	}


}
