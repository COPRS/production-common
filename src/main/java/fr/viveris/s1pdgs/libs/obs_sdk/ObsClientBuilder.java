package fr.viveris.s1pdgs.libs.obs_sdk;

import fr.viveris.s1pdgs.libs.obs_sdk.s3.S3ObsClient;

/**
 * Builder of ObsClient
 * 
 * @author Viveris Technologies
 */
public final class ObsClientBuilder {

    /**
     * @throws Exception
     */
    private ObsClientBuilder() throws ObsServiceException {
        throw new ObsServiceException("Cannot create instance of util class");
    }

    /**
     * get the default ObsClient
     * 
     * @return
     * @throws ObsServiceException
     * @see {@link S3ObsClient}
     */
    public static ObsClient defaultClient() throws ObsServiceException {
        return new S3ObsClient();
    }
}
