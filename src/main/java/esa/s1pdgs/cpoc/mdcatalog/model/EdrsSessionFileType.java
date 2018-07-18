package esa.s1pdgs.cpoc.mdcatalog.model;

import esa.s1pdgs.cpoc.mdcatalog.model.exception.IllegalFileExtension;

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
	public static EdrsSessionFileType valueFromExtension(FileExtension extension) throws IllegalFileExtension {
		switch (extension) {
		case XML:
			return SESSION;
		case RAW:
			return RAW;
		default:
			throw new IllegalFileExtension(extension.toString());
		}
	}
}
