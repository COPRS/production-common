package esa.s1pdgs.cpoc.obs_sdk.s3;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import esa.s1pdgs.cpoc.obs_sdk.s3.S3ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3ObsServices;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3SdkClientException;

/**
 * Test the services to access to the OBS via the amazon S3 API
 * 
 * @author Viveris Technologies
 */
public class S3ObsServicesTest {

    /**
     * Bucket names used for mock
     */
    private final static String BCK_OBJ_EXIST = "bck-obj-exist";
    private final static String BCK_OBJ_NOT_EXIST = "bck-obj-not-exist";
    private final static String BCK_EXC_SDK = "bck-ex-sdk";
    private final static String BCK_EXC_AWS = "bck-ex-aws";

    /**
     * Mock S3 client
     */
    @Mock
    private AmazonS3 s3client;

    /**
     * Service to test
     */
    private S3ObsServices service;

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * List of OBs objects
     */
    private ObjectListing listObjects;

    /**
     * Initialization
     */
    @Before
    public void init() {
        // Init mocks
        MockitoAnnotations.initMocks(this);

        // Init useful objects
        listObjects = new ObjectListing();
        S3ObjectSummary obj1 = new S3ObjectSummary();
        obj1.setKey("key1");
        S3ObjectSummary obj2 = new S3ObjectSummary();
        obj2.setKey("key2");
        S3ObjectSummary obj3 = new S3ObjectSummary();
        obj3.setKey("root/key3");
        listObjects.getObjectSummaries().add(obj1);
        listObjects.getObjectSummaries().add(obj2);
        listObjects.getObjectSummaries().add(obj3);

        // Build service
        service = new S3ObsServices(s3client, 3, 500);
        mockAmazonS3Client();
    }

    /**
     * Clean
     */
    @After
    public void clean() {
        File file1 = new File("test/key1");
        if (file1.exists()) {
            file1.delete();
        }
        File file2 = new File("test/key2");
        if (file2.exists()) {
            file2.delete();
        }
        File file3 = new File("test/root/key3");
        if (file3.exists()) {
            file3.delete();
        }
        File file4 = new File("test/key3");
        if (file4.exists()) {
            file4.delete();
        }
        File file5 = new File("test/root");
        if (file5.exists()) {
            file5.delete();
        }
    }

    /**
     * Mock the amazon S3 client
     */
    private void mockAmazonS3Client() {
        // doesObjectExist
        doReturn(true).when(s3client).doesObjectExist(Mockito.eq(BCK_OBJ_EXIST),
                Mockito.anyString());
        doReturn(false).when(s3client).doesObjectExist(
                Mockito.eq(BCK_OBJ_NOT_EXIST), Mockito.anyString());
        doThrow(new com.amazonaws.SdkClientException("amazon SDK exception"))
                .when(s3client)
                .doesObjectExist(Mockito.eq(BCK_EXC_SDK), Mockito.anyString());
        doThrow(new com.amazonaws.AmazonServiceException(
                "amazon SDK exception")).when(s3client).doesObjectExist(
                        Mockito.eq(BCK_EXC_AWS), Mockito.anyString());

        // list objects
        doReturn(listObjects).when(s3client)
                .listObjects(Mockito.eq(BCK_OBJ_EXIST), Mockito.anyString());
        doThrow(new com.amazonaws.SdkClientException("amazon SDK exception"))
                .when(s3client).listObjects(Mockito.eq(BCK_OBJ_NOT_EXIST),
                        Mockito.anyString());
        doReturn(null).when(s3client).listObjects(Mockito.eq(BCK_OBJ_NOT_EXIST),
                Mockito.eq("null-prefix"));
        doReturn(new ObjectListing()).when(s3client).listObjects(
                Mockito.eq(BCK_OBJ_NOT_EXIST), Mockito.eq("prefix"));
        doThrow(new com.amazonaws.SdkClientException("amazon SDK exception"))
                .when(s3client)
                .listObjects(Mockito.eq(BCK_EXC_SDK), Mockito.anyString());
        doThrow(new com.amazonaws.AmazonServiceException(
                "amazon SDK exception")).when(s3client).listObjects(
                        Mockito.eq(BCK_EXC_AWS), Mockito.anyString());

        // get objects
        doReturn(null).when(s3client).getObject(
                Mockito.any(GetObjectRequest.class), Mockito.any(File.class));

        // putObject
        doReturn(null).when(s3client).putObject(Mockito.anyString(),
                Mockito.anyString(), Mockito.any(File.class));
        doThrow(new com.amazonaws.SdkClientException("amazon SDK exception"))
                .when(s3client).putObject(Mockito.eq(BCK_EXC_SDK),
                        Mockito.anyString(), Mockito.any(File.class));
        doThrow(new com.amazonaws.AmazonServiceException(
                "amazon SDK exception")).when(s3client).putObject(
                        Mockito.eq(BCK_EXC_AWS), Mockito.anyString(),
                        Mockito.any(File.class));
    }

    // ---------------------------------------------------
    // exist
    // ---------------------------------------------------

