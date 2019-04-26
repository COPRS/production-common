package esa.s1pdgs.cpoc.obs_sdk.s3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3Configuration;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3ObsServices;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3SdkClientException;

/**
 * Test the client Amazon S3
 * 
 * @author Viveris Technologies
 */
public class S3ObsClientTest {

    /**
     * Mock configuration
     */
    @Mock
    private S3Configuration configuration;

    /**
     * Mock service
     */
    @Mock
    private S3ObsServices service;

    /**
     * Client to test
     */
    private S3ObsClient client;

    /**
     * Initialization
     * 
     * @throws ObsServiceException
     * @throws S3SdkClientException
     */
    @Before
    public void init() throws ObsServiceException, S3SdkClientException {
        // Init mocks
        MockitoAnnotations.initMocks(this);

        // Mock service
        doReturn(true).when(service).exist(Mockito.anyString(),
                Mockito.eq("key-exist"));
        doReturn(false).when(service).exist(Mockito.anyString(),
                Mockito.eq("key-not-exist"));
        doReturn(2).when(service).getNbObjects(Mockito.anyString(),
                Mockito.eq("key-exist"));
        doReturn(0).when(service).getNbObjects(Mockito.anyString(),
                Mockito.eq("key-not-exist"));
        doReturn(2).when(service).downloadObjectsWithPrefix(Mockito.anyString(),
                Mockito.eq("key-exist"), Mockito.anyString(),
                Mockito.anyBoolean());
        doReturn(0).when(service).downloadObjectsWithPrefix(Mockito.anyString(),
                Mockito.eq("key-not-exist"), Mockito.anyString(),
                Mockito.anyBoolean());
        doReturn(2).when(service).uploadDirectory(Mockito.anyString(),
                Mockito.eq("key-exist"), Mockito.any());
        doReturn(0).when(service).uploadDirectory(Mockito.anyString(),
                Mockito.eq("key-not-exist"), Mockito.any());
        doNothing().when(service).uploadFile(Mockito.anyString(),
                Mockito.anyString(), Mockito.any());

        // Mock configuration
        doReturn("auxiliary-files").when(configuration)
                .getBucketForFamily(Mockito.eq(ObsFamily.AUXILIARY_FILE));
        doReturn("edrs-sessions").when(configuration)
                .getBucketForFamily(Mockito.eq(ObsFamily.EDRS_SESSION));
        doReturn("l0-slices").when(configuration)
                .getBucketForFamily(Mockito.eq(ObsFamily.L0_SLICE));
        doReturn("l0-acns").when(configuration)
                .getBucketForFamily(Mockito.eq(ObsFamily.L0_ACN));
        doReturn("l1-slices").when(configuration)
                .getBucketForFamily(Mockito.eq(ObsFamily.L1_SLICE));
        doReturn("l1-acns").when(configuration)
                .getBucketForFamily(Mockito.eq(ObsFamily.L1_ACN));
        doReturn("l0-segments").when(configuration)
        .getBucketForFamily(Mockito.eq(ObsFamily.L0_SEGMENT));
        doReturn("l0-blanks").when(configuration)
        .getBucketForFamily(Mockito.eq(ObsFamily.L0_BLANK));
        doReturn(22).when(configuration).getIntOfConfiguration(
                Mockito.eq(S3Configuration.TM_S_SHUTDOWN));
        doReturn(8).when(configuration).getIntOfConfiguration(
                Mockito.eq(S3Configuration.TM_S_DOWN_EXEC));
        doReturn(109).when(configuration).getIntOfConfiguration(
                Mockito.eq(S3Configuration.TM_S_UP_EXEC));

        // Build client
        client = new S3ObsClient(configuration, service);
    }

    /**
     * Test default constructor
     * 
     * @throws ObsServiceException
     */
    @Test
    public void testDefaultConstrutor() throws ObsServiceException {
        S3ObsClient defaultClient = new S3ObsClient();
        assertNotNull(defaultClient.configuration);
        assertNotNull(defaultClient.s3Services);
    }

    /**
     * Test getShutdownTimeoutS
     * 
     * @throws ObsServiceException
     */
    @Test
    public void testShutdownTm() throws ObsServiceException {
        assertEquals(22, client.getShutdownTimeoutS());
    }

