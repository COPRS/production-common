package esa.s1pdgs.cpoc.ipf.preparation.worker.type.segment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
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

	private final LevelSegmentMetadata metadata_IW_VV_02F9CE_FULL_1 = new LevelSegmentMetadata(
			"S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_17E4.SAFE", "IW_RAW__0S",
			"S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_17E4.SAFE", "2021-01-04T09:17:09.187813Z",
			"2021-01-04T09:18:33.511574Z", "S1", "B", null, "VV", "FULL", "", "02F9CE");

	private final LevelSegmentMetadata metadata_IW_VV_02F9CE_FULL_2 = new LevelSegmentMetadata(
			"S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_4F09.SAFE", "IW_RAW__0S",
			"S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_4F09.SAFE", "2021-01-04T09:17:09.187813Z",
			"2021-01-04T09:18:33.511574Z", "S1", "B", null, "VV", "FULL", "", "02F9CE");

	private final LevelSegmentMetadata metadata_IW_VH_02F9CE_FULL_1 = new LevelSegmentMetadata(
			"S1B_IW_RAW__0SVH_20210104T091709_20210104T091833_025002_02F9CE_47B0.SAFE", "IW_RAW__0S",
			"S1B_IW_RAW__0SVH_20210104T091709_20210104T091833_025002_02F9CE_47B0.SAFE", "2021-01-04T09:17:09.187813Z",
			"2021-01-04T09:18:33.511574Z", "S1", "B", null, "VH", "FULL", "", "02F9CE");

	private final LevelSegmentMetadata metadata_IW_VH_02F9CE_PART_1 = new LevelSegmentMetadata(
			"S1B_IW_RAW__0SVH_20210104T091709_20210104T091825_025002_02F9CE_283E.SAFE", "IW_RAW__0S",
			"S1B_IW_RAW__0SVH_20210104T091709_20210104T091825_025002_02F9CE_283E.SAFE", "2021-01-04T09:17:09.187813Z",
			"2021-01-04T09:18:25.258812Z", "S1", "B", null, "VH", "PARTIAL", "", "02F9CE");

	private final LevelSegmentMetadata metadata_IW_VH_02F9CE_FULL_2 = new LevelSegmentMetadata(
			"S1B_IW_RAW__0SVH_20210104T091709_20210104T091833_025002_02F9CE_ABCD.SAFE", "IW_RAW__0S",
			"S1B_IW_RAW__0SVH_20210104T091709_20210104T091833_025002_02F9CE_ABCD.SAFE", "2021-01-04T09:17:09.187813Z",
			"2021-01-04T09:18:33.511574Z", "S1", "B", null, "VH", "FULL", "", "02F9CE");

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		uut = new L0SegmentTypeAdapter(metadataClient, AspPropertiesAdapter.of(createAspProperties(true)));
	}

	/**
	 * Scenario 1:
	 * 
	 * Non RFC production when only older and newer complete/full segments are
	 * available
	 * 
	 * @throws MetadataQueryException
	 * @throws IpfPrepWorkerInputsMissingException
	 */
	@Test
	public void testNonRFProductionWithOlderAndNewerFullSegments()
			throws MetadataQueryException, IpfPrepWorkerInputsMissingException {

		Instant insertionTime = Instant.now();

		String metadataInsertionTime1 = toMetadataDateFormat(insertionTime);
		String metadataInsertionTime2 = toMetadataDateFormat(insertionTime.plusSeconds(130));

		metadata_IW_VV_02F9CE_FULL_1.setInsertionTime(metadataInsertionTime1);
		metadata_IW_VV_02F9CE_FULL_2.setInsertionTime(metadataInsertionTime2);
		metadata_IW_VH_02F9CE_FULL_1.setInsertionTime(metadataInsertionTime1);
		metadata_IW_VH_02F9CE_FULL_2.setInsertionTime(metadataInsertionTime2);

		AppDataJob appDataJob1 = new AppDataJob(123L);
		appDataJob1.setCreationDate(Date.from(insertionTime.plusSeconds(3)));
		AppDataJobProduct product1 = new AppDataJobProduct();
		product1.getMetadata().put("startTime", "2021-01-04T09:17:09.187813Z");
		product1.getMetadata().put("productName",
				"S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_17E4.SAFE");
		product1.getMetadata().put("productType", "IW_RAW__0S");
		product1.getMetadata().put("dataTakeId", "02F9CE");
		appDataJob1.setProduct(product1);

		doReturn(Arrays.asList(metadata_IW_VV_02F9CE_FULL_1, metadata_IW_VV_02F9CE_FULL_2)).when(metadataClient)
				.getLevelSegments("02F9CE");

		Product product = uut.mainInputSearch(appDataJob1, null);

		Map<String, List<AppDataJobFile>> inputs = ((L0SegmentProduct) product).toProduct().getInputs();

		assertEquals(1, inputs.size());
		assertNotNull(inputs.get("VV"));
		assertEquals(1, inputs.get("VV").size());
		assertEquals("S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_4F09.SAFE",
				inputs.get("VV").get(0).getFilename());

		try {
			uut.validateInputSearch(appDataJob1, null);
			fail("Missing inputs, exception shall be thrown!");
		} catch (IpfPrepWorkerInputsMissingException missingEx) {
			// Expected
		}

		doReturn(Arrays.asList(metadata_IW_VV_02F9CE_FULL_1, metadata_IW_VV_02F9CE_FULL_2, metadata_IW_VH_02F9CE_FULL_2,
				metadata_IW_VH_02F9CE_FULL_1)).when(metadataClient).getLevelSegments("02F9CE");

		product = uut.mainInputSearch(appDataJob1, null);

		inputs = ((L0SegmentProduct) product).toProduct().getInputs();

		assertEquals(2, inputs.size());
		assertNotNull(inputs.get("VV"));
		assertNotNull(inputs.get("VH"));
		assertEquals(1, inputs.get("VV").size());
		assertEquals(1, inputs.get("VH").size());
		assertEquals("S1B_IW_RAW__0SVH_20210104T091709_20210104T091833_025002_02F9CE_ABCD.SAFE",
				inputs.get("VH").get(0).getFilename());
		assertEquals("S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_4F09.SAFE",
				inputs.get("VV").get(0).getFilename());

		try {
			uut.validateInputSearch(appDataJob1, null);
		} catch (IpfPrepWorkerInputsMissingException missingEx) {
			fail("All necessary inputs shall be provided, selection logic have a bug!");
		}
	}

	/**
	 * Scenario 3:
	 * 
	 * Non RFC production when older and newer complete/full and partial segments
	 * are available
	 * 
	 * @throws MetadataQueryException
	 * @throws IpfPrepWorkerInputsMissingException
	 */
	@Test
	public void testNonRFProductionWithOlderAndNewerFullAndPartialSegments()
			throws MetadataQueryException, IpfPrepWorkerInputsMissingException {

		Instant insertionTime = Instant.now();

		String metadataInsertionTime1 = toMetadataDateFormat(insertionTime);
		String metadataInsertionTime2 = toMetadataDateFormat(insertionTime.plusSeconds(130));

		metadata_IW_VV_02F9CE_FULL_1.setInsertionTime(metadataInsertionTime2);
		metadata_IW_VV_02F9CE_FULL_2.setInsertionTime(metadataInsertionTime1);
		metadata_IW_VH_02F9CE_FULL_1.setInsertionTime(metadataInsertionTime2);
		metadata_IW_VH_02F9CE_PART_1.setInsertionTime(metadataInsertionTime1);

		AppDataJob appDataJob1 = new AppDataJob(123L);
		appDataJob1.setCreationDate(Date.from(insertionTime.plusSeconds(3)));
		AppDataJobProduct product1 = new AppDataJobProduct();
		product1.getMetadata().put("startTime", "2021-01-04T09:17:09.187813Z");
		product1.getMetadata().put("productName",
				"S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_4F09.SAFE");
		product1.getMetadata().put("productType", "IW_RAW__0S");
		product1.getMetadata().put("dataTakeId", "02F9CE");
		appDataJob1.setProduct(product1);

		doReturn(Arrays.asList(metadata_IW_VV_02F9CE_FULL_2, metadata_IW_VH_02F9CE_PART_1)).when(metadataClient)
				.getLevelSegments("02F9CE");

		Product product = uut.mainInputSearch(appDataJob1, null);

		Map<String, List<AppDataJobFile>> inputs = ((L0SegmentProduct) product).toProduct().getInputs();

		assertEquals(2, inputs.size());
		assertNotNull(inputs.get("VV"));
		assertNotNull(inputs.get("VH"));
		assertEquals(1, inputs.get("VV").size());
		assertEquals(1, inputs.get("VH").size());
		assertEquals("S1B_IW_RAW__0SVH_20210104T091709_20210104T091825_025002_02F9CE_283E.SAFE",
				inputs.get("VH").get(0).getFilename());
		assertEquals("S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_4F09.SAFE",
				inputs.get("VV").get(0).getFilename());

		try {
			uut.validateInputSearch(appDataJob1, null);
			fail("Missing inputs, exception shall be thrown!");
		} catch (IpfPrepWorkerInputsMissingException missingEx) {
			// Expected
		}

		doReturn(
				Arrays.asList(metadata_IW_VV_02F9CE_FULL_2, metadata_IW_VH_02F9CE_PART_1, metadata_IW_VH_02F9CE_FULL_1))
						.when(metadataClient).getLevelSegments("02F9CE");

		product = uut.mainInputSearch(appDataJob1, null);

		inputs = ((L0SegmentProduct) product).toProduct().getInputs();

		assertEquals(2, inputs.size());
		assertNotNull(inputs.get("VV"));
		assertNotNull(inputs.get("VH"));
		assertEquals(1, inputs.get("VV").size());
		assertEquals(1, inputs.get("VH").size());
		assertEquals("S1B_IW_RAW__0SVH_20210104T091709_20210104T091833_025002_02F9CE_47B0.SAFE",
				inputs.get("VH").get(0).getFilename());
		assertEquals("S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_4F09.SAFE",
				inputs.get("VV").get(0).getFilename());

		try {
			uut.validateInputSearch(appDataJob1, null);
		} catch (IpfPrepWorkerInputsMissingException missingEx) {
			fail("All necessary inputs shall be provided, selection logic have a bug!");
		}
	}

	private String toMetadataDateFormat(Instant date) {
		return DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.ofInstant(date, ZoneId.of("UTC")));
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
