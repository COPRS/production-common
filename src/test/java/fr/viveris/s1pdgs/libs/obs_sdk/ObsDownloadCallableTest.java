package fr.viveris.s1pdgs.libs.obs_sdk;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Test the class ObsDownloadCallable
 * 
 * @author Viveris Technologies
 */
public class ObsDownloadCallableTest {

    /**
     * Mock OBS client
     */
    @Mock
    private ObsClient obsClient;

    /**
     * Service to test
     */
    private ObsDownloadCallable callable;

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Donwload object used when nominal case
     */
    private ObsDownloadObject object = new ObsDownloadObject("key1",
            ObsFamily.AUXILIARY_FILE, "target-dir");
    private ObsDownloadObject objectSdk =
            new ObsDownloadObject("key2", ObsFamily.EDRS_SESSION, "target-dir");
    private ObsDownloadObject objectAws = new ObsDownloadObject("key3",
            ObsFamily.AUXILIARY_FILE, "target-dir");

    /**
     * Initialization
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     */
    @Before
    public void init() throws ObsServiceException, SdkClientException {
        // Init mocks
        MockitoAnnotations.initMocks(this);

        // Mock obsClient
        doReturn(Integer.valueOf(3)).when(obsClient)
                .downloadObject(Mockito.eq(object));
        doThrow(new SdkClientException("SDK exception")).when(obsClient)
                .downloadObject(Mockito.eq(objectSdk));
        doThrow(new ObsServiceException("AWS exception")).when(obsClient)
                .downloadObject(Mockito.eq(objectAws));

    }

    /**
     * Test nominal case of call function
     * 
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test
    public void testNominalCall()
            throws ObsServiceException, SdkClientException {
        callable = new ObsDownloadCallable(obsClient, object);

        int nbObjects = callable.call();
        assertEquals(3, nbObjects);
        verify(obsClient, times(1)).downloadObject(Mockito.eq(object));
    }

    /**
     * Test when no downloaded object
     * 
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test(expected = ObsServiceException.class)
    public void testWhenNoObj() throws ObsServiceException, SdkClientException {
        doReturn(Integer.valueOf(0)).when(obsClient)
                .downloadObject(Mockito.eq(object));
        callable = new ObsDownloadCallable(obsClient, object);
        callable.call();
    }

    /**
     * Test when osbclient raise SdkClientException exception
     * 
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test(expected = SdkClientException.class)
    public void testCallSdkError()
            throws ObsServiceException, SdkClientException {
        callable = new ObsDownloadCallable(obsClient, objectSdk);
        callable.call();
    }

    /**
     * Test when osbclient raise ObsServiceException exception
     * 
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test(expected = ObsServiceException.class)
    public void testCallAwsError()
            throws ObsServiceException, SdkClientException {
        callable = new ObsDownloadCallable(obsClient, objectAws);
        callable.call();
    }
}
