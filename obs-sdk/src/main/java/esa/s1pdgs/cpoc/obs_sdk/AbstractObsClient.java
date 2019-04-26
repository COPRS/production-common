package esa.s1pdgs.cpoc.obs_sdk;

import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
}
