package esa.s1pdgs.cpoc.ingestor.files.services;

import java.io.File;

import esa.s1pdgs.cpoc.common.errors.processing.IngestorFilePathException;
import esa.s1pdgs.cpoc.common.errors.processing.IngestorIgnoredFileException;
import esa.s1pdgs.cpoc.ingestor.files.model.FileDescriptor;

/**
 * File descriptor
 * 
 * @author Cyrielle
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
    protected AbstractFileDescriptorService(final String directory,
            final String family) {
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
    public FileDescriptor extractDescriptor(final File file)
            throws IngestorFilePathException, IngestorIgnoredFileException {
        String absolutePath = file.getAbsolutePath();

        // Extract object storage key
        if (!absolutePath.contains(directory)) {
            throw new IngestorFilePathException(absolutePath, family,
                    "File is not in root directory");
        }
        String relativePath = absolutePath.substring(directory.length());
        relativePath = relativePath.replace("\\", "/");

        // Ignored if directory
        if (file.isDirectory()) {
            throw new IngestorIgnoredFileException(file.getName());
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
    protected abstract FileDescriptor buildDescriptor(final String relativePath)
            throws IngestorFilePathException;

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
