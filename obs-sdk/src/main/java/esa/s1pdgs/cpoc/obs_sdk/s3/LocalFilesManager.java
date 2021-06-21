package esa.s1pdgs.cpoc.obs_sdk.s3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFilesManager {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFilesManager.class);

    private final Path localFilesLocation;

    private LocalFilesManager(Path localFilesLocation) {
        this.localFilesLocation = localFilesLocation;
    }

    public static LocalFilesManager createFor(final Path localFilesLocation) {
        LOG.info("creating local files manager for cache location {}", localFilesLocation);
        return new LocalFilesManager(localFilesLocation);
    }

    /**
     * @param in   content of the file to download as stream
     * @param name name of the file (may include prefix)
     * @return a {@link FileHandle} implementing {@link AutoCloseable} which deletes the File on {@link AutoCloseable#close()}
     * @throws IOException when the stream could not be downloaded as file
     */
    public FileHandle downLoadAndProvideAsFile(InputStream in, final String name) throws IOException {

        final Path lastPathElement = Paths.get(name).getFileName();
        LOG.info("downloading {} to {}", lastPathElement, localFilesLocation);

        final Path filePath = localFilesLocation.resolve(lastPathElement);
        final Md5SumCalculationHelper md5SumCalculationHelper = Md5SumCalculationHelper.createFor(in);

        Files.copy(md5SumCalculationHelper.getInputStream(), filePath);

        return new FileHandle(filePath.toFile(), md5SumCalculationHelper.getMd5Sum());
    }

    public static final class FileHandle  {

        private final File file;
        private final String md5Sum;

        private FileHandle(final File file, final String md5Sum) {
            this.file = file;
            this.md5Sum = md5Sum;
        }

        public File getFile() {
            return file;
        }

        public String getMd5Sum() {
            return md5Sum;
        }
    }

}