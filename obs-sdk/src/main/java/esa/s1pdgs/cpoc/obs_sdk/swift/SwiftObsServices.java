package esa.s1pdgs.cpoc.obs_sdk.swift;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;

import com.amazonaws.services.s3.model.ObjectListing;

import esa.s1pdgs.cpoc.obs_sdk.s3.S3ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3SdkClientException;

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
     * Check if container exists
     * 
     * @param containerName
     * @return
	 * @throws SwiftSdkClientException
     */
	public boolean containerExist(final String containerName) {
		Container container = client.getContainer(containerName);
		return container.exists();
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
	public int downloadObjectsWithPrefix(final String containerName,
            final String prefixKey, final String directoryPath,
            final boolean ignoreFolders)
            throws SwiftObsServiceException, SwiftSdkClientException {
        int nbObj;
        log(String.format(
                "Downloading objects with prefix %s from bucket %s in %s",
                prefixKey, containerName, directoryPath));

        for (int retryCount = 1;; retryCount++) {
            nbObj = 0;
            // List all objects with given prefix
            try {
            	String marker = "";
            	final int fetchSize = 25;
            	Collection<StoredObject> results;
            	do {
	       			 results = client.getContainer(containerName).list(prefixKey, marker, fetchSize);
	       			 for (StoredObject object : results) {
	       				 marker = object.getPath(); // store marker for next retrival
	       				 // Build temporarly filename
                         String key = object.getPath();
                         String targetDir = directoryPath;
                         if (!targetDir.endsWith(File.separator)) {
                             targetDir += File.separator;
                         }
                         String localFilePath = targetDir + key;
                         // Download object
	       				 log(String.format(
                               "Downloading object %s from container %s in %s",
                               key, containerName, localFilePath));
                        File localFile = new File(localFilePath);
                        if (localFile.getParentFile() != null) {
                            localFile.getParentFile().mkdirs();
                        }
                        try {
                            localFile.createNewFile();
                        } catch (IOException ioe) {
                            throw new SwiftObsServiceException(containerName, key,
                                    "Directory creation fails for " + localFilePath, ioe);
                        }
                        try {
                        	object.downloadObject(localFile);
                        } catch (Exception e) {
                            throw new SwiftObsServiceException(containerName, key,
                                    "Download fails for " + localFilePath, e);                        	
                        }
                        // If needed move in the target directory
                        if (ignoreFolders) {
                            String filename = key;
                            int lastIndex = key.lastIndexOf('/');
                            if (lastIndex != -1) {
                                filename = key.substring(lastIndex + 1);
                            }
                            if (!key.equals(filename)) {
                                File fTo = new File(targetDir + filename);
                                localFile.renameTo(fTo);
                            }
                        }
                        nbObj++;
	       			 }
            	} while (results.size() == fetchSize);           	
                log(String.format(
                        "Download %d objects with prefix %s from bucket %s in %s succeeded",
                        nbObj, prefixKey, containerName, directoryPath));
                return nbObj;
            } catch (Exception e) {
                if (retryCount <= numRetries) {
                    LOGGER.warn(String.format(
                            "Download objects with prefix %s from container %s failed: Attempt : %d / %d",
                            prefixKey, containerName, retryCount, numRetries));
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        throw new SwiftSdkClientException(containerName, prefixKey,
                                String.format("Download in %s fails: %s",
                                        directoryPath, e.getMessage()), e);
                    }
                    continue;
                } else {
                    throw new SwiftSdkClientException(containerName, prefixKey,
                            String.format("Download in %s fails: %s",
                                    directoryPath, e.getMessage()), e);
                }
            }
        }
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
	
	public void createContainer(String containerName) throws SwiftSdkClientException {
		for (int retryCount = 1;; retryCount++) {
            try {
                log(String.format("Creating container %s", containerName));

                Container container = client.getContainer(containerName);
                container.create();
                
                log(String.format("Created container %s", containerName));
                break;
            } catch (Exception e) {
                if (retryCount <= numRetries) {
                    LOGGER.warn(String.format(
                            "Create container %s failed: Attempt : %d / %d", containerName, retryCount, numRetries));
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                    	throw new SwiftSdkClientException(containerName,
                                String.format("Create container fails: %s", ie.getMessage()), ie);
                    }
                    continue;
                } else {
                    throw new SwiftSdkClientException(containerName,
                            String.format("Create container fails: %s", e.getMessage()), e);
                }
            }
        }		
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
