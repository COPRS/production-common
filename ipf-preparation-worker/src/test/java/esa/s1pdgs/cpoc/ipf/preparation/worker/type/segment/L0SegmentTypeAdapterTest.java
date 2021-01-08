package esa.s1pdgs.cpoc.ipf.preparation.worker.type.segment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.AspProperties;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;

public class L0SegmentTypeAdapterTest {
	
	public final static int WAITING_TIME_HOURS_MINIMAL_FAST = 3;
	public final static int WAITING_TIME_HOURS_NOMINAL_FAST = 20;
	public final static int WAITING_TIME_HOURS_MINIMAL_NRT_PT = 1;
	public final static int WAITING_TIME_HOURS_NOMINAL_NRT_PT = 2;
	
	@Mock
	private MetadataClient metadataClient;
	
	private L0SegmentTypeAdapter uut;
	
	@Before
    public void init() {
		MockitoAnnotations.initMocks(this);
		
		uut = new L0SegmentTypeAdapter(metadataClient, AspPropertiesAdapter.of(createAspProperties(true)));
	}
	
	@Test
	public void testNonRFProductionWithOlderAndNewerFullSegments() throws MetadataQueryException, IpfPrepWorkerInputsMissingException {
		
		Instant insertionTime = Instant.now();
		
		String metadataInsertionTime1 = DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.ofInstant(insertionTime, ZoneId.of("UTC")));
		
		LevelSegmentMetadata metadata1 = new LevelSegmentMetadata(
				"S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_17E4",
				"IW_RAW__0S",
				"S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_17E4",
				"2021-01-04T09:17:09.187813Z",
				"2021-01-04T09:18:33.511574Z",
				"S1",
				"B",
				null,
				"VV",
				"FULL",
				"",
				"02F9CE");
		metadata1.setInsertionTime(metadataInsertionTime1);
		
		doReturn(Arrays.asList(metadata1)).when(metadataClient).getLevelSegments("02F9CE");
		
		AppDataJob appDataJob1 = new AppDataJob(123L);
		appDataJob1.setCreationDate(Date.from(insertionTime.plusSeconds(3)));
		AppDataJobProduct product1 = new AppDataJobProduct();
		product1.getMetadata().put("startTime", "2021-01-04T09:17:09.187813Z");
		product1.getMetadata().put("productName", "S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_17E4");
		product1.getMetadata().put("productType", "IW_RAW__0S");
		product1.getMetadata().put("dataTakeId","02F9CE");
		appDataJob1.setProduct(product1);
		
		Product product = uut.mainInputSearch(appDataJob1, null);
		
		uut.validateInputSearch(appDataJob1, null);
		
	}
	
	private AspProperties createAspProperties(boolean disableTimeout) {
		final AspProperties aspProperties = new AspProperties();

		aspProperties.setDisableTimeout(disableTimeout);
		aspProperties.setWaitingTimeHoursMinimalFast(WAITING_TIME_HOURS_MINIMAL_FAST);
		aspProperties.setWaitingTimeHoursNominalFast(WAITING_TIME_HOURS_NOMINAL_FAST);
		aspProperties.setWaitingTimeHoursMinimalNrtPt(WAITING_TIME_HOURS_MINIMAL_NRT_PT);
		aspProperties.setWaitingTimeHoursNominalNrtPt(WAITING_TIME_HOURS_NOMINAL_NRT_PT);

		return aspProperties;
	}


}
