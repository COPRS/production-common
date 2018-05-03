package fr.viveris.s1pdgs.jobgenerator.service.s3;

import java.io.File;

import fr.viveris.s1pdgs.jobgenerator.exception.ObsS3Exception;

public interface S3Services {

	/**
	 * Download a file from object storage
	 * @param keyName
	 */
	public void downloadFile(String keyName, File output) throws ObsS3Exception;

	/**
	 * Get a file from object storage and save it in expectedFilePath
	 * @param keyName
	 */
	public File getFile(String keyName, String expectedFilePath) throws ObsS3Exception;

	/**
	 * Upload a file in object storage
	 * @param keyName
	 * @param uploadFile
	 */
	public void uploadFile(String keyName, File uploadFile) throws ObsS3Exception;
	
	/**
	 * Upload a file in object storage
	 * @param keyName
	 * @param uploadFile
	 */
	public boolean exist(String keyName) throws ObsS3Exception;
	
}
