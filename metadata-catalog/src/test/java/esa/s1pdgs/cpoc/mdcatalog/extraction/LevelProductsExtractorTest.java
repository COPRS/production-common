package esa.s1pdgs.cpoc.mdcatalog.extraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mdcatalog.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L0OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L1OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L2OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.obs.ObsService;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;

public class LevelProductsExtractorTest {

	/**
	 * Elasticsearch services
	 */
	@Mock
	protected EsServices esServices;

	/**
	 * Elasticsearch services
	 */
	@Mock
	protected ObsService obsService;

	/**
	 * MQI service
	 */
	@Mock
	private GenericMqiService<LevelProductDto> mqiService;

	/**
	 * 
	 */
	@Mock
	protected MetadataExtractorConfig extractorConfig;

	/**
	 * Application status
	 */
	@Mock
	protected AppStatus appStatus;

	/**
	 * Extractor
	 */
	protected LevelProductsExtractor extractor;

	/**
	 * Job to process
	 */
	private GenericMessageDto<LevelProductDto> inputMessage;

	/**
	 * Job to process
	 */
	private GenericMessageDto<LevelProductDto> inputMessageSafe;

	/**
	 * Job to process
	 */
	private GenericMessageDto<LevelProductDto> inputMessageAux;
	
    private final ErrorRepoAppender errorAppender = ErrorRepoAppender.NULL;
    
    private final ProcessConfiguration config = new ProcessConfiguration();

	/**
	 * Initialization
	 * 
	 * @throws AbstractCodedException
	 */
	@Before
	public void init() throws AbstractCodedException {
		MockitoAnnotations.initMocks(this);

		// "EW:8.2F||IW:7.4F||SM:7.7F||WM:0.0F"
		Map<String, Float> typeOverlap = new HashMap<String, Float>();
		typeOverlap.put("EW", 8.2F);
		typeOverlap.put("IW", 7.4F);
		typeOverlap.put("SM", 7.7F);
		typeOverlap.put("WM", 0.0F);
		// "EW:60.0F||IW:25.0F||SM:25.0F||WM:0.0F"
		Map<String, Float> typeSliceLength = new HashMap<String, Float>();
		typeSliceLength.put("EW", 60.0F);
		typeSliceLength.put("IW", 25.0F);
		typeSliceLength.put("SM", 25.0F);
		typeSliceLength.put("WM", 0.0F);
		doReturn("config/xsltDir/").when(extractorConfig).getXsltDirectory();
		doReturn(typeOverlap).when(extractorConfig).getTypeOverlap();
		doReturn(typeSliceLength).when(extractorConfig).getTypeSliceLength();

		doNothing().when(appStatus).setError(Mockito.any(), Mockito.anyString());
		doReturn(true).when(mqiService).ack(Mockito.any());

		inputMessage = new GenericMessageDto<LevelProductDto>(123, "",
				new LevelProductDto("product-name", "key-obs", ProductFamily.L0_ACN, "NRT"));

		inputMessageSafe = new GenericMessageDto<LevelProductDto>(123, "",
				new LevelProductDto("S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE",
						"S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE", ProductFamily.L0_ACN, "NRT"));

		inputMessageAux = new GenericMessageDto<LevelProductDto>(123, "",
				new LevelProductDto("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
						"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml", ProductFamily.L0_ACN, "NRT"));

		extractor = new LevelProductsExtractor(esServices, obsService, mqiService, appStatus, extractorConfig,
				(new File("./test/workDir/")).getAbsolutePath() + File.separator, "manifest.safe", errorAppender, config, ".safe");
	}

	@Test
	public void testGetKeyObs() {
		assertEquals("key-obs", extractor.getKeyObs(inputMessage));
		assertEquals("S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE/manifest.safe",
				extractor.getKeyObs(inputMessageSafe));
	}

	@Test
	public void testExtractProductName() {
		assertEquals("product-name", extractor.extractProductNameFromDto(inputMessage.getBody()));
	}

