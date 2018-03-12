package fr.viveris.s1pdgs.mdcatalog.services.files;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.viveris.s1pdgs.mdcatalog.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.EdrsSessionFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.EdrsSessionFileType;
import fr.viveris.s1pdgs.mdcatalog.model.FileExtension;
import fr.viveris.s1pdgs.mdcatalog.model.L0OutputFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.L1OutputFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.exception.FilePathException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.IgnoredFileException;

/**
 * Service to build file descriptor
 * 
 * @author Cyrielle Gailliard
 *
 */
public class FileDescriptorBuilder {

	/**
	 * Pattern for files to extract data
	 */
	private final Pattern pattern;

	/**
	 * Local directory for files
	 */
	private final String localDirectory;

	/**
	 * Constructor
	 * 
	 * @param localDirectory
	 * @param pattern
	 */
	public FileDescriptorBuilder(final String localDirectory, final Pattern pattern) {
		this.localDirectory = localDirectory;
		this.pattern = pattern;
	}
	
	public String toString() {
		return String.format("localDirectory : %s, pattern : %s", localDirectory, pattern.toString());
	}

	/**
	 * Build descriptor for configuration files from path file
	 * 
	 * @param file
	 * 
	 * @return the config file descriptor
	 * 
	 * @throws FilePathException
	 *             if we have
	 */
	public ConfigFileDescriptor buildConfigFileDescriptor(File file) throws FilePathException, IgnoredFileException {

		// Extract object storage key
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= localDirectory.length()) {
			throw new FilePathException(absolutePath, absolutePath, "Filename length is too short");
		}
		String relativePath = absolutePath.substring(localDirectory.length());
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new IgnoredFileException(relativePath);
		}

		// Check if key matches the pattern
		ConfigFileDescriptor configFile = null;
		Matcher m = pattern.matcher(relativePath);
		if (m.matches()) {
			// Extract product name
			String productName = relativePath;
			int indexFirstSeparator = relativePath.indexOf("/");
			if (indexFirstSeparator != -1) {
				productName = relativePath.substring(0, indexFirstSeparator);
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

		} else {
			throw new FilePathException(relativePath, relativePath,
					"File does not match the configuration file pattern");
		}

		return configFile;
	}

	/**
	 * Function which build the file descriptor for edrs session or raw file
	 * 
	 * @param file
	 * 
	 * @return the edrs file descriptor
	 * 
	 * @throws FilePathException
	 * @throws IgnoredFileException
	 */
	public EdrsSessionFileDescriptor buildEdrsSessionFileDescriptor(File file)
			throws FilePathException, IgnoredFileException {
		// Extract relative path
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= localDirectory.length()) {
			throw new FilePathException(absolutePath, absolutePath, "Filename too short");
		}
		String relativePath = absolutePath.substring(localDirectory.length());
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new IgnoredFileException(relativePath);
		}

		Matcher m = pattern.matcher(relativePath);
		if (m.matches()) {
			// Ignore the IIF files
			if (m.group(11).toLowerCase().contains("iif_")) {
				throw new FilePathException(relativePath, relativePath, "IIF file");
			}

			EdrsSessionFileDescriptor descriptor = new EdrsSessionFileDescriptor();
			descriptor.setFilename(m.group(9));
			descriptor.setRelativePath(relativePath);
			descriptor.setProductName(m.group(9));
			descriptor.setExtension(FileExtension.valueOfIgnoreCase(m.group(12)));
			descriptor.setProductType(EdrsSessionFileType.valueFromExtension(descriptor.getExtension()));
			descriptor.setMissionId(m.group(1));
			descriptor.setSatelliteId(m.group(2));
			descriptor.setChannel(Integer.parseInt(m.group(7)));
			descriptor.setSessionIdentifier(m.group(4));
			descriptor.setKeyObjectStorage(relativePath);

			return descriptor;
		} else {
			throw new FilePathException(relativePath, relativePath,
					"File does not match the configuration file pattern");
		}
	}
	
	public L0OutputFileDescriptor buildL0OutputFileDescriptor (File file) 
			throws FilePathException, IgnoredFileException {
		// Extract relative path
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= localDirectory.length()) {
			throw new FilePathException(absolutePath, absolutePath, "Filename too short");
		}
		String relativePath = absolutePath.substring(localDirectory.length());
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new IgnoredFileException(relativePath);
		}
		L0OutputFileDescriptor l0Descriptor = null;
		Matcher m = pattern.matcher(relativePath);
		if (m.matches()) {
			// Extract product name
			String productName = relativePath;
			int indexFirstSeparator = relativePath.indexOf("/");
			if (indexFirstSeparator != -1) {
				productName = relativePath.substring(0, indexFirstSeparator);
			}
			// Extract filename
			String filename = relativePath;
			int indexLastSeparator = relativePath.lastIndexOf("/");
			if (indexFirstSeparator != -1) {
				filename = relativePath.substring(indexLastSeparator + 1);
			}
			l0Descriptor = new L0OutputFileDescriptor();
			l0Descriptor.setProductName(productName);
			l0Descriptor.setRelativePath(relativePath);
			l0Descriptor.setFilename(filename);
			l0Descriptor.setMissionId(m.group(1));
			l0Descriptor.setSatelliteId(m.group(2));
			l0Descriptor.setSwathtype(m.group(3));
			l0Descriptor.setResolution(m.group(5));
			l0Descriptor.setProductClass(m.group(7));
			l0Descriptor.setProductType(m.group(3)+"_"+m.group(4)+m.group(5)+"_"+m.group(6)+m.group(7));
			l0Descriptor.setPolarisation(m.group(8));
			l0Descriptor.setDataTakeId(m.group(12));
		
		} else {
			throw new FilePathException(relativePath, relativePath,
					"File does not match the configuration file pattern");
		}
		
		return l0Descriptor;		
	}
	
	public L1OutputFileDescriptor buildL1OutputFileDescriptor (File file) 
			throws FilePathException, IgnoredFileException {
		// Extract relative path
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= localDirectory.length()) {
			throw new FilePathException(absolutePath, absolutePath, "Filename too short");
		}
		String relativePath = absolutePath.substring(localDirectory.length());
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new IgnoredFileException(relativePath);
		}
		L1OutputFileDescriptor l1Descriptor = null;
		Matcher m = pattern.matcher(relativePath);
		if (m.matches()) {
			// Extract product name
			String productName = relativePath;
			int indexFirstSeparator = relativePath.indexOf("/");
			if (indexFirstSeparator != -1) {
				productName = relativePath.substring(0, indexFirstSeparator);
			}
			// Extract filename
			String filename = relativePath;
			int indexLastSeparator = relativePath.lastIndexOf("/");
			if (indexFirstSeparator != -1) {
				filename = relativePath.substring(indexLastSeparator + 1);
			}
			l1Descriptor = new L1OutputFileDescriptor();
			l1Descriptor.setProductName(productName);
			l1Descriptor.setRelativePath(relativePath);
			l1Descriptor.setFilename(filename);
			l1Descriptor.setMissionId(m.group(1));
			//l1Descriptor.setSatelliteId(m.group(2));
			l1Descriptor.setSwathtype(m.group(2));
			l1Descriptor.setResolution(m.group(4));
			l1Descriptor.setProductClass(m.group(6));
			l1Descriptor.setProductType(m.group(2)+"_"+m.group(3)+m.group(4)+"_"+m.group(5)+m.group(6));
			l1Descriptor.setPolarisation(m.group(6));
			l1Descriptor.setDataTakeId(m.group(11));
						
		} else {
			throw new FilePathException(relativePath, relativePath,
					"File does not match the configuration file pattern");
		}
		return l1Descriptor;
	}
}
