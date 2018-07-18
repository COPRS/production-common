package esa.s1pdgs.cpoc.obs_sdk;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadCallable;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

/**
 * Test the class ObsUploadCallable
 * 
 * @author Viveris Technologies
 */
public class ObsUploadCallableTest {

    /**
     * Mock OBS client
     */
    @Mock
    private ObsClient obsClient;

    /**
     * Service to test
     */
    private ObsUploadCallable callable;

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Donwload object used when nominal case
     */
    private ObsUploadObject object = new ObsUploadObject("key1",
            ObsFamily.AUXILIARY_FILE, new File("test/key1"));
    private ObsUploadObject objectSdk = new ObsUploadObject("key2",
            ObsFamily.EDRS_SESSION, new File("test/key2"));
    private ObsUploadObject objectAws = new ObsUploadObject("key3",
            ObsFamily.AUXILIARY_FILE, new File("test/key3"));

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
        doReturn(Integer.valueOf(2)).when(obsClient)
                .uploadObject(Mockito.eq(object));
        doThrow(new SdkClientException("SDK exception")).when(obsClient)
                .uploadObject(Mockito.eq(objectSdk));
        doThrow(new ObsServiceException("AWS exception")).when(obsClient)
                .uploadObject(Mockito.eq(objectAws));

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
        callable = new ObsUploadCallable(obsClient, object);

        int nbObjects = callable.call();
        assertEquals(2, nbObjects);
        verify(obsClient, times(1)).uploadObject(Mockito.eq(object));
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
        callable = new ObsUploadCallable(obsClient, objectSdk);
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
        callable = new ObsUploadCallable(obsClient, objectAws);
        callable.call();
    }

}
