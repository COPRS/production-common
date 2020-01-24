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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;

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
    
    private final File tmpDir = FileUtils.createTmpDir();

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
     * @throws InterruptedException
     * @throws AmazonClientException
     * @throws AmazonServiceException
     * @throws S3ObsServiceException 
     */
    @Before
    public void init() throws AmazonServiceException, AmazonClientException,
            InterruptedException, S3ObsServiceException {
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
        
        listObjects2 = new ArrayList<String>();
        listObjects2.add("key1");
        listObjects2.add("key2");
        listObjects2.add("root/key3");

        // Build service
        service = new S3ObsServices(s3client, s3tm, 3, 500);
        serviceSpy = Mockito.spy(service);
        mockAmazonS3Client();
        mockAmazonS3TransactionManager();
    }

    /**
     * Clean
     */
    @After
    public void clean() {
    	FileUtils.delete(tmpDir.getPath());
    }

    private void mockAmazonS3TransactionManager() throws AmazonServiceException,
            AmazonClientException, InterruptedException {

        doReturn(upload).when(s3tm).upload(Mockito.anyString(),
                Mockito.anyString(), Mockito.any());
        doThrow(new com.amazonaws.SdkClientException("amazon SDK exception"))
                .when(s3tm).upload(Mockito.eq(BCK_EXC_SDK), Mockito.anyString(),
                        Mockito.any());
        doThrow(new com.amazonaws.AmazonServiceException(
                "amazon SDK exception")).when(s3tm).upload(
                        Mockito.eq(BCK_EXC_AWS), Mockito.anyString(),
                        Mockito.any());
        doReturn(new UploadResult()).when(upload).waitForUploadResult();

    }

    /**
     * Mock the amazon S3 client
     * @throws S3ObsServiceException 
     */
    private void mockAmazonS3Client() throws S3ObsServiceException {
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
        doReturn(listObjects1).when(s3client)
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
        
        
        doReturn(listObjects2).when(serviceSpy).getExpectedFiles(Mockito.eq(BCK_OBJ_EXIST), Mockito.anyString());
        doThrow(new com.amazonaws.SdkClientException("amazon SDK exception")).when(serviceSpy).getExpectedFiles(Mockito.eq(BCK_OBJ_NOT_EXIST),
                Mockito.anyString());
        doReturn(Collections.EMPTY_LIST).when(serviceSpy).getExpectedFiles(Mockito.eq(BCK_OBJ_NOT_EXIST),
                Mockito.eq("null-prefix"));
        doReturn(Collections.EMPTY_LIST).when(serviceSpy).getExpectedFiles( Mockito.eq(BCK_OBJ_NOT_EXIST), Mockito.eq("prefix"));
        doThrow(new com.amazonaws.SdkClientException("amazon SDK exception")).when(serviceSpy).getExpectedFiles(Mockito.eq(BCK_EXC_SDK), Mockito.anyString());
        doThrow(new com.amazonaws.AmazonServiceException("amazon SDK exception")).when(serviceSpy).getExpectedFiles(Mockito.eq(BCK_EXC_AWS), Mockito.anyString());

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
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_AWS)));
        thrown.expect(hasProperty("key", is("test-key")));
//        thrown.expectCause(isA(AmazonServiceException.class));

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
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_AWS)));
        thrown.expect(hasProperty("key", is("prefix")));
