package esa.s1pdgs.cpoc.validation.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
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
import esa.s1pdgs.cpoc.obs_sdk.ObsFamily;
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
		ObsFamily obsFamily = ObsFamily.L0_SLICE;

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

		ObsObject ob1 = new ObsObject("product1", obsFamily);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product1", ob1);

		doReturn(metadataResults).when(metadataService).query(family, null, intervalStart, intervalStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		boolean consistent = validationService.checkConsistencyForFamilyAndTimeFrame(family, intervalStart,
				intervalStop);
		assertTrue(consistent);
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenConsistent2()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;
		ObsFamily obsFamily = ObsFamily.L0_SLICE;

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

		ObsObject ob1 = new ObsObject("product1", obsFamily);
		ObsObject ob2 = new ObsObject("product2", obsFamily);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product1", ob1);
		obsResults.put("product2", ob2);

		doReturn(metadataResults).when(metadataService).query(family, null, intervalStart, intervalStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		boolean consistent = validationService.checkConsistencyForFamilyAndTimeFrame(family, intervalStart,
				intervalStop);
		assertTrue(consistent);
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

		doReturn(metadataResults).when(metadataService).query(family, null, intervalStart, intervalStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		boolean consistent = validationService.checkConsistencyForFamilyAndTimeFrame(family, intervalStart,
				intervalStop);
		assertFalse(consistent);
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenInconsistentObs2()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;
		ObsFamily obsFamily = ObsFamily.L0_SLICE;

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

		ObsObject ob2 = new ObsObject("product2", obsFamily);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product2", ob2);

		doReturn(metadataResults).when(metadataService).query(family, null, intervalStart, intervalStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		boolean consistent = validationService.checkConsistencyForFamilyAndTimeFrame(family, intervalStart,
				intervalStop);
		assertFalse(consistent);
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenInconsistentMetadata()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;
		ObsFamily obsFamily = ObsFamily.L0_SLICE;

		String intervalStart = "2000-01-01T00:00:00.000000Z";
		String intervalStop = "2000-01-03T00:00:00.000000Z";

		LocalDateTime localDateTimeStart = LocalDateTime.parse(intervalStart, DateUtils.METADATA_DATE_FORMATTER);
		LocalDateTime localDateTimeStop = LocalDateTime.parse(intervalStop, DateUtils.METADATA_DATE_FORMATTER);

		Date startDate = Date.from(localDateTimeStart.atZone(ZoneId.of("UTC")).toInstant());
		Date stopDate = Date.from(localDateTimeStop.atZone(ZoneId.of("UTC")).toInstant());

		List<SearchMetadata> metadataResults = new ArrayList<>();

		ObsObject ob1 = new ObsObject("product1", obsFamily);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product1", ob1);

		doReturn(metadataResults).when(metadataService).query(family, null, intervalStart, intervalStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		boolean consistent = validationService.checkConsistencyForFamilyAndTimeFrame(family, intervalStart,
				intervalStop);
		assertFalse(consistent);
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenInconsistentMetadata2()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;
		ObsFamily obsFamily = ObsFamily.L0_SLICE;

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

		ObsObject ob1 = new ObsObject("product1", obsFamily);
		ObsObject ob2 = new ObsObject("product2", obsFamily);
		ObsObject ob3 = new ObsObject("product3", obsFamily);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product1", ob1);
		obsResults.put("product2", ob2);
		obsResults.put("product3", ob3);

		doReturn(metadataResults).when(metadataService).query(family, null, intervalStart, intervalStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		boolean consistent = validationService.checkConsistencyForFamilyAndTimeFrame(family, intervalStart,
				intervalStop);
		assertFalse(consistent);
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenInconsistentMetadataAndObs()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;
		ObsFamily obsFamily = ObsFamily.L0_SLICE;

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

		ObsObject ob1 = new ObsObject("product1", obsFamily);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product1", ob1);

		doReturn(metadataResults).when(metadataService).query(family, null, intervalStart, intervalStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		boolean consistent = validationService.checkConsistencyForFamilyAndTimeFrame(family, intervalStart,
				intervalStop);
		assertFalse(consistent);
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenDateTimeParseException()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;
		ObsFamily obsFamily = ObsFamily.L0_SLICE;

		String intervalStart = "2000-01-01T00:00:00.00g000Z";
		String intervalStop = "2000-01-03T00:00:00.000000Z";

		SearchMetadata md1 = new SearchMetadata();
		md1.setKeyObjectStorage("product1");
		List<SearchMetadata> metadataResults = new ArrayList<>();
		metadataResults.add(md1);

		ObsObject ob1 = new ObsObject("product1", obsFamily);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product1", ob1);

		doReturn(metadataResults).when(metadataService).query(family, null, intervalStart, intervalStop);

		try {
			validationService.checkConsistencyForFamilyAndTimeFrame(family, intervalStart, intervalStop);
			fail("Exception expected");
		} catch (DateTimeParseException e) {
			// expected
		}
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenMetadataQueryException()
			throws MetadataQueryException, SdkClientException {

		ProductFamily family = ProductFamily.L0_SLICE;
		ObsFamily obsFamily = ObsFamily.L0_SLICE;

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

		ObsObject ob1 = new ObsObject("product1", obsFamily);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product1", ob1);

		doThrow(new MetadataQueryException("test generated exception")).when(metadataService).query(family, null,
				intervalStart, intervalStop);
		doReturn(obsResults).when(obsClient).listInterval(family, startDate, stopDate);

		try {
			validationService.checkConsistencyForFamilyAndTimeFrame(family, intervalStart, intervalStop);
			fail("Exception expected");
		} catch (MetadataQueryException e) {
			// expected
		}
	}

	@Test
	public void testCheckConsistencyForFamilyAndTimeFrameWhenSdkClientException()
			throws SdkClientException, MetadataQueryException {

		ProductFamily family = ProductFamily.L0_SLICE;
		ObsFamily obsFamily = ObsFamily.L0_SLICE;

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

		ObsObject ob1 = new ObsObject("product1", obsFamily);
		Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("product1", ob1);

		doReturn(metadataResults).when(metadataService).query(family, null, intervalStart, intervalStop);
		doThrow(new SdkClientException("test generated exception")).when(obsClient).listInterval(family, startDate, stopDate);

		try {
			validationService.checkConsistencyForFamilyAndTimeFrame(family, intervalStart, intervalStop);
			fail("Exception expected");
		} catch (SdkClientException e) {
			// expected
		}
	}

}
