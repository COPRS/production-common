package fr.viveris.s1pdgs.level0.wrapper.services.s3;

import java.io.File;

import fr.viveris.s1pdgs.level0.wrapper.model.exception.ObsS3Exception;

public interface S3Services {

	/**
	 * Download objects with key starting by the prefix in the directory and return
	 * the number of downloaded objects
	 * 
	 * @param prefixKey
	 * @param directoryPath
	 * @return
	 * @throws ObsS3Exception
	 */
	public int downloadFiles(String prefixKey, String directoryPath) throws ObsS3Exception;

	/**
	 * Upload file/directory into object storage and return the number of uploaded objects
	 * @param keyName
	 * @param uploadFile
	 * @return
	 * @throws ObsS3Exception
	 */
	public int uploadDirectory(String keyName, File uploadFile) throws ObsS3Exception;
	
	/**
	 * Upload a file in object storage
	 * 
	 * @param keyName
	 * @param uploadFile
	 */
	public void uploadFile(String keyName, File uploadFile) throws ObsS3Exception;
	
	public boolean exist(String keyName) throws ObsS3Exception;
	
	public int getNbObjects(String prefix) throws ObsS3Exception;

	public String getBucketName();

}
