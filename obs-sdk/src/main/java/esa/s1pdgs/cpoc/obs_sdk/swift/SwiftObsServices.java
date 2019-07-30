package esa.s1pdgs.cpoc.obs_sdk.swift;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;

import com.amazonaws.services.s3.model.ObjectListing;

import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;

public class SwiftObsServices {

	/**
     * Logger
     */
    private static final Log LOGGER = LogFactory.getLog(SwiftObsServices.class);

    /**
     * Swift client
     */
    protected final Account client;

    /**
     * Number of retries until client error
     */
    private final int numRetries;

    /**
     * Delay before retrying
     */
    private final int retryDelay;

    
    public SwiftObsServices(Account client, final int numRetries, final int retryDelay) {
    	this.client = client;
        this.numRetries = numRetries;
        this.retryDelay = retryDelay;
	}
    
    /**
     * Internal function to log messages
     * 
     * @param message
     */
    private void log(final String message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(message);
        }
    }
    
	/**
     * Check if object with such key in container exists
     * 
     * @param containerName
     * @param keyName
     * @return
	 * @throws SwiftSdkClientException
     */
	public boolean exist(final String containerName, final String keyName) {
		Container container = client.getContainer(containerName);
		return container.exists() && container.getObject(keyName).exists();
	}

	/**
     * Get the number of objects in the container whose key matches with prefix
     * 
     * @param containerName
     * @param prefixKey
     * @return
	 * @throws SwiftSdkClientException
     */
	public int getNbObjects(String containerName, String prefixKey) {
		Container container = client.getContainer(containerName);
		
		
		// TODO
		return 0;
	}

	/**
     * Download objects of the given container with a key matching the prefix
     * 
     * @param containerName
     * @param prefixKey
     * @param directoryPath
     * @param ignoreFolders
     * @return the number of download objects
	 * @throws SwiftObsServiceException
	 * @throws SwiftSdkClientException
     */
	public int downloadObjectsWithPrefix(String containerName, String prefixKey, String directoryPath, boolean ignoreFolders) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @param containerName
	 * @param keyName
	 * @param uploadDirectory
	 * @return
	 * @throws SwiftObsServiceException
	 * @throws SwiftSdkClientException
	 */
	public void uploadFile(String containerName, String keyName, final File uploadFile) throws SwiftObsServiceException, SwiftSdkClientException {
	    for (int retryCount = 1;; retryCount++) {
            try {
                log(String.format("Uploading object %s in container %s", keyName,
                		containerName));

                Container container = client.getContainer(containerName);		
                if (!container.exists()) {
                	throw new SwiftObsServiceException(containerName, keyName,
                            String.format("Upload fails: %s", uploadFile));
                }

                StoredObject object = container.getObject(keyName);
                object.uploadObject(uploadFile);

                log(String.format("Upload object %s in container %s succeeded",
                        keyName, containerName));
                break;
            } catch (Exception e) {
                if (retryCount <= numRetries) {
                    LOGGER.warn(String.format(
                            "Upload object %s from container %s failed: Attempt : %d / %d",
                            keyName, containerName, retryCount, numRetries));
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        throw new SwiftSdkClientException(containerName, keyName,
                                String.format("Upload fails: %s", ie.getMessage()), ie);
                    }
                    continue;
                } else {
                    throw new SwiftSdkClientException(containerName, keyName,
                            String.format("Upload fails: %s", e.getMessage()), e);
                }
            }
        }
	}

	/**
     * @param containerName
     * @param keyName
     * @param uploadFile
	 * @throws SwiftObsServiceException
	 * @throws SwiftSdkClientException
     */
	public int uploadDirectory(String containerName, String keyName, Object uploadFile) {
		// TODO Auto-generated method stub
		return 0;
	}
	

	public void delete(String containerName, String keyName) throws SwiftSdkClientException {
		for (int retryCount = 1;; retryCount++) {
            try {
                log(String.format("Deleting object %s from container %s", keyName,
                		containerName));

                Container container = client.getContainer(containerName);		
                if (!container.exists()) {
                	throw new SwiftObsServiceException(containerName, keyName,
                            String.format("Delete fails: %s from %s", keyName, container));
                }

                StoredObject object = container.getObject(keyName);
                object.delete();

                log(String.format("Deleted object %s from container %s succeeded",
                        keyName, containerName));
                break;
            } catch (Exception e) {
                if (retryCount <= numRetries) {
                    LOGGER.warn(String.format(
                            "Delete object %s from container %s failed: Attempt : %d / %d",
                            keyName, containerName, retryCount, numRetries));
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        throw new SwiftSdkClientException(containerName, keyName,
                                String.format("Delete fails: %s", ie.getMessage()), ie);
                    }
                    continue;
                } else {
                    throw new SwiftSdkClientException(containerName, keyName,
                            String.format("Delete fails: %s", e.getMessage()), e);
                }
            }
        }
		
	}

	/**
	 * @param containerName
	 * @return
	 * @throws SwiftObsServiceException
	 * @throws SwiftSdkClientException
	 */
	public ObjectListing listObjectsFromContainer(String containerName) {
		// TODO Auto-generated method stub
		return null;
		
	}

	/**
	 * @param containerName
	 * @param previousObjectListing
	 * @return
	 * @throws SwiftObsServiceException
	 * @throws SwiftSdkClientException
	 */
	public ObjectListing listNextBatchOfObjectsFromContainer(String containerName, ObjectListing previousObjectListing) {
		// TODO Auto-generated method stub
		return null;
		
	}

}
