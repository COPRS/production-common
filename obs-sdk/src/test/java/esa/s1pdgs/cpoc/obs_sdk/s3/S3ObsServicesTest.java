package esa.s1pdgs.cpoc.obs_sdk.s3;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.amazonaws.AmazonClientException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;

import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.obs_sdk.Md5;

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
    
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File tmpDir;
    private File tmpFile;

    /**
     * Mock S3 client
     */
    @Mock
    private AmazonS3 s3client;

    /**
     * Mock S3 transaction manager
     */
    @Mock
    private TransferManager s3tm;

    @Mock
    private Upload upload;

    /**
     * Service to test
     */
    private S3ObsServices service;
    
    private S3ObsServices serviceSpy;

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * List of OBs objects
     */
    private ObjectListing listObjects1;
    private List<String> listObjects2;

    /**
     * Initialization
     * 
     */
    @Before
    public void init() throws AmazonClientException,
            InterruptedException, S3ObsServiceException, IOException {
        tmpDir = tmp.newFolder();
        tmpFile = tmp.newFile();

        // Init mocks
        MockitoAnnotations.initMocks(this);

        // Init useful objects
        listObjects1 = new ObjectListing();
        S3ObjectSummary obj1 = new S3ObjectSummary();
        obj1.setKey("key1");
        S3ObjectSummary obj2 = new S3ObjectSummary();
        obj2.setKey("key2");
        S3ObjectSummary obj3 = new S3ObjectSummary();
        obj3.setKey("root/key3");
        listObjects1.getObjectSummaries().add(obj1);
        listObjects1.getObjectSummaries().add(obj2);
        listObjects1.getObjectSummaries().add(obj3);
        
        listObjects2 = new ArrayList<>();
        listObjects2.add("key1");
        listObjects2.add("key2");
        listObjects2.add("root/key3");

        // Build service
        service = new S3ObsServices(s3client, s3tm, 3, 500);
        serviceSpy = Mockito.spy(service);
        mockAmazonS3Client();
        mockAmazonS3TransferManager();
    }

    private void mockAmazonS3TransferManager() throws
            AmazonClientException, InterruptedException {

        doReturn(upload).when(s3tm).upload(anyString(),
                anyString(), any());
        doReturn(upload).when(s3tm).upload(
                eq(BCK_OBJ_EXIST), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        doThrow(new com.amazonaws.SdkClientException("amazon SDK exception"))
                .when(s3tm).upload(eq(BCK_EXC_SDK), anyString(),
                        any());
        doThrow(new com.amazonaws.SdkClientException("amazon SDK exception"))
                .when(s3tm).upload(eq(BCK_EXC_SDK), anyString(),
                any(InputStream.class), any(ObjectMetadata.class));
        doThrow(new com.amazonaws.AmazonServiceException(
                "amazon SDK exception")).when(s3tm).upload(
                        eq(BCK_EXC_AWS), anyString(),
                        any());
        final UploadResult uploadResult = new UploadResult();
        uploadResult.setETag("dummy");
        doReturn(uploadResult).when(upload).waitForUploadResult();
    }

    /**
     * Mock the amazon S3 client
     */
    private void mockAmazonS3Client() throws S3ObsServiceException {
        // doesObjectExist
        doReturn(true).when(s3client).doesObjectExist(eq(BCK_OBJ_EXIST),
                anyString());
        doReturn(false).when(s3client).doesObjectExist(
                eq(BCK_OBJ_NOT_EXIST), anyString());
        doThrow(new com.amazonaws.SdkClientException("amazon SDK exception"))
                .when(s3client)
                .doesObjectExist(eq(BCK_EXC_SDK), anyString());
        doThrow(new com.amazonaws.AmazonServiceException(
                "amazon SDK exception")).when(s3client).doesObjectExist(
                        eq(BCK_EXC_AWS), anyString());

        // list objects
        doReturn(listObjects1).when(s3client)
                .listObjects(eq(BCK_OBJ_EXIST), anyString());
        doThrow(new com.amazonaws.SdkClientException("amazon SDK exception"))
                .when(s3client).listObjects(eq(BCK_OBJ_NOT_EXIST),
                        anyString());
        doReturn(null).when(s3client).listObjects(eq(BCK_OBJ_NOT_EXIST),
                eq("null-prefix"));
        doReturn(new ObjectListing()).when(s3client).listObjects(
                eq(BCK_OBJ_NOT_EXIST), eq("prefix"));
        doThrow(new com.amazonaws.SdkClientException("amazon SDK exception"))
                .when(s3client)
                .listObjects(eq(BCK_EXC_SDK), anyString());
        doThrow(new com.amazonaws.AmazonServiceException(
                "amazon SDK exception")).when(s3client).listObjects(
                        eq(BCK_EXC_AWS), anyString());

        // get objects
        doReturn(null).when(s3client).getObject(
                any(GetObjectRequest.class), any(File.class));
        
        
        doReturn(listObjects2).when(serviceSpy).getExpectedFiles(eq(BCK_OBJ_EXIST), anyString());
        doThrow(new com.amazonaws.SdkClientException("amazon SDK exception")).when(serviceSpy).getExpectedFiles(eq(BCK_OBJ_NOT_EXIST),
                anyString());
        doReturn(Collections.EMPTY_LIST).when(serviceSpy).getExpectedFiles(eq(BCK_OBJ_NOT_EXIST),
                eq("null-prefix"));
        doReturn(Collections.EMPTY_LIST).when(serviceSpy).getExpectedFiles( eq(BCK_OBJ_NOT_EXIST), eq("prefix"));
        doThrow(new com.amazonaws.SdkClientException("amazon SDK exception")).when(serviceSpy).getExpectedFiles(eq(BCK_EXC_SDK), anyString());
        doThrow(new com.amazonaws.AmazonServiceException("amazon SDK exception")).when(serviceSpy).getExpectedFiles(eq(BCK_EXC_AWS), anyString());

    }

    // ---------------------------------------------------
    // exist
    // ---------------------------------------------------

    /**
     * Test the nominal case of the exists function
     * 
     */
    @Test
    public void testNominalExist()
            throws S3ObsServiceException, S3SdkClientException {

        boolean retTrue = service.exist(BCK_OBJ_EXIST, "test-key");
        assertTrue(retTrue);
        verify(s3client, times(1)).doesObjectExist(eq(BCK_OBJ_EXIST),
                eq("test-key"));

        boolean retFalse = service.exist(BCK_OBJ_NOT_EXIST, "test-key");
        assertFalse(retFalse);
        verify(s3client, times(1)).doesObjectExist(
                eq(BCK_OBJ_NOT_EXIST), eq("test-key"));
    }

    /**
     * Then when Amazon S3 API raises AmazonServiceException
     * 
     */
    @Test
    public void testExistsWhenAwsException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_AWS)));
        thrown.expect(hasProperty("key", is("test-key")));
