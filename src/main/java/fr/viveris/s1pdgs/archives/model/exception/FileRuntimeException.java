package fr.viveris.s1pdgs.archives.model.exception;

public class FileRuntimeException extends AbstractFileException {

	private static final long serialVersionUID = -4138493055116873488L;

	public FileRuntimeException(String msg, String productName) {
		super(msg, productName);
	}
	
	public FileRuntimeException(String msg, String productName, Throwable cause) {
		super(msg, productName, cause);
	}

}
