/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
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
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.AuxDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

public class MetadataBuilderTest {
	@Mock
	private ExtractMetadata extractor;
	
	private final File tmpDir = FileUtils.createTmpDir();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@After
	public final void tearDown() {
		FileUtils.delete(tmpDir.getAbsolutePath());
	}
	
	
	@Test
	public void testBuildConfigFileMetadataXml() throws MetadataExtractionException, MetadataMalformedException {
		
		final AuxDescriptor descriptor = new AuxDescriptor();
		descriptor.setExtension(FileExtension.XML);
		descriptor.setFilename("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setKeyObjectStorage("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setProductType("AUX_OBMEMC");
		descriptor.setRelativePath("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

		final ProductMetadata expectedResult = ProductMetadata.ofJson("{\"productName\": \"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml\"}");

		Mockito.doAnswer(i ->expectedResult)
			.when(extractor).processXMLFile(Mockito.any(AuxDescriptor.class), Mockito.any(File.class));

		final File file = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

		try {
			final MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			final ProductMetadata metadata = metadataBuilder.buildConfigFileMetadata(descriptor, file);

			assertNotNull("Metadata should not be null", metadata);
			assertEquals("Metadata are not equals", expectedResult.toString(), metadata.toString());
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testBuildConfigFileMetadataEOF() throws MetadataExtractionException, MetadataMalformedException {

		final AuxDescriptor descriptor = new AuxDescriptor();
		descriptor.setExtension(FileExtension.EOF);
		descriptor.setFilename("S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");
		descriptor.setKeyObjectStorage("S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");
		descriptor.setProductType("MPL_ORBSCT");
		descriptor.setRelativePath("S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");

		final ProductMetadata expectedResult = ProductMetadata.ofJson("{\"validityStopTime\":\"9999-12-31T23:59:59\",\"productClass\":\"OPER\",\"missionid\":\"S1\",\"creationTime\":\"2017-01-23T16:38:09\",\"insertionTime\":\"2018-05-30T11:40:06\",\"satelliteid\":\"A\",\"validityStartTime\":\"2014-04-03T22:46:09\",\"version\":\"0020\",\"productName\":\"S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF\",\"productType\":\"MPL_ORBSCT\"}");
		
		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processEOFFile(Mockito.any(AuxDescriptor.class), Mockito.any(File.class));

		final File file = new File("workDir/S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");

		try {
			final MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			final ProductMetadata metadata = metadataBuilder.buildConfigFileMetadata(descriptor, file);
			assertNotNull("Metadata should not be null", metadata);
			assertEquals("Metadata are not equals", expectedResult.toString(), metadata.toString());
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testBuildConfigFileMetadataAUXRESORB() throws MetadataExtractionException, MetadataMalformedException {

		final AuxDescriptor descriptor = new AuxDescriptor();
		descriptor.setExtension(FileExtension.EOF);
		descriptor.setFilename("S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");
		descriptor.setKeyObjectStorage("S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");
		descriptor.setProductType("AUX_RESORB");
		descriptor.setRelativePath("S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");

		final ProductMetadata expectedResult = ProductMetadata.ofJson("{\"validityStopTime\":\"2017-12-13T13:45:07\",\"productClass\":\"OPER\",\"missionid\":\"S1\",\"creationTime\":\"2017-12-13T14:38:38\",\"insertionTime\":\"2018-05-30T11:40:06\",\"satelliteid\":\"A\",\"validityStartTime\":\"2017-12-13T10:27:37\",\"version\":\"0001\",\"productType\":\"AUX_RESORB\",\"productName\":\"S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF\"}");

		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processEOFFileWithoutNamespace(Mockito.any(AuxDescriptor.class),Mockito.any(File.class));

		final File file = new File("workDir/S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");

		try {
			final MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			final ProductMetadata metadata = metadataBuilder.buildConfigFileMetadata(descriptor, file);
			
			assertNotNull("Metadata should not be null", metadata);
			assertEquals("Metadata are not equals", expectedResult.toString(), metadata.toString());
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testBuildConfigFileMetadataSAFE() throws MetadataExtractionException, MetadataMalformedException {

		final AuxDescriptor descriptor = new AuxDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");
		descriptor.setKeyObjectStorage("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");
		descriptor.setProductType("AUX_INS");
		descriptor.setRelativePath("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");

		final ProductMetadata expectedResult = ProductMetadata.ofJson("{\"validityStopTime\":\"9999-12-31T23:59:59\",\"site\":\"CLS-Brest\",\"missionid\":\"S1\",\"creationTime\":\"2017-10-13T10:12:16.000000\",\"insertionTime\":\"2018-05-30T11:40:06\",\"satelliteid\":\"A\",\"instrumentConfigurationId\":\"6\",\"validityStartTime\":\"2017-10-17T08:00:00.000000\",\"productName\":\"S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE\",\"productType\":\"AUX_INS\"}");

		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processSAFEFile(Mockito.any(AuxDescriptor.class), Mockito.any(File.class));


		final File file = new File("workDir/S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/manifest.safe");

		try {
			final MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			final ProductMetadata metadata = metadataBuilder.buildConfigFileMetadata(descriptor, file);

			assertNotNull("Metadata should not be null", metadata);
			assertEquals("Metadata are not equals", expectedResult.toString(), metadata.toString());
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test
	public void testBuildConfigFileMetadataInvalidExtension() throws MetadataExtractionException, MetadataMalformedException {

		final AuxDescriptor descriptor = new AuxDescriptor();
		descriptor.setFilename("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setKeyObjectStorage("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setProductType("AUX_OBMEMC");
		descriptor.setRelativePath("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

		final File file = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

		try {
			descriptor.setExtension(FileExtension.DAT);
			final MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			metadataBuilder.buildConfigFileMetadata(descriptor, file);
			fail("An exception should occur");
		} catch (final MetadataExtractionException fe) {
			assertEquals("Raised exception shall concern S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
					AbstractCodedException.ErrorCode.METADATA_EXTRACTION_ERROR, fe.getCode());
		}

		try {
			descriptor.setExtension(FileExtension.RAW);
			final MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			metadataBuilder.buildConfigFileMetadata(descriptor, file);
			fail("An exception should occur");
		} catch (final MetadataExtractionException fe) {
			assertEquals("Raised exception shall concern S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
			        AbstractCodedException.ErrorCode.METADATA_EXTRACTION_ERROR, fe.getCode());
		}

		try {
			descriptor.setExtension(FileExtension.XSD);
			final MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			metadataBuilder.buildConfigFileMetadata(descriptor, file);
			fail("An exception should occur");
		} catch (final MetadataExtractionException fe) {
			assertEquals("Raised exception shall concern S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
			        AbstractCodedException.ErrorCode.METADATA_EXTRACTION_ERROR, fe.getCode());
		}

		try {
			descriptor.setExtension(FileExtension.UNKNOWN);
			final MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			metadataBuilder.buildConfigFileMetadata(descriptor, file);
			fail("An exception should occur");
		} catch (final MetadataExtractionException fe) {
			assertEquals("Raised exception shall concern S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
			        AbstractCodedException.ErrorCode.METADATA_EXTRACTION_ERROR, fe.getCode());
		}
	}

	@Test
	public void testBuildSessionMetadataRaw() throws MetadataExtractionException {
		// Mock the extractor
		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"insertionTime\":\"2018-02-07T13:26:12\",\"missionId\":\"S1\",\"sessionId\":\"707000180\",\"productName\":\"DCS_02_L20171109175634707000180_ch1_DSDB_00001.raw\",\"satelliteId\":\"A\",\"productType\":\"RAW\",\"url\":\"SESSION1/DCS_02_SESSION1_ch1_DSIB.xml\"}");
		
		Mockito.doAnswer(i ->expectedResult)
			.when(extractor).processRAWFile(Mockito.any(EdrsSessionFileDescriptor.class));

		// Build the parameters
		final EdrsSessionFileDescriptor descriptor = new EdrsSessionFileDescriptor();
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
			final MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			final ProductMetadata metadata = metadataBuilder.buildEdrsSessionFileRaw(descriptor);

			assertNotNull("Metadata should not be null", metadata);
			assertEquals("Metadata are not equals", expectedResult.toString(), metadata.toString());
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testBuildSessionMetadataSession() throws MetadataExtractionException {
		// Mock the extractor
		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"insertionTime\":\"2018-02-07T13:26:12\",\"missionId\":\"S1\",\"sessionId\":\"SESSION1\",\"productName\":\"DCS_02_SESSION1_ch1_DSIB.xml\",\"satelliteId\":\"A\",\"productType\":\"SESSION\",\"url\":\"SESSION1/DCS_02_SESSION1_ch1_DSIB.xml\"}");
		
		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processSESSIONFile(Mockito.any(EdrsSessionFileDescriptor.class), Mockito.any());
	
		// Build the parameters
		final EdrsSessionFileDescriptor descriptor = new EdrsSessionFileDescriptor();
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
			final MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			final ProductMetadata metadata = metadataBuilder.buildEdrsSessionFileMetadata(descriptor, new File("/dev/null/foobar"));

			assertNotNull("Metadata should not be null", metadata);
			assertEquals("Metadata are not equals", expectedResult.toString(), metadata.toString());
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testbuildL0SliceOutputFileMetadata() throws MetadataExtractionException, MetadataMalformedException {
		// Mock the extractor
		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"missionDataTakeId\":\"137013\",\"theoreticalSliceLength\":\"25\",\"sliceCoordinates\":{\"coordinates\":[[[86.8273,36.7787],[86.4312,38.7338],[83.6235,38.4629],[84.0935,36.5091],[86.8273,36.7787]]],\"type\":\"Polygon\"},\"insertionTime\":\"2018-05-30T14:27:43\",\"polarisation\":\"DV\",\"sliceNumber\":\"13\",\"absoluteStopOrbit\":\"19684\",\"resolution\":\"_\",\"circulationFlag\":\"13\",\"productName\":\"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE\",\"dataTakeId\":\"021735\",\"productConsolidation\":\"SLICE\",\"absoluteStartOrbit\":\"19684\",\"instrumentConfigurationId\":\"6\",\"relativeStopOrbit\":\"12\",\"relativeStartOrbit\":\"12\",\"startTime\":\"2017-12-13T12:16:23.685188Z\",\"stopTime\":\"2017-12-13T12:16:56.085136Z\",\"productType\":\"IW_RAW__0S\",\"productClass\":\"S\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":628491.556,\"sliceOverlap\":\"7.4\",\"startTimeANX\":\"596091.6080\"}");
		
		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processProduct(Mockito.any(OutputFileDescriptor.class), Mockito.eq(ProductFamily.L0_SLICE), Mockito.any(File.class));

		// Build the parameters
		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
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
		
		final File file = new File("workDir/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");
		
		final CatalogJob job = new CatalogJob();
		job.setProductFamily(ProductFamily.L0_SLICE);

		try {
			final MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			final ProductMetadata metadata = metadataBuilder.buildOutputFileMetadata(descriptor, file, job);

			assertNotNull("Metadata should not be null", metadata);
			assertEquals("Metadata are not equals", expectedResult.toString(), metadata.toString());
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testbuildL0ACNOutputFileMetadata() throws MetadataExtractionException, MetadataMalformedException {
		// Mock the extractor
		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"missionDataTakeId\":\"137013\",\"totalNumberOfSlice\":20.159704,\"sliceCoordinates\":{\"coordinates\":[[[90.3636,18.6541],[84.2062,49.0506],[80.8613,48.7621],[88.0584,18.3765],[90.3636,18.6541]]],\"type\":\"Polygon\"},\"insertionTime\":\"2018-05-30T14:27:43\",\"polarisation\":\"DV\",\"absoluteStopOrbit\":\"19684\",\"resolution\":\"_\",\"circulationFlag\":\"13\",\"productName\":\"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE\",\"dataTakeId\":\"021735\",\"productConsolidation\":\"FULL\",\"absoluteStartOrbit\":\"19684\",\"instrumentConfigurationId\":\"6\",\"relativeStopOrbit\":\"12\",\"relativeStartOrbit\":\"12\",\"startTime\":\"2017-12-13T12:11:23.682488Z\",\"stopTime\":\"2017-12-13T12:19:47.264351Z\",\"productType\":\"IW_RAW__0A\",\"productClass\":\"A\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":799670.769,\"startTimeANX\":\"296088.9120\"}");
		
		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processProduct(Mockito.any(OutputFileDescriptor.class), Mockito.eq(ProductFamily.L0_ACN), Mockito.any(File.class));

		// Build the parameters
		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
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
		
		final File file = new File("workDir/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE/manifest.safe");
		final CatalogJob job = new CatalogJob();
		job.setProductFamily(ProductFamily.L0_ACN);

		try {
			final MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			final ProductMetadata metadata = metadataBuilder.buildOutputFileMetadata(descriptor, file, job);

			assertNotNull("Metadata should not be null", metadata);
			assertEquals("Metadata are not equals", expectedResult.toString(), metadata.toString());
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testbuildL1SliceOutputFileMetadata() throws MetadataExtractionException, MetadataMalformedException {
		// Mock the extractor
		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"missionDataTakeId\":\"137013\",\"theoreticalSliceLength\":\"25\",\"sliceCoordinates\":{\"coordinates\":[[[86.8273,36.7787],[86.4312,38.7338],[83.6235,38.4629],[84.0935,36.5091],[86.8273,36.7787]]],\"type\":\"Polygon\"},\"insertionTime\":\"2018-05-30T14:27:43\",\"polarisation\":\"DV\",\"sliceNumber\":\"13\",\"absoluteStopOrbit\":\"19684\",\"resolution\":\"_\",\"circulationFlag\":\"13\",\"productName\":\"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE\",\"dataTakeId\":\"021735\",\"productConsolidation\":\"SLICE\",\"absoluteStartOrbit\":\"19684\",\"instrumentConfigurationId\":\"6\",\"relativeStopOrbit\":\"12\",\"relativeStartOrbit\":\"12\",\"startTime\":\"2017-12-13T12:16:23.685188Z\",\"stopTime\":\"2017-12-13T12:16:56.085136Z\",\"productType\":\"IW_RAW__0S\",\"productClass\":\"S\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":628491.556,\"sliceOverlap\":\"7.4\",\"startTimeANX\":\"596091.6080\"}");
		
		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processProduct(Mockito.any(OutputFileDescriptor.class), Mockito.eq(ProductFamily.L1_SLICE), Mockito.any(File.class));

		// Build the parameters
		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
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
		
		final File file = new File("workDir/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");
		

		final CatalogJob job = new CatalogJob();
		job.setProductFamily(ProductFamily.L1_SLICE);
		try {
			final MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			final ProductMetadata metadata = metadataBuilder.buildOutputFileMetadata(descriptor, file, job);

			assertNotNull("Metadata should not be null", metadata);
			assertEquals("Metadata are not equals", expectedResult.toString(), metadata.toString());
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testbuildL1ACNOutputFileMetadata() throws MetadataExtractionException, MetadataMalformedException {
		// Mock the extractor
		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"missionDataTakeId\":\"137013\",\"totalNumberOfSlice\":20.159704,\"sliceCoordinates\":{\"coordinates\":[[[90.3636,18.6541],[84.2062,49.0506],[80.8613,48.7621],[88.0584,18.3765],[90.3636,18.6541]]],\"type\":\"Polygon\"},\"insertionTime\":\"2018-05-30T14:27:43\",\"polarisation\":\"DV\",\"absoluteStopOrbit\":\"19684\",\"resolution\":\"_\",\"circulationFlag\":\"13\",\"productName\":\"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE\",\"dataTakeId\":\"021735\",\"productConsolidation\":\"FULL\",\"absoluteStartOrbit\":\"19684\",\"instrumentConfigurationId\":\"6\",\"relativeStopOrbit\":\"12\",\"relativeStartOrbit\":\"12\",\"startTime\":\"2017-12-13T12:11:23.682488Z\",\"stopTime\":\"2017-12-13T12:19:47.264351Z\",\"productType\":\"IW_RAW__0A\",\"productClass\":\"A\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":799670.769,\"startTimeANX\":\"296088.9120\"}");
		
		Mockito.doAnswer(i -> expectedResult)
			.when(extractor).processProduct(Mockito.any(OutputFileDescriptor.class), Mockito.eq(ProductFamily.L1_ACN), Mockito.any(File.class));

		// Build the parameters
		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
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
		
		final File file = new File("workDir/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE/manifest.safe");
		

		final CatalogJob job = new CatalogJob();
		job.setProductFamily(ProductFamily.L1_ACN);

		try {
			final MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			final ProductMetadata metadata = metadataBuilder.buildOutputFileMetadata(descriptor, file, job);

			assertNotNull("Metadata should not be null", metadata);
			assertEquals("Metadata are not equals", expectedResult.toString(), metadata.toString());
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

}
