package fr.viveris.s1pdgs.ingestor.services.s3;

import java.io.File;

import fr.viveris.s1pdgs.ingestor.model.exception.ObjectStorageException;

public interface S3Services {

	/**
	 * Download a file from object storage
	 * @param keyName
	 */
	public void downloadFile(String keyName) throws ObjectStorageException;

	/**
	 * Upload a file in object storage
	 * @param keyName
	 * @param uploadFile
	 */
	public void uploadFile(String keyName, File uploadFile) throws ObjectStorageException;
	
	/**
	 * Upload a file in object storage
	 * @param keyName
	 * @param uploadFile
	 */
	public boolean exist(String keyName) throws ObjectStorageException;
	
}
