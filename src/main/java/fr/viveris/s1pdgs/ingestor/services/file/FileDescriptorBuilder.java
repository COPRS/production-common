package fr.viveris.s1pdgs.ingestor.services.file;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.viveris.s1pdgs.ingestor.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileType;
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
	private final static String PATTERN_CONFIG = "^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$";

	private final static String PATTERN_SESSION = "^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$";

	/**
	 * Local directory for configuration files
	 */
	private final String configLocalDirectory;

	/**
	 * Local directory for ERDS session files
	 */
	private final String sessionLocalDirectory;

	public FileDescriptorBuilder(final String configLocalDirectory, final String sessionLocalDirectory) {
		this.configLocalDirectory = configLocalDirectory;
		this.sessionLocalDirectory = sessionLocalDirectory;
	}

	public ConfigFileDescriptor buildConfigFileDescriptor(File file) throws FilePathException {

		// Extract object storage key
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= configLocalDirectory.length()) {
			throw new FilePathException(absolutePath, absolutePath, "Filename length is too short");
		}
		String relativePath = absolutePath.substring(configLocalDirectory.length());
		relativePath = relativePath.replace("\\", "/");

		// Check if key matches the pattern
		ConfigFileDescriptor configFile = null;
		Pattern p = Pattern.compile(PATTERN_CONFIG, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(relativePath);
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
			configFile = new ConfigFileDescriptor();
			configFile.setFilename(filename);
			configFile.setRelativePath(relativePath);
			configFile.setKeyObjectStorage(relativePath);
			configFile.setProductName(productName);
			configFile.setMissionId(m.group(1));
			configFile.setSatelliteId(m.group(2));
			configFile.setProductClass(m.group(4));
			configFile.setProductType(m.group(5));
			configFile.setExtension(FileExtension.valueOfIgnoreCase(m.group(6).toUpperCase()));
			configFile.setHasToExtractMetadata(false);
			if (isRoot && configFile.getExtension() != FileExtension.SAFE) {
				configFile.setHasToExtractMetadata(true);
			}
			if (!isRoot && filename.equalsIgnoreCase("manifest.safe")) {
				configFile.setHasToExtractMetadata(true);
			}
			if (file.isDirectory()) {
				configFile.setDirectory(true);
				configFile.setHasToBeStored(false);
			} else {
				configFile.setDirectory(false);
				configFile.setHasToBeStored(true);
			}

		} else {
			throw new FilePathException(relativePath, relativePath,
					"File does not match the configuration file pattern");
		}

		return configFile;
	}

	public ErdsSessionFileDescriptor buildErdsSessionFileDescriptor(File file)
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
			throw new IgnoredFileException(relativePath);
		}
		Pattern p = Pattern.compile(PATTERN_SESSION, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(relativePath);
		if (m.matches()) {
			// Ignore the IIF files
			if (m.group(11).toLowerCase().contains("iif_")) {
				throw new FilePathException(relativePath, relativePath, "IIF file");
			}

			ErdsSessionFileDescriptor descriptor = new ErdsSessionFileDescriptor();
			descriptor.setFilename(m.group(9));
			descriptor.setRelativePath(relativePath);
			descriptor.setProductName(m.group(9));
			descriptor.setExtension(FileExtension.valueOfIgnoreCase(m.group(12)));
			descriptor.setProductType(ErdsSessionFileType.valueFromExtension(descriptor.getExtension()));
			descriptor.setMissionId(m.group(1));
			descriptor.setSatelliteId(m.group(2));
			descriptor.setChannel(Integer.parseInt(m.group(7)));
			descriptor.setSessionIdentifier(m.group(4));
			descriptor.setKeyObjectStorage(m.group(4)+m.group(5)+m.group(6)+m.group(7)+m.group(8)+m.group(9));

			return descriptor;
		} else {
			throw new FilePathException(relativePath, relativePath,
					"File does not match the configuration file pattern");
		}
	}
}
