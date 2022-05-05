package esa.s1pdgs.cpoc.datalifecycle.worker.rest;

import java.io.IOException;
import java.util.TimeZone;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.datalifecycle.worker.rest.DataLifecycleServiceDelegator;
import esa.s1pdgs.cpoc.datalifecycle.worker.service.DataLifecycleService;

public class DataLifecycleServiceDelegatorTest {

	@Mock
	private DataLifecycleService service;
	
	private DataLifecycleServiceDelegator delegator;


	@Before
	public void init() throws IOException {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		MockitoAnnotations.initMocks(this);

		this.delegator = new DataLifecycleServiceDelegator(service);
	}

//	private DataLifecycleMetadata newDataLifecycleMetadata() throws Exception {
//		final DataLifecycleMetadata dataLifecycleMetadataToReturn = new DataLifecycleMetadata();
//		dataLifecycleMetadataToReturn.setAvailableInLta(true);
//		dataLifecycleMetadataToReturn.setEvictionDateInCompressedStorage(DateUtils.parse("2019-06-18T11:09:03.805Z"));
//		dataLifecycleMetadataToReturn.setEvictionDateInUncompressedStorage(DateUtils.parse("2019-06-18T11:09:03.805Z"));
//		dataLifecycleMetadataToReturn.setLastModified(DateUtils.parse("2019-06-18T11:09:03.805Z"));
//		dataLifecycleMetadataToReturn.setPathInCompressedStorage("dummyPath");
//		dataLifecycleMetadataToReturn.setPathInUncompressedStorage("dummyPath");
//		dataLifecycleMetadataToReturn.setPersistentInCompressedStorage(false);
//		dataLifecycleMetadataToReturn.setPersistentInUncompressedStorage(true);
//		dataLifecycleMetadataToReturn.setProductFamilyInUncompressedStorage(ProductFamily.L0_ACN);
//		dataLifecycleMetadataToReturn.setProductFamilyInCompressedStorage(ProductFamily.L0_ACN);
//		dataLifecycleMetadataToReturn.setProductName("dummyProductName");
//		return dataLifecycleMetadataToReturn;
//	}
}
