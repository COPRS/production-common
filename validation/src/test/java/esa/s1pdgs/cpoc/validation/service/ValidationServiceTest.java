package esa.s1pdgs.cpoc.validation.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.validation.service.metadata.MetadataService;

public class ValidationServiceTest {

	@Mock
	private MetadataService metadataService;

	@Mock
	private ObsClient obsClient;

	private ValidationService validationService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		validationService = new ValidationService(metadataService, obsClient);
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenConsistent()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;

		String intervalStart = "2000-01-01T00:00:00.000000Z";
		String intervalStop = "2000-01-03T00:00:00.000000Z";

		LocalDateTime localDateTimeStart = LocalDateTime.parse(intervalStart, DateUtils.METADATA_DATE_FORMATTER);
		LocalDateTime localDateTimeStop = LocalDateTime.parse(intervalStop, DateUtils.METADATA_DATE_FORMATTER);

		Date startDate = Date.from(localDateTimeStart.atZone(ZoneId.of("UTC")).toInstant());
		Date stopDate = Date.from(localDateTimeStop.atZone(ZoneId.of("UTC")).toInstant());

		SearchMetadata md1 = new SearchMetadata();
		md1.setKeyObjectStorage("product1");
		List<SearchMetadata> metadataResults = new ArrayList<>();
		metadataResults.add(md1);

		ObsObject ob1 = new ObsObject("product1", family);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product1", ob1);

