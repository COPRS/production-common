package esa.s1pdgs.cpoc.obs_sdk.swift;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javaswift.joss.instructions.UploadInstructions;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;

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
    
    public final int MAX_RESULTS_PER_LIST = 8000;
    
    public static long MAX_SEGMENT_SIZE = 5L*1024*1024*1024;
    
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
	public int getNbObjects(String containerName, String prefixKey) throws SwiftSdkClientException {
        for (int retryCount = 1;; retryCount++) {
            try {
                Container container = client.getContainer(containerName);
                Collection<StoredObject> objectListing = container.list(prefixKey, "", MAX_RESULTS_PER_LIST);
                Collection<StoredObject> tmpCol = objectListing;
                while (tmpCol.size() == MAX_RESULTS_PER_LIST) { // possibly more results available to fetch
                    String marker = "";
                	for (StoredObject o : tmpCol) {
                		marker = o.getName(); 
                	}
                	tmpCol = container.list(prefixKey, marker, MAX_RESULTS_PER_LIST);
              		objectListing.addAll(tmpCol);
                }
                return objectListing.size();
            } catch (Exception e) {
                if (retryCount <= numRetries) {
                    LOGGER.warn(String.format(
                            "Getting number of objects %s failed: Attempt : %d / %d",
                            prefixKey, retryCount, numRetries));
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        throw new SwiftSdkClientException(containerName, prefixKey,
                                String.format(
                                        "Getting number of objects fails: %s",
                                        e.getMessage()), e);
                    }
                } else {
                    throw new SwiftSdkClientException(containerName, prefixKey,
                            String.format("Getting number of objects fails: %s",
                                    e.getMessage()), e);
                }
            }
        }
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
            	String lastNonSegmentName = "";
            	
            	Collection<StoredObject> results;
            	do {
	       			 results = client.getContainer(containerName).list(prefixKey, marker, MAX_RESULTS_PER_LIST);
	       			 for (StoredObject object : results) {
	       				 marker = object.getName(); // store marker for next retrival
	       				 
	       				 // Skip segment files (all files that have a sub directory like naming scheme with a direct parent that exists as a file (the manifest file))
	       				 if (!lastNonSegmentName.isEmpty() &&
	       						 object.getName().equals(lastNonSegmentName + "/" + object.getBareName())) {
	       					continue;
	       				 }
	       				 lastNonSegmentName = object.getName();
	       				 
	       				 // Build temporarly filename
                         String key = object.getName();
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
            	} while (results.size() == MAX_RESULTS_PER_LIST);           	
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
                UploadInstructions uploadInstructions = new UploadInstructions(uploadFile);
                uploadInstructions.setSegmentationSize(MAX_SEGMENT_SIZE);
                object.uploadObject(uploadInstructions);

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
                                String.format("Upload fails: %s", e.getMessage()), e);
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
	 * @throws SwiftSdkClientException 
	 * @throws SwiftObsServiceException 
     */
	public int uploadDirectory(final String bucketName, final String keyName,
            final File uploadDirectory)
            throws SwiftObsServiceException, SwiftSdkClientException {
    	return uploadDirectory(bucketName, keyName, uploadDirectory, new ArrayList<String>(), true);
    }
	
	private int uploadDirectory(final String containerName, final String keyName,
            final File uploadDirectory, List<String> fileList, boolean isBaseDirectory)
            		throws SwiftObsServiceException, SwiftSdkClientException {
		int ret = 0;
        if (uploadDirectory.isDirectory()) {
            File[] childs = uploadDirectory.listFiles();
            if (childs != null) {
                for (File child : childs) {
                	String childKey = keyName + File.separator + child.getName();
                    if (child.isDirectory()) {
                        ret += uploadDirectory(containerName, childKey, child, fileList, false);
                    } else {
                        uploadFile(containerName, childKey, child);
                        fileList.add(childKey);
                        ret += 1;
                    }
                }
            }
            if (isBaseDirectory) {
            	// TODO retrieve MD5 information for all uploaded files and upload a file list named directoryname.md5sum
            	String fileListTxt;
            	for (String file : fileList) {
            		
            	}
            }
        } else {
            uploadFile(containerName, keyName, uploadDirectory);
            ret = 1;
        }
        return ret;
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
                                String.format("Create container fails: %s", e.getMessage()), e);
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
                                String.format("Delete fails: %s", e.getMessage()), e);
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
	 * @throws SwiftSdkClientException 
	 */
	public Collection<StoredObject> listObjectsFromContainer(String containerName) throws SwiftSdkClientException {
		return listNextBatchOfObjectsFromContainer(containerName, "");
	}

	/**
	 * @param containerName
	 * @param marker
	 * @return
	 * @throws SwiftSdkClientException 
	 */
	public Collection<StoredObject> listNextBatchOfObjectsFromContainer(String containerName, String marker) throws SwiftSdkClientException {
		for (int retryCount = 1;; retryCount++) {
			try {
				log(String.format("Listing objects from bucket %s", containerName));
				Container container = client.getContainer(containerName);
                return container.list("", marker, MAX_RESULTS_PER_LIST);
			} catch (Exception e) {
				if (retryCount <= numRetries) {
					LOGGER.warn(String.format("Listing objects from bucket %s failed: Attempt : %d / %d", containerName,
							retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (InterruptedException ie) {
						throw new SwiftSdkClientException(containerName, "",
								String.format("Listing objects fails: %s", e.getMessage()), e);
					}
					continue;
				} else {
					throw new SwiftSdkClientException(containerName, "",
							String.format("Listing objects fails: %s", e.getMessage()), e);
				}
			}
		}
	}
	
	private final Iterable<StoredObject> getAll(final String bucketName, final String prefix) {				
		final List<StoredObject> result = new ArrayList<>(); 
		String marker = "";
		while (true) {			
			Collection<StoredObject> batch = client.getContainer(bucketName).list(prefix, marker, MAX_RESULTS_PER_LIST);
			for (final StoredObject thisObject : batch) {
				result.add(thisObject);		
				marker = thisObject.getName();
			}
			if (batch.size() != MAX_RESULTS_PER_LIST) {
				break;
			}
		}
		return result;		
	}
	
    public final Map<String, InputStream> getAllAsInputStream(final String bucketName, final String prefix) {       	
    	final Map<String, InputStream> result = new LinkedHashMap<>();    	
    	String lastNonSegmentName = "";
    	
    	for (final StoredObject object : getAll(bucketName, prefix)) {
    		final String key = object.getName();
    		
			// Skip segment files (all files that have a sub directory like naming scheme with a direct parent that 
    		// exists as a file (the manifest file))
			if (!lastNonSegmentName.isEmpty() && key.equals(lastNonSegmentName + "/" + object.getBareName())) {
				continue;
			}
			// skip directories as they will be derived from the key
			if (object.isDirectory()) {
				continue;
			}			
			lastNonSegmentName = key;			
			result.put(key, object.getAsObject().downloadObjectAsInputStream());			
    	}
    	return result; 
    }
}
