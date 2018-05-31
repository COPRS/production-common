package fr.viveris.s1pdgs.jobgenerator.service.s3;

import java.io.File;

import fr.viveris.s1pdgs.jobgenerator.exception.ObsS3Exception;

/**
 * Interface for accessing to object storage via S3 API
 * @author Cyrielle Gailliard
 *
 */
public interface S3Services {

	/**
	 * Get a file from object storage and save it in expectedFilePath
	 * @param keyName
	 */
	public File getFile(final String keyName, final String expectedFilePath) throws ObsS3Exception;
	
}
