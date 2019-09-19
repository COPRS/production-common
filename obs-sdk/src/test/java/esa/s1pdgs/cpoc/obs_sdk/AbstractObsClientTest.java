package esa.s1pdgs.cpoc.obs_sdk;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.util.Arrays;
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
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;

/**
 * Test the class AbstractObsClientImpl
 * 
 * @author Viveris Technologies
 */
public class AbstractObsClientTest {
	
	private final File tmpDir = FileUtils.createTmpDir();

	@Mock
	AbstractObsClient uut;
	
	/**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    private final ObsObject errExistObject  = new ObsObject(ProductFamily.AUXILIARY_FILE, "error-key");
    private final ObsObject errExistObject2 = new ObsObject(ProductFamily.EDRS_SESSION, "error-key");
    private final ObsObject existsObject 	= new ObsObject(ProductFamily.AUXILIARY_FILE, "test-key");
    private final ObsObject absentObject 	= new ObsObject(ProductFamily.EDRS_SESSION, "test-key");
    
    private final ObsUploadObject ulErrObj	= new ObsUploadObject(ProductFamily.AUXILIARY_FILE,"error-key",new File("foo"));
    private final ObsUploadObject ulErrObj2	= new ObsUploadObject(ProductFamily.EDRS_SESSION,"error-key",new File("foo"));
    
	@Before
	public void before() throws Exception {
      MockitoAnnotations.initMocks(this);

      doThrow(new ObsServiceException("error 1 message"))
      	.when(uut).exists(Mockito.eq(errExistObject));
      
      doThrow(new SdkClientException("error 2 message"))
      	.when(uut).exists(Mockito.eq(errExistObject2));
      
      doReturn(true)
      	.when(uut).exists(Mockito.eq(existsObject));
      
      doReturn(false)
      	.when(uut).exists(Mockito.eq(absentObject));

      doThrow(new ObsServiceException("error 1 message"))
      	.when(uut).uploadObject(Mockito.eq(ulErrObj));
      
      doThrow(new SdkClientException("error 2 message"))
      	.when(uut).uploadObject(Mockito.eq(ulErrObj2));

      doThrow(new ObsServiceException("error 1 message"))
      	.when(uut).downloadObject(Mockito.eq(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE,"error-key", "test/")));
      
      doThrow(new SdkClientException("error 2 message"))
      	.when(uut).downloadObject(Mockito.eq(new ObsDownloadObject(ProductFamily.EDRS_SESSION,"error-key", "test/")));
      
      doReturn(Collections.emptyList())
      	.when(uut).downloadObject(Mockito.eq(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE,"test-key", "test/")));
      
      doReturn(Collections.singletonList(new File("0")))
      	.when(uut).downloadObject(Mockito.eq(new ObsDownloadObject(ProductFamily.EDRS_SESSION,"test-key", "test/")));
      
      doReturn(Arrays.asList(new File[] {new File("1"), new File("2")}))
      	.when(uut).downloadObject(Mockito.eq(new ObsDownloadObject(ProductFamily.EDRS_SESSION,"test-key/key2", "test/")));
	}
	
	@After
	public final void tearDown() {
		FileUtils.delete(tmpDir.getPath());
	}
	
	
	
	
    /**
     * Test downloadObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws ObsException 
     */
    @Test
    public void testdownloadObjectsSequential()
            throws ObsServiceException, SdkClientException, ObsException {

        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key1", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.EDRS_SESSION, "key2", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key3", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.EDRS_SESSION, "key/key4", "target-dir"));
        uut.downloadObjects(objects);
        //TODO check file creation
    }

    /**
     * Test downloadObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws ObsException 
     */
    @Test
    public void testdownloadObjectsSequentialSdkException()
            throws ObsServiceException, SdkClientException, ObsException {
        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key1", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.EDRS_SESSION, "key-sdk", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key3", "target-dir"));
        try {
        	uut.downloadObjects(objects);
            fail("SdkClientException should be raised");
        } catch (SdkClientException sdkE) {
            
        }
    }

    /**
     * Test downloadObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws ObsException 
     */
    @Test
    public void testdownloadObjectsSequentialServiceException()
            throws ObsServiceException, SdkClientException, ObsException {
        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key1", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.EDRS_SESSION, "key2", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key-aws", "target-dir"));
        try {
        	uut.downloadObjects(objects);
            fail("ObsServiceException should be raised");
        } catch (ObsServiceException sdkE) {
            assertEquals(2, uut.getCounterDownload().get());
            assertEquals(0, uut.getCounterUpload().get());
            assertEquals(0, uut.getCounterGetShutdownTm().get());
            assertEquals(0, uut.getCounterGetDownloadTm().get());
            assertEquals(0, uut.getCounterGetUploadTm().get());
        }
    }

    /**
     * Test downloadObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws ObsException 
     */
    @Test
    public void testdownloadObjectsParallel()
            throws ObsServiceException, SdkClientException, ObsException {
        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key1", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.EDRS_SESSION, "key2", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key3", "target-dir"));
        uut.downloadObjects(objects, true);

        assertEquals(3, uut.getCounterDownload().get());
        assertEquals(0, uut.getCounterUpload().get());
        assertEquals(1, uut.getCounterGetShutdownTm().get());
        assertEquals(1, uut.getCounterGetDownloadTm().get());
        assertEquals(0, uut.getCounterGetUploadTm().get());
    }

    /**
     * Test downloadObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws ObsException 
     */
    @Test
    public void testdownloadObjectsParallelSdkException()
            throws ObsServiceException, SdkClientException, ObsException {
        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key1", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.EDRS_SESSION, "key-sdk", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key3", "target-dir"));
        try {
        	uut.downloadObjects(objects, true);
            fail("SdkClientException should be raised");
        } catch (SdkClientException sdkE) {
            assertTrue(uut.getCounterDownload().get() > 0);
            assertEquals(0, uut.getCounterUpload().get());
            assertEquals(1, uut.getCounterGetShutdownTm().get());
            assertTrue(uut.getCounterGetDownloadTm().get() > 0);
            assertEquals(0, uut.getCounterGetUploadTm().get());
        }
    }

    /**
     * Test downloadObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws ObsException 
     */
    @Test
    public void testdownloadObjectsParallelServiceException()
            throws ObsServiceException, SdkClientException, ObsException {
        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key1", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.EDRS_SESSION, "key2", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key-aws", "target-dir"));
        try {
        	uut.downloadObjects(objects, true);
            fail("ObsServiceException should be raised");
        } catch (ObsServiceException sdkE) {
            assertTrue(uut.getCounterDownload().get() > 0);
            assertEquals(0, uut.getCounterUpload().get());
            assertEquals(1, uut.getCounterGetShutdownTm().get());
            assertTrue(uut.getCounterGetDownloadTm().get() > 0);
            assertEquals(0, uut.getCounterGetUploadTm().get());
        }
    }

    /**
     * Test uploaObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws ObsException 
     */
    @Test
    public void testuploadObjectsSequential()
            throws ObsServiceException, SdkClientException, ObsException {
        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key1", new File("target-dir")));
        objects.add(new ObsUploadObject(ProductFamily.EDRS_SESSION, "key2", new File("target-dir")));
        objects.add(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key3", new File("target-dir")));
        uut.uploadObjects(objects);

        assertEquals(3, uut.getCounterUpload().get());
        assertEquals(0, uut.getCounterDownload().get());
        assertEquals(0, uut.getCounterGetShutdownTm().get());
        assertEquals(0, uut.getCounterGetDownloadTm().get());
        assertEquals(0, uut.getCounterGetUploadTm().get());
    }

    /**
     * Test downloadObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws ObsException 
     */
    @Test
    public void testuploadObjectsSequentialSdkException()
            throws ObsServiceException, SdkClientException, ObsException {
        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key1", new File("target-dir")));
        objects.add(new ObsUploadObject(ProductFamily.EDRS_SESSION, "key-sdk", new File("target-dir")));
        objects.add(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key3", new File("target-dir")));
        try {
        	uut.uploadObjects(objects);
            fail("SdkClientException should be raised");
        } catch (SdkClientException sdkE) {
            assertEquals(0, uut.getCounterDownload().get());
            assertEquals(1, uut.getCounterUpload().get());
            assertEquals(0, uut.getCounterGetShutdownTm().get());
            assertEquals(0, uut.getCounterGetDownloadTm().get());
            assertEquals(0, uut.getCounterGetUploadTm().get());
        }
    }

    /**
     * Test downloadObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws ObsException 
     */
    @Test
    public void testuploadObjectsSequentialServiceException()
            throws ObsServiceException, SdkClientException, ObsException {
        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key1", new File("target-dir")));
        objects.add(new ObsUploadObject(ProductFamily.EDRS_SESSION, "key2", new File("target-dir")));
        objects.add(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key-aws", new File("target-dir")));
        try {
        	uut.uploadObjects(objects);
            fail("ObsServiceException should be raised");
        } catch (ObsServiceException sdkE) {
            assertEquals(0, uut.getCounterDownload().get());
            assertEquals(2, uut.getCounterUpload().get());
            assertEquals(0, uut.getCounterGetShutdownTm().get());
            assertEquals(0, uut.getCounterGetDownloadTm().get());
            assertEquals(0, uut.getCounterGetUploadTm().get());
        }
    }

    /**
     * Test uploaObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws ObsException 
     */
    @Test
    public void testuploadObjectsParallel()
            throws ObsServiceException, SdkClientException, ObsException {
        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key1", new File("target-dir")));
        objects.add(new ObsUploadObject(ProductFamily.EDRS_SESSION, "key2", new File("target-dir")));
        objects.add(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key3", new File("target-dir")));
        uut.uploadObjects(objects, true);

        assertEquals(3, uut.getCounterUpload().get());
        assertEquals(0, uut.getCounterDownload().get());
        assertEquals(1, uut.getCounterGetShutdownTm().get());
        assertEquals(0, uut.getCounterGetDownloadTm().get());
        assertEquals(1, uut.getCounterGetUploadTm().get());
    }

    /**
     * Test downloadObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws ObsException 
     */
    @Test
    public void testuploadObjectsParallelSdkException()
            throws ObsServiceException, SdkClientException, ObsException {
        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key1", new File("target-dir")));
        objects.add(new ObsUploadObject(ProductFamily.EDRS_SESSION, "key-sdk", new File("target-dir")));
        objects.add(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key3", new File("target-dir")));
        try {
        	uut.uploadObjects(objects, true);
            fail("SdkClientException should be raised");
        } catch (SdkClientException sdkE) {
            assertTrue(uut.getCounterUpload().get() > 0);
            assertEquals(0, uut.getCounterDownload().get());
            assertEquals(1, uut.getCounterGetShutdownTm().get());
            assertTrue(uut.getCounterGetUploadTm().get() > 0);
            assertEquals(0, uut.getCounterGetDownloadTm().get());
        }
    }

    /**
     * Test downloadObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws ObsException 
     */
    @Test
    public void testuploadObjectsParallelServiceException()
            throws ObsServiceException, SdkClientException, ObsException {
        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key1", new File("target-dir")));
        objects.add(new ObsUploadObject(ProductFamily.EDRS_SESSION, "key2", new File("target-dir")));
        objects.add(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key-aws", new File("target-dir")));
        try {
        	uut.uploadObjects(objects, true);
            fail("ObsServiceException should be raised");
        } catch (ObsServiceException sdkE) {
            assertTrue(uut.getCounterUpload().get() > 0);
            assertEquals(0, uut.getCounterDownload().get());
            assertEquals(1, uut.getCounterGetShutdownTm().get());
            assertTrue(uut.getCounterGetUploadTm().get() > 0);
            assertEquals(0, uut.getCounterGetDownloadTm().get());
        }
    }
    
    @Test
    public void validateTest_onMd5SumNotFound() throws ObsServiceException, ObsValidationException {
    	uut.onGetAllAsInputStreamReturn(new HashMap<>());
    	ObsObject obsObject = new ObsObject(ProductFamily.AUXILIARY_FILE, "md5sum-not-existing");
    	thrown.expect(ObsValidationException.class);
    	thrown.expectMessage("Checksum file not found for: md5sum-not-existing of family " + ProductFamily.AUXILIARY_FILE);
    	uut.validate(obsObject);
    }
    
    @Test
    public void validateTest_onMultipleMd5() throws ObsServiceException, ObsValidationException {
    	HashMap<String,InputStream> isMap = new HashMap<>();
    	isMap.put("multiple-results.md5sum", new ByteArrayInputStream("".getBytes()));
    	isMap.put("multiple-results.md5sum2", new ByteArrayInputStream("".getBytes()));
    	uut.onGetAllAsInputStreamReturn(isMap);
    	ObsObject obsObject = new ObsObject(ProductFamily.AUXILIARY_FILE, "multiple-results");
    	thrown.expect(ObsValidationException.class);
    	thrown.expectMessage("More than one checksum file returned");
    	uut.validate(obsObject);
    }
    
    @Test
    public void validateTest_onNominalValidation() throws ObsServiceException, ObsValidationException {
    	HashMap<String,String> md5Sums = new HashMap<>();
    	md5Sums.put("key/file1","00000000000000000000000000000001");
    	md5Sums.put("key/file2","00000000000000000000000000000002");
    	uut.onCollectMd5SumsReturn(md5Sums);
    	HashMap<String,InputStream> isMap = new HashMap<>();
    	isMap.put("key.md5sum", new ByteArrayInputStream("00000000000000000000000000000001  key/file1\n00000000000000000000000000000002  key/file2".getBytes()));
    	uut.onGetAllAsInputStreamReturn(isMap);
    	ObsObject obsObject = new ObsObject(ProductFamily.AUXILIARY_FILE, "key");
    	uut.validate(obsObject);
    }
    
    @Test
    public void validateTest_onDifferentChecksum() throws ObsServiceException, ObsValidationException {
    	HashMap<String,String> md5Sums = new HashMap<>();
    	md5Sums.put("key/file1","00000000000000000000000000000001");
    	md5Sums.put("key/file2","00000000000000000000000000000002");
    	uut.onCollectMd5SumsReturn(md5Sums);
    	HashMap<String,InputStream> isMap = new HashMap<>();
    	isMap.put("key.md5sum", new ByteArrayInputStream("00000000000000000000000000000023  key/file1\n00000000000000000000000000000002  key/file2".getBytes()));
    	uut.onGetAllAsInputStreamReturn(isMap);    	
    	ObsObject obsObject = new ObsObject(ProductFamily.AUXILIARY_FILE, "key");
    	thrown.expect(ObsValidationException.class);
    	thrown.expectMessage("Checksum is wrong for object: key/file1 of family " + ProductFamily.AUXILIARY_FILE);
    	uut.validate(obsObject);
    }

    @Test
    public void validateTest_onMissingObject() throws ObsServiceException, ObsValidationException {
    	HashMap<String,String> md5Sums = new HashMap<>();
    	md5Sums.put("key/file1","00000000000000000000000000000001");
    	uut.onCollectMd5SumsReturn(md5Sums);
    	HashMap<String,InputStream> isMap = new HashMap<>();
    	isMap.put("key.md5sum", new ByteArrayInputStream("00000000000000000000000000000001  key/file1\n00000000000000000000000000000002  key/file2".getBytes()));
    	uut.onGetAllAsInputStreamReturn(isMap);    	
    	ObsObject obsObject = new ObsObject(ProductFamily.AUXILIARY_FILE, "key");
    	thrown.expect(ObsValidationException.class);
    	thrown.expectMessage("Object not found: key/file2 of family " + ProductFamily.AUXILIARY_FILE);
    	uut.validate(obsObject);
    }

    @Test
    public void validateTest_onUnexpectedObject() throws ObsServiceException, ObsValidationException {
    	HashMap<String,String> md5Sums = new HashMap<>();
    	md5Sums.put("key/file1","00000000000000000000000000000001");
    	md5Sums.put("key/file2","00000000000000000000000000000002");
    	md5Sums.put("key/unexpected","00000000000000000000000000000003");
    	uut.onCollectMd5SumsReturn(md5Sums);
    	HashMap<String,InputStream> isMap = new HashMap<>();
    	isMap.put("key.md5sum", new ByteArrayInputStream("00000000000000000000000000000001  key/file1\n00000000000000000000000000000002  key/file2".getBytes()));
    	uut.onGetAllAsInputStreamReturn(isMap);    	
    	ObsObject obsObject = new ObsObject(ProductFamily.AUXILIARY_FILE, "key");
    	thrown.expect(ObsValidationException.class);
    	thrown.expectMessage("Unexpected object found: key/unexpected for key of family " + ProductFamily.AUXILIARY_FILE);
    	uut.validate(obsObject);
    }

    @Test
    public void validateTest_onObjectNotFound() throws ObsServiceException, ObsValidationException {
    	uut.onGetAllAsInputStreamReturn(new HashMap<>());
    	ObsObject obsObject = new ObsObject(ProductFamily.AUXILIARY_FILE, "md5sum-not-existing");
    	thrown.expect(ObsValidationException.class);
    	thrown.expectMessage("Checksum file not found for: md5sum-not-existing of family " + ProductFamily.AUXILIARY_FILE);
    	uut.validate(obsObject);
    }
    
    @Test
    public void testDownloadValidArgumentAssertion() throws AbstractCodedException {
    	assertThatThrownBy(() -> uut.download(null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid object: null");
    	assertThatThrownBy(() -> uut.download(Collections.emptyList())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid collection (empty)");
    	assertThatThrownBy(() -> uut.download(java.util.Arrays.asList((ObsDownloadObject)null))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid object: null");
    	assertThatThrownBy(() -> uut.download(java.util.Arrays.asList(new ObsDownloadObject(null, "key", "targetDir")))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
    	assertThatThrownBy(() -> uut.download(java.util.Arrays.asList(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, null, "targetDir")))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key: null");
    	assertThatThrownBy(() -> uut.download(java.util.Arrays.asList(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "", "targetDir")))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key (empty)");
    	assertThatThrownBy(() -> uut.download(java.util.Arrays.asList(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key", null)))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid targetDir: null");
    	assertThatThrownBy(() -> uut.download(java.util.Arrays.asList(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key", "")))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid targetDir (empty)");
    }
    
    @Test
    public void testUploadValidArgumentAssertion() throws AbstractCodedException {
    	assertThatThrownBy(() -> uut.upload(null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid object: null");
    	assertThatThrownBy(() -> uut.upload(Collections.emptyList())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid collection (empty)");
    	assertThatThrownBy(() -> uut.upload(java.util.Arrays.asList((ObsUploadObject)null))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid object: null");
    	assertThatThrownBy(() -> uut.upload(java.util.Arrays.asList(new ObsUploadObject(null, "key", new File("filepath"))))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
    	assertThatThrownBy(() -> uut.upload(java.util.Arrays.asList(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, null, new File("filepath"))))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key: null");
    	assertThatThrownBy(() -> uut.upload(java.util.Arrays.asList(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "",  new File("filepath"))))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key (empty)");
    	assertThatThrownBy(() -> uut.upload(java.util.Arrays.asList(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key", null)))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid file: null");
    	assertThatThrownBy(() -> uut.upload(java.util.Arrays.asList(new ObsUploadObject(ProductFamily.AUXILIARY_FILE, "key", new File(""))))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid file (empty filename)");
    }

    @Test
    public void testValidateValidArgumentAssertion() throws AbstractCodedException {
    	assertThatThrownBy(() -> uut.validate(null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid object: null");
    	assertThatThrownBy(() -> uut.validate(new ObsObject(null, "key"))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
    	assertThatThrownBy(() -> uut.validate(new ObsObject(ProductFamily.AUXILIARY_FILE, null))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key: null");
    	assertThatThrownBy(() -> uut.validate(new ObsObject(ProductFamily.AUXILIARY_FILE, ""))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key (empty)");
    }
    
    @Test
    public void testListIntervalValidArgumentAssertion() throws AbstractCodedException {
    	assertThatThrownBy(() -> uut.listInterval(null, new Date(), new Date())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
    	assertThatThrownBy(() -> uut.listInterval(ProductFamily.AUXILIARY_FILE, null, new Date())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid date: null");
    	assertThatThrownBy(() -> uut.listInterval(ProductFamily.AUXILIARY_FILE, new Date(), null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid date: null");
    }

}