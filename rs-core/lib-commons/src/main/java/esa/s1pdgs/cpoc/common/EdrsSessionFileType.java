package esa.s1pdgs.cpoc.common;

import java.io.File;

/**
 * Enumeration for ERDS session file type
 * @author Viveris Technologies
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
		case AISP:
			ret = RAW;
			break;
		default:
			throw new IllegalArgumentException("Cannot retrieve ERDS session file type from extension " + extension);
		}
		return ret;
	}
	
	public static EdrsSessionFileType ofFilename(final String filename) throws IllegalArgumentException {
		final String name = new File(filename).getName();		
		final String suffix = name.substring(name.lastIndexOf('.') + 1);
		
		return valueFromExtension(FileExtension.valueOfIgnoreCase(suffix));		
	}
}
