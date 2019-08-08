package esa.s1pdgs.cpoc.obs_sdk;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsParallelAccessException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsUnknownObject;

/**
 * Provides an implementation of the ObsClient where the download / upload in
 * parralel is done
 * 
 * @author Viveris Technologies
 */
public abstract class AbstractObsClient implements ObsClient {

    /**
     * @see ObsClient#downloadObjects(List)
     * @see #downloadObjects(List, boolean)
     */
    @Override
    public void downloadObjects(final List<ObsDownloadObject> objects)
            throws SdkClientException, ObsServiceException {
        downloadObjects(objects, false);
    }

    /**
     * @see ObsClient#downloadObjects(List, boolean)
     */
    @Override
    public void downloadObjects(final List<ObsDownloadObject> objects,
            final boolean parralel)
            throws SdkClientException, ObsServiceException {
        if (parralel) {
            // Download objects in parallel
            ExecutorService workerThread =
                    Executors.newFixedThreadPool(objects.size());
            CompletionService<Integer> service =
                    new ExecutorCompletionService<>(workerThread);
            // Launch all downloads
            for (ObsDownloadObject object : objects) {
                service.submit(new ObsDownloadCallable(this, object));
            }

            this.waitForCompletion(workerThread, service, objects.size(),
                    getDownloadExecutionTimeoutS());
        } else {
            // Download object in sequential
            for (ObsDownloadObject object : objects) {
                this.downloadObject(object);
            }
        }
    }

    /**
     * @see ObsClient#uploadFiles(List)
     */
    @Override
    public void uploadObjects(final List<ObsUploadObject> objects)
            throws SdkClientException, ObsServiceException {
        uploadObjects(objects, false);
    }

    /**
     * @see ObsClient#uploadFiles(List, boolean)
     */
    @Override
    public void uploadObjects(final List<ObsUploadObject> objects,
            final boolean parralel)
            throws SdkClientException, ObsServiceException {
        if (parralel) {

            // Upload objects in parallel
            ExecutorService workerThread =
                    Executors.newFixedThreadPool(objects.size());
            CompletionService<Integer> service =
                    new ExecutorCompletionService<>(workerThread);
            // Launch all downloads
            for (ObsUploadObject object : objects) {
                service.submit(new ObsUploadCallable(this, object));
            }
            this.waitForCompletion(workerThread, service, objects.size(),
                    getUploadExecutionTimeoutS());

        } else {
            // Upload object in sequential
            for (ObsUploadObject object : objects) {
                this.uploadObject(object);
            }
        }
    }

    /**
     * Wait for completion ending of all tasks
     * 
     * @param workerThread
     * @param service
     * @param nbTasks
     * @param timeout
     * @throws ObsServiceException
     */
    private void waitForCompletion(final ExecutorService workerThread,
            final CompletionService<Integer> service, final int nbTasks,
            final int timeout) throws ObsServiceException {
        try {
            try {
                // Wait for download endings
                for (int i = 0; i < nbTasks; i++) {
                    try {
                        service.take().get(timeout, TimeUnit.SECONDS);
                    } catch (ExecutionException e) {
                        if (e.getCause() instanceof ObsServiceException) {
                            throw (ObsServiceException) e.getCause();
                        } else {
                            throw new ObsServiceException(e.getMessage(), e);
                        }
                    }
                }
            } finally {
                // Shutdown thread in case of rasied exceptions
                workerThread.shutdownNow();
                workerThread.awaitTermination(getShutdownTimeoutS(),
                        TimeUnit.SECONDS);
            }
        } catch (InterruptedException | TimeoutException e) {
            throw new ObsServiceException(e.getMessage(), e);
        }
    }
    
    /**
     * Check if given file exist in OBS
     * 
     * @param family
     * @param key
     * @return
     * @throws ObsException
     */
	public boolean exist(final ProductFamily family, final String key)
            throws ObsException {
        final ObsObject object = new ObsObject(key, family);
        try {
            return doesObjectExist(object);
        } catch (SdkClientException exc) {
            throw new ObsException(family, key, exc);
        }
    }

	/**
	 * Download a file
	 * 
	 * @param key
	 * @param family
	 * @param targetDir
	 * @return
	 * @throws ObsException
	 * @throws ObsUnknownObject
	 */
	public File downloadFile(final ProductFamily family, final String key, final String targetDir)
			throws ObsException, ObsUnknownObject {
		// If case of session we ignore folder in the key
		String id = key;
		if (family == ProductFamily.EDRS_SESSION) {
			int lastIndex = key.lastIndexOf('/');
			if (lastIndex != -1 && lastIndex < key.length() - 1) {
				id = key.substring(lastIndex + 1);
			}
		}
		// Download object
		ObsDownloadObject object = new ObsDownloadObject(key, family, targetDir);
		try {
			int nbObjects = downloadObject(object);
			if (nbObjects <= 0) {
				throw new ObsUnknownObject(family, key);
			}
		} catch (SdkClientException exc) {
			throw new ObsException(family, key, exc);
		}
		// Get file
		return new File(targetDir + id);
	}
	
	/**
     * Download files per batch
     * 
     * @param filesToDownload
     * @throws AbstractCodedException
     */
    public void downloadFilesPerBatch(
            final List<ObsDownloadFile> filesToDownload)
            throws AbstractCodedException {
        // Build objects
        List<ObsDownloadObject> objects = filesToDownload.stream()
                .map(file -> new ObsDownloadObject(file.getKey(),file.getFamily(), file.getTargetDir()))
                .collect(Collectors.toList());
        // Download
        try {
            downloadObjects(objects, true);
        } catch (SdkClientException exc) {
            throw new ObsParallelAccessException(exc);
        }
    }
    
	/**
	 * Upload a file in object storage
	 * 
	 * @param family
	 * @param key
	 * @param file
	 * @throws ObsException
	 */
	public void uploadFile(final ProductFamily family, final String key, final File file) throws ObsException {
		ObsUploadObject object = new ObsUploadObject(key, family, file);
		try {
			uploadObject(object);
		} catch (SdkClientException exc) {
			throw new ObsException(family, key, exc);
		}
	}
	
	/**
     * Upload files per batch
     * 
     * @param filesToUpload
     * @throws AbstractCodedException
     */
    public void uploadFilesPerBatch(final List<ObsUploadFile> filesToUpload)
            throws AbstractCodedException {

        // Build objects
        List<ObsUploadObject> objects = filesToUpload.stream()
                .map(file -> new ObsUploadObject(file.getKey(),file.getFamily(), file.getFile()))
                .collect(Collectors.toList());
        // Upload
        try {
            uploadObjects(objects, true);
        } catch (SdkClientException exc) {
            throw new ObsParallelAccessException(exc);
        }
    }

    public Map<String,ObsObject> listInterval(final ProductFamily family, Date intervalStart, Date intervalEnd) throws SdkClientException {
    	
    	List<ObsObject> results = getListOfObjectsOfTimeFrameOfFamily(intervalStart, intervalEnd, family);
    	Map<String, ObsObject> map = results.stream()
    		      .collect(Collectors.toMap(ObsObject::getKey, obsObject -> obsObject));
    	    	
    	return map;
    }

	@Override
	public void moveFile(ProductFamily from, ProductFamily to, String key) throws ObsException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Map<String, InputStream> getAllAsInputStream(ProductFamily family, String keyPrefix)  throws SdkClientException {
		throw new UnsupportedOperationException();
	}
}
