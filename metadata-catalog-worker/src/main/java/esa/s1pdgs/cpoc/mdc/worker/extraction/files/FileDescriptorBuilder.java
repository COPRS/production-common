package esa.s1pdgs.cpoc.mdc.worker.extraction.files;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataFilePathException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataIgnoredFileException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataIllegalFileExtension;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.AuxDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.S3FileDescriptor;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

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

	@Override
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
	 * @throws FilePathException if we have
	 */
	public AuxDescriptor buildAuxDescriptor(final File file)
			throws MetadataFilePathException, MetadataIgnoredFileException {
		// Extract object storage key
		final String absolutePath = file.getAbsolutePath();
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
		AuxDescriptor configFile = null;
		final Matcher m = pattern.matcher(relativePath);
		if (m.matches()) {
			// Extract product name
			String productName = relativePath;
			final int indexFirstSeparator = relativePath.indexOf("/");
			if (indexFirstSeparator != -1) {
				productName = relativePath.substring(0, indexFirstSeparator);
			}
			// Extract filename
			String filename = relativePath;
			final int indexLastSeparator = relativePath.lastIndexOf("/");
			if (indexLastSeparator != -1) {
				filename = relativePath.substring(indexLastSeparator + 1);
			}
			// Build descriptor
			configFile = new AuxDescriptor();
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
			throw new MetadataFilePathException(relativePath, "CONFIG",
					String.format("File %s does not match the configuration file pattern %s", relativePath, pattern));
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
	public EdrsSessionFileDescriptor buildEdrsSessionFileDescriptor(final File file,
			final Map<String, String> metadataFromPath, final CatalogJob catJob)
			throws MetadataFilePathException, MetadataIgnoredFileException, MetadataIllegalFileExtension {
		// Extract relative path
		final String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= localDirectory.getAbsolutePath().length()) {
			throw new MetadataFilePathException(absolutePath, "SESSION", "File is not in root directory");
		}
		String relativePath = absolutePath.substring(localDirectory.getAbsolutePath().length() + 1);
		relativePath = relativePath.replace("\\", "/");

		final String dsiborDsdbName = getFilename(relativePath);

		// Ignored if directory
		if (file.isDirectory()) {
			throw new MetadataIgnoredFileException(file.getName());
		}
		final Matcher m = pattern.matcher(relativePath);
		if (m.matches()) {
			final String suffix = file.getName().substring(file.getName().lastIndexOf('.') + 1);
			final FileExtension ext = FileExtension.valueOfIgnoreCase(suffix);

			final EdrsSessionFileDescriptor descriptor = new EdrsSessionFileDescriptor();
			descriptor.setFilename(file.getName());
			descriptor.setRelativePath(catJob.getRelativePath());
			descriptor.setKeyObjectStorage(catJob.getKeyObjectStorage());
			descriptor.setProductFamily(catJob.getProductFamily());
			descriptor.setExtension(ext);
			descriptor.setEdrsSessionFileType(EdrsSessionFileType.valueFromExtension(ext));

			descriptor.setMissionId(metadataFromPath.get("missionId"));
			descriptor.setSatelliteId(metadataFromPath.get("satelliteId"));
			descriptor.setSessionIdentifier(metadataFromPath.get("sessionId"));
			// descriptor.setProductName(descriptor.getSessionIdentifier());
			descriptor.setProductName(dsiborDsdbName);
			descriptor.setStationCode(catJob.getStationName());
			descriptor.setChannel(Integer.parseInt(metadataFromPath.get("channelId")));

			return descriptor;
		} else {
			throw new MetadataFilePathException(relativePath, "SESSION",
					String.format("File %s does not match the configuration file pattern %s", relativePath, pattern));
		}
	}

	private final String getFilename(final String relativePath) {
		final Path path = Paths.get(relativePath);

		if (path == null) {
			throw new IllegalArgumentException(String.format("Path %s evaluated to null", path));
		}

		final Path filename = path.getFileName();
		if (filename == null) {
			throw new IllegalArgumentException(String.format("Filename from Path %s evaluated to null", path));
		}
		return filename.toString();
	}
	
	public OutputFileDescriptor buildOutputFileDescriptor(final File file, final CatalogJob product, final ProductFamily productFamily)
            throws MetadataFilePathException, MetadataIgnoredFileException {
        // Extract relative path
        final String absolutePath = file.getAbsolutePath();
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
        final Matcher m = pattern.matcher(relativePath);
        if (m.matches()) {
            // Extract product name
            String productName = relativePath;
            final int indexFirstSeparator = relativePath.indexOf("/");
            if (indexFirstSeparator != -1) {
                productName = relativePath.substring(0, indexFirstSeparator);
            }
            // Extract filename
            String filename = relativePath;
            final int indexLastSeparator = relativePath.lastIndexOf("/");
            if (indexLastSeparator != -1) {
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
			throw new MetadataFilePathException(
					relativePath, 
					productFamily.name(),
					String.format("File %s does not match the configuration file pattern %s", relativePath, pattern)
			);
        }

        return descriptor;
    }

	/**
	 * Builds a S3FileDescriptor based on the given file.
	 *
	 * @param file file, for which the descriptor should be generated
	 * @return Descriptor, containing information extracted from the filename
	 */
	public S3FileDescriptor buildS3FileDescriptor(File file, CatalogJob product, ProductFamily productFamily)
			throws MetadataFilePathException, MetadataIgnoredFileException {
		final String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= localDirectory.getAbsolutePath().length()) {
			throw new MetadataFilePathException(absolutePath, productFamily.name(), "File is not in root directory");
		}
		String relativePath = absolutePath.substring(localDirectory.getAbsolutePath().length() + 1);
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new MetadataIgnoredFileException(file.getName());
		}

		// Check if key matches the pattern
		S3FileDescriptor descriptor = null;
		final Matcher m = pattern.matcher(relativePath);
		if (m.matches()) {
			// Extract product name
			String productName = relativePath;
			final int indexFirstSeparator = relativePath.indexOf("/");
			if (indexFirstSeparator != -1) {
				productName = relativePath.substring(0, indexFirstSeparator);
			}
			// Extract filename
			String filename = relativePath;
			final int indexLastSeparator = relativePath.lastIndexOf("/");
			if (indexLastSeparator != -1) {
				filename = relativePath.substring(indexLastSeparator + 1);
			}
			// Determine file extension
			FileExtension extension = FileExtension.valueOfIgnoreCase(m.group(12));

			descriptor = new S3FileDescriptor();
			descriptor.setProductType(m.group(3));
			descriptor.setProductClass(m.group(4));
			descriptor.setRelativePath(relativePath);
			descriptor.setFilename(filename);
			descriptor.setExtension(extension);
			descriptor.setProductName(productName);
			descriptor.setMissionId(m.group(1));
			descriptor.setSatelliteId(m.group(2));
			descriptor.setKeyObjectStorage(productName);
			descriptor.setProductFamily(productFamily);
			descriptor.setMode(product.getMode());
			descriptor.setInstanceId(m.group(9));
			descriptor.setGeneratingCentre(m.group(10));
			descriptor.setClassId(m.group(11));
		} else {
			throw new MetadataFilePathException(relativePath, productFamily.name(),
					String.format("File %s does not match the configuration file pattern %s", relativePath, pattern));
		}

		return descriptor;
	}
}
