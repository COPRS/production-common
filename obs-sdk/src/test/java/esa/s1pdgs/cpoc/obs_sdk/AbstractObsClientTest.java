package esa.s1pdgs.cpoc.obs_sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import esa.s1pdgs.cpoc.obs_sdk.AbstractObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

/**
 * Test the class AbstractObsClientImpl
 * 
 * @author Viveris Technologies
 */
public class AbstractObsClientTest {

    /**
     * Test downloadObjects
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     */
    @Test
    public void testdownloadObjectsSequential()
            throws ObsServiceException, SdkClientException {
        AbstractObsClientIncrementImpl client =
                new AbstractObsClientIncrementImpl();

        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(new ObsDownloadObject("key1", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        objects.add(new ObsDownloadObject("key2", ObsFamily.EDRS_SESSION,
                "target-dir"));
        objects.add(new ObsDownloadObject("key3", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        objects.add(new ObsDownloadObject("key/key4", ObsFamily.EDRS_SESSION,
                "target-dir"));
        client.downloadObjects(objects);

        assertEquals(4, client.getCounterDownload().get());
        assertEquals(0, client.getCounterUpload().get());
        assertEquals(0, client.getCounterGetShutdownTm().get());
        assertEquals(0, client.getCounterGetDownloadTm().get());
        assertEquals(0, client.getCounterGetUploadTm().get());
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
        AbstractObsClientIncrementImpl client =
                new AbstractObsClientIncrementImpl();

        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(new ObsDownloadObject("key1", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        objects.add(new ObsDownloadObject("key-sdk", ObsFamily.EDRS_SESSION,
                "target-dir"));
        objects.add(new ObsDownloadObject("key3", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        try {
            client.downloadObjects(objects);
            fail("SdkClientException should be raised");
        } catch (SdkClientException sdkE) {
            assertEquals(1, client.getCounterDownload().get());
            assertEquals(0, client.getCounterUpload().get());
            assertEquals(0, client.getCounterGetShutdownTm().get());
            assertEquals(0, client.getCounterGetDownloadTm().get());
            assertEquals(0, client.getCounterGetUploadTm().get());
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
        AbstractObsClientIncrementImpl client =
                new AbstractObsClientIncrementImpl();

        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(new ObsDownloadObject("key1", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        objects.add(new ObsDownloadObject("key2", ObsFamily.EDRS_SESSION,
                "target-dir"));
        objects.add(new ObsDownloadObject("key-aws", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        try {
            client.downloadObjects(objects);
            fail("ObsServiceException should be raised");
        } catch (ObsServiceException sdkE) {
            assertEquals(2, client.getCounterDownload().get());
            assertEquals(0, client.getCounterUpload().get());
            assertEquals(0, client.getCounterGetShutdownTm().get());
            assertEquals(0, client.getCounterGetDownloadTm().get());
            assertEquals(0, client.getCounterGetUploadTm().get());
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
        AbstractObsClientIncrementImpl client =
                new AbstractObsClientIncrementImpl();

        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(new ObsDownloadObject("key1", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        objects.add(new ObsDownloadObject("key2", ObsFamily.EDRS_SESSION,
                "target-dir"));
        objects.add(new ObsDownloadObject("key3", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        client.downloadObjects(objects, true);

        assertEquals(3, client.getCounterDownload().get());
        assertEquals(0, client.getCounterUpload().get());
        assertEquals(1, client.getCounterGetShutdownTm().get());
        assertEquals(1, client.getCounterGetDownloadTm().get());
        assertEquals(0, client.getCounterGetUploadTm().get());
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
        AbstractObsClientIncrementImpl client =
                new AbstractObsClientIncrementImpl();

        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(new ObsDownloadObject("key1", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        objects.add(new ObsDownloadObject("key-sdk", ObsFamily.EDRS_SESSION,
                "target-dir"));
        objects.add(new ObsDownloadObject("key3", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        try {
            client.downloadObjects(objects, true);
            fail("SdkClientException should be raised");
        } catch (SdkClientException sdkE) {
            assertTrue(client.getCounterDownload().get() > 0);
            assertEquals(0, client.getCounterUpload().get());
            assertEquals(1, client.getCounterGetShutdownTm().get());
            assertTrue(client.getCounterGetDownloadTm().get() > 0);
            assertEquals(0, client.getCounterGetUploadTm().get());
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
        AbstractObsClientIncrementImpl client =
                new AbstractObsClientIncrementImpl();

        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(new ObsDownloadObject("key1", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        objects.add(new ObsDownloadObject("key2", ObsFamily.EDRS_SESSION,
                "target-dir"));
        objects.add(new ObsDownloadObject("key-aws", ObsFamily.AUXILIARY_FILE,
                "target-dir"));
        try {
            client.downloadObjects(objects, true);
            fail("ObsServiceException should be raised");
        } catch (ObsServiceException sdkE) {
            assertTrue(client.getCounterDownload().get() > 0);
            assertEquals(0, client.getCounterUpload().get());
            assertEquals(1, client.getCounterGetShutdownTm().get());
            assertTrue(client.getCounterGetDownloadTm().get() > 0);
            assertEquals(0, client.getCounterGetUploadTm().get());
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
        AbstractObsClientIncrementImpl client =
                new AbstractObsClientIncrementImpl();

        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject("key1", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key2", ObsFamily.EDRS_SESSION,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key3", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        client.uploadObjects(objects);

        assertEquals(3, client.getCounterUpload().get());
        assertEquals(0, client.getCounterDownload().get());
        assertEquals(0, client.getCounterGetShutdownTm().get());
        assertEquals(0, client.getCounterGetDownloadTm().get());
        assertEquals(0, client.getCounterGetUploadTm().get());
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
        AbstractObsClientIncrementImpl client =
                new AbstractObsClientIncrementImpl();

        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject("key1", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key-sdk", ObsFamily.EDRS_SESSION,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key3", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        try {
            client.uploadObjects(objects);
            fail("SdkClientException should be raised");
        } catch (SdkClientException sdkE) {
            assertEquals(0, client.getCounterDownload().get());
            assertEquals(1, client.getCounterUpload().get());
            assertEquals(0, client.getCounterGetShutdownTm().get());
            assertEquals(0, client.getCounterGetDownloadTm().get());
            assertEquals(0, client.getCounterGetUploadTm().get());
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
        AbstractObsClientIncrementImpl client =
                new AbstractObsClientIncrementImpl();

        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject("key1", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key2", ObsFamily.EDRS_SESSION,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key-aws", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        try {
            client.uploadObjects(objects);
            fail("ObsServiceException should be raised");
        } catch (ObsServiceException sdkE) {
            assertEquals(0, client.getCounterDownload().get());
            assertEquals(2, client.getCounterUpload().get());
            assertEquals(0, client.getCounterGetShutdownTm().get());
            assertEquals(0, client.getCounterGetDownloadTm().get());
            assertEquals(0, client.getCounterGetUploadTm().get());
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
        AbstractObsClientIncrementImpl client =
                new AbstractObsClientIncrementImpl();

        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject("key1", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key2", ObsFamily.EDRS_SESSION,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key3", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        client.uploadObjects(objects, true);

        assertEquals(3, client.getCounterUpload().get());
        assertEquals(0, client.getCounterDownload().get());
        assertEquals(1, client.getCounterGetShutdownTm().get());
        assertEquals(0, client.getCounterGetDownloadTm().get());
        assertEquals(1, client.getCounterGetUploadTm().get());
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
        AbstractObsClientIncrementImpl client =
                new AbstractObsClientIncrementImpl();

        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject("key1", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key-sdk", ObsFamily.EDRS_SESSION,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key3", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        try {
            client.uploadObjects(objects, true);
            fail("SdkClientException should be raised");
        } catch (SdkClientException sdkE) {
            assertTrue(client.getCounterUpload().get() > 0);
            assertEquals(0, client.getCounterDownload().get());
            assertEquals(1, client.getCounterGetShutdownTm().get());
            assertTrue(client.getCounterGetUploadTm().get() > 0);
            assertEquals(0, client.getCounterGetDownloadTm().get());
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
        AbstractObsClientIncrementImpl client =
                new AbstractObsClientIncrementImpl();

        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject("key1", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key2", ObsFamily.EDRS_SESSION,
                new File("target-dir")));
        objects.add(new ObsUploadObject("key-aws", ObsFamily.AUXILIARY_FILE,
                new File("target-dir")));
        try {
            client.uploadObjects(objects, true);
            fail("ObsServiceException should be raised");
        } catch (ObsServiceException sdkE) {
            assertTrue(client.getCounterUpload().get() > 0);
            assertEquals(0, client.getCounterDownload().get());
            assertEquals(1, client.getCounterGetShutdownTm().get());
            assertTrue(client.getCounterGetUploadTm().get() > 0);
            assertEquals(0, client.getCounterGetDownloadTm().get());
        }
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