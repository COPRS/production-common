package esa.s1pdgs.cpoc.mdcatalog.model;

import java.util.Locale;

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
	public static FileExtension valueOfIgnoreCase(final String extension) {
		FileExtension ret;
		try {
			String extensionUC = extension.toUpperCase(Locale.getDefault());
			ret = valueOf(extensionUC);
		} catch (IllegalArgumentException e) {
			ret = UNKNOWN;
		}
		return ret;
	}
}
