package esa.s1pdgs.cpoc.obs_sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
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
    public final void testGetDsibNames() throws Exception {
    	final String content = "<DCSU_Session_Information_Block>\n" + 
    			"    <session_id>L20180724144436762001030</session_id>\n" + 
    			"    <time_start>2018-10-01T15:16:09Z</time_start>\n" + 
    			"    <time_stop>2018-10-01T15:33:46Z</time_stop>\n" + 
    			"    <time_created>2018-10-01T15:16:09Z</time_created>\n" + 
    			"    <time_finished>2018-10-01T15:33:19Z</time_finished>\n" + 
    			"    <data_size>36014919890</data_size>\n" + 
    			"    <dsdb_list>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00001.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00002.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00003.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00004.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00005.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00006.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00007.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00008.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00009.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00010.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00011.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00012.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00013.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00014.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00015.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00016.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00017.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00018.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00019.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00020.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00021.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00022.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00023.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00024.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00025.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00026.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00027.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00028.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00029.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00030.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00031.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00032.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00033.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00034.raw</dsdb_name>\n" + 
    			"        <dsdb_name>DCS_02_L20180724144436762001030_ch1_DSDB_00035.raw</dsdb_name>\n" + 
    			"    </dsdb_list>\n" + 
    			"</DCSU_Session_Information_Block>";
    	List<String> result = uut.getDsibNames(new ByteArrayInputStream(content.getBytes()));
    	assertEquals(35, result.size());
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
	public void validate(ObsObject object) throws ObsServiceException {
	  // TODO Auto-generated method stub
	}

	@Override
	public Map<String,String> collectMd5Sums(ObsObject object) throws ObsServiceException, ObsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getBucketFor(ProductFamily family) throws ObsServiceException {
		// TODO Auto-generated method stub
		return null;
	}
}