    /**
     * Test the nominal case of the exists function
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testNominalExist()
            throws S3ObsServiceException, S3SdkClientException {

        boolean retTrue = service.exist(BCK_OBJ_EXIST, "test-key");
        assertTrue(retTrue);
        verify(s3client, times(1)).doesObjectExist(Mockito.eq(BCK_OBJ_EXIST),
                Mockito.eq("test-key"));

        boolean retFalse = service.exist(BCK_OBJ_NOT_EXIST, "test-key");
        assertFalse(retFalse);
        verify(s3client, times(1)).doesObjectExist(
                Mockito.eq(BCK_OBJ_NOT_EXIST), Mockito.eq("test-key"));
    }

    /**
     * Then when Amazon S3 API raises AmazonServiceException
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testExistsWhenAwsException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3ObsServiceException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_AWS)));
        thrown.expect(hasProperty("key", is("test-key")));
        thrown.expectCause(isA(AmazonServiceException.class));

        service.exist(BCK_EXC_AWS, "test-key");
    }

    /**
     * Then when Amazon S3 API raises SdkClientException
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testExistsWhenSDKException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_SDK)));
        thrown.expect(hasProperty("key", is("test-key")));
        thrown.expectCause(isA(SdkClientException.class));

        service.exist(BCK_EXC_SDK, "test-key");
    }

    // ---------------------------------------------------
    // getNbObjects
    // ---------------------------------------------------

    /**
     * Nominal case of GetNbObjects
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testNominalGetNbObject()
            throws S3ObsServiceException, S3SdkClientException {
        assertEquals(0, service.getNbObjects(BCK_OBJ_NOT_EXIST, "null-prefix"));
        assertEquals(0, service.getNbObjects(BCK_OBJ_NOT_EXIST, "prefix"));
        assertEquals(3, service.getNbObjects(BCK_OBJ_EXIST, "prefix"));
    }

    /**
     * Test GetNbObjects when Amazon S3 API raises AmazonServiceException
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testGetNbObjectsWhenAwsException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3ObsServiceException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_AWS)));
        thrown.expect(hasProperty("key", is("prefix")));
        thrown.expectCause(isA(AmazonServiceException.class));

        service.getNbObjects(BCK_EXC_AWS, "prefix");
    }

    /**
     * Test GetNbObjects when Amazon S3 API raises SdkClientException
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testGetNbObjectsWhenSDKException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_SDK)));
        thrown.expect(hasProperty("key", is("prefix")));
        thrown.expectCause(isA(SdkClientException.class));

        service.getNbObjects(BCK_EXC_SDK, "prefix");
    }

    // ---------------------------------------------------
    // downloadObjectsWithPrefix
    // ---------------------------------------------------

    /**
     * downloadObjectsWithPrefix nominal case
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testNominaldownloadObjectsWithPrefixNoObjects()
            throws S3ObsServiceException, S3SdkClientException {
        int nbObjectsNull = service.downloadObjectsWithPrefix(BCK_OBJ_NOT_EXIST,
                "null-prefix", "directory-path", true);
        assertEquals(0, nbObjectsNull);
        verify(s3client, never()).getObject(Mockito.any(GetObjectRequest.class),
                Mockito.any(File.class));

        int nbObjectsEmpty = service.downloadObjectsWithPrefix(
                BCK_OBJ_NOT_EXIST, "prefix", "directory-path", true);
        assertEquals(0, nbObjectsEmpty);
        verify(s3client, never()).getObject(Mockito.any(GetObjectRequest.class),
                Mockito.any(File.class));
    }

    /**
     * downloadObjectsWithPrefix nominal case when ignore folders
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testNominaldownloadObjectsWithPrefixIgnoreFolder()
            throws S3ObsServiceException, S3SdkClientException {
        int nbObjects = service.downloadObjectsWithPrefix(BCK_OBJ_EXIST, "key",
                "test/", true);
        assertEquals(3, nbObjects);
        verify(s3client, times(3)).getObject(
                Mockito.any(GetObjectRequest.class), Mockito.any(File.class));
        verify(s3client, times(1)).getObject(
                Mockito.any(GetObjectRequest.class),
                Mockito.eq(new File("test/key1")));
        verify(s3client, times(1)).getObject(
                Mockito.any(GetObjectRequest.class),
                Mockito.eq(new File("test/key2")));
        verify(s3client, times(1)).getObject(
                Mockito.any(GetObjectRequest.class),
                Mockito.eq(new File("test/root/key3")));
        assertTrue((new File("test/key3")).exists());
        assertFalse((new File("test/root/key3")).exists());
    }

    /**
     * downloadObjectsWithPrefix nominal case when ignore folders
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testNominaldownloadObjectsWithPrefixNotIgnoreFolder()
            throws S3ObsServiceException, S3SdkClientException {
        int nbObjects = service.downloadObjectsWithPrefix(BCK_OBJ_EXIST, "key",
                "test", false);
        assertEquals(3, nbObjects);
        verify(s3client, times(3)).getObject(
                Mockito.any(GetObjectRequest.class), Mockito.any(File.class));
        verify(s3client, times(1)).getObject(
                Mockito.any(GetObjectRequest.class),
                Mockito.eq(new File("test/key1")));
        verify(s3client, times(1)).getObject(
                Mockito.any(GetObjectRequest.class),
                Mockito.eq(new File("test/key2")));
        verify(s3client, times(1)).getObject(
                Mockito.any(GetObjectRequest.class),
                Mockito.eq(new File("test/root/key3")));
        assertFalse((new File("test/key3")).exists());
        assertTrue((new File("test/root/key3")).exists());
    }

    /**
     * Test downloadObjectsWithPrefix when Amazon S3 API raises
     * AmazonServiceException
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testdownloadObjectsWithPrefixWhenAwsException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3ObsServiceException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_AWS)));
        thrown.expect(hasProperty("key", is("prefix")));
        thrown.expectCause(isA(AmazonServiceException.class));

        service.downloadObjectsWithPrefix(BCK_EXC_AWS, "prefix", "directory",
                true);
    }

    /**
     * Test downloadObjectsWithPrefix when Amazon S3 API raises
     * SdkClientException
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testdownloadObjectsWithPrefixsWhenSDKException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_SDK)));
        thrown.expect(hasProperty("key", is("prefix")));
        thrown.expectCause(isA(SdkClientException.class));

        service.downloadObjectsWithPrefix(BCK_EXC_SDK, "prefix", "directory",
                true);
    }

    // ---------------------------------------------------
    // uploadFile
    // ---------------------------------------------------

    /**
     * Nominal test case of uploadFile
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testUploadFileNominal()
            throws S3ObsServiceException, S3SdkClientException {
        service.uploadFile(BCK_OBJ_EXIST, "key-test", new File("pom.xml"));
        verify(s3client, times(1)).putObject(Mockito.eq(BCK_OBJ_EXIST),
                Mockito.eq("key-test"), Mockito.eq(new File("pom.xml")));
    }

    /**
     * Test upload file when SDKClientException raised by Amazon services
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testUploadFileSDKException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_SDK)));
        thrown.expect(hasProperty("key", is("key-test")));
        thrown.expectCause(isA(SdkClientException.class));

        service.uploadFile(BCK_EXC_SDK, "key-test", new File("pom.xml"));
    }

    /**
     * Test upload file when AmazonServiceException raised by Amazon services
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testUploadFileServiceException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3ObsServiceException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_AWS)));
        thrown.expect(hasProperty("key", is("key-test")));
        thrown.expectCause(isA(AmazonServiceException.class));

        service.uploadFile(BCK_EXC_AWS, "key-test", new File("pom.xml"));
    }

    /**
     * Nominal test case of uploadFile
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     */
    @Test
    public void testUploadDirectoryNominalWhenFile()
            throws S3ObsServiceException, S3SdkClientException {
        int ret = service.uploadDirectory(BCK_OBJ_EXIST, "key-test", new File("pom.xml"));
        assertEquals(1, ret);
        verify(s3client, times(1)).putObject(Mockito.eq(BCK_OBJ_EXIST),
                Mockito.eq("key-test"), Mockito.eq(new File("pom.xml")));
    }

