package esa.s1pdgs.cpoc.ipf.preparation.worker.type.segment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.io.File;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.AspProperties;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.DiscardedException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class L0SegmentTypeAdapterTest {

	public final static int WAITING_TIME_HOURS_MINIMAL_FAST = 3;
	public final static int WAITING_TIME_HOURS_NOMINAL_FAST = 20;
	public final static int WAITING_TIME_HOURS_MINIMAL_NRT_PT = 1;
	public final static int WAITING_TIME_HOURS_NOMINAL_NRT_PT = 2;

	@Mock
	private MetadataClient metadataClient;

	@Autowired
	private TaskTableFactory taskTableFactory;

	@Autowired
	private ElementMapper elementMapper;

	private L0SegmentTypeAdapter uut;

	private TaskTableAdapter taskTableAdapter;

	private final LevelSegmentMetadata metadata_IW_VV_02F9CE_FULL_1 = new LevelSegmentMetadata(
			"S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_17E4.SAFE", "IW_RAW__0S",
			"S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_17E4.SAFE", "2021-01-04T09:17:09.187813Z",
			"2021-01-04T09:18:33.511574Z", "S1", "B", null, "VV", "FULL", "", "02F9CE");

	private final LevelSegmentMetadata metadata_IW_VV_02F9CE_FULL_2 = new LevelSegmentMetadata(
			"S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_4F09.SAFE", "IW_RAW__0S",
			"S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_4F09.SAFE", "2021-01-04T09:17:09.187813Z",
			"2021-01-04T09:18:33.511574Z", "S1", "B", null, "VV", "FULL", "", "02F9CE");

	private final LevelSegmentMetadata metadata_IW_VV_02F9CE_PART_1 = new LevelSegmentMetadata(
			"S1B_IW_RAW__0SVV_20210104T091709_20210104T091825_025002_02F9CE_12AB.SAFE", "IW_RAW__0S",
			"S1B_IW_RAW__0SVV_20210104T091709_20210104T091825_025002_02F9CE_12AB.SAFE", "2021-01-04T09:17:09.187813Z",
			"2021-01-04T09:18:25.258812Z", "S1", "B", null, "VV", "PARTIAL", "", "02F9CE");

	private final LevelSegmentMetadata metadata_IW_VV_02F9CE_PART_2 = new LevelSegmentMetadata(
			"S1B_IW_RAW__0SVV_20210104T091709_20210104T091800_025002_02F9CE_12AF.SAFE", "IW_RAW__0S",
			"S1B_IW_RAW__0SVV_20210104T091709_20210104T091800_025002_02F9CE_12AF.SAFE", "2021-01-04T09:17:09.187813Z",
			"2021-01-04T09:18:00.258812Z", "S1", "B", null, "VV", "PARTIAL", "", "02F9CE");

	private final LevelSegmentMetadata metadata_IW_VH_02F9CE_FULL_1 = new LevelSegmentMetadata(
			"S1B_IW_RAW__0SVH_20210104T091709_20210104T091833_025002_02F9CE_47B0.SAFE", "IW_RAW__0S",
			"S1B_IW_RAW__0SVH_20210104T091709_20210104T091833_025002_02F9CE_47B0.SAFE", "2021-01-04T09:17:09.187813Z",
			"2021-01-04T09:18:33.511574Z", "S1", "B", null, "VH", "FULL", "", "02F9CE");

	private final LevelSegmentMetadata metadata_IW_VH_02F9CE_FULL_2 = new LevelSegmentMetadata(
			"S1B_IW_RAW__0SVH_20210104T091709_20210104T091833_025002_02F9CE_ABCD.SAFE", "IW_RAW__0S",
			"S1B_IW_RAW__0SVH_20210104T091709_20210104T091833_025002_02F9CE_ABCD.SAFE", "2021-01-04T09:17:09.187813Z",
			"2021-01-04T09:18:33.511574Z", "S1", "B", null, "VH", "FULL", "", "02F9CE");

	private final LevelSegmentMetadata metadata_IW_VH_02F9CE_PART_1 = new LevelSegmentMetadata(
			"S1B_IW_RAW__0SVH_20210104T091709_20210104T091825_025002_02F9CE_283E.SAFE", "IW_RAW__0S",
			"S1B_IW_RAW__0SVH_20210104T091709_20210104T091825_025002_02F9CE_283E.SAFE", "2021-01-04T09:17:09.187813Z",
			"2021-01-04T09:18:25.258812Z", "S1", "B", null, "VH", "PARTIAL", "", "02F9CE");

	private final LevelSegmentMetadata metadata_IW_VH_02F9CE_PART_2 = new LevelSegmentMetadata(
			"S1B_IW_RAW__0SVH_20210104T091709_20210104T091800_025002_02F9CE_CDEF.SAFE", "IW_RAW__0S",
			"S1B_IW_RAW__0SVH_20210104T091709_20210104T091800_025002_02F9CE_CDEF.SAFE", "2021-01-04T09:17:09.187813Z",
			"2021-01-04T09:18:00.258812Z", "S1", "B", null, "VH", "PARTIAL", "", "02F9CE");

	private final LevelSegmentMetadata metadata_RF_VV_043734_FULL_1 = new LevelSegmentMetadata(
			"S1A_RF_RAW__0SVV_20210104T071910_20210104T071912_035985_043734_C9E3.SAFE", "RF_RAW__0S",
			"S1A_RF_RAW__0SVV_20210104T071910_20210104T071912_035985_043734_C9E3.SAFE", "2021-01-04T07:19:10.000000Z",
			"2021-01-04T07:19:12.000000Z", "S1", "A", null, "VV", "FULL", "", "043734");

	private final LevelSegmentMetadata metadata_RF_VV_043734_FULL_2 = new LevelSegmentMetadata(
			"S1A_RF_RAW__0SVV_20210104T071920_20210104T071922_035985_043734_72C7.SAFE", "RF_RAW__0S",
			"S1A_RF_RAW__0SVV_20210104T071920_20210104T071922_035985_043734_72C7.SAFE", "2021-01-04T07:19:20.000000Z",
			"2021-01-04T07:19:22.000000Z", "S1", "A", null, "VV", "FULL", "", "043734");

	private final LevelSegmentMetadata metadata_RF_VV_043734_PART_1 = new LevelSegmentMetadata(
			"S1A_RF_RAW__0SVV_20210104T071910_20210104T071911_035985_043734_A9E3.SAFE", "RF_RAW__0S",
			"S1A_RF_RAW__0SVV_20210104T071910_20210104T071911_035985_043734_A9E3.SAFE", "2021-01-04T07:19:10.000000Z",
			"2021-01-04T07:19:11.000000Z", "S1", "A", null, "VV", "PARTIAL", "", "043734");

	private final LevelSegmentMetadata metadata_RF_VV_043734_PART_2 = new LevelSegmentMetadata(
			"S1A_RF_RAW__0SVV_20210104T071910_20210104T071911_035985_043734_B9E3.SAFE", "RF_RAW__0S",
			"S1A_RF_RAW__0SVV_20210104T071910_20210104T071911_035985_043734_B9E3.SAFE", "2021-01-04T07:19:10.000000Z",
			"2021-01-04T07:19:11.000000Z", "S1", "A", null, "VV", "PARTIAL", "", "043734");

	private final LevelSegmentMetadata metadata_RF_VV_043734_PART_3 = new LevelSegmentMetadata(
			"S1A_RF_RAW__0SVV_20210104T071920_20210104T071921_035985_043734_75C7.SAFE", "RF_RAW__0S",
			"S1A_RF_RAW__0SVV_20210104T071920_20210104T071921_035985_043734_75C7.SAFE", "2021-01-04T07:19:20.000000Z",
			"2021-01-04T07:19:21.000000Z", "S1", "A", null, "VV", "PARTIAL", "", "043734");

	private final LevelSegmentMetadata metadata_RF_VH_043734_FULL_1 = new LevelSegmentMetadata(
			"S1A_RF_RAW__0SVH_20210104T071910_20210104T071912_035985_043734_2A76.SAFE", "RF_RAW__0S",
			"S1A_RF_RAW__0SVH_20210104T071910_20210104T071912_035985_043734_2A76.SAFE", "2021-01-04T07:19:10.000000Z",
			"2021-01-04T07:19:12.000000Z", "S1", "A", null, "VH", "FULL", "", "043734");

	private final LevelSegmentMetadata metadata_RF_VH_043734_FULL_2 = new LevelSegmentMetadata(
			"S1A_RF_RAW__0SVH_20210104T071920_20210104T071922_035985_043734_EA9C.SAFE", "RF_RAW__0S",
			"S1A_RF_RAW__0SVH_20210104T071920_20210104T071922_035985_043734_EA9C.SAFE", "2021-01-04T07:19:20.000000Z",
			"2021-01-04T07:19:22.000000Z", "S1", "A", null, "VH", "FULL", "", "043734");

	private final LevelSegmentMetadata metadata_RF_VH_043734_PART_1 = new LevelSegmentMetadata(
			"S1A_RF_RAW__0SVH_20210104T071910_20210104T071911_035985_043734_4B76.SAFE", "RF_RAW__0S",
			"S1A_RF_RAW__0SVH_20210104T071910_20210104T071911_035985_043734_4B76.SAFE", "2021-01-04T07:19:10.000000Z",
			"2021-01-04T07:19:11.000000Z", "S1", "A", null, "VH", "PART", "", "043734");

	private final LevelSegmentMetadata metadata_RF_VH_043734_PART_2 = new LevelSegmentMetadata(
			"S1A_RF_RAW__0SVH_20210104T071920_20210104T071921_035985_043734_EE1D.SAFE", "RF_RAW__0S",
			"S1A_RF_RAW__0SVH_20210104T071920_20210104T071921_035985_043734_EE1D.SAFE", "2021-01-04T07:19:20.000000Z",
			"2021-01-04T07:19:21.000000Z", "S1", "A", null, "VH", "PART", "", "043734");

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		uut = new L0SegmentTypeAdapter(metadataClient, AspPropertiesAdapter.of(createAspProperties(true)));

		final File xmlFile = new File("./test/data/l0_segment_config/task_tables/TaskTable.L0ASP.xml");

		taskTableAdapter = new TaskTableAdapter(xmlFile,
				taskTableFactory.buildTaskTable(xmlFile, ApplicationLevel.L0_SEGMENT), elementMapper,
				ProductMode.SLICING);
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

		Product product = uut.mainInputSearch(appDataJob1, taskTableAdapter);
		appDataJob1.setProduct(product.toProduct());
		appDataJob1.setAdditionalInputs(product.overridingInputs());

		Map<String, List<AppDataJobFile>> inputs = appDataJob1.getProduct().getInputs();

		assertEquals(1, inputs.size());
		assertNotNull(inputs.get("VV"));
		assertEquals(1, inputs.get("VV").size());
		assertEquals("S1B_IW_RAW__0SVV_20210104T091709_20210104T091833_025002_02F9CE_4F09.SAFE",
				inputs.get("VV").get(0).getFilename());

		try {
			uut.validateInputSearch(appDataJob1, taskTableAdapter);
			fail("Missing inputs, exception shall be thrown!");
		} catch (IpfPrepWorkerInputsMissingException missingEx) {
			// Expected
		}

		doReturn(Arrays.asList(metadata_IW_VV_02F9CE_FULL_1, metadata_IW_VV_02F9CE_FULL_2, metadata_IW_VH_02F9CE_FULL_2,
				metadata_IW_VH_02F9CE_FULL_1)).when(metadataClient).getLevelSegments("02F9CE");

		product = uut.mainInputSearch(appDataJob1, taskTableAdapter);
		appDataJob1.setProduct(product.toProduct());
		appDataJob1.setAdditionalInputs(product.overridingInputs());

		inputs = appDataJob1.getProduct().getInputs();

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
			uut.validateInputSearch(appDataJob1, taskTableAdapter);
		} catch (IpfPrepWorkerInputsMissingException missingEx) {
			fail("All necessary inputs shall be provided, selection logic have a bug!");
		}
	}

	/**
	 * Scenario 2:
	 * 
	 * Non RFC production when only older and newer partial segments are available
	 * 
	 * @throws MetadataQueryException
	 * @throws IpfPrepWorkerInputsMissingException
	 */
	@Test
	public void testNonRFProductionWithOlderAndNewerPartialSegments()
			throws MetadataQueryException, IpfPrepWorkerInputsMissingException {

		Instant insertionTime = Instant.now();

		String metadataInsertionTime1 = toMetadataDateFormat(insertionTime);
		String metadataInsertionTime2 = toMetadataDateFormat(insertionTime.plusSeconds(130));

		metadata_IW_VV_02F9CE_PART_1.setInsertionTime(metadataInsertionTime1);
		metadata_IW_VV_02F9CE_PART_2.setInsertionTime(metadataInsertionTime2);
		metadata_IW_VH_02F9CE_PART_1.setInsertionTime(metadataInsertionTime1);
		metadata_IW_VH_02F9CE_PART_2.setInsertionTime(metadataInsertionTime2);

		AppDataJob appDataJob1 = new AppDataJob(123L);
		appDataJob1.setCreationDate(Date.from(insertionTime.plusSeconds(3)));
		AppDataJobProduct product1 = new AppDataJobProduct();
		product1.getMetadata().put("startTime", "2021-01-04T09:17:09.187813Z");
		product1.getMetadata().put("productName",
				"S1B_IW_RAW__0SVV_20210104T091709_20210104T091825_025002_02F9CE_12AB.SAFE");
		product1.getMetadata().put("productType", "IW_RAW__0S");
		product1.getMetadata().put("dataTakeId", "02F9CE");
		appDataJob1.setProduct(product1);

		doReturn(Arrays.asList(metadata_IW_VV_02F9CE_PART_1, metadata_IW_VV_02F9CE_PART_2, metadata_IW_VH_02F9CE_PART_1,
				metadata_IW_VH_02F9CE_PART_2)).when(metadataClient).getLevelSegments("02F9CE");

		Product product = uut.mainInputSearch(appDataJob1, taskTableAdapter);
		appDataJob1.setProduct(product.toProduct());
		appDataJob1.setAdditionalInputs(product.overridingInputs());

		Map<String, List<AppDataJobFile>> inputs = appDataJob1.getProduct().getInputs();

		assertEquals(2, inputs.size());
		assertNotNull(inputs.get("VV"));
		assertNotNull(inputs.get("VH"));
		assertEquals(1, inputs.get("VV").size());
		assertEquals(1, inputs.get("VH").size());
		assertEquals("S1B_IW_RAW__0SVH_20210104T091709_20210104T091800_025002_02F9CE_CDEF.SAFE",
				inputs.get("VH").get(0).getFilename());
		assertEquals("S1B_IW_RAW__0SVV_20210104T091709_20210104T091800_025002_02F9CE_12AF.SAFE",
				inputs.get("VV").get(0).getFilename());

		try {
			uut.validateInputSearch(appDataJob1, taskTableAdapter);
			fail("Missing inputs, exception shall be thrown!");
		} catch (IpfPrepWorkerInputsMissingException missingEx) {
			// Expected
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

		Product product = uut.mainInputSearch(appDataJob1, taskTableAdapter);
		appDataJob1.setProduct(product.toProduct());
		appDataJob1.setAdditionalInputs(product.overridingInputs());

		Map<String, List<AppDataJobFile>> inputs = appDataJob1.getProduct().getInputs();

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
			uut.validateInputSearch(appDataJob1, taskTableAdapter);
			fail("Missing inputs, exception shall be thrown!");
		} catch (IpfPrepWorkerInputsMissingException missingEx) {
			// Expected
		}

		doReturn(
				Arrays.asList(metadata_IW_VV_02F9CE_FULL_2, metadata_IW_VH_02F9CE_PART_1, metadata_IW_VH_02F9CE_FULL_1))
						.when(metadataClient).getLevelSegments("02F9CE");

		product = uut.mainInputSearch(appDataJob1, taskTableAdapter);
		appDataJob1.setProduct(product.toProduct());
		appDataJob1.setAdditionalInputs(product.overridingInputs());

		inputs = appDataJob1.getProduct().getInputs();

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
			uut.validateInputSearch(appDataJob1, taskTableAdapter);
		} catch (IpfPrepWorkerInputsMissingException missingEx) {
			fail("All necessary inputs shall be provided, selection logic have a bug!");
		}
	}

	/**
	 * Scenario 4:
	 * 
	 * RFC production when only older and newer complete/full segments are available
	 * 
	 * @throws MetadataQueryException
	 * @throws IpfPrepWorkerInputsMissingException
	 */
	@Test
	public void testRFProductionWithOlderAndNewerFullSegments()
			throws MetadataQueryException, IpfPrepWorkerInputsMissingException {

		Instant insertionTime = Instant.now();

		String metadataInsertionTime1 = toMetadataDateFormat(insertionTime);
		String metadataInsertionTime2 = toMetadataDateFormat(insertionTime.plusSeconds(130));

		metadata_RF_VV_043734_FULL_1.setInsertionTime(metadataInsertionTime1);
		metadata_RF_VV_043734_FULL_2.setInsertionTime(metadataInsertionTime2);
		metadata_RF_VH_043734_FULL_1.setInsertionTime(metadataInsertionTime1);
		metadata_RF_VH_043734_FULL_2.setInsertionTime(metadataInsertionTime2);

		AppDataJob appDataJob1 = new AppDataJob(123L);
		appDataJob1.setCreationDate(Date.from(insertionTime.plusSeconds(3)));
		AppDataJobProduct product1 = new AppDataJobProduct();
		product1.getMetadata().put("startTime", "2021-01-04T07:19:10.000000Z");
		product1.getMetadata().put("productName",
				"S1A_RF_RAW__0SVV_20210104T071910_20210104T071912_035985_043734_C9E3.SAFE");
		product1.getMetadata().put("productType", "RF_RAW__0S");
		product1.getMetadata().put("dataTakeId", "043734");
		appDataJob1.setProduct(product1);

		doReturn(Arrays.asList(metadata_RF_VV_043734_FULL_1, metadata_RF_VV_043734_FULL_2)).when(metadataClient)
				.getLevelSegments("043734");

		Product product = uut.mainInputSearch(appDataJob1, taskTableAdapter);
		appDataJob1.setProduct(product.toProduct());
		appDataJob1.setAdditionalInputs(product.overridingInputs());

		Map<String, List<AppDataJobFile>> inputs = appDataJob1.getProduct().getInputs();

		assertEquals(1, inputs.size());
		assertNotNull(inputs.get("VV"));
		assertEquals(1, inputs.get("VV").size());
		assertEquals("S1A_RF_RAW__0SVV_20210104T071910_20210104T071912_035985_043734_C9E3.SAFE",
				inputs.get("VV").get(0).getFilename());

		try {
			uut.validateInputSearch(appDataJob1, taskTableAdapter);
			fail("Missing inputs, exception shall be thrown!");
		} catch (DiscardedException discardedEx) {
			// Expected
		}

		doReturn(
				Arrays.asList(metadata_RF_VV_043734_FULL_1, metadata_RF_VV_043734_FULL_2, metadata_RF_VH_043734_FULL_1))
						.when(metadataClient).getLevelSegments("043734");

		product = uut.mainInputSearch(appDataJob1, taskTableAdapter);
		appDataJob1.setProduct(product.toProduct());
		appDataJob1.setAdditionalInputs(product.overridingInputs());

		inputs = appDataJob1.getProduct().getInputs();

		assertEquals(2, inputs.size());
		assertNotNull(inputs.get("VV"));
		assertNotNull(inputs.get("VH"));
		assertEquals(1, inputs.get("VV").size());
		assertEquals(1, inputs.get("VH").size());

		assertEquals("S1A_RF_RAW__0SVH_20210104T071910_20210104T071912_035985_043734_2A76.SAFE",
				inputs.get("VH").get(0).getFilename());
		assertEquals("S1A_RF_RAW__0SVV_20210104T071910_20210104T071912_035985_043734_C9E3.SAFE",
				inputs.get("VV").get(0).getFilename());

		try {
			uut.validateInputSearch(appDataJob1, taskTableAdapter);
		} catch (IpfPrepWorkerInputsMissingException missingEx) {
			fail("All necessary inputs shall be provided, selection logic have a bug!");
		} catch (DiscardedException discardedEx) {
			fail("All necessary inputs shall be provided, selection logic have a bug!");
		}
	}

	/**
	 * Scenario 5:
	 * 
	 * RFC production when only older and newer partial segments are available
	 * 
	 * @throws MetadataQueryException
	 * @throws IpfPrepWorkerInputsMissingException
	 */
	@Test
	public void testRFProductionWithOlderAndNewerPartialSegments()
			throws MetadataQueryException, IpfPrepWorkerInputsMissingException {

		Instant insertionTime = Instant.now();

		String metadataInsertionTime1 = toMetadataDateFormat(insertionTime);
		String metadataInsertionTime2 = toMetadataDateFormat(insertionTime.plusSeconds(130));
		String metadataInsertionTime3 = toMetadataDateFormat(insertionTime.plusSeconds(180));

		metadata_RF_VV_043734_PART_1.setInsertionTime(metadataInsertionTime1);
		metadata_RF_VV_043734_PART_2.setInsertionTime(metadataInsertionTime2);
		metadata_RF_VV_043734_PART_3.setInsertionTime(metadataInsertionTime3);
		metadata_RF_VH_043734_PART_1.setInsertionTime(metadataInsertionTime1);
		metadata_RF_VH_043734_PART_2.setInsertionTime(metadataInsertionTime2);

		AppDataJob appDataJob1 = new AppDataJob(123L);
		appDataJob1.setCreationDate(Date.from(insertionTime.plusSeconds(3)));
		AppDataJobProduct product1 = new AppDataJobProduct();
		product1.getMetadata().put("startTime", "2021-01-04T07:19:10.000000Z");
		product1.getMetadata().put("productName",
				"S1A_RF_RAW__0SVV_20210104T071910_20210104T071911_035985_043734_A9E3.SAFE");
		product1.getMetadata().put("productType", "RF_RAW__0S");
		product1.getMetadata().put("dataTakeId", "043734");
		appDataJob1.setProduct(product1);

		doReturn(Arrays.asList(metadata_RF_VV_043734_PART_1, metadata_RF_VV_043734_PART_2)).when(metadataClient)
				.getLevelSegments("043734");

		Product product = uut.mainInputSearch(appDataJob1, taskTableAdapter);
		appDataJob1.setProduct(product.toProduct());
		appDataJob1.setAdditionalInputs(product.overridingInputs());

		Map<String, List<AppDataJobFile>> inputs = appDataJob1.getProduct().getInputs();

		assertEquals(1, inputs.size());
		assertNotNull(inputs.get("VV"));
		assertEquals(1, inputs.get("VV").size());
		assertEquals("S1A_RF_RAW__0SVV_20210104T071910_20210104T071911_035985_043734_B9E3.SAFE",
				inputs.get("VV").get(0).getFilename());

		try {
			uut.validateInputSearch(appDataJob1, taskTableAdapter);
			fail("Missing inputs, exception shall be thrown!");
		} catch (DiscardedException discardedEx) {
			// Expected
		}

		doReturn(
				Arrays.asList(metadata_RF_VV_043734_PART_1, metadata_RF_VV_043734_PART_2, metadata_RF_VV_043734_PART_3))
						.when(metadataClient).getLevelSegments("043734");

		product = uut.mainInputSearch(appDataJob1, taskTableAdapter);
		appDataJob1.setProduct(product.toProduct());
		appDataJob1.setAdditionalInputs(product.overridingInputs());

		inputs = appDataJob1.getProduct().getInputs();

		assertEquals(1, inputs.size());
		assertNotNull(inputs.get("VV"));
		assertEquals(1, inputs.get("VV").size());
		assertEquals("S1A_RF_RAW__0SVV_20210104T071910_20210104T071911_035985_043734_B9E3.SAFE",
				inputs.get("VV").get(0).getFilename());

		try {
			uut.validateInputSearch(appDataJob1, taskTableAdapter);
			fail("Missing inputs, exception shall be thrown!");
		} catch (DiscardedException discardedEx) {
			// Expected
		}

		doReturn(Arrays.asList(metadata_RF_VV_043734_PART_1, metadata_RF_VV_043734_PART_2, metadata_RF_VV_043734_PART_3,
				metadata_RF_VH_043734_PART_1, metadata_RF_VH_043734_PART_2)).when(metadataClient)
						.getLevelSegments("043734");

		product = uut.mainInputSearch(appDataJob1, taskTableAdapter);
		appDataJob1.setProduct(product.toProduct());
		appDataJob1.setAdditionalInputs(product.overridingInputs());

		inputs = appDataJob1.getProduct().getInputs();

		assertEquals(2, inputs.size());
		assertNotNull(inputs.get("VV"));
		assertNotNull(inputs.get("VH"));
		assertEquals(1, inputs.get("VV").size());
		assertEquals(1, inputs.get("VH").size());
		assertEquals("S1A_RF_RAW__0SVV_20210104T071910_20210104T071911_035985_043734_B9E3.SAFE",
				inputs.get("VV").get(0).getFilename());
		assertEquals("S1A_RF_RAW__0SVH_20210104T071910_20210104T071911_035985_043734_4B76.SAFE",
				inputs.get("VH").get(0).getFilename());

		try {
			uut.validateInputSearch(appDataJob1, taskTableAdapter);
		} catch (IpfPrepWorkerInputsMissingException missingEx) {
			fail("All necessary inputs shall be provided, selection logic have a bug!");
		} catch (DiscardedException discardedEx) {
			fail("All necessary inputs shall be provided, selection logic have a bug!");
		}

	}

	/**
	 * Scenario 6:
	 * 
	 * RFC production when older and newer complete/full and partial segments are
	 * available
	 * 
	 * @throws MetadataQueryException
	 * @throws IpfPrepWorkerInputsMissingException
	 */
	@Test
	public void testRFProductionWithOlderAndNewerFullAndPartialSegments()
			throws MetadataQueryException, IpfPrepWorkerInputsMissingException {

		Instant insertionTime = Instant.now();

		String metadataInsertionTime1 = toMetadataDateFormat(insertionTime);
		String metadataInsertionTime2 = toMetadataDateFormat(insertionTime.plusSeconds(130));
		String metadataInsertionTime3 = toMetadataDateFormat(insertionTime.plusSeconds(180));

		metadata_RF_VV_043734_PART_3.setInsertionTime(metadataInsertionTime1);
		metadata_RF_VV_043734_FULL_1.setInsertionTime(metadataInsertionTime2);
		metadata_RF_VV_043734_FULL_2.setInsertionTime(metadataInsertionTime3);
		metadata_RF_VH_043734_FULL_1.setInsertionTime(metadataInsertionTime1);
		metadata_RF_VH_043734_PART_2.setInsertionTime(metadataInsertionTime2);

		AppDataJob appDataJob1 = new AppDataJob(123L);
		appDataJob1.setCreationDate(Date.from(insertionTime.plusSeconds(3)));
		AppDataJobProduct product1 = new AppDataJobProduct();
		product1.getMetadata().put("startTime", "2021-01-04T07:19:20.000000Z");
		product1.getMetadata().put("productName",
				"S1A_RF_RAW__0SVV_20210104T071920_20210104T071921_035985_043734_75C7.SAFE");
		product1.getMetadata().put("productType", "RF_RAW__0S");
		product1.getMetadata().put("dataTakeId", "043734");
		appDataJob1.setProduct(product1);

		doReturn(Arrays.asList(metadata_RF_VV_043734_PART_3, metadata_RF_VV_043734_FULL_1, metadata_RF_VV_043734_FULL_2,
				metadata_RF_VH_043734_FULL_1, metadata_RF_VH_043734_PART_2)).when(metadataClient)
						.getLevelSegments("043734");

		Product product = uut.mainInputSearch(appDataJob1, taskTableAdapter);
		appDataJob1.setProduct(product.toProduct());
		appDataJob1.setAdditionalInputs(product.overridingInputs());

		Map<String, List<AppDataJobFile>> inputs = appDataJob1.getProduct().getInputs();

		assertEquals(2, inputs.size());
		assertNotNull(inputs.get("VV"));
		assertNotNull(inputs.get("VH"));
		assertEquals(1, inputs.get("VV").size());
		assertEquals(1, inputs.get("VH").size());
		assertEquals("S1A_RF_RAW__0SVV_20210104T071920_20210104T071922_035985_043734_72C7.SAFE",
				inputs.get("VV").get(0).getFilename());
		assertEquals("S1A_RF_RAW__0SVH_20210104T071920_20210104T071921_035985_043734_EE1D.SAFE",
				inputs.get("VH").get(0).getFilename());

		try {
			uut.validateInputSearch(appDataJob1, taskTableAdapter);
		} catch (IpfPrepWorkerInputsMissingException missingEx) {
			fail("All necessary inputs shall be provided, selection logic have a bug!");
		} catch (DiscardedException discardedEx) {
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