		doReturn(metadataResults).when(metadataService).query(family, localDateTimeStart, localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		int discrepancies = validationService.checkConsistencyForInterval(localDateTimeStart, localDateTimeStop);
		assertEquals(0, discrepancies);
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenConsistent2()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;

		String intervalStart = "2000-01-01T00:00:00.000000Z";
		String intervalStop = "2000-01-03T00:00:00.000000Z";

		LocalDateTime localDateTimeStart = LocalDateTime.parse(intervalStart, DateUtils.METADATA_DATE_FORMATTER);
		LocalDateTime localDateTimeStop = LocalDateTime.parse(intervalStop, DateUtils.METADATA_DATE_FORMATTER);

		Date startDate = Date.from(localDateTimeStart.atZone(ZoneId.of("UTC")).toInstant());
		Date stopDate = Date.from(localDateTimeStop.atZone(ZoneId.of("UTC")).toInstant());

		SearchMetadata md1 = new SearchMetadata();
		md1.setKeyObjectStorage("product1");
		SearchMetadata md2 = new SearchMetadata();
		md2.setKeyObjectStorage("product2");

		List<SearchMetadata> metadataResults = new ArrayList<>();
		metadataResults.add(md1);
		metadataResults.add(md2);

		ObsObject ob1 = new ObsObject("product1", family);
		ObsObject ob2 = new ObsObject("product2", family);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product1", ob1);
		obsResults.put("product2", ob2);

		doReturn(metadataResults).when(metadataService).query(family, localDateTimeStart, localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		int discrepancies = validationService.checkConsistencyForInterval(localDateTimeStart, localDateTimeStop);
		assertEquals(0, discrepancies);
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenInconsistentObs()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;

		String intervalStart = "2000-01-01T00:00:00.000000Z";
		String intervalStop = "2000-01-03T00:00:00.000000Z";

		LocalDateTime localDateTimeStart = LocalDateTime.parse(intervalStart, DateUtils.METADATA_DATE_FORMATTER);
		LocalDateTime localDateTimeStop = LocalDateTime.parse(intervalStop, DateUtils.METADATA_DATE_FORMATTER);

		Date startDate = Date.from(localDateTimeStart.atZone(ZoneId.of("UTC")).toInstant());
		Date stopDate = Date.from(localDateTimeStop.atZone(ZoneId.of("UTC")).toInstant());

		SearchMetadata md1 = new SearchMetadata();
		md1.setKeyObjectStorage("product1");
		List<SearchMetadata> metadataResults = new ArrayList<>();
		metadataResults.add(md1);

		Map<String, ObsObject> obsResults = new HashMap<>();

		doReturn(metadataResults).when(metadataService).query(family, localDateTimeStart, localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		int discrepancies = validationService.checkConsistencyForInterval(localDateTimeStart, localDateTimeStop);
		assertEquals(1, discrepancies);
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenInconsistentObs2()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;

		String intervalStart = "2000-01-01T00:00:00.000000Z";
		String intervalStop = "2000-01-03T00:00:00.000000Z";

		LocalDateTime localDateTimeStart = LocalDateTime.parse(intervalStart, DateUtils.METADATA_DATE_FORMATTER);
		LocalDateTime localDateTimeStop = LocalDateTime.parse(intervalStop, DateUtils.METADATA_DATE_FORMATTER);

		Date startDate = Date.from(localDateTimeStart.atZone(ZoneId.of("UTC")).toInstant());
		Date stopDate = Date.from(localDateTimeStop.atZone(ZoneId.of("UTC")).toInstant());

		SearchMetadata md1 = new SearchMetadata();
		md1.setKeyObjectStorage("product1");
		SearchMetadata md2 = new SearchMetadata();
		md2.setKeyObjectStorage("product2");

		List<SearchMetadata> metadataResults = new ArrayList<>();
		metadataResults.add(md1);
		metadataResults.add(md2);

		ObsObject ob2 = new ObsObject("product2", family);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product2", ob2);

		doReturn(metadataResults).when(metadataService).query(family, localDateTimeStart, localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		int discrepancies = validationService.checkConsistencyForInterval(localDateTimeStart, localDateTimeStop);
		assertEquals(1, discrepancies);
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenInconsistentMetadata()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;

		String intervalStart = "2000-01-01T00:00:00.000000Z";
		String intervalStop = "2000-01-03T00:00:00.000000Z";

		LocalDateTime localDateTimeStart = LocalDateTime.parse(intervalStart, DateUtils.METADATA_DATE_FORMATTER);
		LocalDateTime localDateTimeStop = LocalDateTime.parse(intervalStop, DateUtils.METADATA_DATE_FORMATTER);

		Date startDate = Date.from(localDateTimeStart.atZone(ZoneId.of("UTC")).toInstant());
		Date stopDate = Date.from(localDateTimeStop.atZone(ZoneId.of("UTC")).toInstant());

		List<SearchMetadata> metadataResults = new ArrayList<>();

		ObsObject ob1 = new ObsObject("product1", family);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product1", ob1);

		doReturn(metadataResults).when(metadataService).query(family, localDateTimeStart, localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		int discrepancies = validationService.checkConsistencyForInterval(localDateTimeStart, localDateTimeStop);
		assertEquals(1, discrepancies);
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenInconsistentMetadata2()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;

		String intervalStart = "2000-01-01T00:00:00.000000Z";
		String intervalStop = "2000-01-03T00:00:00.000000Z";

		LocalDateTime localDateTimeStart = LocalDateTime.parse(intervalStart, DateUtils.METADATA_DATE_FORMATTER);
		LocalDateTime localDateTimeStop = LocalDateTime.parse(intervalStop, DateUtils.METADATA_DATE_FORMATTER);

		Date startDate = Date.from(localDateTimeStart.atZone(ZoneId.of("UTC")).toInstant());
		Date stopDate = Date.from(localDateTimeStop.atZone(ZoneId.of("UTC")).toInstant());

		SearchMetadata md2 = new SearchMetadata();
		md2.setKeyObjectStorage("product2");

		List<SearchMetadata> metadataResults = new ArrayList<>();
		metadataResults.add(md2);

		ObsObject ob1 = new ObsObject("product1", family);
		ObsObject ob2 = new ObsObject("product2", family);
		ObsObject ob3 = new ObsObject("product3", family);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product1", ob1);
		obsResults.put("product2", ob2);
		obsResults.put("product3", ob3);

		doReturn(metadataResults).when(metadataService).query(family, localDateTimeStart, localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		int discrepancies = validationService.checkConsistencyForInterval(localDateTimeStart, localDateTimeStop);
		assertEquals(2, discrepancies);
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenInconsistentMetadataAndObs()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;

		String intervalStart = "2000-01-01T00:00:00.000000Z";
		String intervalStop = "2000-01-03T00:00:00.000000Z";

		LocalDateTime localDateTimeStart = LocalDateTime.parse(intervalStart, DateUtils.METADATA_DATE_FORMATTER);
		LocalDateTime localDateTimeStop = LocalDateTime.parse(intervalStop, DateUtils.METADATA_DATE_FORMATTER);

		Date startDate = Date.from(localDateTimeStart.atZone(ZoneId.of("UTC")).toInstant());
		Date stopDate = Date.from(localDateTimeStop.atZone(ZoneId.of("UTC")).toInstant());

		SearchMetadata md2 = new SearchMetadata();
		md2.setKeyObjectStorage("product2");

		List<SearchMetadata> metadataResults = new ArrayList<>();
		metadataResults.add(md2);

		ObsObject ob1 = new ObsObject("product1", family);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product1", ob1);

		doReturn(metadataResults).when(metadataService).query(family, localDateTimeStart, localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		int discrepancies = validationService.checkConsistencyForInterval(localDateTimeStart, localDateTimeStop);
		assertEquals(2, discrepancies);
	}
	
	@Test
	public void testCheckConsistencyForAuxScenario()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.AUXILIARY_FILE;

		String intervalStart = "2000-01-01T00:00:00.000000Z";
		String intervalStop = "2000-01-03T00:00:00.000000Z";

		LocalDateTime localDateTimeStart = LocalDateTime.parse(intervalStart, DateUtils.METADATA_DATE_FORMATTER);
		LocalDateTime localDateTimeStop = LocalDateTime.parse(intervalStop, DateUtils.METADATA_DATE_FORMATTER);

		Date startDate = Date.from(localDateTimeStart.atZone(ZoneId.of("UTC")).toInstant());
		Date stopDate = Date.from(localDateTimeStop.atZone(ZoneId.of("UTC")).toInstant());

		SearchMetadata md1 = new SearchMetadata();
		md1.setKeyObjectStorage("AUX_PP1_PRODUCT1");
		SearchMetadata md2 = new SearchMetadata();
		md2.setKeyObjectStorage("MPL_SF_PRODUCT2");
		SearchMetadata md3 = new SearchMetadata();
		md3.setKeyObjectStorage("AUX_MPL_PRODUCT3");

		List<SearchMetadata> metadataResults = new ArrayList<>();
		metadataResults.add(md1);
		metadataResults.add(md2);
		metadataResults.add(md3);

		ObsObject ob1 = new ObsObject("AUX_PP1_PRODUCT1", family);
		ObsObject ob2 = new ObsObject("MPL_SF_PRODUCT2", family);
		
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("AUX_PP1/data.xsd", ob1);
		obsResults.put("AUX_PP1/other.xsd", ob1);
		
		obsResults.put("MPL_SF/other.xsd", ob2);

		doReturn(metadataResults).when(metadataService).query(family, localDateTimeStart, localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		int discrepancies = validationService.checkConsistencyForInterval(localDateTimeStart, localDateTimeStop);
		assertEquals(1, discrepancies);
	}
	
	@Test
	public void testCheckConsistencyForSessionScenario()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.EDRS_SESSION;

		String intervalStart = "2000-01-01T00:00:00.000000Z";
		String intervalStop = "2000-01-03T00:00:00.000000Z";

		LocalDateTime localDateTimeStart = LocalDateTime.parse(intervalStart, DateUtils.METADATA_DATE_FORMATTER);
		LocalDateTime localDateTimeStop = LocalDateTime.parse(intervalStop, DateUtils.METADATA_DATE_FORMATTER);

		Date startDate = Date.from(localDateTimeStart.atZone(ZoneId.of("UTC")).toInstant());
		Date stopDate = Date.from(localDateTimeStop.atZone(ZoneId.of("UTC")).toInstant());

		SearchMetadata md1 = new SearchMetadata();
		md1.setKeyObjectStorage("S1A/L231232132/ch02/DCS_L231232132_ch2_DSDB_01921.raw");
		SearchMetadata md2 = new SearchMetadata();
		md2.setKeyObjectStorage("S1B/L231232132/ch01/DCS_L231232132_ch2_DSDB_01922.raw");
		SearchMetadata md3 = new SearchMetadata();
		md3.setKeyObjectStorage("S1C/notvalid");

		List<SearchMetadata> metadataResults = new ArrayList<>();
		metadataResults.add(md1);
		metadataResults.add(md2);
		metadataResults.add(md3);

		ObsObject ob1 = new ObsObject("S1A/L231232132/ch02/DCS_L231232132_ch2_DSDB_01921.raw", family);
		ObsObject ob2 = new ObsObject("S1B/L231232132/ch01/DCS_L231232132_ch2_DSDB_01922.raw", family);
		
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("S1A/L231232132/ch02/DCS_L231232132_ch2_DSDB_01921.raw", ob1);
		obsResults.put("S1B/L231232132/ch01/DCS_L231232132_ch2_DSDB_01922.raw", ob2);
		

		doReturn(metadataResults).when(metadataService).query(family, localDateTimeStart, localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		int discrepancies = validationService.checkConsistencyForInterval(localDateTimeStart, localDateTimeStop);
		assertEquals(1, discrepancies);
	}
	
	@Test
	public void testCheckConsistencyForSliceScenario()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;

		String intervalStart = "2000-01-01T00:00:00.000000Z";
		String intervalStop = "2000-01-03T00:00:00.000000Z";

		LocalDateTime localDateTimeStart = LocalDateTime.parse(intervalStart, DateUtils.METADATA_DATE_FORMATTER);
		LocalDateTime localDateTimeStop = LocalDateTime.parse(intervalStop, DateUtils.METADATA_DATE_FORMATTER);

		Date startDate = Date.from(localDateTimeStart.atZone(ZoneId.of("UTC")).toInstant());
		Date stopDate = Date.from(localDateTimeStop.atZone(ZoneId.of("UTC")).toInstant());

		SearchMetadata md1 = new SearchMetadata();
		md1.setKeyObjectStorage("S1B_IW_RAW__0SDV_20181001T143515_20181001T143547_012960_017EFD_5A8C.SAFE");
		SearchMetadata md2 = new SearchMetadata();
		md2.setKeyObjectStorage("S1A_IW_RAW__0SDV_20181001T143515_20181001T143547_012960_017EFD_5A8C.SAFE");

		List<SearchMetadata> metadataResults = new ArrayList<>();
		metadataResults.add(md1);
		metadataResults.add(md2);
		
		ObsObject ob1 = new ObsObject("S1B_IW_RAW__0SDV_20181001T143515_20181001T143547_012960_017EFD_5A8C.SAFE/manifest.safe", family);
		ObsObject ob2 = new ObsObject("S1B_IW_RAW__0SDV_20181001T143515_20181001T143547_012960_017EFD_5A8C.SAFE/s1b-ew-raw-s-hh-20181001t142523-20181001t142631-012960-017efc-index.dat", family);
		
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("S1B_IW_RAW__0SDV_20181001T143515_20181001T143547_012960_017EFD_5A8C.SAFE/manifest.safe", ob1);
		obsResults.put("S1B_IW_RAW__0SDV_20181001T143515_20181001T143547_012960_017EFD_5A8C.SAFE/s1b-ew-raw-s-hh-20181001t142523-20181001t142631-012960-017efc-index.dat", ob2);
		

		doReturn(metadataResults).when(metadataService).query(family, localDateTimeStart, localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		int discrepancies = validationService.checkConsistencyForInterval(localDateTimeStart, localDateTimeStop);
		assertEquals(1, discrepancies);
	}
}
