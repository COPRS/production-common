package esa.s1pdgs.cpoc.obs_sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import esa.s1pdgs.cpoc.common.ProductFamily;

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
                      new ObsObject("error-key", ObsFamily.AUXILIARY_FILE)));
      doThrow(new SdkClientException("error 2 message")).when(uut)
              .doesObjectExist(Mockito.eq(
                      new ObsObject("error-key", ObsFamily.EDRS_SESSION)));
      doReturn(true).when(uut).doesObjectExist(Mockito
              .eq(new ObsObject("test-key", ObsFamily.AUXILIARY_FILE)));
      doReturn(false).when(uut).doesObjectExist(
              Mockito.eq(new ObsObject("test-key", ObsFamily.EDRS_SESSION)));

      doThrow(new ObsServiceException("error 1 message")).when(uut)
              .uploadObject(Mockito.eq(new ObsUploadObject("error-key",
                      ObsFamily.AUXILIARY_FILE, new File("pom.xml"))));
      doThrow(new SdkClientException("error 2 message")).when(uut)
              .uploadObject(Mockito.eq(new ObsUploadObject("error-key",
                      ObsFamily.EDRS_SESSION, new File("pom.xml"))));
      doReturn(2).when(uut)
              .uploadObject(Mockito.eq(new ObsUploadObject("test-key",
                      ObsFamily.AUXILIARY_FILE, new File("pom.xml"))));
      doReturn(1).when(uut)
              .uploadObject(Mockito.eq(new ObsUploadObject("test-key",
                      ObsFamily.EDRS_SESSION, new File("pom.xml"))));

      doThrow(new ObsServiceException("error 1 message")).when(uut)
              .downloadObject(Mockito.eq(new ObsDownloadObject("error-key",
                      ObsFamily.AUXILIARY_FILE, "test/")));
      doThrow(new SdkClientException("error 2 message")).when(uut)
              .downloadObject(Mockito.eq(new ObsDownloadObject("error-key",
                      ObsFamily.EDRS_SESSION, "test/")));
      doReturn(0).when(uut)
              .downloadObject(Mockito.eq(new ObsDownloadObject("test-key",
                      ObsFamily.AUXILIARY_FILE, "test/")));
      doReturn(1).when(uut)
              .downloadObject(Mockito.eq(new ObsDownloadObject("test-key",
                      ObsFamily.EDRS_SESSION, "test/")));
      doReturn(2).when(uut).downloadObject(
              Mockito.eq(new ObsDownloadObject("test-key/key2",
                      ObsFamily.EDRS_SESSION, "test/")));
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
        objects.add(new ObsDownloadObject("key1", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        objects.add(new ObsDownloadObject("key2", ObsFamily.EDRS_SESSION,
                "target-dir"));
        objects.add(new ObsDownloadObject("key3", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        objects.add(new ObsDownloadObject("key/key4", ObsFamily.EDRS_SESSION,
                "target-dir"));
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
        objects.add(new ObsDownloadObject("key1", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        objects.add(new ObsDownloadObject("key-sdk", ObsFamily.EDRS_SESSION,
                "target-dir"));
        objects.add(new ObsDownloadObject("key3", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
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
        objects.add(new ObsDownloadObject("key1", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        objects.add(new ObsDownloadObject("key2", ObsFamily.EDRS_SESSION,
                "target-dir"));
        objects.add(new ObsDownloadObject("key-aws", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
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
        objects.add(new ObsDownloadObject("key1", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        objects.add(new ObsDownloadObject("key2", ObsFamily.EDRS_SESSION,
                "target-dir"));
        objects.add(new ObsDownloadObject("key3", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
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
        objects.add(new ObsDownloadObject("key1", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        objects.add(new ObsDownloadObject("key-sdk", ObsFamily.EDRS_SESSION,
                "target-dir"));
        objects.add(new ObsDownloadObject("key3", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
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
        objects.add(new ObsDownloadObject("key1", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        objects.add(new ObsDownloadObject("key2", ObsFamily.EDRS_SESSION,
                "target-dir"));
        objects.add(new ObsDownloadObject("key-aws", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
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
     */
    @Test
    public void testuploadObjectsSequential()
            throws ObsServiceException, SdkClientException {
        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject("key1", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key2", ObsFamily.EDRS_SESSION,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key3", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
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
     */
    @Test
    public void testuploadObjectsSequentialSdkException()
            throws ObsServiceException, SdkClientException {
        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject("key1", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key-sdk", ObsFamily.EDRS_SESSION,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key3", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
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
     */
    @Test
    public void testuploadObjectsSequentialServiceException()
            throws ObsServiceException, SdkClientException {
        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject("key1", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key2", ObsFamily.EDRS_SESSION,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key-aws", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
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
     */
    @Test
    public void testuploadObjectsParallel()
            throws ObsServiceException, SdkClientException {
        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject("key1", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key2", ObsFamily.EDRS_SESSION,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key3", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
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
     */
    @Test
    public void testuploadObjectsParallelSdkException()
            throws ObsServiceException, SdkClientException {
        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject("key1", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key-sdk", ObsFamily.EDRS_SESSION,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key3", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
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
     */
    @Test
    public void testuploadObjectsParallelServiceException()
            throws ObsServiceException, SdkClientException {
        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject("key1", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key2", ObsFamily.EDRS_SESSION,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key-aws", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
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

    /**
     * Test getObsFamily
     */
    @Test
    public void testGetObsFamily() {
        assertEquals(ObsFamily.AUXILIARY_FILE, uut.getObsFamily(ProductFamily.AUXILIARY_FILE));
        assertEquals(ObsFamily.EDRS_SESSION, uut.getObsFamily(ProductFamily.EDRS_SESSION));
        assertEquals(ObsFamily.UNKNOWN, uut.getObsFamily(ProductFamily.BLANK));
        assertEquals(ObsFamily.L0_ACN, uut.getObsFamily(ProductFamily.L0_ACN));
        assertEquals(ObsFamily.L0_SLICE, uut.getObsFamily(ProductFamily.L0_SLICE));
        assertEquals(ObsFamily.L0_SEGMENT, uut.getObsFamily(ProductFamily.L0_SEGMENT));
        assertEquals(ObsFamily.UNKNOWN, uut.getObsFamily(ProductFamily.L0_JOB));
        assertEquals(ObsFamily.UNKNOWN, uut.getObsFamily(ProductFamily.L0_REPORT));
        assertEquals(ObsFamily.L1_ACN, uut.getObsFamily(ProductFamily.L1_ACN));
        assertEquals(ObsFamily.L1_SLICE, uut.getObsFamily(ProductFamily.L1_SLICE));
        assertEquals(ObsFamily.UNKNOWN, uut.getObsFamily(ProductFamily.L1_REPORT));
        assertEquals(ObsFamily.UNKNOWN, uut.getObsFamily(ProductFamily.JOB_ORDER));
    }
}

class AbstractObsClientIncrementImpl extends AbstractObsClient {

    private final AtomicInteger counterUpload;
    private final AtomicInteger counterDownload;
    private final AtomicInteger counterGetShutdownTm;
    private final AtomicInteger counterGetDownloadTm;
    private final AtomicInteger counterGetUploadTm;

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
    public boolean doesObjectExist(ObsObject object)
            throws SdkClientException, ObsServiceException {
        throw new ObsServiceException("Method not implemented");
    }

    @Override
    public boolean doesPrefixExist(ObsObject object)
            throws SdkClientException, ObsServiceException {
        throw new ObsServiceException("Method not implemented");
    }

    @Override
    public int downloadObject(ObsDownloadObject object)
            throws SdkClientException, ObsServiceException {
        if (object.getKey().equals("key-sdk")) {
            throw new SdkClientException("Method not implemented");
        } else if (object.getKey().equals("key-aws")) {
            throw new ObsServiceException("Method not implemented");
        } else {
            return counterDownload.incrementAndGet();
        }

    }

    @Override
    public int uploadObject(ObsUploadObject object)
            throws SdkClientException, ObsServiceException {
        if (object.getKey().equals("key-sdk")) {
            throw new SdkClientException("Method not implemented");
        } else if (object.getKey().equals("key-aws")) {
            throw new ObsServiceException("Method not implemented");
        } else {
            return counterUpload.incrementAndGet();
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
	public List<ObsObject> getListOfObjectsOfTimeFrameOfFamily(Date timeFrameBegin, Date timeFrameEnd,
			ObsFamily obsFamily) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub
		return null;
	}
}