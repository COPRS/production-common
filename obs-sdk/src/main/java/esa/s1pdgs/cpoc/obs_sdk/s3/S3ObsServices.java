package esa.s1pdgs.cpoc.obs_sdk.s3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;

import esa.s1pdgs.cpoc.obs_sdk.AbstractObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

/**
 * Provides services to manage objects in the object storage wia the AmazonS3
 * APIs
 * 
 * @author Viveris Technologies
 */
public class S3ObsServices {

    /**
     * Logger
     */
    private static final Log LOGGER = LogFactory.getLog(S3ObsServices.class);

    /**
     * Amazon S3 client
     */
    protected final AmazonS3 s3client;

    /**
     * Amazon S3 client
     */
    protected final TransferManager s3tm;

    /**
     * Number of retries until client error
     */
    private final int numRetries;

    /**
     * Delay before retrying
     */
    private final int retryDelay;

    /**
     * @param s3client
     */
    public S3ObsServices(final AmazonS3 s3client, final TransferManager s3tm, final int numRetries,
            final int retryDelay) {
        this.s3client = s3client;
        this.s3tm = s3tm;
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
     * Check if object with such key in bucket exists
     * 
     * @param bucketName
     * @param keyName
     * @return
     * @throws SdkClientException
     */
    public boolean exist(final String bucketName, final String keyName)
            throws S3ObsServiceException, S3SdkClientException {
        for (int retryCount = 1;; retryCount++) {
            try {
                return s3client.doesObjectExist(bucketName, keyName);
            } catch (com.amazonaws.AmazonServiceException ase) {
                throw new S3ObsServiceException(bucketName, keyName,
                        String.format("Checking object existance fails: %s",
                                ase.getMessage()),
                        ase);
            } catch (com.amazonaws.SdkClientException sce) {
                if (retryCount <= numRetries) {
                    LOGGER.warn(String.format(
                            "Checking object existance %s failed: Attempt : %d / %d",
                            keyName, retryCount, numRetries));
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException e) {
                        throw new S3SdkClientException(bucketName, keyName,
                                String.format(
                                        "Checking object existance fails: %s",
                                        sce.getMessage()),
                                sce);
                    }
                    continue;
                } else {
                    throw new S3SdkClientException(bucketName, keyName,
                            String.format("Checking object existance fails: %s",
                                    sce.getMessage()),
                            sce);
                }
            }
        }
    }

