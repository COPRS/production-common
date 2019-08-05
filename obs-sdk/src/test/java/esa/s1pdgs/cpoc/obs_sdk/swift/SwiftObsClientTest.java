package esa.s1pdgs.cpoc.obs_sdk.swift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.javaswift.joss.client.core.AbstractStoredObject;
import org.javaswift.joss.client.mock.AccountMock;
import org.javaswift.joss.client.mock.ContainerMock;
import org.javaswift.joss.instructions.UploadInstructions;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.javaswift.joss.swift.Swift;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

public class SwiftObsClientTest {

    /**
     * Mock configuration
     */
    @Mock
    private SwiftConfiguration configuration;

    /**
     * Mock service
     */
    @Mock
    private SwiftObsServices service;
    
    /**
     * Mock account
     */
    private AccountMock account;
    
    /**
     * Client to test
     */
    private SwiftObsClient client;

    /**
     * Initialization
     * 
     * @throws ObsServiceException
     * @throws SwiftSdkClientException
     * @throws SwiftSdkClientException 
     */
    @Before
    public void init() throws ObsServiceException, SwiftSdkClientException, SwiftSdkClientException {
    	account = new AccountMock();
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
                .getContainerForFamily(Mockito.eq(ProductFamily.AUXILIARY_FILE));
        doReturn("edrs-sessions").when(configuration)
                .getContainerForFamily(Mockito.eq(ProductFamily.EDRS_SESSION));
        doReturn("l0-slices").when(configuration)
                .getContainerForFamily(Mockito.eq(ProductFamily.L0_SLICE));
        doReturn("l0-acns").when(configuration)
                .getContainerForFamily(Mockito.eq(ProductFamily.L0_ACN));
        doReturn("l1-slices").when(configuration)
                .getContainerForFamily(Mockito.eq(ProductFamily.L1_SLICE));
        doReturn("l1-acns").when(configuration)
                .getContainerForFamily(Mockito.eq(ProductFamily.L1_ACN));
        doReturn("l0-segments").when(configuration)
        .getContainerForFamily(Mockito.eq(ProductFamily.L0_SEGMENT));
        doReturn("l0-blanks").when(configuration)
        .getContainerForFamily(Mockito.eq(ProductFamily.L0_BLANK));
        doReturn(22).when(configuration).getIntOfConfiguration(
                Mockito.eq(SwiftConfiguration.TM_S_SHUTDOWN));
        doReturn(8).when(configuration).getIntOfConfiguration(
                Mockito.eq(SwiftConfiguration.TM_S_DOWN_EXEC));
        doReturn(109).when(configuration).getIntOfConfiguration(
                Mockito.eq(SwiftConfiguration.TM_S_UP_EXEC));

        // Build client
        client = new SwiftObsClient(configuration, service);
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
                .doesObjectExist(new ObsObject("key-exist", ProductFamily.L0_ACN));
        assertTrue(ret);
        verify(service, times(1)).exist(Mockito.eq("l0-acns"), Mockito.eq("key-exist"));

        ret = client.doesObjectExist(
                new ObsObject("key-not-exist", ProductFamily.L1_SLICE));
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
                new ObsObject("key-exist", ProductFamily.L0_SLICE));
        assertTrue(ret);
        verify(service, times(1)).getNbObjects(Mockito.eq("l0-slices"),
                Mockito.eq("key-exist"));

