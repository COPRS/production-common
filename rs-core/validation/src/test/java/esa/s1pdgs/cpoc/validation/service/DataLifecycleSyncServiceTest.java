package esa.s1pdgs.cpoc.validation.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.datalifecycle.client.DataLifecycleClientUtil;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.validation.config.ApplicationProperties;
import esa.s1pdgs.cpoc.validation.config.DataLifecycleSyncConfig;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@EnableConfigurationProperties
public class DataLifecycleSyncServiceTest {
	
	@Mock
	private ObsClient obsClient;

	@Mock
	private DataLifecycleMetadataRepository lifecycleMetadataRepo;
	
	private DataLifecycleSyncService syncService;
	
	@Autowired
	private ApplicationProperties appProperties;

	@Autowired
	private DataLifecycleSyncConfig lifecycleSyncConfig;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		syncService = new DataLifecycleSyncService(obsClient, lifecycleMetadataRepo, appProperties, lifecycleSyncConfig);
	}
	
	@Test
	public void syncOBSwithDataLifecycleIndex_NoUpdate() throws SdkClientException, DataLifecycleMetadataRepositoryException {
		
		ObsObject o1 = new ObsObject(ProductFamily.L0_SLICE, "l0slice1");
		ObsObject o2 = new ObsObject(ProductFamily.L0_SLICE, "l0slice2");
		
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put(o1.getKey(), o1);
		obsResults.put(o2.getKey(), o2);
		
		DataLifecycleMetadata m1 = new DataLifecycleMetadata();
		m1.setProductName(DataLifecycleClientUtil.getProductName(o1.getKey()));
		m1.setProductFamilyInUncompressedStorage(ProductFamily.L0_SLICE);
		m1.setPathInUncompressedStorage(o1.getKey());
		
		DataLifecycleMetadata m2 = new DataLifecycleMetadata();
		m2.setProductName(DataLifecycleClientUtil.getProductName(o2.getKey()));
		m2.setProductFamilyInUncompressedStorage(ProductFamily.L0_SLICE);
		m2.setPathInUncompressedStorage(o2.getKey());
		
		doReturn(obsResults).when(obsClient).listInterval(eq(ProductFamily.L0_SLICE), any(Date.class), any(Date.class));
		doReturn(Optional.of(m1)).when(lifecycleMetadataRepo).findByProductName(DataLifecycleClientUtil.getProductName(o1.getKey()));
		doReturn(Optional.of(m2)).when(lifecycleMetadataRepo).findByProductName(DataLifecycleClientUtil.getProductName(o2.getKey()));
		
		DataLifecycleSyncStats stats = syncService.syncDataLifecycleIndexFromOBS(new Date(), new Date());
		
		assertEquals(0, stats.getErrors());
		assertEquals(2, stats.getUnchanged());
		assertEquals(0, stats.getFamilyUpdated());
		assertEquals(0, stats.getPathUpdated());
		assertEquals(0, stats.getNewCreated());
	}
	
	@Test
	public void syncOBSwithDataLifecycleIndex_SyncFamily() throws SdkClientException, DataLifecycleMetadataRepositoryException {
		
		ObsObject o1 = new ObsObject(ProductFamily.L0_SLICE, "l0slice1");
		ObsObject o2 = new ObsObject(ProductFamily.L0_SLICE, "l0slice2");
		
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put(o1.getKey(), o1);
		obsResults.put(o2.getKey(), o2);
		
		DataLifecycleMetadata m1 = new DataLifecycleMetadata();
		m1.setProductName(DataLifecycleClientUtil.getProductName(o1.getKey()));
		m1.setProductFamilyInUncompressedStorage(ProductFamily.L0_SLICE);
		m1.setPathInUncompressedStorage(o1.getKey());
		
		DataLifecycleMetadata m2 = new DataLifecycleMetadata();
		m2.setProductName(DataLifecycleClientUtil.getProductName(o2.getKey()));
		m2.setProductFamilyInUncompressedStorage(null);
		m2.setPathInUncompressedStorage(o2.getKey());
		
		doReturn(obsResults).when(obsClient).listInterval(eq(ProductFamily.L0_SLICE), any(Date.class), any(Date.class));
		doReturn(Optional.of(m1)).when(lifecycleMetadataRepo).findByProductName(DataLifecycleClientUtil.getProductName(o1.getKey()));
		doReturn(Optional.of(m2)).when(lifecycleMetadataRepo).findByProductName(DataLifecycleClientUtil.getProductName(o2.getKey()));
		
		DataLifecycleSyncStats stats = syncService.syncDataLifecycleIndexFromOBS(new Date(), new Date());
		
		assertEquals(0, stats.getErrors());
		assertEquals(1, stats.getUnchanged());
		assertEquals(1, stats.getFamilyUpdated());
		assertEquals(0, stats.getPathUpdated());
		assertEquals(0, stats.getNewCreated());
	}
	
	@Test
	public void syncOBSwithDataLifecycleIndex_SyncPath() throws SdkClientException, DataLifecycleMetadataRepositoryException {
		
		ObsObject o1 = new ObsObject(ProductFamily.L0_SLICE, "l0slice1");
		ObsObject o2 = new ObsObject(ProductFamily.L0_SLICE, "l0slice2");
		
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put(o1.getKey(), o1);
		obsResults.put(o2.getKey(), o2);
		
		DataLifecycleMetadata m1 = new DataLifecycleMetadata();
		m1.setProductName(DataLifecycleClientUtil.getProductName(o1.getKey()));
		m1.setProductFamilyInUncompressedStorage(ProductFamily.L0_SLICE);
		m1.setPathInUncompressedStorage(null);
		
		DataLifecycleMetadata m2 = new DataLifecycleMetadata();
		m2.setProductName(DataLifecycleClientUtil.getProductName(o2.getKey()));
		m2.setProductFamilyInUncompressedStorage(ProductFamily.L0_SLICE);
		m2.setPathInUncompressedStorage(o2.getKey());
		
		doReturn(obsResults).when(obsClient).listInterval(eq(ProductFamily.L0_SLICE), any(Date.class), any(Date.class));
		doReturn(Optional.of(m1)).when(lifecycleMetadataRepo).findByProductName(DataLifecycleClientUtil.getProductName(o1.getKey()));
		doReturn(Optional.of(m2)).when(lifecycleMetadataRepo).findByProductName(DataLifecycleClientUtil.getProductName(o2.getKey()));
		
		DataLifecycleSyncStats stats = syncService.syncDataLifecycleIndexFromOBS(new Date(), new Date());
		
		assertEquals(0, stats.getErrors());
		assertEquals(1, stats.getUnchanged());
		assertEquals(0, stats.getFamilyUpdated());
		assertEquals(1, stats.getPathUpdated());
		assertEquals(0, stats.getNewCreated());
	}
	
	
	@Test
	public void syncOBSwithDataLifecycleIndex_CreateNew() throws SdkClientException, DataLifecycleMetadataRepositoryException {
		
		ObsObject o1 = new ObsObject(ProductFamily.L0_SLICE, "l0slice1");
		ObsObject o2 = new ObsObject(ProductFamily.L0_SLICE, "l0slice2");
		
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put(o1.getKey(), o1);
		obsResults.put(o2.getKey(), o2);
		
		DataLifecycleMetadata m1 = new DataLifecycleMetadata();
		m1.setProductName(DataLifecycleClientUtil.getProductName(o1.getKey()));
		m1.setProductFamilyInUncompressedStorage(ProductFamily.L0_SLICE);
		m1.setPathInUncompressedStorage(o1.getKey());
		
		doReturn(obsResults).when(obsClient).listInterval(eq(ProductFamily.L0_SLICE), any(Date.class), any(Date.class));
		doReturn(Optional.of(m1)).when(lifecycleMetadataRepo).findByProductName(DataLifecycleClientUtil.getProductName(o1.getKey()));
		doReturn(Optional.empty()).when(lifecycleMetadataRepo).findByProductName(DataLifecycleClientUtil.getProductName(o2.getKey()));
		
		DataLifecycleSyncStats stats = syncService.syncDataLifecycleIndexFromOBS(new Date(), new Date());
		
		assertEquals(0, stats.getErrors());
		assertEquals(1, stats.getUnchanged());
		assertEquals(0, stats.getFamilyUpdated());
		assertEquals(0, stats.getPathUpdated());
		assertEquals(1, stats.getNewCreated());
	}
	
	@Test
	public void syncOBSwithDataLifecycleIndex_Combined() throws SdkClientException, DataLifecycleMetadataRepositoryException {
		
		ObsObject o1 = new ObsObject(ProductFamily.L0_SLICE, "l0slice1");
		ObsObject o2 = new ObsObject(ProductFamily.L0_SLICE, "l0slice2");
		
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put(o1.getKey(), o1);
		obsResults.put(o2.getKey(), o2);
		
		DataLifecycleMetadata m1 = new DataLifecycleMetadata();
		m1.setProductName(DataLifecycleClientUtil.getProductName(o1.getKey()));
		m1.setProductFamilyInUncompressedStorage(null);
		m1.setPathInUncompressedStorage(null);
		
		doReturn(obsResults).when(obsClient).listInterval(eq(ProductFamily.L0_SLICE), any(Date.class), any(Date.class));
		doReturn(Optional.of(m1)).when(lifecycleMetadataRepo).findByProductName(DataLifecycleClientUtil.getProductName(o1.getKey()));
		doReturn(Optional.empty()).when(lifecycleMetadataRepo).findByProductName(DataLifecycleClientUtil.getProductName(o2.getKey()));
		
		DataLifecycleSyncStats stats = syncService.syncDataLifecycleIndexFromOBS(new Date(), new Date());
		
		assertEquals(0, stats.getErrors());
		assertEquals(0, stats.getUnchanged());
		assertEquals(1, stats.getFamilyUpdated());
		assertEquals(1, stats.getPathUpdated());
		assertEquals(1, stats.getNewCreated());
	}
	
	@Test
	public void syncOBSwithDataLifecycleIndex_SdkClientException() throws SdkClientException, DataLifecycleMetadataRepositoryException {
		
		ObsObject o1 = new ObsObject(ProductFamily.L0_SLICE, "l0slice1");
		ObsObject o2 = new ObsObject(ProductFamily.L0_SLICE, "l0slice2");
		
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put(o1.getKey(), o1);
		obsResults.put(o2.getKey(), o2);
		
		DataLifecycleMetadata m1 = new DataLifecycleMetadata();
		m1.setProductName(DataLifecycleClientUtil.getProductName(o1.getKey()));
		m1.setProductFamilyInUncompressedStorage(ProductFamily.L0_SLICE);
		m1.setPathInUncompressedStorage(o1.getKey());
		
		DataLifecycleMetadata m2 = new DataLifecycleMetadata();
		m2.setProductName(DataLifecycleClientUtil.getProductName(o2.getKey()));
		m2.setProductFamilyInUncompressedStorage(ProductFamily.L0_SLICE);
		m2.setPathInUncompressedStorage(o2.getKey());
		
		doThrow(new SdkClientException("")).when(obsClient).listInterval(eq(ProductFamily.L0_SLICE), any(Date.class), any(Date.class));
		doReturn(Optional.of(m1)).when(lifecycleMetadataRepo).findByProductName(DataLifecycleClientUtil.getProductName(o1.getKey()));
		doReturn(Optional.of(m2)).when(lifecycleMetadataRepo).findByProductName(DataLifecycleClientUtil.getProductName(o2.getKey()));
		
		DataLifecycleSyncStats stats = syncService.syncDataLifecycleIndexFromOBS(new Date(), new Date());
		
		assertEquals(1, stats.getErrors());
		assertEquals(0, stats.getUnchanged());
		assertEquals(0, stats.getFamilyUpdated());
		assertEquals(0, stats.getPathUpdated());
		assertEquals(0, stats.getNewCreated());
	}
	
	
	@Test
	public void syncOBSwithDataLifecycleIndex_DataLifecycleMetadataRepositoryException() throws SdkClientException, DataLifecycleMetadataRepositoryException {
		
		ObsObject o1 = new ObsObject(ProductFamily.L0_SLICE, "l0slice1");
		ObsObject o2 = new ObsObject(ProductFamily.L0_SLICE, "l0slice2");
		
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put(o1.getKey(), o1);
		obsResults.put(o2.getKey(), o2);
		
		DataLifecycleMetadata m1 = new DataLifecycleMetadata();
		m1.setProductName(DataLifecycleClientUtil.getProductName(o1.getKey()));
		m1.setProductFamilyInUncompressedStorage(ProductFamily.L0_SLICE);
		m1.setPathInUncompressedStorage(o1.getKey());
		
		DataLifecycleMetadata m2 = new DataLifecycleMetadata();
		m2.setProductName(DataLifecycleClientUtil.getProductName(o2.getKey()));
		m2.setProductFamilyInUncompressedStorage(ProductFamily.L0_SLICE);
		m2.setPathInUncompressedStorage(o2.getKey());
		
		doReturn(obsResults).when(obsClient).listInterval(eq(ProductFamily.L0_SLICE), any(Date.class), any(Date.class));
		doThrow(new DataLifecycleMetadataRepositoryException("")).when(lifecycleMetadataRepo).findByProductName(DataLifecycleClientUtil.getProductName(o1.getKey()));
		doThrow(new DataLifecycleMetadataRepositoryException("")).when(lifecycleMetadataRepo).findByProductName(DataLifecycleClientUtil.getProductName(o2.getKey()));
		
		DataLifecycleSyncStats stats = syncService.syncDataLifecycleIndexFromOBS(new Date(), new Date());
		
		assertEquals(2, stats.getErrors());
		assertEquals(0, stats.getUnchanged());
		assertEquals(0, stats.getFamilyUpdated());
		assertEquals(0, stats.getPathUpdated());
		assertEquals(0, stats.getNewCreated());
	}
	


}
