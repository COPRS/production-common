package fr.viveris.s1pdgs.libs.obs_sdk;

import java.util.concurrent.Callable;

/**
 * Callable to upload a file / folder in the OBS
 * 
 * @author Viveris Technologies
 */
public class ObsUploadCallable implements Callable<Integer> {

    /**
     * OBS client
     */
    private final ObsClient obsClient;

    /**
     * Objects to upload
     */
    private final ObsUploadObject object;

    /**
     * Constructor
     * 
     * @param obsClient
     * @param object
     */
    public ObsUploadCallable(final ObsClient obsClient,
            final ObsUploadObject object) {
        this.obsClient = obsClient;
        this.object = object;
    }

    /**
     * Call
     */
    @Override
    public Integer call() throws ObsServiceException, SdkClientException {
        return obsClient.uploadObject(object);
    }

}
