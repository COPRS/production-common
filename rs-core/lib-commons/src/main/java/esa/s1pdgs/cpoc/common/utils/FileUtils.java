package esa.s1pdgs.cpoc.common.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;

/**
 * Class for managing files (read / write / delete)
 * 
 * @author Viveris Technologies
 */
public class FileUtils {
    /**
     * Write the string into the file
     * 
     * @param file
     * @param data
     * @throws InternalErrorException
     */
    public static void writeFile(final File file, final String data)
            throws InternalErrorException {
        try {
            final FileWriter fileWriter = new FileWriter(file);
            final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            try {
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                fileWriter.close();
            } catch (final IOException e) {
                throw new InternalErrorException(
                        "Cannot write file " + file.getAbsolutePath(),
                        e);
            } finally {
                if (fileWriter != null) {
                    fileWriter.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }

            }
        } catch (final IOException e) {
            throw new InternalErrorException(
                    "Cannot close file " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Write the string into the file
     * 
     * @param filePath
     * @param data
     * @throws InternalErrorException
     */
    public static void writeFile(final String filePath, final String data)
            throws InternalErrorException {
        final File file = new File(filePath);
        writeFile(file, data);
    }

    /**
     * Read the file into a string
     * 
     * @param file
     * @return
     * @throws InternalErrorException
     */
    public static String readFile(final File file)
            throws InternalErrorException {
        try {
            return new String(Files.readAllBytes(file.toPath()),
                    Charset.defaultCharset());
        } catch (final IOException ioe) {
            throw new InternalErrorException("Cannot read file for "
                    + file.getName() + ": " + ioe.getMessage(), ioe);
        }
    }
    
    public static void deleteWithRetries(final File file, final int numRetries, final long retrySleep) 
    		throws InterruptedException {
    	Retries.performWithRetries(
    			() -> {	delete(file.getPath()) ; return null;}, 
    			"Deletion of " + file,
    			numRetries, 
    			retrySleep
    	);    	
    }

    /**
     * Delete a directory and all its subdirectories
     * 
     * @param path
     * @throws IOException
     */
    public static void delete(final String path){
        try {
			final Path pathObj = Paths.get(path);
			Files.walk(pathObj, FileVisitOption.FOLLOW_LINKS)
			        .sorted(Comparator.reverseOrder())
			        .map(Path::toFile)
			        .forEach(File::delete);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
    }
    
    public static final long size(final Collection<File> files) {
    	long size = 0;
    	for (final File file : files) {
    		if (file.isDirectory()) {
    			size += size(file.listFiles());
    		} else {
    			size += file.length();
    		}
    	}
    	return size;
    }
    
    public static final long size(final File ... files) {
    	if (files == null) {
    		return 0L;
    	}
    	return size(Arrays.asList(files));
    }
    
    public static final File createTmpDir() {
    	try {
			return Files.createTempDirectory("tmp").toFile();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
    }
}
