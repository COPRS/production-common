package fr.viveris.s1pdgs.ingestor.files.services;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.viveris.s1pdgs.ingestor.exceptions.FilePathException;
import fr.viveris.s1pdgs.ingestor.exceptions.IgnoredFileException;
import fr.viveris.s1pdgs.ingestor.files.model.FileDescriptor;

/**
 * File descriptor
 * 
 * @author Cyrielle
 *
 */
public abstract class AbstractFileDescriptorService {

	/**
	 * Pattern
	 */
	protected final Pattern pattern;

	/**
	 * Local directory
	 */
	protected final String directory;

	/**
	 * Family
	 */
	protected final String family;

	/**
	 * Constructor
	 * 
	 * @param directory
	 * @param pattern
	 */
	protected AbstractFileDescriptorService(final String directory, final String patternStr, final String family) {
		this.directory = directory;
		this.pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
		this.family = family;
	}

	/**
	 * Extract information from filename
	 * 
	 * @param file
	 * @return
	 * @throws FilePathException
	 * @throws IgnoredFileException
	 */
	public FileDescriptor extractDescriptor(File file) throws FilePathException, IgnoredFileException {
		// Extract object storage key
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= directory.length()) {
			throw new FilePathException(absolutePath, absolutePath, family, "Filename length is too short");
		}
		String relativePath = absolutePath.substring(directory.length());
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new IgnoredFileException(relativePath, file.getName());
		}

		// Check if key matches the pattern
		Matcher m = pattern.matcher(relativePath);

		return buildFromMatcher(m, relativePath);
	}

	/**
	 * Implementation function of extraction from the pattern
	 * 
	 * @param m
	 * @return
	 */
	protected abstract FileDescriptor buildFromMatcher(final Matcher matcher, final String relativePath)
			throws FilePathException;

	/**
	 * @return the pattern
	 */
	public Pattern getPattern() {
		return pattern;
	}

	/**
	 * @return the directory
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * @return the family
	 */
	public String getFamily() {
		return family;
	}

}
