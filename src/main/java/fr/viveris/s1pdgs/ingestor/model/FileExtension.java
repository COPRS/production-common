package fr.viveris.s1pdgs.ingestor.model;

/**
 * Enumeration for file extension
 * @author Cyrielle
 *
 */
public enum FileExtension {
	XML, SAFE, EOF, RAW, XSD, DAT, UNKNOWN;

	/**
	 * Determinate value from an extension
	 * @param extension
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static FileExtension valueOfIgnoreCase(String extension) throws IllegalArgumentException {
		try {
			return valueOf(extension.toUpperCase());
		} catch (IllegalArgumentException e) {
			return UNKNOWN;
		}
	}
}
