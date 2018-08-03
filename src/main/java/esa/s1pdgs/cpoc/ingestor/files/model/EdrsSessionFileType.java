package esa.s1pdgs.cpoc.ingestor.files.model;

/**
 * Enumeration for ERDS session file type
 * @author Cyrielle Gailliard
 *
 */
public enum EdrsSessionFileType {
	RAW, SESSION;
	
	/**
	 * Determinate value from an extension
	 * @param extension
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static EdrsSessionFileType valueFromExtension(final FileExtension extension) throws IllegalArgumentException {
		EdrsSessionFileType ret;
		switch (extension) {
		case XML:
			ret = SESSION;
			break;
		case RAW:
			ret = RAW;
			break;
		default:
			throw new IllegalArgumentException("Cannot retrieve ERDS session file type from extension " + extension);
		}
		return ret;
	}
}