//        thrown.expectCause(isA(AmazonServiceException.class));

        service.exist(BCK_EXC_AWS, "test-key");
    }

    /**
     * Then when Amazon S3 API raises SdkClientException
     * 
     */
    @Test
    public void testExistsWhenSDKException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_SDK)));
        thrown.expect(hasProperty("key", is("test-key")));
        thrown.expectCause(instanceOf(SdkClientException.class));

        service.exist(BCK_EXC_SDK, "test-key");
    }

    // ---------------------------------------------------
    // getNbObjects
    // ---------------------------------------------------

    /**
     * Nominal case of GetNbObjects
     * 
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
     */
    @Test
    public void testGetNbObjectsWhenAwsException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_AWS)));
        thrown.expect(hasProperty("key", is("prefix")));
//        thrown.expectCause(isA(AmazonServiceException.class));

        service.getNbObjects(BCK_EXC_AWS, "prefix");
    }

    /**
     * Test GetNbObjects when Amazon S3 API raises SdkClientException
     * 
     */
    @Test
    public void testGetNbObjectsWhenSDKException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_SDK)));
        thrown.expect(hasProperty("key", is("prefix")));
        thrown.expectCause(instanceOf(SdkClientException.class));

        service.getNbObjects(BCK_EXC_SDK, "prefix");
    }

    // ---------------------------------------------------
    // downloadObjectsWithPrefix
    // ---------------------------------------------------

    /**
     * downloadObjectsWithPrefix nominal case
     * 
     */
    @Test
    public void testNominaldownloadObjectsWithPrefixNoObjects()
            throws S3ObsServiceException, S3SdkClientException {
    	List<File> files = serviceSpy.downloadObjectsWithPrefix(BCK_OBJ_NOT_EXIST,
                "null-prefix", "directory-path", true);
        assertEquals(0, files.size());
        verify(s3client, never()).getObject(any(GetObjectRequest.class),
                any(File.class));

        files = serviceSpy.downloadObjectsWithPrefix(
                BCK_OBJ_NOT_EXIST, "prefix", "directory-path", true);
        assertEquals(0, files.size());
        verify(s3client, never()).getObject(any(GetObjectRequest.class),
                any(File.class));
    }

    /**
     * downloadObjectsWithPrefix nominal case when ignore folders
     * 
     */
    @Test
    public void testNominaldownloadObjectsWithPrefixIgnoreFolder()
            throws S3ObsServiceException, S3SdkClientException {
    	List<File> files = serviceSpy.downloadObjectsWithPrefix(BCK_OBJ_EXIST, "key", tmpDir.getPath(), true);
        assertEquals(3, files.size());
        verify(s3client, times(3)).getObject(
                any(GetObjectRequest.class), any(File.class));
        verify(s3client, times(1)).getObject(
                any(GetObjectRequest.class),
                eq(new File(tmpDir,"key1")));
        verify(s3client, times(1)).getObject(
                any(GetObjectRequest.class),
                eq(new File(tmpDir,"key2")));
        verify(s3client, times(1)).getObject(
                any(GetObjectRequest.class),
                eq(new File(tmpDir,"root/key3")));
        assertTrue((new File(tmpDir,"key3")).exists());
        assertFalse((new File(tmpDir,"root/key3")).exists());
    }

    /**
     * downloadObjectsWithPrefix nominal case when ignore folders
     * 
     */
    @Test
    public void testNominaldownloadObjectsWithPrefixNotIgnoreFolder()
            throws S3ObsServiceException, S3SdkClientException {
    	List<File> files = serviceSpy.downloadObjectsWithPrefix(BCK_OBJ_EXIST, "key",
    			tmpDir.getPath(), false);
        assertEquals(3, files.size());
        verify(s3client, times(3)).getObject(
                any(GetObjectRequest.class), any(File.class));
        verify(s3client, times(1)).getObject(
                any(GetObjectRequest.class),
                eq(new File(tmpDir,"key1")));
        verify(s3client, times(1)).getObject(
                any(GetObjectRequest.class),
                eq(new File(tmpDir,"key2")));
        verify(s3client, times(1)).getObject(
                any(GetObjectRequest.class),
                eq(new File(tmpDir,"root/key3")));
        assertFalse((new File(tmpDir,"key3")).exists());
        assertTrue((new File(tmpDir,"root/key3")).exists());
    }

    /**
     * Test downloadObjectsWithPrefix when Amazon S3 API raises
     * AmazonServiceException
     * 
     */
    @Test
    public void testdownloadObjectsWithPrefixWhenAwsException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_AWS)));
        thrown.expect(hasProperty("key", is("prefix")));
