package esa.s1pdgs.cpoc.obs_sdk.s3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFilesManager {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFilesManager.class);

    private final Path localFilesLocation;

    public LocalFilesManager(Path localFilesLocation) {
        this.localFilesLocation = localFilesLocation;
    }

    /**
     * @param in   content of the file to download as stream
     * @param name name of the file
     * @return a {@link FileHandle} implementing {@link AutoCloseable} which deletes the File on {@link AutoCloseable#close()}
     * @throws IOException when the stream could not be downloaded as file
     */
    public FileHandle downLoadAndProvideAsFile(InputStream in, final String name) throws IOException {

        LOG.info("downloading {} to {}", name, localFilesLocation);

        final Path filePath = localFilesLocation.resolve(name);
        final Md5SumCalculationHelper md5SumCalculationHelper = Md5SumCalculationHelper.createFor(in);

        Files.copy(md5SumCalculationHelper.getInputStream(), filePath);

        return new FileHandle(filePath.toFile(), md5SumCalculationHelper.getMd5Sum());
    }

    public static final class FileHandle implements AutoCloseable {

        private final File file;
        private final String md5Sum;

        private FileHandle(final File file, final String md5Sum) {
            this.file = file;
            this.md5Sum = md5Sum;
        }

        @Override
        public void close() throws Exception {
            try {
                if (!file.delete()) {
                    LOG.error("the file {} has not been deleted", file.getName());
                } else {
                    LOG.info("deleted {}", file.getName());
                }
            } catch (Exception e) {
                LOG.error("the file {} could not be deleted", file.getName(), e);
            }
        }

        public File getFile() {
            return file;
        }

        public String getMd5Sum() {
            return md5Sum;
        }
    }

    public static class Md5SumCalculationHelper {
        private final DigestInputStream digestIn;

        public static Md5SumCalculationHelper createFor(final InputStream in) {
            return new Md5SumCalculationHelper(in);
        }

        private Md5SumCalculationHelper(final InputStream in) {
            try {
                this.digestIn = new DigestInputStream(in, MessageDigest.getInstance("MD5"));
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("could not create message digest for algorithm MD5");
            }
        }

        public InputStream getInputStream() {
            return digestIn;
        }

        public String getMd5Sum() {
            {
                final byte[] hash = digestIn.getMessageDigest().digest();
                return Hex.encodeHexString(hash, true);
            }
        }
    }
}