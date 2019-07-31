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
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;

/**
 * Class for managing files (read / write / delete)
 * 
 * @author Viveris Technologies
 */
public class FileUtils {
	private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Write the string into the file
     * 
     * @param fileToComplete
     * @param data
     * @throws InternalErrorException
     */
    public static void writeFile(final File fileToComplete, final String data)
            throws InternalErrorException {
        try {
            FileWriter fileWriter = new FileWriter(fileToComplete);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            try {
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                fileWriter.close();
            } catch (IOException e) {
                throw new InternalErrorException(
                        "Cannot write file " + fileToComplete.getAbsolutePath(),
                        e);
            } finally {
                if (fileWriter != null) {
                    fileWriter.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }

            }
        } catch (IOException e) {
            throw new InternalErrorException(
                    "Cannot close file " + fileToComplete.getAbsolutePath(), e);
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
        File file = new File(filePath);
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
        } catch (IOException ioe) {
            throw new InternalErrorException("Cannot read file for "
                    + file.getName() + ": " + ioe.getMessage(), ioe);
        }
    }
    
    public static void deleteWithRetries(final File file, int numRetries, long retrySleep) 
    		throws InternalErrorException, InterruptedException {    	
    	int attempt = 0;
    	while (true) {
    		try {
    			delete(file.getPath());
    			break;
    		} catch (IOException e) {
    			attempt++;  
    			if (attempt > numRetries) {
    				throw new InternalErrorException(
    						String.format(
    								"Error on deleting %s after %s attempts: %s", 
    								file,
    								String.valueOf(attempt),
    								LogUtils.toString(e)
    						)
    				);
    			}  			
    			if (LOG.isWarnEnabled()) {
        			LOG.warn("Error on deleting {} ({}/{}), retrying in {}ms: {}", file, attempt, numRetries+1, retrySleep, 
        					LogUtils.toString(e));
    			}
    			Thread.sleep(retrySleep);
    		}
    	}
    	
    }

    /**
     * Delete a directory and all its subdirectories
     * 
     * @param path
     * @throws IOException
     */
    public static void delete(final String path) throws IOException {
        Path pathObj = Paths.get(path);
        Files.walk(pathObj, FileVisitOption.FOLLOW_LINKS)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
