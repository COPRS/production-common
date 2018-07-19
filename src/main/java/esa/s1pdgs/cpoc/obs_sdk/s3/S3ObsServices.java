package esa.s1pdgs.cpoc.obs_sdk.s3;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

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
     * Nomber of retry for client error
     */
    private final int nbRetry;
    
    /**
     * Delay before retriing
     */
    private final int retryDelay;

    /**
     * @param s3client
     */
    public S3ObsServices(final AmazonS3 s3client,
            final int nbRetry,
            final int retryDelay) {
        this.s3client = s3client;
        this.nbRetry = nbRetry;
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
        for(int nbretry = 1;;nbretry++) {
            try {
                return s3client.doesObjectExist(bucketName, keyName);
            } catch (com.amazonaws.AmazonServiceException ase) {
                throw new S3ObsServiceException(bucketName, keyName,
                        String.format("Checking object existance fails: %s",
                                ase.getMessage()),
                        ase);
            } catch (com.amazonaws.SdkClientException sce) {
                if(nbretry <= nbRetry) {
                    LOGGER.warn(String.format("Checking object existance %s failed: Attempt : %d / %d", keyName, nbretry, nbRetry));
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException e) {
                        throw new S3SdkClientException(bucketName, keyName,
                                String.format("Checking object existance fails: %s",
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
        int nbObj;
        for(int nbretry = 1;;nbretry++) {
            nbObj = 0;
            try {
                ObjectListing objectListing =
                        s3client.listObjects(bucketName, prefixKey);
                if (objectListing != null && !CollectionUtils
                        .isEmpty(objectListing.getObjectSummaries())) {
                    nbObj = objectListing.getObjectSummaries().size();
                }
                return nbObj;
            } catch (com.amazonaws.AmazonServiceException ase) {
                throw new S3ObsServiceException(bucketName, prefixKey,
                        String.format("Getting number of objects fails: %s",
                                ase.getMessage()),
                        ase);
            } catch (com.amazonaws.SdkClientException sce) {
                if(nbretry <= nbRetry) {
                    LOGGER.warn(String.format("Getting number of objects %s failed: Attempt : %d / %d", prefixKey, nbretry, nbRetry));
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException e) {
                        throw new S3SdkClientException(bucketName, prefixKey,
                                String.format("Getting number of objects fails: %s",
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
     * @return the number of download objects
     * @throws SdkClientException
     * @throws ObsServiceException
     */
    public int downloadObjectsWithPrefix(final String bucketName,
            final String prefixKey, final String directoryPath,
            final boolean ignoreFolders)
            throws S3ObsServiceException, S3SdkClientException {
        int nbObj;
        log(String.format(
                "Downloading objects with prefix %s from bucket %s in %s",
                prefixKey, bucketName, directoryPath));

        for(int nbretry = 1;;nbretry++) {
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
                        // Build temporarly filename
                        String key = objectSummary.getKey();
                        String targetDir = directoryPath;
                        if (!targetDir.endsWith(File.separator)) {
                            targetDir += File.separator;
                        }
                        String localFilePath = targetDir + key;
                        // Download object
                        log(String.format(
                                "Downloading object %s from bucket %s in %s", key,
                                bucketName, localFilePath));
                        File localFile = new File(localFilePath);
                        if (localFile.getParentFile() != null) {
                            localFile.getParentFile().mkdirs();
                        }
                        try {
                            localFile.createNewFile();
                        } catch (IOException ioe) {
                            throw new S3ObsServiceException(bucketName, key,
                                    "Directory creation fails for " + localFilePath,
                                    ioe);
                        }
                        s3client.getObject(new GetObjectRequest(bucketName, key),
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
                            }
                        }
                        nbObj++;
                    }
                }
    
                log(String.format(
                        "Download %d objects with prefix %s from bucket %s in %s succeeded",
                        nbObj, prefixKey, bucketName, directoryPath));
                return nbObj;
            } catch (com.amazonaws.AmazonServiceException ase) {
                throw new S3ObsServiceException(bucketName, prefixKey,
                        String.format("Download in %s fails: %s", directoryPath,
                                ase.getMessage()),
                        ase);
            } catch (com.amazonaws.SdkClientException ase) {
                if(nbretry <= nbRetry) {
                    LOGGER.warn(String.format("Download objects with prefix %s from bucket %s failed: Attempt : %d / %d", prefixKey, bucketName, nbretry, nbRetry));
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException e) {
                        throw new S3SdkClientException(bucketName, prefixKey,
                                String.format("Download in %s fails: %s", directoryPath,
                                        ase.getMessage()),
                                ase);
                    }
                    continue;
                } else {
                    throw new S3SdkClientException(bucketName, prefixKey,
                            String.format("Download in %s fails: %s", directoryPath,
                                    ase.getMessage()),
                            ase);
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
    public void uploadFile(final String bucketName, final String keyName,
            final File uploadFile)
            throws S3ObsServiceException, S3SdkClientException {
        for(int nbretry=1;;nbretry++) {
            try {
                log(String.format("Uploading object %s in bucket %s", keyName,
                        bucketName));
    
                s3client.putObject(bucketName, keyName, uploadFile);
                log(String.format("Upload object %s in bucket %s succeeded",
                        keyName, bucketName));
                break;
            } catch (com.amazonaws.AmazonServiceException ase) {
                throw new S3ObsServiceException(bucketName, keyName,
                        String.format("Upload fails: %s", ase.getMessage()), ase);
            } catch (com.amazonaws.SdkClientException sce) {
                if(nbretry <= nbRetry) {
                    LOGGER.warn(String.format("Upload object %s from bucket %s failed: Attempt : %d / %d", keyName, bucketName, nbretry, nbRetry));
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException e) {
                        throw new S3SdkClientException(bucketName, keyName,
                                String.format("Upload fails: %s", sce.getMessage()), sce);
                    }
                    continue;
                } else {
                    throw new S3SdkClientException(bucketName, keyName,
                            String.format("Upload fails: %s", sce.getMessage()), sce);
                }
            }
        }
    }

    /**
     * @param bucketName
     * @param keyName
     * @param uploadDirectory
     * @return
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    public int uploadDirectory(final String bucketName, final String keyName,
            final File uploadDirectory)
            throws S3ObsServiceException, S3SdkClientException {
        int ret = 0;
        if (uploadDirectory.isDirectory()) {
            File[] childs = uploadDirectory.listFiles();
            if (childs != null) {
                for (File child : childs) {
                    if (child.isDirectory()) {
                        ret += this.uploadDirectory(bucketName,
                                keyName + File.separator + child.getName(),
                                child);
                    } else {
                        this.uploadFile(bucketName,
                                keyName + File.separator + child.getName(),
                                child);
                        ret += 1;
                    }
                }
            }
        } else {
            this.uploadFile(bucketName, keyName, uploadDirectory);
            ret = 1;
        }
        return ret;
    }
}
