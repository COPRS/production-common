package fr.viveris.s1pdgs.ingestor.model;

/**
 * Enumeration for ERDS session file type
 * @author Cyrielle Gailliard
 *
 */
public enum ErdsSessionFileType {
	RAW, SESSION;
	
	/**
	 * Determinate value from an extension
	 * @param extension
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static ErdsSessionFileType valueFromExtension(FileExtension extension) throws IllegalArgumentException {
		switch (extension) {
		case XML:
			return SESSION;
		case RAW:
			return RAW;
		default:
			// TODO custome exception
			throw new IllegalArgumentException("Cannot retrieve ERDS session file type from extension " + extension);
		}
	}
}