    /**
     * Get the number of objects in the bucket whose key matches with prefix
     * 
     * @param bucketName
     * @param prefixKey
     * @return
     * @throws SdkClientException
     */
    public int getNbObjects(final String bucketName, final String prefixKey)
            throws S3ObsServiceException, S3SdkClientException {
        for (int retryCount = 1;; retryCount++) {
        	int nbObj = 0;
            try {
                ObjectListing objectListing =
                        s3client.listObjects(bucketName, prefixKey);
                if (objectListing != null && !CollectionUtils
                        .isEmpty(objectListing.getObjectSummaries())) {
        			for (S3ObjectSummary s : objectListing.getObjectSummaries()) {
        				if (!s.getKey().endsWith(AbstractObsClient.MD5SUM_SUFFIX)) {
        					nbObj++;
        				}
        			}
                }
                return nbObj;
            } catch (com.amazonaws.AmazonServiceException ase) {
                throw new S3ObsServiceException(bucketName, prefixKey,
                        String.format("Getting number of objects fails: %s",
                                ase.getMessage()),
                        ase);
            } catch (com.amazonaws.SdkClientException sce) {
                if (retryCount <= numRetries) {
                    LOGGER.warn(String.format(
                            "Getting number of objects %s failed: Attempt : %d / %d",
                            prefixKey, retryCount, numRetries));
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException e) {
                        throw new S3SdkClientException(bucketName, prefixKey,
                                String.format(
                                        "Getting number of objects fails: %s",
                                        sce.getMessage()),
                                sce);
                    }
                    continue;
                } else {
                    throw new S3SdkClientException(bucketName, prefixKey,
                            String.format("Getting number of objects fails: %s",
                                    sce.getMessage()),
                            sce);
                }
            }
        }
    }

    /**
     * Download objects of the given bucket with a key matching the prefix
     * 
     * @param bucketName
     * @param prefixKey
     * @param directoryPath
     * @param ignoreFolders
     * @return the download files
     * @throws SdkClientException
     * @throws ObsServiceException
     */
    public List<File> downloadObjectsWithPrefix(final String bucketName,
            final String prefixKey, final String directoryPath,
            final boolean ignoreFolders)
            throws S3ObsServiceException, S3SdkClientException {
        log(String.format(
                "Downloading objects with prefix %s from bucket %s in %s",
                prefixKey, bucketName, directoryPath));
        List<File> files = new ArrayList<>();
        int nbObj;
        for (int retryCount = 1;; retryCount++) {
        	nbObj = 0;
            // List all objects with given prefix
            try {
                ObjectListing objectListing =
                        s3client.listObjects(bucketName, prefixKey);
                if (objectListing != null && !CollectionUtils
                        .isEmpty(objectListing.getObjectSummaries())) {
                    // Download each object
                    for (S3ObjectSummary objectSummary : objectListing
                            .getObjectSummaries()) {
                    	
                    	String key = objectSummary.getKey();

                    	// only download md5sum files if it has been explicitly asked for a md5sum file
	       				if (!prefixKey.endsWith(AbstractObsClient.MD5SUM_SUFFIX) && key.endsWith(AbstractObsClient.MD5SUM_SUFFIX)) {
	       					continue;
	       				}
                    	
                        // Build temporarly filename
                        String targetDir = directoryPath;
                        if (!targetDir.endsWith(File.separator)) {
                            targetDir += File.separator;
                        }

                        String localFilePath = targetDir + key;
                        // Download object
                        log(String.format(
                                "Downloading object %s from bucket %s in %s",
                                key, bucketName, localFilePath));
                        File localFile = new File(localFilePath);
                        if (localFile.getParentFile() != null) {
                            localFile.getParentFile().mkdirs();
                        }
                        try {
                            localFile.createNewFile();
                        } catch (IOException ioe) {
                            throw new S3ObsServiceException(bucketName, key,
                                    "Directory creation fails for "
                                            + localFilePath,
                                    ioe);
                        }
                        s3client.getObject(
                                new GetObjectRequest(bucketName, key),
                                localFile);
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
                }
                log(String.format(
                        "Download %d objects with prefix %s from bucket %s in %s succeeded",
                        nbObj, prefixKey, bucketName, directoryPath));
                return files;
            } catch (com.amazonaws.AmazonServiceException ase) {
                throw new S3ObsServiceException(bucketName, prefixKey,
                        String.format("Download in %s fails: %s", directoryPath,
                                ase.getMessage()),
                        ase);
            } catch (com.amazonaws.SdkClientException ase) {
                if (retryCount <= numRetries) {
                    LOGGER.warn(String.format(
                            "Download objects with prefix %s from bucket %s failed: Attempt : %d / %d",
                            prefixKey, bucketName, retryCount, numRetries));
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException e) {
                        throw new S3SdkClientException(bucketName, prefixKey,
                                String.format("Download in %s fails: %s",
                                        directoryPath, ase.getMessage()),
                                ase);
                    }
                    continue;
                } else {
                    throw new S3SdkClientException(bucketName, prefixKey,
                            String.format("Download in %s fails: %s",
                                    directoryPath, ase.getMessage()),
                            ase);
                }
            }
        }
    }
    
    public final List<S3ObjectSummary> getAll(final String bucketName, final String prefix) {    
    	final List<S3ObjectSummary> result = new ArrayList<>();
    	ObjectListing listing = null;
    	do {
   			listing = listing == null ? s3client.listObjects(bucketName, prefix) : s3client.listNextBatchOfObjects(listing);
	    	for(S3ObjectSummary object : listing.getObjectSummaries()) {
	    		if (!object.getKey().endsWith(AbstractObsClient.MD5SUM_SUFFIX)) {
	    			result.add(object);
	    		}
	    	}
    	} while (listing.isTruncated());
    	
    	return result;
    }
    
    public final Map<String, InputStream> getAllAsInputStream(final String bucketName, final String prefix) throws S3ObsServiceException, S3SdkClientException {       	    	
        for (int retryCount = 1;; retryCount++) {
        	final Map<String, InputStream> result = new LinkedHashMap<>();
        	
            try {
            	for (final S3ObjectSummary summary : getAll(bucketName, prefix)) {
            		final String key = summary.getKey();
            		final S3Object obj = s3client.getObject(bucketName, key);  
            		result.put(key, obj.getObjectContent());
            	}
            	return result;
            } catch (com.amazonaws.AmazonServiceException ase) {
                throw new S3ObsServiceException(bucketName, prefix, String.format("Listing fails: %s", ase.getMessage()), ase);
            } catch (com.amazonaws.SdkClientException sce) {
                if (retryCount <= numRetries) {
                    LOGGER.warn(String.format(
                            "Listing prefixed objects %s from bucket %s failed: Attempt : %d / %d",
                            prefix, bucketName, retryCount, numRetries)
                    );
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException e) {
                        throw new S3SdkClientException(bucketName, prefix,String.format("Listing fails: %s", sce.getMessage()),sce);
                    }
                    continue;
                } else {
                    throw new S3SdkClientException(bucketName, prefix, String.format("Upload fails: %s", sce.getMessage()), sce);
                }
            }
        }
    }

    /**
     * @param bucketName
     * @param keyName
     * @param uploadFile
     * @throws S3SdkClientException
     */
    public String uploadFile(final String bucketName, final String keyName,
            final File uploadFile)
            throws S3ObsServiceException, S3SdkClientException {
    	String md5 = null;
        for (int retryCount = 1;; retryCount++) {
            try {
                log(String.format("Uploading object %s in bucket %s", keyName,
                        bucketName));
  
                Upload upload = s3tm.upload(bucketName, keyName, uploadFile);
                upload.addProgressListener((ProgressEvent progressEvent) -> {
                	LOGGER.trace(String.format(
                            "Uploading object %s in bucket %s: progress %s",
                            keyName, bucketName, progressEvent.toString()));
                });

                try {
                	UploadResult uploadResult = upload.waitForUploadResult();
                    md5 = uploadResult.getETag();
                } catch (InterruptedException e) {
                    throw new S3ObsServiceException(bucketName, keyName,
                            "Upload fails: interrupted during waiting multipart upload completion",
                            e);
                }

                log(String.format("Upload object %s in bucket %s succeeded",
                        keyName, bucketName));
                break;
            } catch (com.amazonaws.AmazonServiceException ase) {
                throw new S3ObsServiceException(bucketName, keyName,
                        String.format("Upload fails: %s", ase.getMessage()),
                        ase);
            } catch (com.amazonaws.SdkClientException sce) {
                if (retryCount <= numRetries) {
                    LOGGER.warn(String.format(
                            "Upload object %s from bucket %s failed: Attempt : %d / %d",
                            keyName, bucketName, retryCount, numRetries));
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException e) {
                        throw new S3SdkClientException(bucketName, keyName,
                                String.format("Upload fails: %s",
                                        sce.getMessage()),
                                sce);
                    }
                    continue;
                } else {
                    throw new S3SdkClientException(bucketName, keyName,
                            String.format("Upload fails: %s", sce.getMessage()),
                            sce);
                }
            }
        }
        return md5 + "  " + keyName;
    }

    /**
     * @param bucketName
     * @param keyName
     * @param uploadDirectory
     * @return
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    public List<String> uploadDirectory(final String bucketName, final String keyName,
            final File uploadDirectory)
            throws S3ObsServiceException, S3SdkClientException {
    	List<String> fileList = new ArrayList<>(); 
        if (uploadDirectory.isDirectory()) {
            File[] childs = uploadDirectory.listFiles();
            if (childs != null) {
                for (File child : childs) {
                    if (child.isDirectory()) {
                    	fileList.addAll(uploadDirectory(bucketName,
                                keyName + File.separator + child.getName(), child));
                    } else {
                        fileList.add(uploadFile(bucketName,
                                keyName + File.separator + child.getName(), child));
                    }
                }
            }
        } else {
            fileList.add(uploadFile(bucketName, keyName, uploadDirectory));
        }
        return fileList;
    }
    
	/**
	 * @param bucketName
	 * @return
	 * @throws S3ObsServiceException
	 * @throws S3SdkClientException
	 */
	public ObjectListing listObjectsFromBucket(final String bucketName)
			throws S3ObsServiceException, S3SdkClientException {

		for (int retryCount = 1;; retryCount++) {
			try {
				log(String.format("Listing objects from bucket %s", bucketName));
				return s3client.listObjects(bucketName);

			} catch (com.amazonaws.AmazonServiceException ase) {
				throw new S3ObsServiceException(bucketName, "",
						String.format("Listing objects fails: %s", ase.getMessage()), ase);
			} catch (com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(String.format("Listing objects from bucket %s failed: Attempt : %d / %d", bucketName,
							retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (InterruptedException e) {
						throw new S3SdkClientException(bucketName, "",
								String.format("Listing objects fails: %s", sce.getMessage()), sce);
					}
					continue;
				} else {
					throw new S3SdkClientException(bucketName, "",
							String.format("Listing objects fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	/**
	 * @param bucketName
	 * @param previousObjectListing
	 * @return
	 * @throws S3ObsServiceException
	 * @throws S3SdkClientException
	 */
	public ObjectListing listNextBatchOfObjectsFromBucket(final String bucketName,
			final ObjectListing previousObjectListing) throws S3ObsServiceException, S3SdkClientException {

		for (int retryCount = 1;; retryCount++) {
			try {
				log(String.format("Listing next batch of objects from bucket %s", bucketName));
				return s3client.listNextBatchOfObjects(previousObjectListing);

			} catch (com.amazonaws.AmazonServiceException ase) {
				throw new S3ObsServiceException(bucketName, "",
						String.format("Listing next batch of objects fails: %s", ase.getMessage()), ase);
			} catch (com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(String.format("Listing next batch of objects from bucket %s failed: Attempt : %d / %d",
							bucketName, retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (InterruptedException e) {
						throw new S3SdkClientException(bucketName, "",
								String.format("Listing next batch of objects fails: %s", sce.getMessage()), sce);
					}
					continue;
				} else {
					throw new S3SdkClientException(bucketName, "",
							String.format("Listing next batch of objects fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	public void moveFile(CopyObjectRequest request) throws S3ObsServiceException, S3SdkClientException {

		for (int retryCount = 1;; retryCount++) {
			try {
				log(String.format("Performing %s", request));
				s3client.copyObject(request);
			} catch (com.amazonaws.AmazonServiceException ase) {
				throw new S3ObsServiceException(request.getSourceBucketName(), request.getSourceKey(),
						String.format("Move of objects fails: %s", ase.getMessage()), ase);
			} catch (com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(String.format("Move of objects from bucket %s failed: Attempt : %d / %d",
							request.getSourceBucketName(), retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (InterruptedException e) {
						throw new S3SdkClientException(request.getSourceBucketName(), request.getSourceKey(),
								String.format("Move of objects fails: %s", sce.getMessage()), sce);
					}
					continue;
				} else {
					throw new S3SdkClientException(request.getSourceBucketName(), request.getSourceKey(),
							String.format("Move of objects fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}
    
}
