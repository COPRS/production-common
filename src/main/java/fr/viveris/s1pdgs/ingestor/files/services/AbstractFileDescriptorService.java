package fr.viveris.s1pdgs.ingestor.files.services;

import java.io.File;

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
	protected AbstractFileDescriptorService(final String directory, final String family) {
		this.directory = directory;
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
	public FileDescriptor extractDescriptor(final File file) throws FilePathException, IgnoredFileException {
		String absolutePath = file.getAbsolutePath();
		
		// Extract object storage key
		if (!absolutePath.contains(directory)) {
			throw new FilePathException(file.getName(), absolutePath, family, "File is not in root directory");
		}
		String relativePath = absolutePath.substring(directory.length());
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new IgnoredFileException(relativePath, file.getName());
		}

		// Check if key matches the pattern

		return buildDescriptor(relativePath);
	}

	/**
	 * Implementation function of extraction from the pattern
	 * 
	 * @param m
	 * @return
	 */
	protected abstract FileDescriptor buildDescriptor(final String relativePath) throws FilePathException;

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
