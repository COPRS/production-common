package esa.s1pdgs.cpoc.mdcatalog.extraction.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.ConfigFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.xml.XmlConverter;

public class MetadataBuilderTest {

	@Mock
	private ExtractMetadata extractor;

	@Mock
    XmlConverter xmlConverter;
	
	private static final String LOCAL_DIRECTORY = "/tmp";

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	@Test
	public void testBuildConfigFileMetadataXml() throws JSONException, MetadataExtractionException, MetadataMalformedException {
		
		ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
		descriptor.setExtension(FileExtension.XML);
		descriptor.setFilename("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setKeyObjectStorage("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setProductType("AUX_OBMEMC");
		descriptor.setRelativePath("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

		JSONObject expectedResult = new JSONObject("{\"productName\": \"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml\"}");

		Mockito.doAnswer(i ->expectedResult)
			.when(extractor).processXMLFile(Mockito.any(ConfigFileDescriptor.class), Mockito.any(File.class));

		File file = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

		try {
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor, xmlConverter, LOCAL_DIRECTORY);
			JSONObject dto = metadataBuilder.buildConfigFileMetadata(descriptor, file);

			assertNotNull("Metadata should not be null", dto);
			assertEquals("Metadata are not equals", expectedResult.toString(), dto.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testBuildConfigFileMetadataEOF() throws JSONException, MetadataExtractionException, MetadataMalformedException {

		ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
		descriptor.setExtension(FileExtension.EOF);
		descriptor.setFilename("S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");
		descriptor.setKeyObjectStorage("S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");
		descriptor.setProductType("MPL_ORBSCT");
		descriptor.setRelativePath("S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");

		JSONObject expectedResult = new JSONObject("{\"validityStopTime\":\"9999-12-31T23:59:59\",\"productClass\":\"OPER\",\"missionid\":\"S1\",\"creationTime\":\"2017-01-23T16:38:09\",\"insertionTime\":\"2018-05-30T11:40:06\",\"satelliteid\":\"A\",\"validityStartTime\":\"2014-04-03T22:46:09\",\"version\":\"0020\",\"productName\":\"S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF\",\"productType\":\"MPL_ORBSCT\"}");
		
		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processEOFFile(Mockito.any(ConfigFileDescriptor.class), Mockito.any(File.class));

		File file = new File("workDir/S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");

		try {
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor, xmlConverter, LOCAL_DIRECTORY);
			JSONObject dto = metadataBuilder.buildConfigFileMetadata(descriptor, file);
			assertNotNull("Metadata should not be null", dto);
			assertEquals("Metadata are not equals", expectedResult.toString(), dto.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testBuildConfigFileMetadataAUXRESORB() throws JSONException, MetadataExtractionException, MetadataMalformedException {

		ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
		descriptor.setExtension(FileExtension.EOF);
		descriptor.setFilename("S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");
		descriptor.setKeyObjectStorage("S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");
		descriptor.setProductType("AUX_RESORB");
		descriptor.setRelativePath("S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");

		JSONObject expectedResult = new JSONObject("{\"validityStopTime\":\"2017-12-13T13:45:07\",\"productClass\":\"OPER\",\"missionid\":\"S1\",\"creationTime\":\"2017-12-13T14:38:38\",\"insertionTime\":\"2018-05-30T11:40:06\",\"satelliteid\":\"A\",\"validityStartTime\":\"2017-12-13T10:27:37\",\"version\":\"0001\",\"productType\":\"AUX_RESORB\",\"productName\":\"S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF\"}");

		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processEOFFileWithoutNamespace(Mockito.any(ConfigFileDescriptor.class),Mockito.any(File.class));

		File file = new File("workDir/S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");

		try {
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor, xmlConverter, LOCAL_DIRECTORY);
			JSONObject dto = metadataBuilder.buildConfigFileMetadata(descriptor, file);
			
			assertNotNull("Metadata should not be null", dto);
			assertEquals("Metadata are not equals", expectedResult.toString(), dto.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testBuildConfigFileMetadataSAFE() throws JSONException, MetadataExtractionException, MetadataMalformedException {

		ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");
		descriptor.setKeyObjectStorage("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");
		descriptor.setProductType("AUX_INS");
		descriptor.setRelativePath("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");

		JSONObject expectedResult = new JSONObject("{\"validityStopTime\":\"9999-12-31T23:59:59\",\"site\":\"CLS-Brest\",\"missionid\":\"S1\",\"creationTime\":\"2017-10-13T10:12:16.000000\",\"insertionTime\":\"2018-05-30T11:40:06\",\"satelliteid\":\"A\",\"instrumentConfigurationId\":\"6\",\"validityStartTime\":\"2017-10-17T08:00:00.000000\",\"productName\":\"S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE\",\"productType\":\"AUX_INS\"}");

		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processSAFEFile(Mockito.any(ConfigFileDescriptor.class), Mockito.any(File.class));


		File file = new File("workDir/S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/manifest.safe");

		try {
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor, xmlConverter, LOCAL_DIRECTORY);
			JSONObject dto = metadataBuilder.buildConfigFileMetadata(descriptor, file);

			assertNotNull("Metadata should not be null", dto);
			assertEquals("Metadata are not equals", expectedResult.toString(), dto.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test
	public void testBuildConfigFileMetadataInvalidExtension() throws MetadataExtractionException, MetadataMalformedException {

		ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
		descriptor.setFilename("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setKeyObjectStorage("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setProductType("AUX_OBMEMC");
		descriptor.setRelativePath("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

		File file = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

		try {
			descriptor.setExtension(FileExtension.DAT);
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor, xmlConverter, LOCAL_DIRECTORY);
			metadataBuilder.buildConfigFileMetadata(descriptor, file);
			fail("An exception should occur");
		} catch (MetadataExtractionException fe) {
			assertEquals("Raised exception shall concern S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
					AbstractCodedException.ErrorCode.METADATA_EXTRACTION_ERROR, fe.getCode());
		}

		try {
			descriptor.setExtension(FileExtension.RAW);
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor, xmlConverter, LOCAL_DIRECTORY);
			metadataBuilder.buildConfigFileMetadata(descriptor, file);
			fail("An exception should occur");
		} catch (MetadataExtractionException fe) {
			assertEquals("Raised exception shall concern S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
			        AbstractCodedException.ErrorCode.METADATA_EXTRACTION_ERROR, fe.getCode());
		}

		try {
			descriptor.setExtension(FileExtension.XSD);
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor, xmlConverter, LOCAL_DIRECTORY);
			metadataBuilder.buildConfigFileMetadata(descriptor, file);
			fail("An exception should occur");
		} catch (MetadataExtractionException fe) {
			assertEquals("Raised exception shall concern S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
			        AbstractCodedException.ErrorCode.METADATA_EXTRACTION_ERROR, fe.getCode());
		}

		try {
			descriptor.setExtension(FileExtension.UNKNOWN);
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor, xmlConverter, LOCAL_DIRECTORY);
			metadataBuilder.buildConfigFileMetadata(descriptor, file);
			fail("An exception should occur");
		} catch (MetadataExtractionException fe) {
			assertEquals("Raised exception shall concern S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
			        AbstractCodedException.ErrorCode.METADATA_EXTRACTION_ERROR, fe.getCode());
		}
	}

	@Test
	public void testBuildSessionMetadataRaw() throws JSONException, MetadataExtractionException {
		// Mock the extractor
		JSONObject expectedResult = new JSONObject(
				"{\"insertionTime\":\"2018-02-07T13:26:12\",\"missionId\":\"S1\",\"sessionId\":\"707000180\",\"productName\":\"DCS_02_L20171109175634707000180_ch1_DSDB_00001.raw\",\"satelliteId\":\"A\",\"productType\":\"RAW\",\"url\":\"SESSION1/DCS_02_SESSION1_ch1_DSIB.xml\"}");
		
		Mockito.doAnswer(i ->expectedResult)
			.when(extractor).processRAWFile(Mockito.any(EdrsSessionFileDescriptor.class));

		// Build the parameters
		EdrsSessionFileDescriptor descriptor = new EdrsSessionFileDescriptor();
		descriptor.setExtension(FileExtension.RAW);
		descriptor.setFilename("DCS_02_L20171109175634707000180_ch1_DSDB_00001.raw");
		descriptor.setKeyObjectStorage("S1A/707000180/ch01/DCS_02_L20171109175634707000180_ch1_DSDB_00001.raw");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("DCS_02_L20171109175634707000180_ch1_DSDB_00001.raw");
		descriptor.setEdrsSessionFileType(EdrsSessionFileType.RAW);
		descriptor.setRelativePath("S1A/707000180/ch01/DCS_02_L20171109175634707000180_ch1_DSDB_00001.raw");
		descriptor.setChannel(1);
		descriptor.setSessionIdentifier("707000180");

		try {
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor, xmlConverter, LOCAL_DIRECTORY);
			JSONObject dto = metadataBuilder.buildEdrsSessionFileMetadata(descriptor);

			assertNotNull("Metadata should not be null", dto);
			assertEquals("Metadata are not equals", expectedResult.toString(), dto.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testBuildSessionMetadataSession() throws JSONException, MetadataExtractionException {
		// Mock the extractor
		JSONObject expectedResult = new JSONObject(
				"{\"insertionTime\":\"2018-02-07T13:26:12\",\"missionId\":\"S1\",\"sessionId\":\"SESSION1\",\"productName\":\"DCS_02_SESSION1_ch1_DSIB.xml\",\"satelliteId\":\"A\",\"productType\":\"SESSION\",\"url\":\"SESSION1/DCS_02_SESSION1_ch1_DSIB.xml\"}");
		
		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processSESSIONFile(Mockito.any(EdrsSessionFileDescriptor.class));
	
		// Build the parameters
		EdrsSessionFileDescriptor descriptor = new EdrsSessionFileDescriptor();
		descriptor.setExtension(FileExtension.XML);
		descriptor.setFilename("DCS_02_SESSION1_ch1_DSIB.xml");
		descriptor.setKeyObjectStorage("S1A/SESSION1/ch01/DCS_02_SESSION1_ch1_DSIB.xml");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("DCS_02_SESSION1_ch1_DSIB.xml");
		descriptor.setEdrsSessionFileType(EdrsSessionFileType.SESSION);
		descriptor.setRelativePath("S1A/SESSION1/ch01/DCS_02_SESSION1_ch1_DSIB.xml");
		descriptor.setChannel(1);
		descriptor.setSessionIdentifier("SESSION1");

		try {
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor, xmlConverter, LOCAL_DIRECTORY);
			JSONObject dto = metadataBuilder.buildEdrsSessionFileMetadata(descriptor);

			assertNotNull("Metadata should not be null", dto);
			assertEquals("Metadata are not equals", expectedResult.toString(), dto.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testbuildL0SliceOutputFileMetadata() throws JSONException, MetadataExtractionException, MetadataMalformedException {
		// Mock the extractor
		JSONObject expectedResult = new JSONObject(
				"{\"missionDataTakeId\":\"137013\",\"theoreticalSliceLength\":\"25\",\"sliceCoordinates\":{\"coordinates\":[[[86.8273,36.7787],[86.4312,38.7338],[83.6235,38.4629],[84.0935,36.5091],[86.8273,36.7787]]],\"type\":\"Polygon\"},\"insertionTime\":\"2018-05-30T14:27:43\",\"polarisation\":\"DV\",\"sliceNumber\":\"13\",\"absoluteStopOrbit\":\"19684\",\"resolution\":\"_\",\"circulationFlag\":\"13\",\"productName\":\"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE\",\"dataTakeId\":\"021735\",\"productConsolidation\":\"SLICE\",\"absoluteStartOrbit\":\"19684\",\"instrumentConfigurationId\":\"6\",\"relativeStopOrbit\":\"12\",\"relativeStartOrbit\":\"12\",\"startTime\":\"2017-12-13T12:16:23.685188Z\",\"stopTime\":\"2017-12-13T12:16:56.085136Z\",\"productType\":\"IW_RAW__0S\",\"productClass\":\"S\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":628491.556,\"sliceOverlap\":\"7.4\",\"startTimeANX\":\"596091.6080\"}");
		
		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processProduct(Mockito.any(OutputFileDescriptor.class), Mockito.eq(ProductFamily.L0_SLICE), Mockito.any(File.class));

		// Build the parameters
		OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");
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
		
		File file = new File("workDir/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");

		try {
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor, xmlConverter, LOCAL_DIRECTORY);
			JSONObject dto = metadataBuilder.buildOutputFileMetadata(descriptor, file, ProductFamily.L0_SLICE);

			assertNotNull("Metadata should not be null", dto);
			assertEquals("Metadata are not equals", expectedResult.toString(), dto.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testbuildL0ACNOutputFileMetadata() throws JSONException, MetadataExtractionException, MetadataMalformedException {
		// Mock the extractor
		JSONObject expectedResult = new JSONObject(
				"{\"missionDataTakeId\":\"137013\",\"totalNumberOfSlice\":20.159704,\"sliceCoordinates\":{\"coordinates\":[[[90.3636,18.6541],[84.2062,49.0506],[80.8613,48.7621],[88.0584,18.3765],[90.3636,18.6541]]],\"type\":\"Polygon\"},\"insertionTime\":\"2018-05-30T14:27:43\",\"polarisation\":\"DV\",\"absoluteStopOrbit\":\"19684\",\"resolution\":\"_\",\"circulationFlag\":\"13\",\"productName\":\"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE\",\"dataTakeId\":\"021735\",\"productConsolidation\":\"FULL\",\"absoluteStartOrbit\":\"19684\",\"instrumentConfigurationId\":\"6\",\"relativeStopOrbit\":\"12\",\"relativeStartOrbit\":\"12\",\"startTime\":\"2017-12-13T12:11:23.682488Z\",\"stopTime\":\"2017-12-13T12:19:47.264351Z\",\"productType\":\"IW_RAW__0A\",\"productClass\":\"A\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":799670.769,\"startTimeANX\":\"296088.9120\"}");
		
		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processProduct(Mockito.any(OutputFileDescriptor.class), Mockito.eq(ProductFamily.L0_ACN), Mockito.any(File.class));

		// Build the parameters
		OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE/manifest.safe");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE");
		descriptor.setRelativePath("S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE");
		descriptor.setSwathtype("IW");
		descriptor.setResolution("_");
		descriptor.setProductClass("A");
		descriptor.setProductType("IW_RAW__0A");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("021735");
		
		File file = new File("workDir/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE/manifest.safe");

		try {
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor, xmlConverter, LOCAL_DIRECTORY);
			JSONObject dto = metadataBuilder.buildOutputFileMetadata(descriptor, file, ProductFamily.L0_ACN);

			assertNotNull("Metadata should not be null", dto);
			assertEquals("Metadata are not equals", expectedResult.toString(), dto.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testbuildL1SliceOutputFileMetadata() throws JSONException, MetadataExtractionException, MetadataMalformedException {
		// Mock the extractor
		JSONObject expectedResult = new JSONObject(
				"{\"missionDataTakeId\":\"137013\",\"theoreticalSliceLength\":\"25\",\"sliceCoordinates\":{\"coordinates\":[[[86.8273,36.7787],[86.4312,38.7338],[83.6235,38.4629],[84.0935,36.5091],[86.8273,36.7787]]],\"type\":\"Polygon\"},\"insertionTime\":\"2018-05-30T14:27:43\",\"polarisation\":\"DV\",\"sliceNumber\":\"13\",\"absoluteStopOrbit\":\"19684\",\"resolution\":\"_\",\"circulationFlag\":\"13\",\"productName\":\"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE\",\"dataTakeId\":\"021735\",\"productConsolidation\":\"SLICE\",\"absoluteStartOrbit\":\"19684\",\"instrumentConfigurationId\":\"6\",\"relativeStopOrbit\":\"12\",\"relativeStartOrbit\":\"12\",\"startTime\":\"2017-12-13T12:16:23.685188Z\",\"stopTime\":\"2017-12-13T12:16:56.085136Z\",\"productType\":\"IW_RAW__0S\",\"productClass\":\"S\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":628491.556,\"sliceOverlap\":\"7.4\",\"startTimeANX\":\"596091.6080\"}");
		
		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processProduct(Mockito.any(OutputFileDescriptor.class), Mockito.eq(ProductFamily.L1_SLICE), Mockito.any(File.class));

		// Build the parameters
		OutputFileDescriptor descriptor = new OutputFileDescriptor();
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
		
		File file = new File("workDir/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");

		try {
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor, xmlConverter, LOCAL_DIRECTORY);
			JSONObject dto = metadataBuilder.buildOutputFileMetadata(descriptor, file, ProductFamily.L1_SLICE);

			assertNotNull("Metadata should not be null", dto);
			assertEquals("Metadata are not equals", expectedResult.toString(), dto.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testbuildL1ACNOutputFileMetadata() throws JSONException, MetadataExtractionException, MetadataMalformedException {
		// Mock the extractor
		JSONObject expectedResult = new JSONObject(
				"{\"missionDataTakeId\":\"137013\",\"totalNumberOfSlice\":20.159704,\"sliceCoordinates\":{\"coordinates\":[[[90.3636,18.6541],[84.2062,49.0506],[80.8613,48.7621],[88.0584,18.3765],[90.3636,18.6541]]],\"type\":\"Polygon\"},\"insertionTime\":\"2018-05-30T14:27:43\",\"polarisation\":\"DV\",\"absoluteStopOrbit\":\"19684\",\"resolution\":\"_\",\"circulationFlag\":\"13\",\"productName\":\"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE\",\"dataTakeId\":\"021735\",\"productConsolidation\":\"FULL\",\"absoluteStartOrbit\":\"19684\",\"instrumentConfigurationId\":\"6\",\"relativeStopOrbit\":\"12\",\"relativeStartOrbit\":\"12\",\"startTime\":\"2017-12-13T12:11:23.682488Z\",\"stopTime\":\"2017-12-13T12:19:47.264351Z\",\"productType\":\"IW_RAW__0A\",\"productClass\":\"A\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":799670.769,\"startTimeANX\":\"296088.9120\"}");
		
		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processProduct(Mockito.any(OutputFileDescriptor.class), Mockito.eq(ProductFamily.L1_ACN), Mockito.any(File.class));

		// Build the parameters
		OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE");
		descriptor.setKeyObjectStorage("S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE");
		descriptor.setRelativePath("S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE");
		descriptor.setSwathtype("IW");
		descriptor.setResolution("_");
		descriptor.setProductClass("A");
		descriptor.setProductType("IW_RAW__0A");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("021735");
		
		File file = new File("workDir/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE/manifest.safe");

		try {
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor, xmlConverter, LOCAL_DIRECTORY);
			JSONObject dto = metadataBuilder.buildOutputFileMetadata(descriptor, file, ProductFamily.L1_ACN);

			assertNotNull("Metadata should not be null", dto);
			assertEquals("Metadata are not equals", expectedResult.toString(), dto.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

}
