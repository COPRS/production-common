package fr.viveris.s1pdgs.ingestor.services.s3;

import java.io.File;

public interface S3Services {

	/**
	 * Download a file from object storage
	 * @param keyName
	 */
	public void downloadFile(String keyName);

	/**
	 * Upload a file in object storage
	 * @param keyName
	 * @param uploadFile
	 */
	public void uploadFile(String keyName, File uploadFile);
}