	@Test
	public void testCleanProcessing() throws IOException {
		File newWorkDir = new File("./test/workDir2");
		newWorkDir.mkdirs();

		(new File("./test/workDir2/S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE")).mkdirs();
		(new File("./test/workDir2/S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE/manifest.safe")).createNewFile();
		(new File("./test/workDir2/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml")).createNewFile();

		extractor = new LevelProductsExtractor(esServices, obsService, mqiService, appStatus, extractorConfig,
				"./test/workDir2/", "manifest.safe", errorAppender, config,".safe");
		assertTrue((new File("./test/workDir2/S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE")).exists());
		assertTrue((new File("./test/workDir2/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml")).exists());

		extractor.cleanProcessing(inputMessage);
		assertTrue((new File("./test/workDir2/S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE")).exists());
		assertTrue((new File("./test/workDir2/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml")).exists());

		extractor.cleanProcessing(inputMessageSafe);
		assertFalse((new File("./test/workDir2/S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE")).exists());
		assertTrue((new File("./test/workDir2/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml")).exists());

		extractor.cleanProcessing(inputMessageAux);
		assertFalse((new File("./test/workDir2/S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE")).exists());
		assertFalse((new File("./test/workDir2/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml")).exists());

		FileUtils.delete("./test/workDir2");
	}

