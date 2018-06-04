package fr.viveris.s1pdgs.ingestor.exceptions;

public class FilePathException extends FileTerminatedException {
	
	private static final long serialVersionUID = 2694835373130815240L;
	
	private static final String MESSAGE = "Description extraction failed: %s";
	
	private String path;

	public FilePathException(String productName, String path, String msg) {
		super(ErrorCode.INGESTOR_INVALID_PATH, productName, String.format(MESSAGE, msg));
		this.path = path;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	@Override
	public String getLogMessage() {
		return String.format("[path %s] [msg %s]", this.path, getMessage());
	}

}
