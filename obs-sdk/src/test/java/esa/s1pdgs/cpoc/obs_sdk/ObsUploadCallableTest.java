package esa.s1pdgs.cpoc.obs_sdk;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;

/**
 * Test the class ObsUploadCallable
 * 
 * @author Viveris Technologies
 */
public class ObsUploadCallableTest {

    private final File tmpDir = FileUtils.createTmpDir();
    
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
     * Download object used when nominal case
     */
    private ObsUploadObject object = new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key1", new File(tmpDir, "key1"));
    private ObsUploadObject objectSdk = new ObsUploadObject(ProductFamily.EDRS_SESSION, "key2", new File(tmpDir,"key2"));
    private ObsUploadObject objectAws = new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key3", new File(tmpDir,"key3"));

    /**
     * Initialization
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     */
    @Before
    public void init() throws ObsServiceException, SdkClientException, AbstractCodedException {
        MockitoAnnotations.initMocks(this);
//        doThrow(new SdkClientException("SDK exception")).when(obsClient)
//        .uploadFilesPerBatch(Mockito.eq(Arrays.asList(objectSdk)));
//        doThrow(new ObsServiceException("AWS exception")).when(obsClient)
//        .uploadFilesPerBatch(Mockito.eq(Arrays.asList(objectAws)));
    }
    
    @After
    public void tearDown() throws Exception {  
        FileUtils.delete(tmpDir.getPath());        
    }

    /**
     * Test nominal case of call function
     * 
     * @throws ObsServiceException
     * @throws SdkClientException
     * @throws AbstractCodedException 
     * @throws ObsEmptyFileException 
     */
    @Test
    public void testNominalCall()
            throws ObsServiceException, SdkClientException, AbstractCodedException, ObsEmptyFileException {
        callable = new ObsUploadCallable(obsClient, object);
        callable.call();
        verify(obsClient, times(1)).upload(Mockito.eq(Arrays.asList(object)), Mockito.any());
    }

// FIXME: Enable tests
//    /**
//     * Test when osbclient raise SdkClientException exception
//     * 
//     * @throws ObsServiceException
//     * @throws SdkClientException
//     */
//    @Test(expected = SdkClientException.class)
//    public void testCallSdkError()
//            throws ObsServiceException, SdkClientException {
//        callable = new ObsUploadCallable(obsClient, objectSdk);
//        callable.call();
//    }
//
//    /**
//     * Test when osbclient raise ObsServiceException exception
//     * 
//     * @throws ObsServiceException
//     * @throws SdkClientException
//     */
//    @Test(expected = ObsServiceException.class)
//    public void testCallAwsError()
//            throws ObsServiceException, SdkClientException {
//        callable = new ObsUploadCallable(obsClient, objectAws);
//        callable.call();
//    }

}
