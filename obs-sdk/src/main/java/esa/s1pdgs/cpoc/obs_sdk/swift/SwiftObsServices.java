package esa.s1pdgs.cpoc.obs_sdk.swift;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import esa.s1pdgs.cpoc.obs_sdk.Md5;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javaswift.joss.instructions.UploadInstructions;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;

import esa.s1pdgs.cpoc.obs_sdk.AbstractObsClient;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3ObsServices;

public class SwiftObsServices {

	/**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(S3ObsServices.class);

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
    	client.setHashPassword("hashPassword"); // required for 
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
        	int nbObj = 0;
            try {
            	Collection<StoredObject> objectListing;
                Container container = client.getContainer(containerName);
                String marker = "";
            	String lastNonSegmentName = "";

                do {
                	objectListing = container.list(prefixKey, marker, MAX_RESULTS_PER_LIST);
                	for (StoredObject o : objectListing) {
                		marker = o.getName();

                		// Skip segment files (all files that have a sub directory like naming scheme with a direct parent that exists as a file (the manifest file))
                		if (!lastNonSegmentName.isEmpty() &&
	       					o.getName().equals(lastNonSegmentName + "/" + o.getBareName())) {
	       					continue;
	       				}
	       				lastNonSegmentName = o.getName();
                		
                		if (!o.getName().endsWith(Md5.MD5SUM_SUFFIX)) {
                			nbObj++;
                		}
                	}
                } while (objectListing.size() == MAX_RESULTS_PER_LIST); // possibly more results available to fetch
                return nbObj;
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
     * @return the downloaded files
	 * @throws SwiftObsServiceException
	 * @throws SwiftSdkClientException
     */
	public List<File> downloadObjectsWithPrefix(final String containerName,
            final String prefixKey, final String directoryPath,
            final boolean ignoreFolders)
            throws SwiftObsServiceException, SwiftSdkClientException {
        log(String.format(
                "Downloading objects with prefix %s from bucket %s in %s",
                prefixKey, containerName, directoryPath));
        List<File> files = new ArrayList<>();
        int nbObj;
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
	       				 
	       				 // only download md5sum files if it has been explicitly asked for a md5sum file
	       				 if (!prefixKey.endsWith(Md5.MD5SUM_SUFFIX) && object.getName().endsWith(Md5.MD5SUM_SUFFIX)) {
	       					 continue;
	       				 }
	       				 
	       				 // Build temporary filename
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
                                localFile = fTo;
                            }
                        }
                        files.add(localFile);
                        nbObj++;
	       			 }
            	} while (results.size() == MAX_RESULTS_PER_LIST);           	
                log(String.format(
                        "Download %d objects with prefix %s from bucket %s in %s succeeded",
                        nbObj, prefixKey, containerName, directoryPath));
                return files;
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
	 * @param uploadFile
	 * @return file information (md5 sum with filename)
	 * @throws SwiftObsServiceException
	 * @throws SwiftSdkClientException
	 */
	public String uploadFile(String containerName, String keyName, final File uploadFile)
			throws SwiftObsServiceException, SwiftSdkClientException {
		String md5 = null;
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
                md5 = object.getEtag();

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
	    return md5 + "  " + keyName;
	}

	public String uploadStream(String containerName, String keyName, final InputStream in, long contentLength) throws SwiftSdkClientException {
		String md5 = null;
		for (int retryCount = 1;; retryCount++) {
			try {
				log(String.format("Uploading object %s in container %s", keyName,
						containerName));

				Container container = client.getContainer(containerName);
				if (!container.exists()) {
					throw new SwiftObsServiceException(containerName, keyName,
							String.format("Upload fails: %s", keyName));
				}

				StoredObject object = container.getObject(keyName);
				UploadInstructions uploadInstructions = new UploadInstructions(in, contentLength);
				uploadInstructions.setSegmentationSize(MAX_SEGMENT_SIZE);
				object.uploadObject(uploadInstructions);
				md5 = object.getEtag();

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
		return md5 + "  " + keyName;
	}

	/**
     * @param containerName
     * @param keyName
     * @param uploadDirectory
     * @return file informations (list of md5 sums with filenames)
	 * @throws SwiftSdkClientException 
	 * @throws SwiftObsServiceException 
     */
	public List<String> uploadDirectory(final String containerName, final String keyName,
            final File uploadDirectory)
            throws SwiftObsServiceException, SwiftSdkClientException {
		List<String> fileList = new ArrayList<>(); 
        if (uploadDirectory.isDirectory()) {
            File[] childs = uploadDirectory.listFiles();
            if (childs != null) {
                for (File child : childs) {
                    if (child.isDirectory()) {
                        fileList.addAll(uploadDirectory(containerName,
                        		keyName + "/" + child.getName(), child));
                    } else {
                        fileList.add(uploadFile(containerName,
                        		keyName + "/" + child.getName(), child));
                    }
                }
            }           
        } else {
        	fileList.add(uploadFile(containerName, keyName, uploadDirectory));
        }
        return fileList;
	}
	
	public void createContainer(String containerName) throws SwiftSdkClientException {
		for (int retryCount = 1;; retryCount++) {
            try {
                log(String.format("Creating cobject.ontainer %s", containerName));

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

	public final void move(final String obsKey, final String sourceContainer, final String targetContainer) {
		final Container source = client.getContainer(sourceContainer);
		final StoredObject object = source.getObject(obsKey);
		
		final Container target = client.getContainer(targetContainer);
		final StoredObject newObject = target.getObject(obsKey);
		object.copyObject(target, newObject);
		object.delete();
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
	 * @param leMarker
	 * @return
	 * @throws SwiftSdkClientException 
	 */
	public Collection<StoredObject> listNextBatchOfObjectsFromContainer(String containerName, String leMarker) throws SwiftSdkClientException {
		String marker = leMarker;		
		for (int retryCount = 1;; retryCount++) {
			try {
				log(String.format("Listing objects from bucket %s", containerName));
				Container container = client.getContainer(containerName);
				Collection<StoredObject> objects;
				List<StoredObject> result = new ArrayList<>();
				do {
					objects = container.list("", marker, MAX_RESULTS_PER_LIST);
					for (StoredObject object : objects) {
						marker = object.getName();
						if (!object.getName().endsWith(Md5.MD5SUM_SUFFIX)) {
							result.add(object);
						}
					}
				} while(objects.size() != 0 && result.size() == 0); // when result is completely filtered, fetch again
                return result;
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
	
	private final Iterable<StoredObject> getAll(final String containerName, final String prefix) {				
		final List<StoredObject> result = new ArrayList<>(); 
		String marker = "";
		String lastNonSegmentName = "";
		while (true) {
			Collection<StoredObject> batch = client.getContainer(containerName).list(prefix, marker, MAX_RESULTS_PER_LIST);
			for (final StoredObject thisObject : batch) {
				final String key = thisObject.getName();
				// only download md5sum files if it has been explicitly asked for a md5sum file
        		if (!prefix.endsWith(Md5.MD5SUM_SUFFIX) && key.endsWith(Md5.MD5SUM_SUFFIX)) {
   					continue;
   				}

				// Skip segment files (all files that have a sub directory like naming scheme with a direct parent that 
				// exists as a file (the manifest file))
				if (!lastNonSegmentName.isEmpty() && key.equals(lastNonSegmentName + "/" + thisObject.getBareName())) {
					continue;
				}
				// skip directories as they will be derived from the key
				if (thisObject.isDirectory()) {
					continue;
				}
				lastNonSegmentName = key;

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
    	for (final StoredObject object : getAll(bucketName, prefix)) {
			result.put(object.getName(), object.getAsObject().downloadObjectAsInputStream());			
    	}
    	return result; 
    }

	public final Map<String,String> collectMd5Sums(final String bucketName, final String prefix) throws SwiftObsServiceException, SwiftSdkClientException {
    	Map<String,String> result;
    	for (int retryCount = 1;; retryCount++) {
    		result = new HashMap<>();
            try {
            	for(StoredObject storedObject : getAll(bucketName, prefix)) {
            		final String key = storedObject.getName();
           			result.put(key, storedObject.getEtag());
            	}
            	return result;
            } catch (Exception e) {
                if (retryCount <= numRetries) {
                    LOGGER.warn(String.format(
                            "Listing prefixed objects %s from bucket %s failed: Attempt : %d / %d",
                            prefix, bucketName, retryCount, numRetries)
                    );
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        throw new SwiftSdkClientException(bucketName, prefix,String.format("Listing fails: %s", e.getMessage()), e);
                    }
                    continue;
                } else {
                    throw new SwiftSdkClientException(bucketName, prefix, String.format("Upload fails: %s", e.getMessage()), e);
                }
            }
        }
    }

	public long size(String bucketName, String key) throws SwiftSdkClientException {
		log(String.format("Get size of object %s from bucket %s", key, bucketName));
		List<StoredObject> results = new ArrayList<>();
		getAll(bucketName, key).forEach(results::add);
		
		if (results.size() != 1) {
			throw new SwiftSdkClientException(bucketName, key, String.format(
					"Size query for object %s from bucket %s returned %s results", key, bucketName, results.size()));
		}
		return results.get(0).getContentLength();
	}

	public String getChecksum(String bucketName, String key) throws SwiftSdkClientException {
		log(String.format("Get checksum of object %s from bucket %s", key, bucketName));
		List<StoredObject> results = new ArrayList<>();
		getAll(bucketName, key).forEach(results::add);
		if (results.size() != 1) {
			throw new SwiftSdkClientException(bucketName, key, String.format(
					"Checksum query for object %s from bucket %s returned %s results", key, bucketName, results.size()));
		}
		return results.get(0).getEtag();
	}

	public URL createTemporaryDownloadUrl(String bucketName, String key, long expirationTimeInSeconds) throws SwiftSdkClientException {
		Container container = client.getContainer(bucketName);
		StoredObject storedObject = container.getObject(key);
		String urlStr = storedObject.getTempGetUrl(expirationTimeInSeconds);
		try {
			return new URL(urlStr);
		} catch (MalformedURLException e) {
			throw new SwiftSdkClientException(bucketName, key, "Could not create temporary download URL");
		}
	}
}
