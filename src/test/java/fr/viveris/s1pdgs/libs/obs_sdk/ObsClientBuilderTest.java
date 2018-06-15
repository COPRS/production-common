package fr.viveris.s1pdgs.libs.obs_sdk;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.viveris.s1pdgs.libs.obs_sdk.s3.S3ObsClient;

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
        ObsClient client = ObsClientBuilder.defaultClient();
        assertEquals(S3ObsClient.class, client.getClass());
    }
}
