package esa.s1pdgs.cpoc.obs_sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.input.ObsReportingInput;

/**
 * Provides an implementation of the ObsClient where the download / upload in
 * parallel is done
 * 
 * @author Viveris Technologies
 */
public abstract class AbstractObsClient implements ObsClient {
	public static final String MD5SUM_SUFFIX = ".md5sum";
	
	private final ObsConfigurationProperties configuration;
	
	public AbstractObsClient(final ObsConfigurationProperties configuration) {
		this.configuration = configuration;
	}

	protected final String getBucketFor(final ProductFamily family) throws ObsServiceException {
		return configuration.getBucketFor(family);
	}

    protected abstract List<File> downloadObject(ObsDownloadObject object) throws SdkClientException, ObsServiceException;
    
    protected abstract void uploadObject(ObsUploadObject object) throws SdkClientException, ObsServiceException, ObsException;

    private final List<File> downloadObjects(final List<ObsDownloadObject> objects,
            final boolean parallel)
            throws SdkClientException, ObsServiceException, ObsException {
    	
    	final List<File> files = new ArrayList<>();
        if (objects.size() > 1 && parallel) {
            // Download objects in parallel
            final ExecutorService workerThread = Executors.newFixedThreadPool(objects.size());
            final CompletionService<List<File>> service = new ExecutorCompletionService<>(workerThread);
            // Launch all downloads
            final List<Future<List<File>>> futures = new ArrayList<>();
            for (final ObsDownloadObject object : objects) {
            	futures.add(service.submit(new ObsDownloadCallable(this, object)));
            }
            
            waitForCompletion(workerThread, service, objects.size(),configuration.getTimeoutDownExec());

            for (final Future<List<File>> future : futures) {
	            try {
	            	files.addAll(future.get());
	            } catch (InterruptedException | ExecutionException e) {
	            	// unlikely case, because waitForCompletion() already ensured that the executions are finished...
	            	throw new ObsServiceException(e.getMessage(), e);
	            }
            }
        } else {
    		final Reporting reporting = ReportingUtils.newReportingBuilderFor("Read")
    				.newReporting();
      	
            // Download object in sequential
            for (final ObsDownloadObject object : objects) {            
             	reporting.begin(
             			new ObsReportingInput(getBucketFor(object.getFamily()), object.getKey()),
             			new ReportingMessage("Start downloading from OBS")
             	);
             	try {
					final List<File> results = downloadObject(object);
					if (results.size() <= 0) {
						throw new ObsUnknownObject(object.getFamily(), object.getKey());
					}					
					final long dlSize =	FileUtils.size(results);
					reporting.end(new ReportingMessage(dlSize, "End downloading from OBS"));             	
					files.addAll(results);
				} catch (SdkClientException | RuntimeException e) {
					reporting.error(new ReportingMessage("Error on downloading from OBS: {}", LogUtils.toString(e)));
					throw e;
				}
            }
        }
        return files;
    }

    private final void uploadObjects(final List<ObsUploadObject> objects,
            final boolean parallel)
            throws SdkClientException, ObsServiceException, ObsException {
        if (objects.size() > 1 && parallel) {
            // Upload objects in parallel
            final ExecutorService workerThread =
                    Executors.newFixedThreadPool(objects.size());
            final CompletionService<Void> service =
                    new ExecutorCompletionService<>(workerThread);
            // Launch all downloads
            for (final ObsUploadObject object : objects) {
                service.submit(new ObsUploadCallable(this, object));
            }
            waitForCompletion(workerThread, service, objects.size(), configuration.getTimeoutUpExec());

        } else {
      		final Reporting reporting = ReportingUtils.newReportingBuilderFor("Write")
    				.newReporting();
        	
            // Upload object in sequential
            for (final ObsUploadObject object : objects) {            	
             	reporting.begin(
             			new ObsReportingInput(getBucketFor(object.getFamily()), object.getKey()),
             			new ReportingMessage("Start uploading to OBS")
             	);
             	
             	try {
    				uploadObject(object);
    				final long dlSize =	FileUtils.size(object.getFile());
    				reporting.end(new ReportingMessage(dlSize, "End uploading to OBS"));             	
    			} catch (SdkClientException | RuntimeException e) {
    				reporting.error(new ReportingMessage("Error on uploading to OBS: {}", LogUtils.toString(e)));
    				throw e;
    			}
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
                    } catch (final ExecutionException e) {
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
                workerThread.awaitTermination(configuration.getTimeoutShutdown(),TimeUnit.SECONDS);
            }
        } catch (InterruptedException | TimeoutException e) {
            throw new ObsServiceException(e.getMessage(), e);
        }
    }    
	