        ret = client.doesPrefixExist(
                new ObsObject("key-not-exist", ProductFamily.L1_SLICE));
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
                ProductFamily.AUXILIARY_FILE, "target-dir"));
        assertEquals(2, ret);
        verify(service, times(1)).downloadObjectsWithPrefix(Mockito.eq("auxiliary-files"),
                Mockito.eq("key-exist"), Mockito.eq("target-dir"), Mockito.eq(false));

        ret = client.downloadObject(new ObsDownloadObject("key-not-exist",
                ProductFamily.EDRS_SESSION, "target-dir"));
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
        int ret = client.uploadObject(new ObsUploadObject("key-exist", ProductFamily.L0_ACN, new File("target")));
        assertEquals(2, ret);
        verify(service, times(1)).uploadDirectory(Mockito.eq("l0-acns"),
                Mockito.eq("key-exist"), Mockito.eq(new File("target")));
        verify(service, never()).uploadFile(Mockito.anyString(),
                Mockito.anyString(), Mockito.any());
        
        ret = client.uploadObject(new ObsUploadObject("key-not-exist", ProductFamily.L0_ACN, new File("target")));
        assertEquals(0, ret);
        verify(service, times(1)).uploadDirectory(Mockito.eq("l0-acns"),
                Mockito.eq("key-not-exist"), Mockito.eq(new File("target")));
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
        int ret = client.uploadObject(new ObsUploadObject("key-exist", ProductFamily.L0_ACN, new File("pom.xml")));
        assertEquals(1, ret);
        verify(service, times(1)).uploadFile(Mockito.eq("l0-acns"),
                Mockito.eq("key-exist"), Mockito.eq(new File("pom.xml")));
        verify(service, never()).uploadDirectory(Mockito.anyString(),
                Mockito.anyString(), Mockito.any());
    }
    
    @Test
	public void testGetListOfObjectsOfTimeFrameOfFamilyOneExists() throws ObsServiceException, SdkClientException {

		Date timeFrameBegin = Date.from(Instant.parse("2020-01-01T00:00:00Z"));
		Date timeFrameEnd = Date.from(Instant.parse("2020-01-03T00:00:00Z"));
		Date obj1Date = Date.from(Instant.parse("2020-01-02T00:00:00Z"));

		Container container = new ContainerMock(account, "l0-slices");
		StoredObject obj1 = new StoredObjectCustomMock(container, "obj1");
		obj1.setLastModified(obj1Date);

		List<StoredObject> objListing1 = new LinkedList<>();
		objListing1.add(obj1);

		doReturn(objListing1).when(service).listObjectsFromContainer("l0-slices");

		List<ObsObject> returnedObjs = client.getListOfObjectsOfTimeFrameOfFamily(timeFrameBegin, timeFrameEnd,
				ProductFamily.L0_SLICE);

		assertEquals(1, returnedObjs.size());
		assertEquals("obj1", returnedObjs.get(0).getKey());
		verify(service, times(1)).listObjectsFromContainer("l0-slices");
		verify(service, never()).listNextBatchOfObjectsFromContainer(Mockito.anyString(), Mockito.any());
	}

    @Test
	public void testGetListOfObjectsOfTimeFrameOfFamilyNoneExists() throws ObsServiceException, SdkClientException {

		Date timeFrameBegin = Date.from(Instant.parse("2020-01-01T00:00:00Z"));
		Date timeFrameEnd = Date.from(Instant.parse("2020-01-03T00:00:00Z"));
		Date obj1Date = Date.from(Instant.parse("2020-01-04T00:00:00Z"));

		Swift swift = new Swift();
		AccountMock accountMock = new AccountMock(swift);
		swift.createContainer("l0-slices");
		Container container = accountMock.getContainer("l0-slices");
		StoredObject obj1 = new StoredObjectCustomMock(container, "obj1");
		obj1.setLastModified(obj1Date);
		swift.uploadObject(container, obj1, new UploadInstructions(new byte[0]));

	  	List<StoredObject> objListing1 = new LinkedList<>();
		objListing1.add(obj1);

		doReturn(objListing1).when(service).listObjectsFromContainer("l0-slices");
		List<ObsObject> returnedObjs = client.getListOfObjectsOfTimeFrameOfFamily(timeFrameBegin, timeFrameEnd,
				ProductFamily.L0_SLICE);

		assertEquals(0, returnedObjs.size());
		verify(service, times(1)).listObjectsFromContainer("l0-slices");
		verify(service, never()).listNextBatchOfObjectsFromContainer(Mockito.anyString(), Mockito.any());
	}

    @Test
	public void testGetListOfObjectsOfTimeFrameOfFamilyWithTruncatedList()
			throws ObsServiceException, SdkClientException {

		Date timeFrameBegin = Date.from(Instant.parse("2020-01-01T00:00:00Z"));
		Date timeFrameEnd = Date.from(Instant.parse("2020-01-03T00:00:00Z"));

		Date obj1Date = Date.from(Instant.parse("2020-01-02T00:00:00Z"));
		Date obj2Date = Date.from(Instant.parse("2020-01-04T00:00:00Z"));
		Date obj3Date = Date.from(Instant.parse("2020-01-02T03:00:00Z"));
		
		Container container = new ContainerMock(account, "l0-slices");
		StoredObject obj1 = new StoredObjectCustomMock(container, "obj1");
		obj1.setLastModified(obj1Date);
		StoredObject obj2 = new StoredObjectCustomMock(container, "obj2");
		obj2.setLastModified(obj2Date);
		StoredObject obj3 = new StoredObjectCustomMock(container, "obj3");
		obj3.setLastModified(obj3Date);

		List<StoredObject> objListing1 = new ArrayList<>();
		objListing1.add(obj1);
		objListing1.add(obj2);
		objListing1.add(obj3);

		doReturn(objListing1).when(service).listObjectsFromContainer("l0-slices");

		List<ObsObject> returnedObjs = client.getListOfObjectsOfTimeFrameOfFamily(timeFrameBegin, timeFrameEnd,
				ProductFamily.L0_SLICE);

		assertEquals(2, returnedObjs.size());
//		assertEquals("obj1", returnedObjs.get(0).getKey());
//		assertEquals("obj3", returnedObjs.get(1).getKey());
//		verify(service, times(1)).listObjectsFromContainer(Mockito.anyString());
//		verify(service, times(1)).listNextBatchOfObjectsFromContainer(Mockito.anyString(), Mockito.any());
	}
}

class StoredObjectCustomMock extends AbstractStoredObject {

	Date lastModified;
	
	public StoredObjectCustomMock(Container container, String name) {
		super(container, name, false);
	}
	
	@Override
	public void setLastModified(Date date) {
		lastModified = date;
	}
	
	@Override
	public Date getLastModifiedAsDate() {
		return lastModified;
	}
	
}
