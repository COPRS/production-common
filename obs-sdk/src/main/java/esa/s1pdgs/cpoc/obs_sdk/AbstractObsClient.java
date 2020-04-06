package esa.s1pdgs.cpoc.obs_sdk;

import static esa.s1pdgs.cpoc.obs_sdk.AbstractObsClient.VoidCallable.wrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
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
import esa.s1pdgs.cpoc.obs_sdk.report.ReportingProductFactory;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3SdkClientException;
import esa.s1pdgs.cpoc.obs_sdk.swift.SwiftSdkClientException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingMessage;

/**
 * Provides an implementation of the ObsClient where the download / upload in
 * parallel is done
 * 
 * @author Viveris Technologies
 */
public abstract class AbstractObsClient implements ObsClient {
	private final ObsConfigurationProperties configuration;
	private final ReportingProductFactory reportingProductFactory;
	
	public AbstractObsClient(
			final ObsConfigurationProperties configuration, 
			final ReportingProductFactory reportingProductFactory
	) {
		this.configuration = configuration;
		this.reportingProductFactory = reportingProductFactory;
	}

	protected final String getBucketFor(final ProductFamily family) throws ObsServiceException {
		return configuration.getBucketFor(family);
	}

    protected abstract List<File> downloadObject(ObsDownloadObject object) throws SdkClientException, ObsServiceException;
    
    protected abstract void uploadObject(FileObsUploadObject object) throws SdkClientException, ObsServiceException, ObsException;

	protected abstract void uploadObject(final StreamObsUploadObject object) throws ObsServiceException, S3SdkClientException, SwiftSdkClientException;

