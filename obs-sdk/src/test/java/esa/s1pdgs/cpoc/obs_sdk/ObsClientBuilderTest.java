package esa.s1pdgs.cpoc.obs_sdk;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsClientBuilder;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3ObsClient;

/**
 * Test the builder of ObsClient
 * @author Viveris Technologies
 *
 */
public class ObsClientBuilderTest {

    /**
     * Test the default client
     * @throws ObsServiceException
     */
    @Test
    public void testDefaultClient() throws ObsServiceException {
        ObsClient client = ObsClientBuilder.defaultS3Client();
        assertEquals(S3ObsClient.class, client.getClass());
    }
}
