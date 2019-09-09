package esa.s1pdgs.cpoc.obs_sdk;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
 * parallel is done
 * 
 * @author Viveris Technologies
 */
public abstract class AbstractObsClient implements ObsClient {

    /**
     * Get the timeout for waiting threads termination in seconds
     * @return
     * @throws ObsServiceException
     */
	protected abstract int getShutdownTimeoutS() throws ObsServiceException;

    /**
     * Get the timeout for download execution in seconds
     * @return
     * @throws ObsServiceException
     */
	protected abstract int getDownloadExecutionTimeoutS() throws ObsServiceException;

    /**
     * Get the timeout for upload execution in seconds
     * @return
     * @throws ObsServiceException
     */
	protected abstract int getUploadExecutionTimeoutS() throws ObsServiceException;
	
    /**
     * @throws ExecutionException 
     * @throws InterruptedException 
     * @see ObsClient#downloadObjects(List)
     * @see #downloadObjects(List, boolean)
     */
    public List<File> downloadObjects(final List<ObsDownloadObject> objects)
            throws SdkClientException, ObsServiceException {
        return downloadObjects(objects, false);
    }

    protected abstract List<File> downloadObject(ObsDownloadObject object) throws SdkClientException, ObsServiceException;
    
    protected abstract void uploadObject(ObsUploadObject object) throws SdkClientException, ObsServiceException, ObsException;
    
    /**
     * @see ObsClient#downloadObjects(List, boolean)
     */
    public List<File> downloadObjects(final List<ObsDownloadObject> objects,
            final boolean parallel)
            throws SdkClientException, ObsServiceException {
    	List<File> files = new ArrayList<>();
        if (objects.size() > 1 && parallel) {
            // Download objects in parallel
            ExecutorService workerThread =
                    Executors.newFixedThreadPool(objects.size());
            CompletionService<List<File>> service =
                    new ExecutorCompletionService<>(workerThread);
            // Launch all downloads
            List<Future<List<File>>> futures = new ArrayList<>();
            for (ObsDownloadObject object : objects) {
            	futures.add(service.submit(new ObsDownloadCallable(this, object)));
            }
            
            waitForCompletion(workerThread, service, objects.size(),
            		getDownloadExecutionTimeoutS());

            for (Future<List<File>> future : futures) {
	            try {
	            	files.addAll(future.get());
	            } catch (InterruptedException | ExecutionException e) {
	            	// unlikely case, because waitForCompletion() already ensured that the executions are finished...
	            	throw new ObsServiceException(e.getMessage(), e);
	            }
            }
        } else {
            // Download object in sequential
            for (ObsDownloadObject object : objects) {
                files.addAll(downloadObject(object));
            }
        }
        return files;
    }

    /**
     * @see ObsClient#uploadFiles(List)
     */
    public void uploadObjects(final List<ObsUploadObject> objects)
            throws SdkClientException, ObsServiceException, ObsException {
        uploadObjects(objects, false);
    }

    /**
     * @see ObsClient#uploadFiles(List, boolean)
     */
    public void uploadObjects(final List<ObsUploadObject> objects,
            final boolean parallel)
            throws SdkClientException, ObsServiceException, ObsException {
        if (objects.size() > 1 && parallel) {
            // Upload objects in parallel
            ExecutorService workerThread =
                    Executors.newFixedThreadPool(objects.size());
            CompletionService<Void> service =
                    new ExecutorCompletionService<>(workerThread);
            // Launch all downloads
            for (ObsUploadObject object : objects) {
                service.submit(new ObsUploadCallable(this, object));
            }
            waitForCompletion(workerThread, service, objects.size(),
                    getUploadExecutionTimeoutS());

        } else {
            // Upload object in sequential
            for (ObsUploadObject object : objects) {
                uploadObject(object);
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
            final CompletionService<?> service, final int nbTasks,
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
                // Shutdown thread in case of raised exceptions
                workerThread.shutdownNow();
                workerThread.awaitTermination(getShutdownTimeoutS(),
                        TimeUnit.SECONDS);
            }
        } catch (InterruptedException | TimeoutException e) {
            throw new ObsServiceException(e.getMessage(), e);
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
		ObsDownloadObject object = new ObsDownloadObject(family, key, targetDir);
		try {
			downloadObject(object);
			/* FIXME handle not found situations differently
			   if (nbObjects <= 0) {
				throw new ObsUnknownObject(family, key);
			} */
		} catch (SdkClientException exc) {
			throw new ObsException(family, key, exc);
		}
		// Get file
		return new File(targetDir + id);
	}
	
	/**
     * Download files per batch
     * 
     * @param objects
     * @throws AbstractCodedException
     */
    public List<File> download(
            final List<ObsDownloadObject> objects)
            throws AbstractCodedException {
        try {
            return downloadObjects(objects, true);
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
		ObsUploadObject object = new ObsUploadObject(family, key, file);
		try {
			uploadObject(object);
		} catch (SdkClientException exc) {
			throw new ObsException(family, key, exc);
		}
	}
	
	/**
     * Upload files per batch
     * 
     * @param objects
     * @throws AbstractCodedException
     */
    public void upload(final List<ObsUploadObject> objects)
            throws AbstractCodedException {
        try {
            uploadObjects(objects, true);
        } catch (SdkClientException exc) {
            throw new ObsParallelAccessException(exc);
        }
    }

    public Map<String,ObsObject> listInterval(final ProductFamily family, Date intervalStart, Date intervalEnd) throws SdkClientException {
    	
    	List<ObsObject> results = getObsObjectsOfFamilyWithinTimeFrame(family, intervalStart, intervalEnd);
    	Map<String, ObsObject> map = results.stream()
    		      .collect(Collectors.toMap(ObsObject::getKey, obsObject -> obsObject));
    	    	
    	return map;
    }

	@Override
	public void move(ObsObject from, ProductFamily to) throws ObsException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Map<String, InputStream> getAllAsInputStream(ProductFamily family, String keyPrefix)  throws SdkClientException {
		throw new UnsupportedOperationException();
	}
}
