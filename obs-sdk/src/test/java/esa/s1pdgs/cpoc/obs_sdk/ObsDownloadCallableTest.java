package esa.s1pdgs.cpoc.obs_sdk;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadCallable;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

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
     * Download object used when nominal case
     */
    private ObsDownloadObject object = new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key1", "target-dir");
    private ObsDownloadObject objectSdk = new ObsDownloadObject(ProductFamily.EDRS_SESSION, "key2", "target-dir");
    private ObsDownloadObject objectAws = new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key3", "target-dir");

    /**
     * Initialization
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws AbstractCodedException 
     */
    @Before
    public void init() throws ObsServiceException, SdkClientException, AbstractCodedException {
        MockitoAnnotations.initMocks(this);
//        doThrow(new SdkClientException("SDK exception")).when(obsClient)
//        .downloadFilesPerBatch(Mockito.eq(Arrays.asList(objectSdk)));
//        doThrow(new ObsServiceException("AWS exception")).when(obsClient)
//        .downloadFilesPerBatch(Mockito.eq(Arrays.asList(objectAws)));
    }

    /**
     * Test nominal case of call function
     * 
     * @throws ObsServiceException
     * @throws SdkClientException
     * @throws AbstractCodedException 
     */
    @Test
    public void testNominalCall()
            throws ObsServiceException, SdkClientException, AbstractCodedException {
        callable = new ObsDownloadCallable(obsClient, object);
        callable.call();        
        verify(obsClient, times(1)).download(Mockito.eq(Arrays.asList(object)));
    }

// FIXME: Enable tests
//    /**
//     * Test when no downloaded object
//     * 
//     * @throws ObsServiceException
//     * @throws SdkClientException
//     * @throws AbstractCodedException 
//     */
//    @Test(expected = ObsServiceException.class)
//    public void testWhenNoObj() throws ObsServiceException, SdkClientException, AbstractCodedException {
//        callable = new ObsDownloadCallable(obsClient, object);
//        callable.call();
//    }
//
//    /**
//     * Test when osbclient raise SdkClientException exception
//     * 
//     * @throws ObsServiceException
//     * @throws SdkClientException
//     * @throws AbstractCodedException 
//     */
//    @Test(expected = SdkClientException.class)
//    public void testCallSdkError()
//            throws ObsServiceException, SdkClientException, AbstractCodedException {
//        callable = new ObsDownloadCallable(obsClient, objectSdk);
//        callable.call();
//    }
//
//    /**
//     * Test when osbclient raise ObsServiceException exception
//     * 
//     * @throws ObsServiceException
//     * @throws SdkClientException
//     * @throws AbstractCodedException 
//     */
//    @Test(expected = ObsServiceException.class)
//    public void testCallAwsError()
//            throws ObsServiceException, SdkClientException, AbstractCodedException {
//        callable = new ObsDownloadCallable(obsClient, objectAws);
//        callable.call();
//    }
}