//        thrown.expectCause(isA(S3SdkClientException.class));

        serviceSpy.downloadObjectsWithPrefix(BCK_EXC_AWS, "prefix", "directory",
                true);
    }

    /**
     * Test downloadObjectsWithPrefix when Amazon S3 API raises
     * SdkClientException
     * 
     */
    @Test
    public void testdownloadObjectsWithPrefixsWhenSDKException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_SDK)));
        thrown.expect(hasProperty("key", is("prefix")));
        thrown.expectCause(instanceOf(SdkClientException.class));

        service.downloadObjectsWithPrefix(BCK_EXC_SDK, "prefix", "directory",
                true);
    }

    // ---------------------------------------------------
    // uploadFile
    // ---------------------------------------------------

    /**
     * Nominal test case of uploadFile
     * 
     */
    @Test
    public void testUploadFileNominal()
            throws S3ObsServiceException, S3SdkClientException {
        service.uploadFile(BCK_OBJ_EXIST, "key-test", tmpFile);
        verify(s3tm, times(1)).upload(eq(BCK_OBJ_EXIST),
                eq("key-test"), any(DigestInputStream.class), any(ObjectMetadata.class));
    }

    /**
     * Test upload file when SDKClientException raised by Amazon services
     * 
     */
    @Test
    public void testUploadFileSDKException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_SDK)));
        thrown.expect(hasProperty("key", is("key-test")));
        thrown.expectCause(instanceOf(SdkClientException.class));

        service.uploadFile(BCK_EXC_SDK, "key-test", tmpFile);
    }

    /**
     * Test upload file when AmazonServiceException raised by Amazon services
     * 
     */
    @Test
    public void testUploadFileServiceException()
            throws S3ObsServiceException, S3SdkClientException {
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_AWS)));
        thrown.expect(hasProperty("key", is("key-test")));
