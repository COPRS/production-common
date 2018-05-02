package fr.viveris.s1pdgs.ingestor.services.file;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.viveris.s1pdgs.ingestor.model.EdrsSessionFileType;
import fr.viveris.s1pdgs.ingestor.model.FileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.FileExtension;
import fr.viveris.s1pdgs.ingestor.model.exception.FilePathException;
import fr.viveris.s1pdgs.ingestor.model.exception.IgnoredFileException;

/**
 * Service to build file descriptor
 * 
 * @author Cyrielle Gailliard
 *
 */
public class FileDescriptorBuilder {

	/**
	 * Pattern for configuration files to extract data
	 */
	private final Pattern patternConfig;

	/**
	 * Pattern for configuration files to extract data
	 */
	private final Pattern patternSession;

	/**
	 * Local directory for configuration files
	 */
	private final String configLocalDirectory;

	/**
	 * Local directory for ERDS session files
	 */
	private final String sessionLocalDirectory;

	/**
	 * Constructor
	 * 
	 * @param configLocalDirectory
	 * @param sessionLocalDirectory
	 * @param patternConfig
	 * @param patternSession
	 */
	public FileDescriptorBuilder(final String configLocalDirectory, final String sessionLocalDirectory,
			final Pattern patternConfig, final Pattern patternSession) {
		this.configLocalDirectory = configLocalDirectory;
		this.sessionLocalDirectory = sessionLocalDirectory;
		this.patternConfig = patternConfig;
		this.patternSession = patternSession;
	}

	/**
	 * Build descriptor for configuration files from path file
	 * 
	 * @param file
	 * @return
	 * @throws FilePathException
	 *             if we have
	 */
	public FileDescriptor buildConfigFileDescriptor(File file) throws FilePathException, IgnoredFileException {

		// Extract object storage key
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= configLocalDirectory.length()) {
			throw new FilePathException(absolutePath, absolutePath, "Filename length is too short");
		}
		String relativePath = absolutePath.substring(configLocalDirectory.length());
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new IgnoredFileException(relativePath, file.getName());
		}

		// Check if key matches the pattern
		FileDescriptor configFile = null;
		Matcher m = patternConfig.matcher(relativePath);
		if (m.matches()) {
			// Extract product name
			String productName = relativePath;
			boolean isRoot = true;
			int indexFirstSeparator = relativePath.indexOf("/");
			if (indexFirstSeparator != -1) {
				productName = relativePath.substring(0, indexFirstSeparator);
				isRoot = false;
			}
			// Extract filename
			String filename = relativePath;
			int indexLastSeparator = relativePath.lastIndexOf("/");
			if (indexFirstSeparator != -1) {
				filename = relativePath.substring(indexLastSeparator + 1);
			}
			// Build descriptor
			configFile = new FileDescriptor();
			configFile.setRelativePath(relativePath);
			configFile.setKeyObjectStorage(relativePath);
			configFile.setProductName(productName);
			configFile.setHasToBePublished(false);
			if (isRoot || filename.equalsIgnoreCase("manifest.safe")) {
				configFile.setHasToBePublished(true);
			}
			configFile.setMissionId(m.group(1));
			configFile.setSatelliteId(m.group(2));

		} else {
			throw new FilePathException(relativePath, relativePath,
					"File does not match the configuration file pattern");
		}

		return configFile;
	}

	public FileDescriptor buildEdrsSessionFileDescriptor(File file)
			throws FilePathException, IgnoredFileException {
		// Extract relative path
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= sessionLocalDirectory.length()) {
			throw new FilePathException(absolutePath, absolutePath, "Filename too short");
		}
		String relativePath = absolutePath.substring(sessionLocalDirectory.length());
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new IgnoredFileException(relativePath, file.getName());
		}

		Matcher m = patternSession.matcher(relativePath);
		if (m.matches()) {
			// Ignore the IIF files
			if (m.group(11).toLowerCase().contains("iif_")) {
				throw new FilePathException(relativePath, relativePath, "IIF file");
			}

			// "^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$";
			FileDescriptor descriptor = new FileDescriptor();
			descriptor.setRelativePath(relativePath);
			descriptor.setProductName(m.group(9));
			descriptor.setExtension(FileExtension.valueOfIgnoreCase(m.group(12)));
			descriptor.setProductType(EdrsSessionFileType.valueFromExtension(descriptor.getExtension()));
			descriptor.setChannel(Integer.parseInt(m.group(7)));
			descriptor.setKeyObjectStorage(relativePath);
			descriptor.setMissionId(m.group(1));
			descriptor.setSatelliteId(m.group(2));

			return descriptor;
		} else {
			throw new FilePathException(relativePath, relativePath,
					"File does not match the configuration file pattern");
		}
	}
}