    /**
     * Test getShutdownTimeoutS
     * 
     * @throws ObsServiceException
     */
    @Test
    public void testUploadExecTm() throws ObsServiceException {
        assertEquals(109, client.getUploadExecutionTimeoutS());
    }

    /**
     * Test getDownloadExecutionTimeoutS
     * 
     * @throws ObsServiceException
     */
    @Test
    public void testDownloadExecTm() throws ObsServiceException {
        assertEquals(8, client.getDownloadExecutionTimeoutS());
    }

    /**
     * Test doesObjectExist
     * 
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test
    public void testDoesObjectExist()
            throws ObsServiceException, SdkClientException {
        boolean ret = client
                .doesObjectExist(new ObsObject("key-exist", ObsFamily.L0_ACN));
        assertTrue(ret);
        verify(service, times(1)).exist(Mockito.eq("l0-acns"), Mockito.eq("key-exist"));

        ret = client.doesObjectExist(
                new ObsObject("key-not-exist", ObsFamily.L1_SLICE));
        assertFalse(ret);
        verify(service, times(1)).exist(Mockito.eq("l1-slices"),
                Mockito.eq("key-not-exist"));
    }

    /**
     * Test doesPrefixExist
     * 
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test
    public void testDoesPrefixExist()
            throws ObsServiceException, SdkClientException {
        boolean ret = client.doesPrefixExist(
                new ObsObject("key-exist", ObsFamily.L0_SLICE));
        assertTrue(ret);
        verify(service, times(1)).getNbObjects(Mockito.eq("l0-slices"),
                Mockito.eq("key-exist"));

        ret = client.doesPrefixExist(
                new ObsObject("key-not-exist", ObsFamily.L1_SLICE));
        assertFalse(ret);
        verify(service, times(1)).getNbObjects(Mockito.eq("l1-slices"),
                Mockito.eq("key-not-exist"));
    }

    /**
     * Test downloadObject
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test
    public void testDownloadObject() throws ObsServiceException, SdkClientException {
        int ret = client.downloadObject(new ObsDownloadObject("key-exist",
                ObsFamily.AUXILIARY_FILE, "target-dir"));
        assertEquals(2, ret);
        verify(service, times(1)).downloadObjectsWithPrefix(Mockito.eq("auxiliary-files"),
                Mockito.eq("key-exist"), Mockito.eq("target-dir"), Mockito.eq(false));

        ret = client.downloadObject(new ObsDownloadObject("key-not-exist",
                ObsFamily.EDRS_SESSION, "target-dir"));
        assertEquals(0, ret);
        verify(service, times(1)).downloadObjectsWithPrefix(Mockito.eq("edrs-sessions"),
                Mockito.eq("key-not-exist"), Mockito.eq("target-dir"), Mockito.eq(true));
    }
    
    /**
     * Test uploadObject when directory
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test
    public void testUploadObjectDirectory() throws ObsServiceException, SdkClientException {
        int ret = client.uploadObject(new ObsUploadObject("key-exist", ObsFamily.L0_ACN, new File("test")));
        assertEquals(2, ret);
        verify(service, times(1)).uploadDirectory(Mockito.eq("l0-acns"),
                Mockito.eq("key-exist"), Mockito.eq(new File("test")));
        verify(service, never()).uploadFile(Mockito.anyString(),
                Mockito.anyString(), Mockito.any());
        
        ret = client.uploadObject(new ObsUploadObject("key-not-exist", ObsFamily.L0_ACN, new File("test")));
        assertEquals(0, ret);
        verify(service, times(1)).uploadDirectory(Mockito.eq("l0-acns"),
                Mockito.eq("key-not-exist"), Mockito.eq(new File("test")));
        verify(service, never()).uploadFile(Mockito.anyString(),
                Mockito.anyString(), Mockito.any());
    }
    
    /**
     * Test uploadObject when file
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test
    public void testUploadObjectFile() throws ObsServiceException, SdkClientException {
        int ret = client.uploadObject(new ObsUploadObject("key-exist", ObsFamily.L0_ACN, new File("pom.xml")));
        assertEquals(1, ret);
        verify(service, times(1)).uploadFile(Mockito.eq("l0-acns"),
                Mockito.eq("key-exist"), Mockito.eq(new File("pom.xml")));
        verify(service, never()).uploadDirectory(Mockito.anyString(),
                Mockito.anyString(), Mockito.any());
    }
}
