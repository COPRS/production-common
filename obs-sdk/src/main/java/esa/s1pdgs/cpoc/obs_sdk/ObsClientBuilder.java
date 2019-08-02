package esa.s1pdgs.cpoc.obs_sdk;

import esa.s1pdgs.cpoc.obs_sdk.s3.S3ObsClient;

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
     * get the default ObsS3Client
     * 
     * @return
     * @throws ObsServiceException
     * @see {@link S3ObsClient}
     */
    public static ObsClient defaultS3Client() throws ObsServiceException {
        return new S3ObsClient();
    }
    
    
    /**
     * get the default ObsSwiftClient
     * 
     * @return
     * @throws ObsServiceException
     * @see {@link S3ObsClient}
     */
    public static ObsClient defaultSwiftClient() throws ObsServiceException {
        return new S3ObsClient();
    }

}