    private List<File> downloadObjects(final List<ObsDownloadObject> objects,
									   final boolean parallel, final ReportingFactory reportingFactory)
            throws SdkClientException, ObsServiceException, ObsException {
    	
    	final List<File> files = new ArrayList<>();
        if (objects.size() > 1 && parallel) {
            // Download objects in parallel
            final ExecutorService workerThread = Executors.newFixedThreadPool(objects.size());
            final CompletionService<List<File>> service = new ExecutorCompletionService<>(workerThread);
            // Launch all downloads
            final List<Future<List<File>>> futures = new ArrayList<>();
            for (final ObsDownloadObject object : objects) {
            	futures.add(service.submit(downloadCall(reportingFactory, object)));
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
    		final Reporting reporting = reportingFactory.newReporting("ObsRead");
      	
            // Download object in sequential
            for (final ObsDownloadObject object : objects) {            
             	reporting.begin(
             			reportingProductFactory.reportingInputFor(object, getBucketFor(object.getFamily())),
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

	private Callable<List<File>> downloadCall(final ReportingFactory reportingFactory, final ObsDownloadObject object) {
		return () -> {
			{
				final List<File> downloaded = download(Collections.singletonList(object), reportingFactory);
				if (downloaded.size() <= 0) {
					throw new ObsServiceException(
							String.format("Unknown object %s with family %s", object.getKey(), object.getFamily()));
				}
				return downloaded;
			}
		};
	}

	private void uploadObjects(final List<FileObsUploadObject> objects,
							   final boolean parallel, final ReportingFactory reportingFactory)
            throws SdkClientException, ObsServiceException, ObsException {
        if (objects.size() > 1 && parallel) {
            // Upload objects in parallel
            final ExecutorService workerThread =
                    Executors.newFixedThreadPool(objects.size());
            final CompletionService<Void> service =
                    new ExecutorCompletionService<>(workerThread);
            // Launch all downloads
            for (final FileObsUploadObject object : objects) {
                service.submit(wrap(()  -> upload(Collections.singletonList(object), reportingFactory)));
            }
            waitForCompletion(workerThread, service, objects.size(), configuration.getTimeoutUpExec());

        } else {
      		final Reporting reporting = reportingFactory.newReporting("ObsWrite");
        	
            // Upload object in sequential
            for (final FileObsUploadObject object : objects) {            	
             	reporting.begin(
             			reportingProductFactory.reportingInputFor(object, getBucketFor(object.getFamily())),
             			new ReportingMessage("Start uploading to OBS")
             	);
             	
             	try {
    				uploadObject(object);
    				final long ulSize =	FileUtils.size(object.getFile());
    				reporting.end(new ReportingMessage(ulSize, "End uploading to OBS"));             	
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
	public List<File> download(final List<ObsDownloadObject> objects, final ReportingFactory reportingFactory) throws AbstractCodedException {
		ValidArgumentAssertion.assertValidArgument(objects);
		try {
			return downloadObjects(objects, true, reportingFactory);
		} catch (final SdkClientException exc) {
			throw new ObsParallelAccessException(exc);
		}
	}

	/**
	 * Upload files per batch
	 * 
	 * @param objects
	 * @throws AbstractCodedException
	 * @throws ObsEmptyFileException
	 */
	@Override
	public void upload(final List<FileObsUploadObject> objects, final ReportingFactory reportingFactory) throws AbstractCodedException, ObsEmptyFileException {
		ValidArgumentAssertion.assertValidArgument(objects);

		for (final FileObsUploadObject o : objects) {
			if (FileUtils.size(o.getFile()) == 0) {
				throw new ObsEmptyFileException("Empty file detected: " + o.getFile().getName());
			}
		}

		try {
			uploadObjects(objects, true, reportingFactory);
		} catch (final SdkClientException exc) {
			throw new ObsParallelAccessException(exc);
		}
	}
	
	@Override
	public void uploadStreams(final List<StreamObsUploadObject> objects, final ReportingFactory reportingFactory)
			throws AbstractCodedException, ObsEmptyFileException {
		ValidArgumentAssertion.assertValidArgument(objects);

		for (final StreamObsUploadObject o : objects) {
			if (o.getContentLength() == 0) {
				throw new ObsEmptyFileException("Empty stream detected: " + o.getKey());
			}
		}

		try {
			uploadStreams(objects, true, reportingFactory);
		} catch (final SdkClientException exc) {
			throw new ObsParallelAccessException(exc);
		}
	}

	private void uploadStreams(final List<StreamObsUploadObject> objects, final boolean parallel, final ReportingFactory reportingFactory) throws ObsServiceException, S3SdkClientException, SwiftSdkClientException {
		if (objects.size() > 1 && parallel) {
			// Upload objects in parallel
			final ExecutorService workerThread =
					Executors.newFixedThreadPool(objects.size());
			final CompletionService<Void> service =
					new ExecutorCompletionService<>(workerThread);
			// Launch all downloads
			for (final StreamObsUploadObject object : objects) {
				service.submit(wrap(() -> uploadStreams(Collections.singletonList(object), reportingFactory)));
			}
			waitForCompletion(workerThread, service, objects.size(), configuration.getTimeoutUpExec());

		} else {
			final Reporting reporting = reportingFactory.newReporting("ObsWrite");

			// Upload object in sequential
			for (final StreamObsUploadObject object : objects) {
				reporting.begin(
						reportingProductFactory.reportingInputFor(object, getBucketFor(object.getFamily())),
						new ReportingMessage("Start uploading to OBS")
				);

				try (final StreamObsUploadObject closeableStream = object) {	
					uploadObject(closeableStream);	
				} catch (SdkClientException | RuntimeException e) {
					reporting.error(new ReportingMessage("Error on uploading to OBS: {}", LogUtils.toString(e)));
					throw e;
				} catch (final IOException e1) {
					// on close, just ignore it
				}
				reporting.end(new ReportingMessage(object.getContentLength(), "End uploading to OBS"));
			}
		}
	}

	@Override
	public Map<String,ObsObject> listInterval(final ProductFamily family, final Date intervalStart, final Date intervalEnd) throws SdkClientException {
    	ValidArgumentAssertion.assertValidArgument(family);
    	ValidArgumentAssertion.assertValidArgument(intervalStart);
    	ValidArgumentAssertion.assertValidArgument(intervalEnd);
    	
    	final List<ObsObject> results = getObsObjectsOfFamilyWithinTimeFrame(family, intervalStart, intervalEnd);
    	return results.stream()
    		      .collect(Collectors.toMap(ObsObject::getKey, obsObject -> obsObject));
    }
	
	@Override
    public void validate(final ObsObject object) throws ObsServiceException, ObsValidationException {
		ValidArgumentAssertion.assertValidArgument(object);			
		try {
			final Map<String, InputStream> isMap = getAllAsInputStream(object.getFamily(), Md5.md5KeyFor(object));
			if (isMap.size() > 1) {
				Utils.closeQuietly(isMap.values());
				throw new ObsValidationException("More than one checksum file returned");
			}	
			if (isMap.isEmpty()) {
				throw new ObsValidationException("Checksum file not found for: {} of family {}", object.getKey(), object.getFamily());
			} 
			try(final InputStream is = isMap.get(Md5.md5KeyFor(object))) {
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

	@FunctionalInterface
	interface VoidCallable {

		static Callable<Void> wrap(final VoidCallable callable) {
			return () -> {
				callable.call();
				return null;
			};
		}

		void call() throws Exception;
	}
	
}
