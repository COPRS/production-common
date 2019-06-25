package esa.s1pdgs.cpoc.mdcatalog.extraction.files;

import java.io.File;
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
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L0OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L1OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L2OutputFileDescriptor;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;

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
	public ConfigFileDescriptor buildConfigFileDescriptor(File file) throws MetadataFilePathException, MetadataIgnoredFileException {

		// Extract object storage key
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= localDirectory.length()) {
			throw new MetadataFilePathException(absolutePath, "CONFIG", "File is not in root directory");
		}
		String relativePath = absolutePath.substring(localDirectory.length());
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
			configFile.setProductType(m.group(5));
			configFile.setProductFamily(ProductFamily.AUXILIARY_FILE);
			configFile.setExtension(FileExtension.valueOfIgnoreCase(m.group(6).toUpperCase()));

		} else {
			throw new MetadataFilePathException(relativePath, "CONFIG",
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
	 * @throws IllegalFileExtension
	 */
	public EdrsSessionFileDescriptor buildEdrsSessionFileDescriptor(File file)
			throws MetadataFilePathException, MetadataIgnoredFileException, MetadataIllegalFileExtension {
		// Extract relative path
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= localDirectory.length()) {
			throw new MetadataFilePathException(absolutePath, "SESSION", "File is not in root directory");
		}
		String relativePath = absolutePath.substring(localDirectory.length());
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new MetadataIgnoredFileException(file.getName());
		}

		Matcher m = pattern.matcher(relativePath);
		if (m.matches()) {
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
			descriptor.setProductFamily(ProductFamily.EDRS_SESSION);

			return descriptor;
		} else {
			throw new MetadataFilePathException(relativePath, "SESSION",
					"File does not match the configuration file pattern");
		}
	}

	public L0OutputFileDescriptor buildL0OutputFileDescriptor(File file, ProductDto product)
			throws MetadataFilePathException, MetadataIgnoredFileException {
		// Extract relative path
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= localDirectory.length()) {
			throw new MetadataFilePathException(absolutePath, "L0_PRODUCT", "File is not in root directory");
		}
		String relativePath = absolutePath.substring(localDirectory.length());
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new MetadataIgnoredFileException(file.getName());
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
			l0Descriptor.setMode(product.getMode());
			l0Descriptor.setMissionId(m.group(1));
			l0Descriptor.setSatelliteId(m.group(2));
			l0Descriptor.setSwathtype(m.group(3));
			l0Descriptor.setResolution(m.group(5));
			l0Descriptor.setProductClass(m.group(7));
			l0Descriptor.setProductType(m.group(3) + "_" + m.group(4) + m.group(5) + "_" + m.group(6) + m.group(7));
			l0Descriptor.setPolarisation(m.group(8));
			l0Descriptor.setDataTakeId(m.group(12));
			l0Descriptor.setKeyObjectStorage(productName);
			l0Descriptor.setExtension(FileExtension.valueOfIgnoreCase(m.group(13)));
			if("S".equals(m.group(7))) {
	            l0Descriptor.setProductFamily(ProductFamily.L0_SLICE);
			} else {
			    l0Descriptor.setProductFamily(ProductFamily.L0_ACN);
			}

		} else {
			throw new MetadataFilePathException(relativePath, "L0_PRODUCT",
					"File does not match the configuration file pattern");
		}

		return l0Descriptor;
	}
	
	public L0OutputFileDescriptor buildL0SegmentFileDescriptor(File file, LevelSegmentDto product)
            throws MetadataFilePathException, MetadataIgnoredFileException {
        // Extract relative path
        String absolutePath = file.getAbsolutePath();
        if (absolutePath.length() <= localDirectory.length()) {
            throw new MetadataFilePathException(absolutePath, "L0_SEGMENT", "File is not in root directory");
        }
        String relativePath = absolutePath.substring(localDirectory.length());
        relativePath = relativePath.replace("\\", "/");

        // Ignored if directory
        if (file.isDirectory()) {
            throw new MetadataIgnoredFileException(file.getName());
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
            l0Descriptor.setMode(product.getMode());
            l0Descriptor.setMissionId(m.group(1));
            l0Descriptor.setSatelliteId(m.group(2));
            l0Descriptor.setSwathtype(m.group(3));
            l0Descriptor.setResolution(m.group(5));
            l0Descriptor.setProductClass(m.group(7));
            l0Descriptor.setProductType(m.group(3) + "_" + m.group(4) + m.group(5) + "_" + m.group(6) + m.group(7));
            l0Descriptor.setPolarisation(m.group(8));
            l0Descriptor.setDataTakeId(m.group(12));
            l0Descriptor.setKeyObjectStorage(productName);
            l0Descriptor.setExtension(FileExtension.valueOfIgnoreCase(m.group(13)));
            l0Descriptor.setProductFamily(ProductFamily.L0_SEGMENT);

        } else {
            throw new MetadataFilePathException(relativePath, "L0_SEGMENT",
                    "File does not match the configuration file pattern");
        }

        return l0Descriptor;
    }

	public L1OutputFileDescriptor buildL1OutputFileDescriptor(File file, ProductDto product)
			throws MetadataFilePathException, MetadataIgnoredFileException {
		// Extract relative path
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= localDirectory.length()) {
			throw new MetadataFilePathException(absolutePath, "L1_PRODUCT", "File is not in root directory");
		}
		String relativePath = absolutePath.substring(localDirectory.length());
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new MetadataIgnoredFileException(file.getName());
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
			l1Descriptor.setMode(product.getMode());
			l1Descriptor.setMissionId(m.group(1));
			l1Descriptor.setSatelliteId(m.group(2));
			l1Descriptor.setSwathtype(m.group(3));
			l1Descriptor.setResolution(m.group(5));
			l1Descriptor.setProductClass(m.group(7));
			l1Descriptor.setProductType(m.group(3) + "_" + m.group(4) + m.group(5) + "_" + m.group(6) + m.group(7));
			l1Descriptor.setPolarisation(m.group(8));
			l1Descriptor.setDataTakeId(m.group(12));
			l1Descriptor.setKeyObjectStorage(productName);
			l1Descriptor.setExtension(FileExtension.valueOfIgnoreCase(m.group(13)));
			if("S".equals(m.group(7))) {
                l1Descriptor.setProductFamily(ProductFamily.L1_SLICE);
            } else {
                l1Descriptor.setProductFamily(ProductFamily.L1_ACN);
            }

		} else {
			throw new MetadataFilePathException(relativePath, "L1_PRODUCT",
					"File does not match the configuration file pattern");
		}
		return l1Descriptor;
	}
	
	public L2OutputFileDescriptor buildL2OutputFileDescriptor(File file, ProductDto product)
			throws MetadataFilePathException, MetadataIgnoredFileException {
		// Extract relative path
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= localDirectory.length()) {
			throw new MetadataFilePathException(absolutePath, "L2_PRODUCT", "File is not in root directory");
		}
		String relativePath = absolutePath.substring(localDirectory.length());
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new MetadataIgnoredFileException(file.getName());
		}
		L2OutputFileDescriptor l2Descriptor = null;
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
			l2Descriptor = new L2OutputFileDescriptor();
			l2Descriptor.setProductName(productName);
			l2Descriptor.setRelativePath(relativePath);
			l2Descriptor.setFilename(filename);
			l2Descriptor.setMode(product.getMode());
			l2Descriptor.setMissionId(m.group(1));
			l2Descriptor.setSatelliteId(m.group(2));
			l2Descriptor.setSwathtype(m.group(3));
			l2Descriptor.setResolution(m.group(5));
			l2Descriptor.setProductClass(m.group(7));
			l2Descriptor.setProductType(m.group(3) + "_" + m.group(4) + m.group(5) + "_" + m.group(6) + m.group(7));
			l2Descriptor.setPolarisation(m.group(8));
			l2Descriptor.setDataTakeId(m.group(12));
			l2Descriptor.setKeyObjectStorage(productName);
			l2Descriptor.setExtension(FileExtension.valueOfIgnoreCase(m.group(13)));
			if ("S".equals(m.group(7))) {
				l2Descriptor.setProductFamily(ProductFamily.L2_SLICE);
			} else {
				l2Descriptor.setProductFamily(ProductFamily.L2_ACN);
			}

		} else {
			throw new MetadataFilePathException(relativePath, "L2_PRODUCT",
					"File does not match the configuration file pattern");
		}
		return l2Descriptor;
	}
}
