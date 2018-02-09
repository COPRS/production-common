package fr.viveris.s1pdgs.ingestor.model.exception;

public class FilePathException extends FileException {
	
	private static final long serialVersionUID = 2694835373130815240L;
	
	private static final String MESSAGE = "Description extraction failed for %s: %s";
	
	private String path;

	public FilePathException(String productName, String path, String msg) {
		super(String.format(MESSAGE, path, msg), path);
		this.path = path;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

}