//        thrown.expectCause(isA(AmazonServiceException.class));

        service.uploadFile(BCK_EXC_AWS, "key-test",tmpDir);
    }

    /**
     * Nominal test case of uploadFile
     * 
     */
    @Test
    public void testUploadDirectoryNominalWhenFile() throws Exception {
    	final File testFile = new File(tmpDir,"foo");
    	FileUtils.writeFile(testFile, "123");
    	
        List<Md5.Entry> ret = service.uploadDirectory(BCK_OBJ_EXIST, "key-test", testFile);
        assertEquals(1, ret.size());
        verify(s3tm, times(1)).upload(
                eq(BCK_OBJ_EXIST),
                eq("key-test"),
                isA(DigestInputStream.class),
                isA(ObjectMetadata.class));
    }

    /**
     * Nominal test case of uploadFile
     * 
     */
    @Test
    public void testUploadDirectoryNominal()
            throws S3ObsServiceException, S3SdkClientException, IOException {
        File file1 = new File(tmpDir,"key1");
        File file2 = new File(tmpDir,"key2");
        File file3 = new File(tmpDir,"key");
        File file4 = new File(tmpDir,"key/key3");
        File file5 = new File(tmpDir,"ghost");

        file1.createNewFile();
        file2.createNewFile();
        file3.mkdirs();
        file4.createNewFile();
        file5.mkdirs();
        
        service.uploadDirectory(BCK_OBJ_EXIST, "key-test", tmpDir);
        verify(s3tm, times(3)).upload(eq(BCK_OBJ_EXIST),
                anyString(), any(DigestInputStream.class), any(ObjectMetadata.class));
        verify(s3tm, times(1)).upload(eq(BCK_OBJ_EXIST),
                eq("key-test" + "/" + "key1"),
                any(DigestInputStream.class), any(ObjectMetadata.class));
        verify(s3tm, times(1)).upload(eq(BCK_OBJ_EXIST),
                eq("key-test" + "/" + "key2"),
                any(DigestInputStream.class), any(ObjectMetadata.class));
        verify(s3tm, times(1)).upload(
                eq(BCK_OBJ_EXIST), eq("key-test"
                        + "/" + "key" + "/" + "key3"),
                any(DigestInputStream.class), any(ObjectMetadata.class));

        file1.delete();
        file2.delete();
        file4.delete();
        file3.delete();
        file5.delete();
    }

    /**
     * Nominal test case of uploadFile
     * 
     */
    @Test
    public void testUploadDirectoryNominalNoChild()
            throws S3ObsServiceException, S3SdkClientException {
        File file3 = new File(tmpDir,"key");
        file3.mkdirs();

        List<Md5.Entry> ret = service.uploadDirectory(BCK_OBJ_EXIST, "key-test", file3);
        assertEquals(0, ret.size());
        verify(s3tm, never()).upload(anyString(), anyString(),
                any(File.class));

        file3.delete();
    }

    @Test
    public void testUploadStreamNominal() throws S3ObsServiceException, S3SdkClientException, IOException {
        try (InputStream in = getClass().getResourceAsStream("/testfile1.txt")) {
            service.uploadStream(BCK_OBJ_EXIST, "key-test", in, 100);
            verify(s3tm, times(1)).upload(eq(BCK_OBJ_EXIST),
                    eq("key-test"), any(InputStream.class), argThat(hasContentLength(100)));
        }
    }

    @Test
    public void testUploadStreamSDKException() throws IOException, S3ObsServiceException, S3SdkClientException {
        try (InputStream in = getClass().getResourceAsStream("/testfile1.txt")) {
            thrown.expect(S3SdkClientException.class);
            thrown.expect(hasProperty("bucket", is(BCK_EXC_SDK)));
            thrown.expect(hasProperty("key", is("key-test")));
            thrown.expectCause(instanceOf(SdkClientException.class));
            service.uploadStream(BCK_EXC_SDK, "key-test", in, 100);
        }
    }

    private ArgumentMatcher<ObjectMetadata> hasContentLength(long contentLength) {
        return new ArgumentMatcher<ObjectMetadata>() {
            @Override
            public boolean matches(ObjectMetadata objectMetadata) {
                return objectMetadata != null && objectMetadata.getContentLength() == contentLength;
            }

            @Override
            public String toString() {
                return "any metadata with contentLength = " + contentLength;
            }
        };
    }
    
	@Test
	public void testReadMd5StreamAndGetFiles_OneFile_1() throws IOException {

		try (FileInputStream md5stream = new FileInputStream(
				new File("src/test/resources/S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE.md5sum"))) {

			List<String> files = service.readMd5StreamAndGetFiles(
					"S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE/manifest.safe", md5stream);
			
			assertEquals(1, files.size());
			assertEquals("S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE/manifest.safe", files.get(0) );
		}
	}
	
	@Test
	public void testReadMd5StreamAndGetFiles_OneFile_2() throws IOException {

		try (FileInputStream md5stream = new FileInputStream(
				new File("src/test/resources/S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE.md5sum"))) {

			List<String> files = service.readMd5StreamAndGetFiles(
					"S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE/data/D1D09290000100212001", md5stream);
			
			assertEquals(1, files.size());
			assertEquals("S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE/data/D1D09290000100212001", files.get(0) );
		}
	}
	
	@Test
	public void testReadMd5StreamAndGetFiles_OneFile_notexist() throws IOException {

		try (FileInputStream md5stream = new FileInputStream(
				new File("src/test/resources/S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE.md5sum"))) {

			List<String> files = service.readMd5StreamAndGetFiles(
					"S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE/notexist", md5stream);
			
			assertEquals(0, files.size());
		}
	}
	
	@Test
	public void testReadMd5StreamAndGetFiles_Directory() throws IOException {
		
		try (FileInputStream md5stream = new FileInputStream(
				new File("src/test/resources/S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE.md5sum"))) {
			
			List<String> files = service.readMd5StreamAndGetFiles(
					"S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE", md5stream);
			
			assertEquals(3, files.size());
			assertEquals("S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE/data/D1D09290000100212001", files.get(0));
			assertEquals("S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE/manifest.safe", files.get(1));
			assertEquals("S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE/support/s1-aux-wnd.xsd", files.get(2));
		}
		
	}
}