//        thrown.expectCause(isA(AmazonServiceException.class));

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
    	List<File> files = serviceSpy.downloadObjectsWithPrefix(BCK_OBJ_NOT_EXIST,
                "null-prefix", "directory-path", true);
        assertEquals(0, files.size());
        verify(s3client, never()).getObject(Mockito.any(GetObjectRequest.class),
                Mockito.any(File.class));

        files = serviceSpy.downloadObjectsWithPrefix(
                BCK_OBJ_NOT_EXIST, "prefix", "directory-path", true);
        assertEquals(0, files.size());
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
    	List<File> files = serviceSpy.downloadObjectsWithPrefix(BCK_OBJ_EXIST, "key", tmpDir.getPath(), true);
        assertEquals(3, files.size());
        verify(s3client, times(3)).getObject(
                Mockito.any(GetObjectRequest.class), Mockito.any(File.class));
        verify(s3client, times(1)).getObject(
                Mockito.any(GetObjectRequest.class),
                Mockito.eq(new File(tmpDir,"key1")));
        verify(s3client, times(1)).getObject(
                Mockito.any(GetObjectRequest.class),
                Mockito.eq(new File(tmpDir,"key2")));
        verify(s3client, times(1)).getObject(
                Mockito.any(GetObjectRequest.class),
                Mockito.eq(new File(tmpDir,"root/key3")));
        assertTrue((new File(tmpDir,"key3")).exists());
        assertFalse((new File(tmpDir,"root/key3")).exists());
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
    	List<File> files = serviceSpy.downloadObjectsWithPrefix(BCK_OBJ_EXIST, "key",
    			tmpDir.getPath(), false);
        assertEquals(3, files.size());
        verify(s3client, times(3)).getObject(
                Mockito.any(GetObjectRequest.class), Mockito.any(File.class));
        verify(s3client, times(1)).getObject(
                Mockito.any(GetObjectRequest.class),
                Mockito.eq(new File(tmpDir,"key1")));
        verify(s3client, times(1)).getObject(
                Mockito.any(GetObjectRequest.class),
                Mockito.eq(new File(tmpDir,"key2")));
        verify(s3client, times(1)).getObject(
                Mockito.any(GetObjectRequest.class),
                Mockito.eq(new File(tmpDir,"root/key3")));
        assertFalse((new File(tmpDir,"key3")).exists());
        assertTrue((new File(tmpDir,"root/key3")).exists());
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
        service.uploadFile(BCK_OBJ_EXIST, "key-test", tmpDir);
        verify(s3tm, times(1)).upload(Mockito.eq(BCK_OBJ_EXIST),
                Mockito.eq("key-test"), Mockito.eq(tmpDir));
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

        service.uploadFile(BCK_EXC_SDK, "key-test", tmpDir);
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
        thrown.expect(S3SdkClientException.class);
        thrown.expect(hasProperty("bucket", is(BCK_EXC_AWS)));
        thrown.expect(hasProperty("key", is("key-test")));
//        thrown.expectCause(isA(AmazonServiceException.class));

        service.uploadFile(BCK_EXC_AWS, "key-test",tmpDir);
    }

    /**
     * Nominal test case of uploadFile
     * 
     * @throws S3ObsServiceException
     * @throws S3SdkClientException
     * @throws InternalErrorException 
     */
    @Test
    public void testUploadDirectoryNominalWhenFile() throws Exception {
    	final File testFile = new File(tmpDir,"foo");
    	FileUtils.writeFile(testFile, "123");
    	
        List<String> ret = service.uploadDirectory(BCK_OBJ_EXIST, "key-test", testFile);
        assertEquals(1, ret.size());
        verify(s3tm, times(1)).upload(Mockito.eq(BCK_OBJ_EXIST), Mockito.eq("key-test"), Mockito.eq(testFile));
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
        verify(s3tm, times(3)).upload(Mockito.eq(BCK_OBJ_EXIST),
                Mockito.anyString(), Mockito.any(File.class));
        verify(s3tm, times(1)).upload(Mockito.eq(BCK_OBJ_EXIST),
                Mockito.eq("key-test" + File.separator + "key1"),
                Mockito.eq(new File(tmpDir,"key1")));
        verify(s3tm, times(1)).upload(Mockito.eq(BCK_OBJ_EXIST),
                Mockito.eq("key-test" + File.separator + "key2"),
                Mockito.eq(new File(tmpDir,"key2")));
        verify(s3tm, times(1)).upload(
                Mockito.eq(BCK_OBJ_EXIST), Mockito.eq("key-test"
                        + File.separator + "key" + File.separator + "key3"),
                Mockito.eq(new File(tmpDir,"key/key3")));

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
        File file3 = new File(tmpDir,"key");
        file3.mkdirs();

        List<String> ret = service.uploadDirectory(BCK_OBJ_EXIST, "key-test", file3);
        assertEquals(0, ret.size());
        verify(s3tm, never()).upload(Mockito.anyString(), Mockito.anyString(),
                Mockito.any(File.class));

        file3.delete();
    }
    
    @Test
    public void testIdentifyMd5File() {
    	
    	String md5file1 = service.identifyMd5File("L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSIB.xml");
    	assertEquals("L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSIB.xml.md5sum", md5file1);
    	
    	String md5file2 = service.identifyMd5File("L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSDB_00035.raw");
    	assertEquals("L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSDB_00035.raw.md5sum", md5file2);
    	
    	String md5file3 = service.identifyMd5File("S1__AUX_WND_V20181002T210000_G20180929T181057.SAFE/");
    	assertEquals("S1__AUX_WND_V20181002T210000_G20180929T181057.SAFE.md5sum", md5file3);
    	
    	String md5file4 = service.identifyMd5File("S1B_OPER_MPL_ORBPRE_20190711T200257_20190718T200257_0001.EOF");
    	assertEquals("S1B_OPER_MPL_ORBPRE_20190711T200257_20190718T200257_0001.EOF.md5sum", md5file4);
    	
    	String md5file5 = service.identifyMd5File("S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE/manifest.safe");
    	assertEquals("S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE.md5sum", md5file5);
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
