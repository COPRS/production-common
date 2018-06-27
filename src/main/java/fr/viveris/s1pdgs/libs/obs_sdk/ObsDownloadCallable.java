package fr.viveris.s1pdgs.libs.obs_sdk;

import java.util.concurrent.Callable;

/**
 * Callable to download a file / folder from the OBS
 * 
 * @author Viveris Technologies
 */
public class ObsDownloadCallable implements Callable<Integer> {

    /**
     * OBS client
     */
    private final ObsClient obsClient;

    /**
     * Objects to download
     */
    private final ObsDownloadObject object;

    /**
     * Default constructor
     * 
     * @param obsClient
     * @param object
     */
    public ObsDownloadCallable(final ObsClient obsClient,
            final ObsDownloadObject object) {
        this.obsClient = obsClient;
        this.object = object;
    }

    /**
     * Call
     */
    @Override
    public Integer call() throws ObsServiceException, SdkClientException {
        int nbObj = obsClient.downloadObject(object);
        if (nbObj <= 0) {
            throw new ObsServiceException(
                    String.format("Unknown object %s with family %s",
                            object.getKey(), object.getFamily()));
        }
        return nbObj;
    }

}
