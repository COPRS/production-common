package fr.viveris.s1pdgs.ingestor.files.services;

import java.io.File;

import fr.viveris.s1pdgs.ingestor.exceptions.ObjectStorageException;

/**
 * Interface for accessing to the object storage
 * @author Cyrielle Gailliard
 *
 */
public interface ObsServices {

	/**
	 * Upload a file in object storage
	 * @param keyName
	 * @param uploadFile
	 */
	void uploadFile(String keyName, File uploadFile) throws ObjectStorageException;
	
	/**
	 * Upload a file in object storage
	 * @param keyName
	 * @param uploadFile
	 */
	boolean exist(String keyName) throws ObjectStorageException;
	
}
