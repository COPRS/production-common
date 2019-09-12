package esa.s1pdgs.cpoc.obs_sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;

/**
 * Test the class AbstractObsClientImpl
 * 
 * @author Viveris Technologies
 */
public class AbstractObsClientTest {

	AbstractObsClientIncrementImpl uut;
	
	/**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

	@Before
	public void before() throws ObsServiceException, SdkClientException {
      uut = new AbstractObsClientIncrementImpl();
      
      /*
      MockitoAnnotations.initMocks(this);

      doThrow(new ObsServiceException("error 1 message")).when(uut)
              .doesObjectExist(Mockito.eq(
                      new ObsObject("error-key", ProductFamily.AUXILIARY_FILE)));
      doThrow(new SdkClientException("error 2 message")).when(uut)
              .doesObjectExist(Mockito.eq(
                      new ObsObject("error-key", ProductFamily.EDRS_SESSION)));
      doReturn(true).when(uut).doesObjectExist(Mockito
              .eq(new ObsObject("test-key", ProductFamily.AUXILIARY_FILE)));
      doReturn(false).when(uut).doesObjectExist(
              Mockito.eq(new ObsObject("test-key", ProductFamily.EDRS_SESSION)));

      doThrow(new ObsServiceException("error 1 message")).when(uut)
              .uploadObject(Mockito.eq(new ObsUploadObject("error-key",
                      ProductFamily.AUXILIARY_FILE, new File("pom.xml"))));
      doThrow(new SdkClientException("error 2 message")).when(uut)
              .uploadObject(Mockito.eq(new ObsUploadObject("error-key",
                      ProductFamily.EDRS_SESSION, new File("pom.xml"))));
      doReturn(2).when(uut)
              .uploadObject(Mockito.eq(new ObsUploadObject("test-key",
                      ProductFamily.AUXILIARY_FILE, new File("pom.xml"))));
      doReturn(1).when(uut)
              .uploadObject(Mockito.eq(new ObsUploadObject("test-key",
                      ProductFamily.EDRS_SESSION, new File("pom.xml"))));

      doThrow(new ObsServiceException("error 1 message")).when(uut)
              .downloadObject(Mockito.eq(new ObsDownloadObject("error-key",
                      ProductFamily.AUXILIARY_FILE, "test/")));
      doThrow(new SdkClientException("error 2 message")).when(uut)
              .downloadObject(Mockito.eq(new ObsDownloadObject("error-key",
                      ProductFamily.EDRS_SESSION, "test/")));
      doReturn(0).when(uut)
              .downloadObject(Mockito.eq(new ObsDownloadObject("test-key",
                      ProductFamily.AUXILIARY_FILE, "test/")));
      doReturn(1).when(uut)
              .downloadObject(Mockito.eq(new ObsDownloadObject("test-key",
                      ProductFamily.EDRS_SESSION, "test/")));
      doReturn(2).when(uut).downloadObject(
              Mockito.eq(new ObsDownloadObject("test-key/key2",
                      ProductFamily.EDRS_SESSION, "test/")));
      */
	}
	
    /**
     * Test downloadObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     */
    @Test
    public void testdownloadObjectsSequential()
            throws ObsServiceException, SdkClientException {

        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key1", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.EDRS_SESSION, "key2", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key3", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.EDRS_SESSION, "key/key4", "target-dir"));
        uut.downloadObjects(objects);

        assertEquals(4, uut.getCounterDownload().get());
        assertEquals(0, uut.getCounterUpload().get());
        assertEquals(0, uut.getCounterGetShutdownTm().get());
        assertEquals(0, uut.getCounterGetDownloadTm().get());
        assertEquals(0, uut.getCounterGetUploadTm().get());
        //TODO check file creation
    }

    /**
     * Test downloadObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     */
    @Test
    public void testdownloadObjectsSequentialSdkException()
            throws ObsServiceException, SdkClientException {
        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key1", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.EDRS_SESSION, "key-sdk", "target-dir"));
        objects.add(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key3", "target-dir"));
        try {
        	uut.downloadObjects(objects);
            fail("SdkClientException should be raised");
        } catch (SdkClientException sdkE) {
            assertEquals(1, uut.getCounterDownload().get());
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
     */
    @Test
    public void testdownloadObjectsSequentialServiceException()
            throws ObsServiceException, SdkClientException {
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
     */
    @Test
    public void testdownloadObjectsParallel()
            throws ObsServiceException, SdkClientException {
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
     */
    @Test
    public void testdownloadObjectsParallelSdkException()
            throws ObsServiceException, SdkClientException {
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
     */
    @Test
    public void testdownloadObjectsParallelServiceException()
            throws ObsServiceException, SdkClientException {
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
}

class AbstractObsClientIncrementImpl extends AbstractObsClient {

    private final AtomicInteger counterUpload;
    private final AtomicInteger counterDownload;
    private final AtomicInteger counterGetShutdownTm;
    private final AtomicInteger counterGetDownloadTm;
    private final AtomicInteger counterGetUploadTm;
    
    private Map<String,String> resultOfCollectMd5Sums;
    public void onCollectMd5SumsReturn(Map<String,String> resultOfCollectMd5Sums) {
    	this.resultOfCollectMd5Sums = resultOfCollectMd5Sums;
    }
    
    private Map<String,InputStream> resultOfGetAllAsInputStream;
    public void onGetAllAsInputStreamReturn(Map<String,InputStream> resultOfGetAllAsInputStream) {
    	this.resultOfGetAllAsInputStream = resultOfGetAllAsInputStream;
    }

    public AbstractObsClientIncrementImpl() {
        counterUpload = new AtomicInteger();
        counterDownload = new AtomicInteger();
        counterGetShutdownTm = new AtomicInteger();
        counterGetDownloadTm = new AtomicInteger();
        counterGetUploadTm = new AtomicInteger();
    }

    /**
     * @return the counterUpload
     */
    public AtomicInteger getCounterUpload() {
        return counterUpload;
    }

    /**
     * @return the counterDownload
     */
    public AtomicInteger getCounterDownload() {
        return counterDownload;
    }

    /**
     * @return the counterGetShutdownTm
     */
    public AtomicInteger getCounterGetShutdownTm() {
        return counterGetShutdownTm;
    }

    /**
     * @return the counterGetDownloadTm
     */
    public AtomicInteger getCounterGetDownloadTm() {
        return counterGetDownloadTm;
    }

    /**
     * @return the counterGetUploadTm
     */
    public AtomicInteger getCounterGetUploadTm() {
        return counterGetUploadTm;
    }

    @Override
    public boolean exists(ObsObject object)
            throws SdkClientException, ObsServiceException {
        throw new ObsServiceException("Method not implemented");
    }

    @Override
    public boolean prefixExists(ObsObject object)
            throws SdkClientException, ObsServiceException {
        throw new ObsServiceException("Method not implemented");
    }

    @Override
    public List<File> downloadObject(ObsDownloadObject object)
            throws SdkClientException, ObsServiceException {
        if (object.getKey().equals("key-sdk")) {
            throw new SdkClientException("Method not implemented");
        } else if (object.getKey().equals("key-aws")) {
            throw new ObsServiceException("Method not implemented");
        } else {
            counterDownload.incrementAndGet();
        }
        List<File> files = new ArrayList<>();
        files.add(new File("dummy"));
        return files;
    }

    @Override
    public void uploadObject(ObsUploadObject object)
            throws SdkClientException, ObsServiceException {
        if (object.getKey().equals("key-sdk")) {
            throw new SdkClientException("Method not implemented");
        } else if (object.getKey().equals("key-aws")) {
            throw new ObsServiceException("Method not implemented");
        } else {
            counterUpload.incrementAndGet();
        }
    }

    @Override
    public int getShutdownTimeoutS() throws ObsServiceException {
        return counterGetShutdownTm.incrementAndGet();
    }

    @Override
    public int getDownloadExecutionTimeoutS() throws ObsServiceException {
        return counterGetDownloadTm.incrementAndGet();
    }

    @Override
    public int getUploadExecutionTimeoutS() throws ObsServiceException {
        return counterGetUploadTm.incrementAndGet();
    }

	@Override
	public List<ObsObject> getObsObjectsOfFamilyWithinTimeFrame(ProductFamily obsFamily,
			Date timeFrameBegin, Date timeFrameEnd) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String,String> collectMd5Sums(ObsObject object) throws ObsServiceException, ObsException {
		return resultOfCollectMd5Sums;
	}

	@Override
	protected String getBucketFor(ProductFamily family) throws ObsServiceException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Map<String, InputStream> getAllAsInputStream(ProductFamily family, String keyPrefix)  throws SdkClientException {
		return resultOfGetAllAsInputStream;
	}
}