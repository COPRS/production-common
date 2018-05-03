package fr.viveris.s1pdgs.archives.model.exception;

/**
 * @author Olivier Bex-Chauvet
 *
 */
public class IllegalFileExtension extends AbstractFileException {

	private static final long serialVersionUID = 2663452897332948566L;

	private static final String MESSAGE = "Cannot retrieve ERDS session file type from extension %s";
	/**
	 * @param productName
	 */
	public IllegalFileExtension(String extension) {
		super(String.format(MESSAGE, extension), extension);
	}


}
