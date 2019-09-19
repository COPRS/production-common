package esa.s1pdgs.cpoc.obs_sdk.s3;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.ObsConfigurationProperties;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

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
    private ObsConfigurationProperties configuration;

    /**
     * Mock service
     */
    @Mock
    private S3ObsServices service;
    
    /**
     * 
     */
    @Mock
    private ObjectListing objListing1;
    
    /**
     * 
     */
    @Mock
    private ObjectListing objListing2;

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
        doReturn("dummy").when(service).uploadFile(Mockito.anyString(),
                Mockito.anyString(), Mockito.any());

        // Mock configuration
        doReturn("auxiliary-files").when(configuration)
        		.getBucketFor(Mockito.eq(ProductFamily.AUXILIARY_FILE));
        doReturn("edrs-sessions").when(configuration)
                .getBucketFor(Mockito.eq(ProductFamily.EDRS_SESSION));
        doReturn("l0-slices").when(configuration)
                .getBucketFor(Mockito.eq(ProductFamily.L0_SLICE));
        doReturn("l0-acns").when(configuration)
                .getBucketFor(Mockito.eq(ProductFamily.L0_ACN));
        doReturn("l1-slices").when(configuration)
                .getBucketFor(Mockito.eq(ProductFamily.L1_SLICE));
        doReturn("l1-acns").when(configuration)
                .getBucketFor(Mockito.eq(ProductFamily.L1_ACN));
        doReturn("l0-segments").when(configuration)
        		.getBucketFor(Mockito.eq(ProductFamily.L0_SEGMENT));
        doReturn("l0-blanks").when(configuration)
        		.getBucketFor(Mockito.eq(ProductFamily.L0_BLANK));

        // Build client
        client = new S3ObsClient(configuration, service);
    }

    /**
     * Test exist
     * 
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test
    public void testExist() throws ObsServiceException, SdkClientException {
        boolean ret = client.exists(new ObsObject(ProductFamily.L0_ACN, "key-exist"));
        assertTrue(ret);
        verify(service, times(1)).exist(Mockito.eq("l0-acns"), Mockito.eq("key-exist"));

        ret = client.exists( new ObsObject(ProductFamily.L1_SLICE, "key-not-exist"));
        assertFalse(ret);
        verify(service, times(1)).exist(Mockito.eq("l1-slices"), Mockito.eq("key-not-exist"));
    }

    /**
     * Test prefixExist
     * 
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test
    public void testPrefixExist() throws ObsServiceException, SdkClientException {
        boolean ret = client.prefixExists(new ObsObject(ProductFamily.L0_SLICE, "key-exist"));
        assertTrue(ret);
        verify(service, times(1)).getNbObjects(Mockito.eq("l0-slices"),
                Mockito.eq("key-exist"));

        ret = client.prefixExists(new ObsObject(ProductFamily.L1_SLICE, "key-not-exist"));
        assertFalse(ret);
        verify(service, times(1)).getNbObjects(Mockito.eq("l1-slices"),Mockito.eq("key-not-exist"));
    }

    /**
     * Test downloadObject
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test
    public void testDownloadObject() throws ObsServiceException, SdkClientException {
        client.downloadObject(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key-exist", "target-dir"));
        verify(service, times(1)).downloadObjectsWithPrefix(Mockito.eq("auxiliary-files"),
                Mockito.eq("key-exist"), Mockito.eq("target-dir"), Mockito.eq(false));

        client.downloadObject(new ObsDownloadObject(ProductFamily.EDRS_SESSION, "key-not-exist", "target-dir"));
        verify(service, times(1)).downloadObjectsWithPrefix(Mockito.eq("edrs-sessions"),
                Mockito.eq("key-not-exist"), Mockito.eq("target-dir"), Mockito.eq(true));
    }
    
    /**
     * Test uploadObject when directory
     * @throws ObsServiceException
     * @throws SdkClientException
     * @throws ObsException 
     */
    @Test
    public void testUploadObjectDirectory() throws ObsServiceException, SdkClientException, ObsException {
        client.uploadObject(new ObsUploadObject(ProductFamily.L0_ACN, "key-exist", new File("target")));
        verify(service, times(1)).uploadDirectory(Mockito.eq("l0-acns"),
                Mockito.eq("key-exist"), Mockito.eq(new File("target")));
        verify(service, times(1)).uploadFile(Mockito.anyString(),
                Mockito.anyString(), Mockito.any()); // for the 1st md5sum
        
        client.uploadObject(new ObsUploadObject(ProductFamily.L0_ACN, "key-not-exist", new File("target")));
        verify(service, times(1)).uploadDirectory(Mockito.eq("l0-acns"),
                Mockito.eq("key-not-exist"), Mockito.eq(new File("target")));
        verify(service, times(2)).uploadFile(Mockito.anyString(),
                Mockito.anyString(), Mockito.any()); // for the 2nd md5sum
    }
    
    /**
     * Test uploadObject when file
     * @throws ObsServiceException
     * @throws SdkClientException
     * @throws ObsException 
     */
    @Test
    public void testUploadObjectFile() throws ObsServiceException, SdkClientException, ObsException {
        client.uploadObject(new ObsUploadObject(ProductFamily.L0_ACN, "key-exist", new File("pom.xml")));
        verify(service, times(1))
        	.uploadFile(Mockito.eq("l0-acns"), Mockito.eq("key-exist"), Mockito.eq(new File("pom.xml")));
        verify(service, never()).uploadDirectory(Mockito.anyString(),
                Mockito.anyString(), Mockito.any());
    }
    
	@Test
	public void testGetListOfObjectsOfTimeFrameOfFamilyOneExists() throws ObsServiceException, SdkClientException {

		Date timeFrameBegin = Date.from(Instant.parse("2020-01-01T00:00:00Z"));
		Date timeFrameEnd = Date.from(Instant.parse("2020-01-03T00:00:00Z"));
		Date obj1Date = Date.from(Instant.parse("2020-01-02T00:00:00Z"));

		S3ObjectSummary obj1 = new S3ObjectSummary();
		obj1.setKey("obj1");
		obj1.setLastModified(obj1Date);

		List<S3ObjectSummary> objSums = new ArrayList<>();
		objSums.add(obj1);

		doReturn(objSums).when(objListing1).getObjectSummaries();
		doReturn(false).when(objListing1).isTruncated();
		doReturn(objListing1).when(service).listObjectsFromBucket("l0-slices");

		List<ObsObject> returnedObjs = client.getObsObjectsOfFamilyWithinTimeFrame(ProductFamily.L0_SLICE, timeFrameBegin, timeFrameEnd);

		assertEquals(1, returnedObjs.size());
		assertEquals("obj1", returnedObjs.get(0).getKey());
		verify(service, times(1)).listObjectsFromBucket("l0-slices");
		verify(service, never()).listNextBatchOfObjectsFromBucket(Mockito.anyString(), Mockito.any());
	}

	@Test
	public void testGetListOfObjectsOfTimeFrameOfFamilyNoneExists() throws ObsServiceException, SdkClientException {

		Date timeFrameBegin = Date.from(Instant.parse("2020-01-01T00:00:00Z"));
		Date timeFrameEnd = Date.from(Instant.parse("2020-01-03T00:00:00Z"));
		Date obj1Date = Date.from(Instant.parse("2020-01-04T00:00:00Z"));

		S3ObjectSummary obj1 = new S3ObjectSummary();
		obj1.setKey("obj1");
		obj1.setLastModified(obj1Date);

		List<S3ObjectSummary> objSums = new ArrayList<>();
		objSums.add(obj1);

		doReturn(objSums).when(objListing1).getObjectSummaries();
		doReturn(false).when(objListing1).isTruncated();
		doReturn(objListing1).when(service).listObjectsFromBucket("l0-slices");

		List<ObsObject> returnedObjs = client.getObsObjectsOfFamilyWithinTimeFrame(ProductFamily.L0_SLICE, timeFrameBegin, timeFrameEnd);

		assertEquals(0, returnedObjs.size());
		verify(service, times(1)).listObjectsFromBucket("l0-slices");
		verify(service, never()).listNextBatchOfObjectsFromBucket(Mockito.anyString(), Mockito.any());
	}

	@Test
	public void testGetListOfObjectsOfTimeFrameOfFamilyWithTruncatedList()
			throws ObsServiceException, SdkClientException {

		Date timeFrameBegin = Date.from(Instant.parse("2020-01-01T00:00:00Z"));
		Date timeFrameEnd = Date.from(Instant.parse("2020-01-03T00:00:00Z"));

		Date obj1Date = Date.from(Instant.parse("2020-01-02T00:00:00Z"));
		Date obj2Date = Date.from(Instant.parse("2020-01-04T00:00:00Z"));
		Date obj3Date = Date.from(Instant.parse("2020-01-02T03:00:00Z"));

		S3ObjectSummary obj1 = new S3ObjectSummary();
		obj1.setKey("obj1");
		obj1.setLastModified(obj1Date);

		S3ObjectSummary obj2 = new S3ObjectSummary();
		obj2.setKey("obj2");
		obj2.setLastModified(obj2Date);

		S3ObjectSummary obj3 = new S3ObjectSummary();
		obj3.setKey("obj3");
		obj3.setLastModified(obj3Date);

		List<S3ObjectSummary> objSums1 = new ArrayList<>();
		objSums1.add(obj1);
		objSums1.add(obj2);

		List<S3ObjectSummary> objSums2 = new ArrayList<>();
		objSums2.add(obj3);

		doReturn(objSums1).when(objListing1).getObjectSummaries();
		doReturn(true).when(objListing1).isTruncated();
		doReturn(objListing1).when(service).listObjectsFromBucket("l0-slices");

		doReturn(objSums2).when(objListing2).getObjectSummaries();
		doReturn(false).when(objListing2).isTruncated();
		doReturn(objListing2).when(service).listNextBatchOfObjectsFromBucket("l0-slices", objListing1);

		List<ObsObject> returnedObjs = client.getObsObjectsOfFamilyWithinTimeFrame(ProductFamily.L0_SLICE, timeFrameBegin, timeFrameEnd);

		assertEquals(2, returnedObjs.size());
		assertEquals("obj1", returnedObjs.get(0).getKey());
		assertEquals("obj3", returnedObjs.get(1).getKey());
		verify(service, times(1)).listObjectsFromBucket(Mockito.anyString());
		verify(service, times(1)).listNextBatchOfObjectsFromBucket(Mockito.anyString(), Mockito.any());
	}
	
	@Test
    public void testExistsValidArgumentAssertion() throws AbstractCodedException {
    	assertThatThrownBy(() -> client.exists(null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid object: null");
    	assertThatThrownBy(() -> client.exists(new ObsObject(null, "key"))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
    	assertThatThrownBy(() -> client.exists(new ObsObject(ProductFamily.AUXILIARY_FILE, null))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key: null");
    	assertThatThrownBy(() -> client.exists(new ObsObject(ProductFamily.AUXILIARY_FILE, ""))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key (empty)");
    }
	
	@Test
    public void testPrefixExistsValidArgumentAssertion() throws AbstractCodedException {
    	assertThatThrownBy(() -> client.prefixExists(null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid object: null");
    	assertThatThrownBy(() -> client.prefixExists(new ObsObject(null, "key"))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
    	assertThatThrownBy(() -> client.prefixExists(new ObsObject(ProductFamily.AUXILIARY_FILE, null))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key: null");
    	assertThatThrownBy(() -> client.prefixExists(new ObsObject(ProductFamily.AUXILIARY_FILE, ""))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key (empty)");
    }

	@Test
    public void testGetObsObjectsOfFamilyWithinTimeFrameValidArgumentAssertion() throws AbstractCodedException {
    	assertThatThrownBy(() -> client.getObsObjectsOfFamilyWithinTimeFrame(null, new Date(), new Date())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
    	assertThatThrownBy(() -> client.getObsObjectsOfFamilyWithinTimeFrame(ProductFamily.AUXILIARY_FILE, null, new Date())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid date: null");
    	assertThatThrownBy(() -> client.getObsObjectsOfFamilyWithinTimeFrame(ProductFamily.AUXILIARY_FILE, new Date(), null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid date: null");
    }
	
	@Test
    public void testMoveValidArgumentAssertion() throws AbstractCodedException {
    	assertThatThrownBy(() -> client.move(null, ProductFamily.AUXILIARY_FILE)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid object: null");
    	assertThatThrownBy(() -> client.move(new ObsObject(null, "key"), ProductFamily.AUXILIARY_FILE)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
    	assertThatThrownBy(() -> client.move(new ObsObject(ProductFamily.AUXILIARY_FILE, null), ProductFamily.AUXILIARY_FILE)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key: null");
    	assertThatThrownBy(() -> client.move(new ObsObject(ProductFamily.AUXILIARY_FILE, ""), ProductFamily.AUXILIARY_FILE)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key (empty)");
    	assertThatThrownBy(() -> client.move(new ObsObject(ProductFamily.AUXILIARY_FILE, "key"), null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
	}
	
	@Test
	public void testGetAllAsInputStreamValidArgumentAssertion() throws AbstractCodedException {
    	assertThatThrownBy(() -> client.getAllAsInputStream(null, "prefix")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
    	assertThatThrownBy(() -> client.getAllAsInputStream(ProductFamily.AUXILIARY_FILE, null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid prefix: null");
    	assertThatThrownBy(() -> client.getAllAsInputStream(ProductFamily.AUXILIARY_FILE, "")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid prefix (empty)");
	}
	
}
