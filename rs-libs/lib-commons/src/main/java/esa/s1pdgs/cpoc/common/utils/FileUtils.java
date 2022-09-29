package esa.s1pdgs.cpoc.common.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;

/**
 * Class for managing files (read / write / delete)
 * 
 * @author Viveris Technologies
 */
public class FileUtils {
	
	  static class LinkProduct extends SimpleFileVisitor<Path>
	  {
	    private Path source;
	    private Path target;
	    
	    public LinkProduct(final Path source, final Path target) {
			this.source = source;
			this.target = target;
		}

		@Override
	    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
	        throws IOException
	    {
	      final Path targetPath = target.resolve(source.relativize(dir));
	      if (!Files.exists(targetPath))
	      {
	        Files.createDirectory(targetPath);
	      }
	      return FileVisitResult.CONTINUE;
	    }

	    @Override
	    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
	    {
	      final Path targetPath = target.resolve(source.relativize(file));
	      if (Files.exists(targetPath))
	      {
	        Files.delete(targetPath);
	      }
	      Files.createLink(targetPath, file);
	      return FileVisitResult.CONTINUE;
	    }
	  }
	  
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
    
    public static final void hardlink(final File src, final File dest) {
        try {
          Files.walkFileTree(src.toPath(), new LinkProduct(src.toPath(), dest.toPath()));
        }
        catch (final IOException ex)  {
          throw new RuntimeException(
        		  String.format(
        				  "Error creating hardlink from %s to %s: %s", 
        				  src,
        				  dest,
        				  ex.getMessage()
        		  ),
        		  ex
          );
        }
    }
    
    public static final List<File> list(final File directory, final FilenameFilter filter) {
    	// filter may be null for no filtering
    	final File[] content = directory.listFiles(filter);
    	if (content == null) {
    		return Collections.emptyList();
    	}
    	return Arrays.asList(content);
    }
}
