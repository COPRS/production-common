package esa.s1pdgs.cpoc.mdcatalog.extraction.files;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataFilePathException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataIgnoredFileException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataIllegalFileExtension;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.ConfigFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;

/**
 * Service to build file descriptor
 * 
 * @author Cyrielle Gailliard
 *
 */
public class FileDescriptorBuilder {

	
	private static final List<String> AUX_ECE_TYPES = Arrays.asList("AMV_ERRMAT", "AMH_ERRMAT");
	
	/**
	 * Pattern for files to extract data
	 */
	private final Pattern pattern;

	/**
	 * Local directory for files
	 */
	private final File localDirectory;

	/**
	 * Constructor
	 * 
	 * @param localDirectory
	 * @param pattern
	 */
	public FileDescriptorBuilder(final File localDirectory, final Pattern pattern) {
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
	public ConfigFileDescriptor buildConfigFileDescriptor(File file) throws MetadataFilePathException, MetadataIgnoredFileException {
		// Extract object storage key
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= localDirectory.getAbsolutePath().length()) {
			throw new MetadataFilePathException(absolutePath, "CONFIG", "File is not in root directory");
		}
		String relativePath = absolutePath.substring(localDirectory.getAbsolutePath().length() + 1);
		relativePath = relativePath.replace("\\", "/");

		
		
		// Ignored if directory
		if (file.isDirectory()) {
			throw new MetadataIgnoredFileException(file.getName());
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
			configFile.setKeyObjectStorage(productName);
			configFile.setProductName(productName);
			configFile.setMissionId(m.group(1));
			configFile.setSatelliteId(m.group(2));
			configFile.setProductClass(m.group(4));
			
			String typeString = m.group(5);
			
			if (AUX_ECE_TYPES.contains(typeString)) {
				typeString = "AUX_ECE";
			}			
			configFile.setProductType(typeString);
			configFile.setProductFamily(ProductFamily.AUXILIARY_FILE);
			configFile.setExtension(FileExtension.valueOfIgnoreCase(m.group(6).toUpperCase()));

		} else {
			throw new MetadataFilePathException(
					relativePath, 
					"CONFIG", 
					String.format("File %s does not match the configuration file pattern %s", relativePath, pattern)
			);
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
	 * @throws IllegalFileExtension
	 */
	public EdrsSessionFileDescriptor buildEdrsSessionFileDescriptor(File file)
			throws MetadataFilePathException, MetadataIgnoredFileException, MetadataIllegalFileExtension {
		// Extract relative path
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= localDirectory.getAbsolutePath().length()) {
			throw new MetadataFilePathException(absolutePath, "SESSION", "File is not in root directory");
		}
		String relativePath = absolutePath.substring(localDirectory.getAbsolutePath().length() + 1);
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new MetadataIgnoredFileException(file.getName());
		}
		Matcher m = pattern.matcher(relativePath);
		if (m.matches()) {
			EdrsSessionFileDescriptor descriptor = new EdrsSessionFileDescriptor();
			descriptor.setFilename(m.group(6));
			descriptor.setRelativePath(relativePath);
			descriptor.setProductName(m.group(6));
			descriptor.setExtension(FileExtension.valueOfIgnoreCase(m.group(9)));
			descriptor.setEdrsSessionFileType(EdrsSessionFileType.valueFromExtension(descriptor.getExtension()));
//			descriptor.setMissionId(m.group(1));
//			descriptor.setSatelliteId(m.group(2));
			descriptor.setChannel(Integer.parseInt(m.group(4)));
			descriptor.setSessionIdentifier(m.group(1));
			descriptor.setKeyObjectStorage(relativePath);
			descriptor.setProductFamily(ProductFamily.EDRS_SESSION);
			
				
			
			return descriptor;
		} else {
			throw new MetadataFilePathException(relativePath, "SESSION",
					"File does not match the configuration file pattern");
		}
	}
	
	public OutputFileDescriptor buildOutputFileDescriptor(File file, ProductionEvent product, ProductFamily productFamily)
            throws MetadataFilePathException, MetadataIgnoredFileException {
        // Extract relative path
        String absolutePath = file.getAbsolutePath();
        if (absolutePath.length() <= localDirectory.getAbsolutePath().length()) {
            throw new MetadataFilePathException(absolutePath, productFamily.name(), "File is not in root directory");
        }
        String relativePath = absolutePath.substring(localDirectory.getAbsolutePath().length() + 1);
        relativePath = relativePath.replace("\\", "/");

        // Ignored if directory
        if (file.isDirectory()) {
            throw new MetadataIgnoredFileException(file.getName());
        }
        OutputFileDescriptor descriptor = null;
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
            descriptor = new OutputFileDescriptor();
            descriptor.setProductName(productName);
            descriptor.setRelativePath(relativePath);
            descriptor.setFilename(filename);
            descriptor.setMode(product.getMode());
            descriptor.setMissionId(m.group(1));
            descriptor.setSatelliteId(m.group(2));
            descriptor.setSwathtype(m.group(3));
            descriptor.setResolution(m.group(5));
            descriptor.setProductClass(m.group(7));
            descriptor.setProductType(m.group(3) + "_" + m.group(4) + m.group(5) + "_" + m.group(6) + m.group(7));
            descriptor.setPolarisation(m.group(8));
            descriptor.setDataTakeId(m.group(12));
            descriptor.setKeyObjectStorage(productName);
            descriptor.setExtension(FileExtension.valueOfIgnoreCase(m.group(13)));
            descriptor.setProductFamily(productFamily);

        } else {
            throw new MetadataFilePathException(relativePath, productFamily.name(),
                    "File does not match the configuration file pattern");
        }

        return descriptor;
    }

}