    /**
     * Nominal test case of uploadFile
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     * @throws IOException
     */
    @Test
    public void testUploadDirectoryNominal()
            throws S3ObsServiceException, S3SdkClientException, IOException {
        File file1 = new File("test/key1");
        File file2 = new File("test/key2");
        File file3 = new File("test/key");
        File file4 = new File("test/key/key3");
        File file5 = new File("test/ghost");

        file1.createNewFile();
        file2.createNewFile();
        file3.mkdirs();
        file4.createNewFile();
        file5.mkdirs();

        service.uploadDirectory(BCK_OBJ_EXIST, "key-test", new File("test"));
        verify(s3client, times(3)).putObject(Mockito.eq(BCK_OBJ_EXIST),
                Mockito.anyString(), Mockito.any(File.class));
        verify(s3client, times(1)).putObject(Mockito.eq(BCK_OBJ_EXIST),
                Mockito.eq("key-test" + File.separator + "key1"),
                Mockito.eq(new File("test/key1")));
        verify(s3client, times(1)).putObject(Mockito.eq(BCK_OBJ_EXIST),
                Mockito.eq("key-test" + File.separator + "key2"),
                Mockito.eq(new File("test/key2")));
        verify(s3client, times(1)).putObject(
                Mockito.eq(BCK_OBJ_EXIST), Mockito.eq("key-test"
                        + File.separator + "key" + File.separator + "key3"),
                Mockito.eq(new File("test/key/key3")));

        file1.delete();
        file2.delete();
        file4.delete();
        file3.delete();
        file5.delete();
    }

    /**
     * Nominal test case of uploadFile
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     * @throws IOException
     */
    @Test
    public void testUploadDirectoryNominalNoChild()
            throws S3ObsServiceException, S3SdkClientException, IOException {
        File file3 = new File("test/key");
        file3.mkdirs();

        int ret = service.uploadDirectory(BCK_OBJ_EXIST, "key-test", new File("test/key"));
        assertEquals(0, ret);
        verify(s3client, never()).putObject(Mockito.anyString(),
                Mockito.anyString(), Mockito.any(File.class));

        file3.delete();
    }
}
