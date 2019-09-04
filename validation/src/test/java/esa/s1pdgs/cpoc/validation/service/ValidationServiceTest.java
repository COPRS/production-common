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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.validation.service.metadata.MetadataService;

public class ValidationServiceTest {

	private static final Logger LOGGER = LogManager.getLogger(ValidationServiceTest.class);
	
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
	public void testQueryFamily() {
		assertEquals("AUXILIARY_FILE", validationService.getQueryFamily(ProductFamily.AUXILIARY_FILE));
		assertEquals("AUXILIARY_FILE", validationService.getQueryFamily(ProductFamily.AUXILIARY_FILE_ZIP));
	}

	@Test
	public void testCheckConsistencyForAuxScenario() throws SdkClientException, MetadataQueryException {

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

		SearchMetadata md4 = new SearchMetadata();
		md4.setKeyObjectStorage("S1B_AUX_CAL_V201600000_G201700000000.SAFE");

		List<SearchMetadata> metadataResults = new ArrayList<>();
		metadataResults.add(md1);
		metadataResults.add(md2);
		metadataResults.add(md3); // Fail
		metadataResults.add(md4);

		ObsObject ob1 = new ObsObject(family, "AUX_PP1_PRODUCT1");
		ObsObject ob2 = new ObsObject(family, "MPL_SF_PRODUCT2");
		ObsObject ob3 = new ObsObject(family, "S1B_AUX_CAL_V201600000_G201700000000.SAFE/manifest.SAFE");
		ObsObject ob4 = new ObsObject(family, "S1B_AUX_CAL_V201600000_G201700000000.SAFE/data.dat");

		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("AUX_PP1/data.xsd", ob1);
		obsResults.put("AUX_PP1/other.xsd", ob1);

		obsResults.put("MPL_SF/other.xsd", ob2);

		obsResults.put("S1B_AUX_CAL_V201600000_G201700000000.SAFE/manifest.SAFE", ob3);
		obsResults.put("S1B_AUX_CAL_V201600000_G201700000000.SAFE/data.dat", ob4);

		doReturn(metadataResults).when(metadataService).query(family, localDateTimeStart, localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		final Reporting.Factory reportingFactory = new LoggerReporting.Factory("ValidationService");
		int discrepancies = validationService.validateProductFamily(reportingFactory, ProductFamily.AUXILIARY_FILE, localDateTimeStart, localDateTimeStop);
		assertEquals(1, discrepancies);
	}
	
	@Test
	public void testCheckConsistencyForZippedAuxScenario() throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.AUXILIARY_FILE_ZIP;

		String intervalStart = "2000-01-01T00:00:00.000000Z";
		String intervalStop = "2000-01-03T00:00:00.000000Z";

		LocalDateTime localDateTimeStart = LocalDateTime.parse(intervalStart, DateUtils.METADATA_DATE_FORMATTER);
		LocalDateTime localDateTimeStop = LocalDateTime.parse(intervalStop, DateUtils.METADATA_DATE_FORMATTER);

		Date startDate = Date.from(localDateTimeStart.atZone(ZoneId.of("UTC")).toInstant());
		Date stopDate = Date.from(localDateTimeStop.atZone(ZoneId.of("UTC")).toInstant());

		SearchMetadata md1 = new SearchMetadata();
		md1.setKeyObjectStorage("S1B_AUX_CAL_V201600000_G201700000000.SAFE");

		List<SearchMetadata> metadataResults = new ArrayList<>();
		metadataResults.add(md1);

		ObsObject ob1 = new ObsObject(family, "S1B_AUX_CAL_V201600000_G201700000000.SAFE.zip");

		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("S1B_AUX_CAL_V201600000_G201700000000.SAFE.zip", ob1);

		doReturn(metadataResults).when(metadataService).query(family, localDateTimeStart, localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		final Reporting.Factory reportingFactory = new LoggerReporting.Factory("ValidationService");
		int discrepancies = validationService.validateProductFamily(reportingFactory, ProductFamily.AUXILIARY_FILE_ZIP, localDateTimeStart, localDateTimeStop);
		assertEquals(0, discrepancies);
	}

	@Test
	public void testCheckConsistencyForSessionScenario() throws SdkClientException, MetadataQueryException {

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

		ObsObject ob1 = new ObsObject(family, "S1A/L231232132/ch02/DCS_L231232132_ch2_DSDB_01921.raw");
		ObsObject ob2 = new ObsObject(family, "S1B/L231232132/ch01/DCS_L231232132_ch2_DSDB_01922.raw");

		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("S1A/L231232132/ch02/DCS_L231232132_ch2_DSDB_01921.raw", ob1);
		obsResults.put("S1B/L231232132/ch01/DCS_L231232132_ch2_DSDB_01922.raw", ob2);

		doReturn(metadataResults).when(metadataService).query(family, localDateTimeStart, localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		final Reporting.Factory reportingFactory = new LoggerReporting.Factory("ValidationService");
		int discrepancies = validationService.validateProductFamily(reportingFactory, ProductFamily.EDRS_SESSION, localDateTimeStart, localDateTimeStop);
		assertEquals(1, discrepancies);
	}

	@Test
	public void testCheckConsistencyForSliceScenario() throws SdkClientException, MetadataQueryException {

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

		ObsObject ob1 = new ObsObject(family, 
				"S1B_IW_RAW__0SDV_20181001T143515_20181001T143547_012960_017EFD_5A8C.SAFE/manifest.safe");
		ObsObject ob2 = new ObsObject(family, 
				"S1B_IW_RAW__0SDV_20181001T143515_20181001T143547_012960_017EFD_5A8C.SAFE/s1b-ew-raw-s-hh-20181001t142523-20181001t142631-012960-017efc-index.dat");

		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("S1B_IW_RAW__0SDV_20181001T143515_20181001T143547_012960_017EFD_5A8C.SAFE/manifest.safe", ob1);
		obsResults.put(
				"S1B_IW_RAW__0SDV_20181001T143515_20181001T143547_012960_017EFD_5A8C.SAFE/s1b-ew-raw-s-hh-20181001t142523-20181001t142631-012960-017efc-index.dat",
				ob2);

		doReturn(metadataResults).when(metadataService).query(family, localDateTimeStart, localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		final Reporting.Factory reportingFactory = new LoggerReporting.Factory("ValidationService");
		int discrepancies = validationService.validateProductFamily(reportingFactory, ProductFamily.L0_SLICE, localDateTimeStart, localDateTimeStop);
		assertEquals(1, discrepancies);
	}
	
	/**
	 * Check if a normal slice product with substructure is identified correctly
	 */
	@Test
	public void testSliceVerification() {
		SearchMetadata sm = new SearchMetadata();
		sm.setKeyObjectStorage("S1B_WV_OCN_2SSV.SAFE");
		
		List<ObsObject> obs = new ArrayList<>();
		obs.add(new ObsObject(ProductFamily.L0_SLICE, "S1B_WV_OCN_2SSV.SAFE/manifest"));
		
		assertEquals(true,validationService.verifySliceForObject(sm, obs));		
	}
	
	/**
	 * Check if a zipped slice is identified correctly with plain metadata
	 */
	@Test
	public void testZippedSliceVerification() {
		SearchMetadata sm = new SearchMetadata();
		sm.setKeyObjectStorage("S1B_WV_OCN_2SSV.SAFE");
		
		List<ObsObject> obs = new ArrayList<>();
		obs.add(new ObsObject(ProductFamily.L0_SLICE_ZIP, "S1B_WV_OCN_2SSV.SAFE.zip"));
		
		assertEquals(true,validationService.verifySliceForObject(sm, obs));
		
	}
	
	@Test
	public void testAUXVerification() {
		SearchMetadata sm = new SearchMetadata();
		sm.setKeyObjectStorage("AUX_PP1.EOF");
		
		List<ObsObject> obs = new ArrayList<>();
		obs.add(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX_PP1.EOF"));
		
		assertEquals(true,validationService.verifyAuxMetadataForObject(sm, obs));		
	}
	
	@Test
	public void testZippedAUXVerification() {
		SearchMetadata sm = new SearchMetadata();
		sm.setKeyObjectStorage("AUX_PP1.EOF");
		
		List<ObsObject> obs = new ArrayList<>();
		obs.add(new ObsObject(ProductFamily.AUXILIARY_FILE_ZIP, "AUX_PP1.EOF.zip"));
		
		assertEquals(true,validationService.verifyAuxMetadataForObject(sm, obs));		
	}
	
	@Test
	public void testZippedAUXSafeVerification() {
		SearchMetadata sm = new SearchMetadata();
		sm.setKeyObjectStorage("AUX_PP1.SAFE");
		
		List<ObsObject> obs = new ArrayList<>();
		obs.add(new ObsObject(ProductFamily.AUXILIARY_FILE_ZIP, "AUX_PP1.SAFE.zip"));
		
		assertEquals(true,validationService.verifyAuxMetadataForObject(sm, obs));		
	}
	
	@Test
	public void testSessionVerification() {
		SearchMetadata sm = new SearchMetadata();
		sm.setKeyObjectStorage("S1B/L202020/ch02/DCS_something.raw");
		
		List<ObsObject> obs = new ArrayList<>();
		obs.add(new ObsObject(ProductFamily.AUXILIARY_FILE_ZIP, "S1B/L202020/ch02/DCS_something.raw"));
		
		assertEquals(true,validationService.verifySessionForObject(sm, obs));		
	}
}