	/**
     * Download files per batch
     * 
     * @param objects
     * @throws AbstractCodedException
     * @throws IllegalArgumentException
     */
    @Override
	public List<File> download(final List<ObsDownloadObject> objects) throws AbstractCodedException {
    	ValidArgumentAssertion.assertValidArgument(objects);
        try {
            return downloadObjects(objects, true);
        } catch (final SdkClientException exc) {
            throw new ObsParallelAccessException(exc);
        }
    }

	/**
     * Upload files per batch
     * 
     * @param objects
     * @throws AbstractCodedException
     */
    @Override
	public void upload(final List<ObsUploadObject> objects)
            throws AbstractCodedException {
    	ValidArgumentAssertion.assertValidArgument(objects);
        try {
            uploadObjects(objects, true);
        } catch (final SdkClientException exc) {
            throw new ObsParallelAccessException(exc);
        }
    }

    @Override
	public Map<String,ObsObject> listInterval(final ProductFamily family, final Date intervalStart, final Date intervalEnd) throws SdkClientException {
    	ValidArgumentAssertion.assertValidArgument(family);
    	ValidArgumentAssertion.assertValidArgument(intervalStart);
    	ValidArgumentAssertion.assertValidArgument(intervalEnd);
    	
    	final List<ObsObject> results = getObsObjectsOfFamilyWithinTimeFrame(family, intervalStart, intervalEnd);
    	final Map<String, ObsObject> map = results.stream()
    		      .collect(Collectors.toMap(ObsObject::getKey, obsObject -> obsObject));
    	    	
    	return map;
    }
	
	@Override
    public void validate(final ObsObject object) throws ObsServiceException, ObsValidationException {
		ValidArgumentAssertion.assertValidArgument(object);
		try {
			final Map<String, InputStream> isMap = getAllAsInputStream(object.getFamily(), object.getKey() + MD5SUM_SUFFIX);
			if (isMap.size() > 1) {
				Utils.closeQuietly(isMap.values());
				throw new ObsValidationException("More than one checksum file returned");
			}	
			if (isMap.isEmpty()) {
				throw new ObsValidationException("Checksum file not found for: {} of family {}", object.getKey(), object.getFamily());
			} 
			try(final InputStream is = isMap.get(object.getKey() + MD5SUM_SUFFIX)) {
				final Map<String,String> md5sums = collectMd5Sums(object);
				try(BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
					String line;
	                while ((line = reader.readLine()) != null) {    
	                	final int idx = line.indexOf("  ");
	                	if (idx >= 0 && line.length() > (idx + 2)) {
	                		final String md5 = line.substring(0, idx);
	                		final String key = line.substring(idx + 2);
	                		final String currentMd5 = md5sums.get(key);
	                		if (null == currentMd5) {
	                			throw new ObsValidationException("Object not found: {} of family {}", key, object.getFamily());
	                		}
	                		if (!md5.equals(currentMd5)) {
	                			throw new ObsValidationException("Checksum is wrong for object: {} of family {}", key, object.getFamily());
	                		}
	                		md5sums.remove(key);
	                	}
		            }
                }
				for (final String key : md5sums.keySet()) {
					throw new ObsValidationException("Unexpected object found: {} for {} of family {}", key, object.getKey(), object.getFamily());
				}
			}
			finally {
				Utils.closeQuietly(isMap.values());
			}			
		} catch (SdkClientException | ObsException | IOException e) {
			throw new ObsServiceException("Unexpected error: " + e.getMessage(), e);
		}
    }
	
	protected abstract Map<String,String> collectMd5Sums(ObsObject object) throws ObsServiceException, ObsException;
	
}