	@Test
	public void testExtractMetadataL0Slice() throws MetadataExtractionException, AbstractCodedException {

		File file = new File((new File("./test/workDir/")).getAbsolutePath() + File.separator
				+ "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE" + File.separator
				+ "manifest.safe");

		inputMessageSafe = new GenericMessageDto<LevelProductDto>(123, "",
				new LevelProductDto("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
						"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
						ProductFamily.L0_SLICE, "NRT"));

		doReturn(file).when(obsService).downloadFile(Mockito.any(), Mockito.anyString(), Mockito.anyString());

		L0OutputFileDescriptor descriptor = new L0OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
		descriptor.setRelativePath("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
		descriptor.setSwathtype("IW");
		descriptor.setResolution("_");
		descriptor.setProductClass("S");
		descriptor.setProductType("IW_RAW__0S");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("021735");
		descriptor.setProductFamily(ProductFamily.L0_SLICE);

		JSONObject expected = extractor.mdBuilder.buildL0SliceOutputFileMetadata(descriptor, file);

		final LoggerReporting.Factory reportingFactory = new LoggerReporting.Factory(
				LogManager.getLogger(GenericExtractorTest.class), "TestMetadataExtraction").product(
						ProductFamily.L0_SLICE.toString(),
						"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");

		JSONObject result = extractor.extractMetadata(reportingFactory, inputMessageSafe);
		for (String key : expected.keySet()) {
			if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) || "creationTime".equals(key))) {
				assertEquals(expected.get(key), result.get(key));
			}
		}

		verify(obsService, times(1)).downloadFile(Mockito.eq(ProductFamily.L0_SLICE),
				Mockito.eq("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe"),
				Mockito.eq(extractor.localDirectory));

	}

	@Test
	public void testExtractMetadataL0Acn() throws MetadataExtractionException, AbstractCodedException {

		File file = new File((new File("./test/workDir/")).getAbsolutePath() + File.separator
				+ "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE" + File.separator
				+ "manifest.safe");

		inputMessageSafe = new GenericMessageDto<LevelProductDto>(123, "",
				new LevelProductDto("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
						"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
						ProductFamily.L0_ACN, "NRT"));

		doReturn(file).when(obsService).downloadFile(Mockito.any(), Mockito.anyString(), Mockito.anyString());

		L0OutputFileDescriptor descriptor = new L0OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
		descriptor.setRelativePath("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
		descriptor.setSwathtype("IW");
		descriptor.setResolution("_");
		descriptor.setProductClass("S");
		descriptor.setProductType("IW_RAW__0S");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("021735");
		descriptor.setProductFamily(ProductFamily.L0_SLICE);

		JSONObject expected = extractor.mdBuilder.buildL0SliceOutputFileMetadata(descriptor, file);
		final LoggerReporting.Factory reportingFactory = new LoggerReporting.Factory(
				LogManager.getLogger(GenericExtractorTest.class), "TestMetadataExtraction").product(
						ProductFamily.L0_ACN.toString(),
						"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");

		JSONObject result = extractor.extractMetadata(reportingFactory, inputMessageSafe);
		for (String key : expected.keySet()) {
			if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) || "creationTime".equals(key))) {
				assertEquals(expected.get(key), result.get(key));
			}
		}

		verify(obsService, times(1)).downloadFile(Mockito.eq(ProductFamily.L0_ACN),
				Mockito.eq("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe"),
				Mockito.eq(extractor.localDirectory));

	}

	@Test
	public void testExtractMetadataL1Slice() throws MetadataExtractionException, AbstractCodedException {

		File file = new File((new File("./test/workDir/")).getAbsolutePath() + File.separator
				+ "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE" + File.separator
				+ "manifest.safe");

		inputMessageSafe = new GenericMessageDto<LevelProductDto>(123, "",
				new LevelProductDto("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
						"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
						ProductFamily.L1_SLICE, "NRT"));

		doReturn(file).when(obsService).downloadFile(Mockito.any(), Mockito.anyString(), Mockito.anyString());

		L1OutputFileDescriptor descriptor = new L1OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
		descriptor.setKeyObjectStorage("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
		descriptor.setRelativePath("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
		descriptor.setSwathtype("IW");
		descriptor.setResolution("_");
		descriptor.setProductClass("S");
		descriptor.setProductType("IW_RAW__0S");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("021735");
		descriptor.setProductFamily(ProductFamily.L1_SLICE);

		JSONObject expected = extractor.mdBuilder.buildL1SliceOutputFileMetadata(descriptor, file);
		final LoggerReporting.Factory reportingFactory = new LoggerReporting.Factory(
				LogManager.getLogger(GenericExtractorTest.class), "TestMetadataExtraction").product(
						ProductFamily.L1_SLICE.toString(),
						"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");

		JSONObject result = extractor.extractMetadata(reportingFactory, inputMessageSafe);
		for (String key : expected.keySet()) {
			if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) || "creationTime".equals(key))) {
				assertEquals(expected.get(key), result.get(key));
			}
		}

		verify(obsService, times(1)).downloadFile(Mockito.eq(ProductFamily.L1_SLICE),
				Mockito.eq("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe"),
				Mockito.eq(extractor.localDirectory));

	}

	@Test
	public void testExtractMetadataL1Acn() throws MetadataExtractionException, AbstractCodedException {

		String l1acnName = "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE";

		File file = new File((new File("./test/workDir/")).getAbsolutePath() + File.separator + l1acnName
				+ File.separator + "manifest.safe");

		inputMessageSafe = new GenericMessageDto<LevelProductDto>(123, "",
				new LevelProductDto(l1acnName, l1acnName, ProductFamily.L1_ACN, "NRT"));

		doReturn(file).when(obsService).downloadFile(Mockito.any(), Mockito.anyString(), Mockito.anyString());

		L1OutputFileDescriptor descriptor = new L1OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename(l1acnName);
		descriptor.setKeyObjectStorage(l1acnName);
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName(l1acnName);
		descriptor.setRelativePath(l1acnName);
		descriptor.setSwathtype("IW");
		descriptor.setResolution("_");
		descriptor.setProductClass("A");
		descriptor.setProductType("IW_RAW__0A");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("021735");
		descriptor.setProductFamily(ProductFamily.L1_ACN);

		JSONObject expected = extractor.mdBuilder.buildL1AcnOutputFileMetadata(descriptor, file);

		final LoggerReporting.Factory reportingFactory = new LoggerReporting.Factory(
				LogManager.getLogger(GenericExtractorTest.class), "TestMetadataExtraction")
						.product(ProductFamily.L1_ACN.toString(), l1acnName);

		JSONObject result = extractor.extractMetadata(reportingFactory, inputMessageSafe);
		for (String key : expected.keySet()) {
			if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) || "creationTime".equals(key))) {
				assertEquals(expected.get(key), result.get(key));
			}
		}

		verify(obsService, times(1)).downloadFile(Mockito.eq(ProductFamily.L1_ACN),
				Mockito.eq(l1acnName + "/manifest.safe"), Mockito.eq(extractor.localDirectory));

	}

	@Test
	public void testExtractMetadataL2Slice() throws AbstractCodedException {

		String l2SliceName = "S1A_WV_OCN__2SSV_20190518T160559_20190518T161434_027284_0313A0_46F2.SAFE";

		File file = new File((new File("./test/workDir/")).getAbsolutePath() + File.separator + l2SliceName
				+ File.separator + "manifest.safe");

		inputMessageSafe = new GenericMessageDto<LevelProductDto>(123, "",
				new LevelProductDto(l2SliceName, l2SliceName, ProductFamily.L2_SLICE, "NRT"));

		doReturn(file).when(obsService).downloadFile(Mockito.any(), Mockito.anyString(), Mockito.anyString());

		L2OutputFileDescriptor descriptor = new L2OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename(l2SliceName);
		descriptor.setKeyObjectStorage(l2SliceName);
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName(l2SliceName);
		descriptor.setRelativePath(l2SliceName);
		descriptor.setSwathtype("WV");
		descriptor.setResolution("_");
		descriptor.setProductClass("S");
		descriptor.setProductType("WV_OCN__2S");
		descriptor.setPolarisation("SV");
		descriptor.setDataTakeId("0313A0");
		descriptor.setProductFamily(ProductFamily.L2_SLICE);

		JSONObject expected = extractor.mdBuilder.buildL2SliceOutputFileMetadata(descriptor, file);
		final LoggerReporting.Factory reportingFactory = new LoggerReporting.Factory(
				LogManager.getLogger(GenericExtractorTest.class), "TestMetadataExtraction")
						.product(ProductFamily.L2_SLICE.toString(), l2SliceName);

		JSONObject result = extractor.extractMetadata(reportingFactory, inputMessageSafe);
		for (String key : expected.keySet()) {
			if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) || "creationTime".equals(key))) {
				assertEquals(expected.get(key), result.get(key));
			}
		}

		verify(obsService, times(1)).downloadFile(Mockito.eq(ProductFamily.L2_SLICE),
				Mockito.eq(l2SliceName + "/manifest.safe"), Mockito.eq(extractor.localDirectory));

	}

	@Test
	public void testExtractMetadataL2Acn() throws AbstractCodedException {

		String l2acnName = "S1A_WV_OCN__2ASV_20190518T160559_20190518T161434_027284_0313A0_2960.SAFE";

		File file = new File((new File("./test/workDir/")).getAbsolutePath() + File.separator + l2acnName
				+ File.separator + "manifest.safe");

		inputMessageSafe = new GenericMessageDto<LevelProductDto>(123, "",
				new LevelProductDto(l2acnName, l2acnName, ProductFamily.L2_ACN, "NRT"));

		doReturn(file).when(obsService).downloadFile(Mockito.any(), Mockito.anyString(), Mockito.anyString());

		L2OutputFileDescriptor descriptor = new L2OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename(l2acnName);
		descriptor.setKeyObjectStorage(l2acnName);
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName(l2acnName);
		descriptor.setRelativePath(l2acnName);
		descriptor.setSwathtype("WV");
		descriptor.setResolution("_");
		descriptor.setProductClass("A");
		descriptor.setProductType("WV_OCN__2A");
		descriptor.setPolarisation("SV");
		descriptor.setDataTakeId("0313A0");
		descriptor.setProductFamily(ProductFamily.L2_ACN);

		JSONObject expected = extractor.mdBuilder.buildL2AcnOutputFileMetadata(descriptor, file);

		final LoggerReporting.Factory reportingFactory = new LoggerReporting.Factory(
				LogManager.getLogger(GenericExtractorTest.class), "TestMetadataExtraction")
						.product(ProductFamily.L2_ACN.toString(), l2acnName);

		JSONObject result = extractor.extractMetadata(reportingFactory, inputMessageSafe);
		for (String key : expected.keySet()) {
			if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) || "creationTime".equals(key))) {
				assertEquals(expected.get(key), result.get(key));
			}
		}

		verify(obsService, times(1)).downloadFile(Mockito.eq(ProductFamily.L2_ACN),
				Mockito.eq(l2acnName + "/manifest.safe"), Mockito.eq(extractor.localDirectory));
	}

}
