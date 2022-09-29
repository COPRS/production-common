/**
 * 
 */
package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.AuxDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.S3FileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.report.ReportingFactory;

/**
 * @author Olivier BEX-CHAUVET
 */
public class ExtractMetadataTest {

	private ExtractMetadata extractor;

	XmlConverter xmlConverter;

	private final File testDir = new File("src/test/resources/workDir");

	@Before
	public void init() {
		xmlConverter = new XmlConverter();
		final Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
		jaxb2Marshaller.setPackagesToScan("esa.s1pdgs.cpoc.mdc.worker.extraction.model");
		final Map<String, Object> map = new ConcurrentHashMap<String, Object>();
		map.put("jaxb.formatted.output", true);
		map.put("jaxb.encoding", "UTF-8");
		jaxb2Marshaller.setMarshallerProperties(map);
		xmlConverter.setMarshaller(jaxb2Marshaller);
		xmlConverter.setUnmarshaller(jaxb2Marshaller);

		// "EW:8.2F||IW:7.4F||SM:7.7F||WM:0.0F"
		final Map<String, Float> typeOverlap = new HashMap<String, Float>();
		typeOverlap.put("EW", 8.2F);
		typeOverlap.put("IW", 7.4F);
		typeOverlap.put("SM", 7.7F);
		typeOverlap.put("WV", 0.0F);
		// "EW:60.0F||IW:25.0F||SM:25.0F||WM:0.0F"
		final Map<String, Float> typeSliceLength = new HashMap<String, Float>();
		typeSliceLength.put("EW", 60.0F);
		typeSliceLength.put("IW", 25.0F);
		typeSliceLength.put("SM", 25.0F);
		typeSliceLength.put("WV", 0.0F);

		final Map<String, String> packetStoreTypes = new HashMap<>();
		packetStoreTypes.put("S1A-0", "Emergency");
		packetStoreTypes.put("S1A-1", "Emergency");
		packetStoreTypes.put("S1A-2", "RFC");
		packetStoreTypes.put("S1A-22", "Standard");
		packetStoreTypes.put("S1A-37", "PassThrough");
		packetStoreTypes.put("S1B-0", "Emergency");
		packetStoreTypes.put("S1B-1", "Emergency");
		packetStoreTypes.put("S1B-2", "RFC");
		packetStoreTypes.put("S1B-22", "Standard");
		packetStoreTypes.put("S1B-37", "PassThrough");
		final Map<String, String> packetStoreTypesTimelinesses = new HashMap<>();
		packetStoreTypesTimelinesses.put("Emergency", "PT");
		packetStoreTypesTimelinesses.put("HKTM", "NRT");
		packetStoreTypesTimelinesses.put("NRT", "NRT");
		packetStoreTypesTimelinesses.put("GPS", "NRT");
		packetStoreTypesTimelinesses.put("PassThrough", "PT");
		packetStoreTypesTimelinesses.put("Standard", "FAST24");
		packetStoreTypesTimelinesses.put("RFC", "FAST24");
		packetStoreTypesTimelinesses.put("WV", "FAST24");
		packetStoreTypesTimelinesses.put("Filler", "FAST24");
		packetStoreTypesTimelinesses.put("Spare", "FAST24");

		final List<String> timelinessPriorityFromHighToLow = Arrays.asList("PT", "NRT", "FAST24");

		extractor = new ExtractMetadata(typeOverlap, typeSliceLength, Collections.<String, String>emptyMap(),
				packetStoreTypes, packetStoreTypesTimelinesses, timelinessPriorityFromHighToLow, "config/xsltDir/",
				xmlConverter);
	}

	@Test
	public void testProcessXMLFile() {
		ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"validityStopTime\":\"9999-12-31T23:59:59.000000Z\",\"productClass\":\"OPER\",\"missionid\":\"S1\","
						+ "\"creationTime\":\"2014-02-12T12:28:19.000000Z\",\"insertionTime\":\"2018-05-31T14:34:17.000000Z\","
						+ "\"satelliteid\":\"A\",\"validityStartTime\":\"2014-02-01T00:00:00.000000Z\","
						+ "\"productName\":\"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml\","
						+ "\"url\":\"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml\",\"productType\":\"AUX_OBMEMC\","
						+ "\"productFamily\":\"AUXILIARY_FILE\"}");

		AuxDescriptor descriptor = new AuxDescriptor();
		descriptor.setExtension(FileExtension.XML);
		descriptor.setFilename("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setKeyObjectStorage("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setProductType("AUX_OBMEMC");
		descriptor.setRelativePath("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);

		File file = new File(testDir, "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

		try {
			final ProductMetadata result = extractor.processXMLFile(descriptor, file);

			assertNotNull("JSON object should not be null", result);
			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			assertTrue("JSON object value validityStartTime are not equals", expectedResult.get("validityStartTime")
					.toString().equals(result.get("validityStartTime").toString()));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}

		expectedResult = ProductMetadata.ofJson(
				"{\"validityStopTime\":\"2099-12-31T23:59:59.000000Z\",\"productClass\":\"OPER\",\"missionId\":\"S1\",\"creationTime\":\"2014-02-12T12:28:19.000000Z\",\"insertionTime\":\"2018-06-04T09:38:40.000000Z\",\"satelliteId\":\"B\",\"validityStartTime\":\"2014-02-01T00:00:00.000000Z\",\"productName\":\"S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml\",\"productType\":\"AUX_OBMEMC\",\"url\":\"S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml\",\"productFamily\":\"AUXILIARY_FILE\"}");

		descriptor = new AuxDescriptor();
		descriptor.setExtension(FileExtension.XML);
		descriptor.setFilename("S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml");
		descriptor.setKeyObjectStorage("S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("B");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml");
		descriptor.setProductType("AUX_OBMEMC");
		descriptor.setRelativePath("S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml");
		descriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);

		file = new File(testDir, "S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml");

		try {
			final ProductMetadata result = extractor.processXMLFile(descriptor, file);

			assertNotNull("JSON object should not be null", result);
			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			assertTrue("JSON object value validityStartTime are not equals", expectedResult.get("validityStartTime")
					.toString().equals(result.get("validityStartTime").toString()));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test(expected = AbstractCodedException.class)
	public void testProcessXMLMissingFileFail() throws MetadataExtractionException, MetadataMalformedException {

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
		descriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);
		final File file = new File(testDir, "S1A_OPER_OUX_OBMEMC_PDMC_20140201T000000.xml");

		extractor.processXMLFile(descriptor, file);
	}

	@Test
	public void testProcessEOFFile() {

		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"productFamily\":\"AUXILIARY_FILE\",\"absolutOrbit\":\"\",\"productClass\":\"OPER\",\"missionId\":\"S1\",\"creationTime\":\"2017-12-08T20:02:13.000000Z\",\"insertionTime\":\"2021-01-08T17:09:08.384000Z\",\"satelliteId\":\"A\",\"processorVersion\":3,\"version\":\"0001\",\"productName\":\"S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF\",\"url\":\"S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF\",\"platformSerialIdentifier\":\"A\",\"validityStopTime\":\"2017-12-15T20:03:09.000000Z\",\"site\":\"FOS\",\"platformShortName\":\"SENTINEL 1\",\"validityStartTime\":\"2017-12-08T20:03:09.000000Z\",\"productType\":\"MPL_ORBPRE\"}");

		final AuxDescriptor descriptor = new AuxDescriptor();
		descriptor.setExtension(FileExtension.EOF);
		descriptor.setFilename("S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
		descriptor.setKeyObjectStorage("S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
		descriptor.setProductType("MPL_ORBPRE");
		descriptor.setRelativePath("S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
		descriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);

		final File file = new File(testDir, "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");

		try {
			final ProductMetadata result = extractor.processEOFFile(descriptor, file);

			assertNotNull("JSON object should not be null", result);
			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			assertTrue("JSON object value validityStartTime are not equals", expectedResult.get("validityStartTime")
					.toString().equals(result.get("validityStartTime").toString()));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test
	public void testProcessLandMskFile() {

		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"productFamily\":\"AUXILIARY_FILE\",\"absolutOrbit\":\"\",\"productClass\":\"OPER\",\"missionId\":\"S1\",\"creationTime\":\"2019-07-11T11:33:00.000000Z\",\"insertionTime\":\"2021-01-08T17:00:08.348000Z\",\"satelliteId\":\"_\",\"processorVersion\":\"S1PRO-004\",\"version\":\"0001\",\"productName\":\"S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF\",\"url\":\"S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF\",\"platformSerialIdentifier\":\"_\",\"validityStopTime\":\"9999-12-31T23:59:59.999999Z\",\"site\":\"S1PDGS\",\"platformShortName\":\"S1\",\"validityStartTime\":\"2014-04-03T21:02:00.000000Z\",\"productType\":\"MSK__LAND_\"}");

		final AuxDescriptor descriptor = new AuxDescriptor();
		descriptor.setExtension(FileExtension.EOF);
		descriptor.setFilename("S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
		descriptor.setKeyObjectStorage("S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("_");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
		descriptor.setProductType("MSK__LAND_");
		descriptor.setRelativePath("S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
		descriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);

		final File file = new File(testDir, "S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");

		try {
			final ProductMetadata result = extractor.processEOFFile(descriptor, file);

			assertNotNull("JSON object should not be null", result);
			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			assertTrue("JSON object value validityStartTime are not equals", expectedResult.get("validityStartTime")
					.toString().equals(result.get("validityStartTime").toString()));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test(expected = AbstractCodedException.class)
	public void testProcessEOFMissingFileFail() throws MetadataExtractionException, MetadataMalformedException {

		final AuxDescriptor descriptor = new AuxDescriptor();
		descriptor.setExtension(FileExtension.EOF);
		descriptor.setFilename("S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
		descriptor.setKeyObjectStorage("S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
		descriptor.setProductType("MPL_ORBPRE");
		descriptor.setRelativePath("S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");

		final File file = new File(testDir, "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0003.EOF");

		extractor.processEOFFile(descriptor, file);
	}

	@Test
	public void testProcessEOFFileWithoutFile() {

		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"productFamily\":\"AUXILIARY_FILE\",\"absolutOrbit\":19683,\"productClass\":\"OPER\",\"missionId\":\"S1\",\"creationTime\":\"2017-12-13T14:38:38.000000Z\",\"insertionTime\":\"2021-01-06T16:15:46.186000Z\",\"satelliteId\":\"A\",\"processorVersion\":\"1.4.0\",\"version\":\"0001\",\"productName\":\"S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF\",\"url\":\"S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF\",\"platformSerialIdentifier\":\"A\",\"validityStopTime\":\"2017-12-13T13:45:07.000000Z\",\"site\":\"OPOD\",\"selectedOrbitFirstAzimuthTimeUtc\":\"2017-12-13T10:27:47.593331Z\",\"platformShortName\":\"Sentinel-1\",\"validityStartTime\":\"2017-12-13T10:27:37.000000Z\",\"productType\":\"AUX_RESORB\"}");

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
		descriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);

		final File file = new File(testDir,
				"S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");

		try {
			final ProductMetadata result = extractor.processEOFFileWithoutNamespace(descriptor, file);

			assertNotNull("JSON object should not be null", result);
			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			assertTrue("JSON object value validityStartTime are not equals", expectedResult.get("validityStartTime")
					.toString().equals(result.get("validityStartTime").toString()));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test(expected = AbstractCodedException.class)
	public void testProcessEOFFileWithoutMissingFileFail()
			throws MetadataExtractionException, MetadataMalformedException {

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

		final File file = new File(testDir,
				"S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134508.EOF");

		extractor.processEOFFileWithoutNamespace(descriptor, file);
	}

	@Test
	public void testProcessSAFEFile() throws MetadataExtractionException {

		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"validityStopTime\":\"9999-12-31T23:59:59.000000Z\",\"site\":\"CLS-Brest\",\"missionid\":\"S1\",\"creationTime\":\"2017-10-13T10:12:00.000000Z\",\"insertionTime\":\"2018-05-31T14:34:17.000000Z\",\"satelliteid\":\"A\",\"instrumentConfigurationId\":\"6\",\"validityStartTime\":\"2017-10-17T08:00:00.000000Z\",\"productName\":\"S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE\",\"productType\":\"AUX_CAL\",\"url\":\"S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE\",\"productFamily\":\"AUXILIARY_FILE\",\"productClass\":\"OPER\"}");

		final AuxDescriptor descriptor = new AuxDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE");
		descriptor.setProductType("AUX_CAL");
		descriptor.setRelativePath("S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE");
		descriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);

		final File file = new File(testDir, "S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE/manifest.safe");

		try {
			final ProductMetadata result = extractor.processSAFEFile(descriptor, file);

			System.out.println(result);

			assertNotNull("JSON object should not be null", result);
//			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			assertTrue("JSON object value validityStartTime are not equals", expectedResult.get("validityStartTime")
					.toString().equals(result.get("validityStartTime").toString()));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test(expected = AbstractCodedException.class)
	public void testProcessSAFEFileFail() throws MetadataExtractionException, MetadataMalformedException {

		final AuxDescriptor descriptor = new AuxDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE");
		descriptor.setProductType("AUX_CAL");
		descriptor.setRelativePath("S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE");

		final File file = new File(testDir, "S1A_AUX_CAL_V20171017T080000_G20171013T101201.SAFE");

		extractor.processSAFEFile(descriptor, file);
	}

	public void testProcessRAWFile() {

		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"insertionTime\":\"2018-02-07T13:26:12\",\"missionId\":\"S1\",\"sessionId\":\"707000180\",\"productName\":\"DCS_02_L20171109175634707000180_ch1_DSDB_00001.raw\",\"satelliteId\":\"A\",\"productType\":\"RAW\",\"url\":\"SESSION1/DCS_02_SESSION1_ch1_DSIB.xml\",\"productFamily\":\"EDRS_SESSION\"}");

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
		descriptor.setProductFamily(ProductFamily.EDRS_SESSION);

		try {
			final ProductMetadata result = extractor.processRAWFile(descriptor);
			System.out.println(result);

			assertNotNull("JSON object should not be null", result);
			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			assertTrue("JSON object value missionId are not equals", expectedResult.get("missionId")
					.toString().equals(result.get("missionId").toString()));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	public void testProcessSESSIONFile() {
		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"insertionTime\":\"2018-02-07T13:26:12\",\"missionId\":\"S1\",\"sessionId\":\"SESSION1\",\"timeStart\":\"2017-12-13T14:59:48Z\",\"timeStop\":\"2017-12-13T15:17:25Z\",\"rawNames\":[],\"productName\":\"DCS_02_SESSION1_ch1_DSIB.xml\",\"satelliteId\":\"A\",\"productType\":\"SESSION\",\"url\":\"SESSION1/DCS_02_SESSION1_ch1_DSIB.xml\",\"productFamily\":\"EDRS_SESSION\"}");

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
		descriptor.setProductFamily(ProductFamily.EDRS_SESSION);
		descriptor.setSessionIdentifier("sessionId");

		final File file = new File(testDir, "S1A/SESSION1/ch01/DCS_02_SESSION1_ch1_DSIB.xml");

		try {
			final ProductMetadata result = extractor.processSESSIONFile(descriptor, file);
			System.out.println(expectedResult);
			System.out.println(result);
			assertNotNull("JSON object should not be null", result);
			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			assertTrue("JSON object value missionId are not equals",
					expectedResult.get("missionId").toString().equals(result.get("missionId").toString()));
		} catch (final AbstractCodedException fe) {
			fe.printStackTrace();
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test
	public void testProcessL0SlicesFileIW() {

		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"missionDataTakeId\":137013,\"theoreticalSliceLength\":25,\"sliceCoordinates\":{\"coordinates\":[[[86.8273,36.7787],[86.4312,38.7338],[83.6235,38.4629],[84.0935,36.5091],[86.8273,36.7787]]],\"type\":\"polygon\"},\"insertionTime\":\"2018-05-31T14:34:18\",\"creationTime\":\"2018-05-31T15:33:43\",\"polarisation\":\"DV\",\"sliceNumber\":13,\"absoluteStopOrbit\":19684,\"resolution\":\"_\",\"circulationFlag\":13,\"productName\":\"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe\",\"dataTakeId\":\"021735\",\"productConsolidation\":\"SLICE\",\"absoluteStartOrbit\":19684,\"validityStopTime\":\"2017-12-13T12:16:56.085136Z\",\"instrumentConfigurationId\":6,\"relativeStopOrbit\":12,\"relativeStartOrbit\":12,\"startTime\":\"2017-12-13T12:16:23.685188Z\",\"stopTime\":\"2017-12-13T12:16:56.085136Z\",\"productType\":\"IW_RAW__0S\",\"productClass\":\"S\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":628491.556,\"url\":\"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE\",\"sliceOverlap\":7.4,\"startTimeANX\":\"596091.6080\",\"validityStartTime\":\"2017-12-13T12:16:23.685188Z\",\"processMode\":\"FAST\",\"productFamily\":\"L0_SLICE\"}");

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
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
		descriptor.setMode("FAST");

		final File file = new File(testDir,
				"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");

		try {
			final ProductMetadata result = extractor.processProduct(descriptor, ProductFamily.L0_SLICE, file);
			assertNotNull("JSON object should not be null", result);
//			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			assertTrue("JSON object value productConsolidation are not equals", expectedResult
					.get("productConsolidation").toString().equals(result.get("productConsolidation").toString()));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test
	public void testProcessL0SlicesFileWV() {
		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE");
		descriptor.setRelativePath("S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE");
		descriptor.setSwathtype("WV");
		descriptor.setResolution("_");
		descriptor.setProductClass("S");
		descriptor.setProductType("WV_RAW__0S");
		descriptor.setPolarisation("SV");
		descriptor.setDataTakeId("0294F4");
		descriptor.setProductFamily(ProductFamily.L0_SLICE);
		descriptor.setMode("FAST");

		final File file = new File(testDir,
				"S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE/manifest.safe");

		try {
			final ProductMetadata result = extractor.processProduct(descriptor, ProductFamily.L0_SLICE, file);

			assertNotNull("JSON object should not be null", result);
			@SuppressWarnings("unchecked")
			Map<String,Object> sliceCoordinates = (Map<String,Object>)result.get("sliceCoordinates");
			assertTrue("sliceCoordinates type should be linestring",
					"linestring".equals(sliceCoordinates.get("type")));
			@SuppressWarnings("unchecked")
			List<List<Double>> coordinates = (List<List<Double>>)sliceCoordinates.get("coordinates");
			assertEquals(108.5909, coordinates.get(0).get(0));
			assertEquals(-62.2900, coordinates.get(0).get(1));
			assertEquals(105.8055, coordinates.get(1).get(0));
			assertEquals(-65.5655, coordinates.get(1).get(1));
			assertEquals(1, result.getInt("sliceNumber"));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test(expected = AbstractCodedException.class)
	public void testProcessL0SlicesMissingFileFail() throws MetadataExtractionException, MetadataMalformedException {

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
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

		final File file = new File(testDir,
				"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DA.SAFE/manifest.safe");

		extractor.processProduct(descriptor, ProductFamily.L0_SLICE, file);
	}

	@Test
	public void testProcessL0SegmentFile() {

		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"missionDataTakeId\":72627,\"productFamily\":\"L0_SEGMENT\",\"insertionTime\":\"2018-10-15T11:44:03.000000Z\",\"creationTime\":\"2018-10-15T11:44:03.000000Z\",\"polarisation\":\"DV\",\"absoluteStopOrbit\":9809,\"resolution\":\"_\",\"circulationFlag\":7,\"productName\":\"S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE\",\"dataTakeId\":\"021735\",\"productConsolidation\":\"FULL\",\"productSensingConsolidation\":\"DUMMY VALUE (FOR TEST)\",\"timeliness\":\"PT\",\"absoluteStartOrbit\":9809,\"validityStopTime\":\"2018-02-27T12:53:00.422905Z\",\"instrumentConfigurationId\":1,\"relativeStopOrbit\":158,\"relativeStartOrbit\":158,\"startTime\":\"2018-02-27T12:51:14.794304Z\",\"stopTime\":\"2018-02-27T12:53:00.422905Z\",\"productType\":\"IW_RAW__0S\",\"productClass\":\"S\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"DESCENDING\",\"satelliteId\":\"B\",\"stopTimeANX\":1849446.881,\"url\":\"S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE\",\"startTimeANX\":1743818.281,\"validityStartTime\":\"2018-02-27T12:51:14.794304Z\",\"segmentCoordinates\":{\"coordinates\":[[[-94.8783,73.8984],[-98.2395,67.6029],[-88.9623,66.8368],[-82.486,72.8925],[-94.8783,73.8984]]],\"type\":\"polygon\"},\"processMode\":\"NOMINAL\"}");

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("B");
		descriptor.setProductName("S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE");
		descriptor.setRelativePath("S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE");
		descriptor.setSwathtype("IW");
		descriptor.setResolution("_");
		descriptor.setProductClass("S");
		descriptor.setProductType("IW_RAW__0S");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("021735");
		descriptor.setProductFamily(ProductFamily.L0_SEGMENT);
		descriptor.setMode("NOMINAL");

		final File file = new File(testDir,
				"S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE/manifest.safe");

		try {
			final ProductMetadata result = extractor.processL0Segment(descriptor, file, ReportingFactory.NULL);
			assertNotNull("JSON object should not be null", result);
//			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			assertTrue("JSON object value validityStartTime are not equals",
					expectedResult.getString("validityStartTime").equals(result.getString("validityStartTime")));
			assertTrue("JSON object value validityStopTime are not equals",
					expectedResult.getString("validityStopTime").equals(result.getString("validityStopTime")));
			assertTrue("JSON object value productConsolidation are not equals",
					expectedResult.getString("productConsolidation").equals(result.getString("productConsolidation")));
			assertTrue("JSON object value productSensingConsolidation are not equals", expectedResult
					.getString("productSensingConsolidation").equals(result.getString("productSensingConsolidation")));

			
			Iterator<String> it = expectedResult.keys().iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (!("insertionTime".equals(key) || "segmentCoordinates".equals(key) || "creationTime".equals(key))) {
					assertTrue(expectedResult.get(key).equals(result.get(key)));
				}
			}
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test
	public void testMaxTimeliness() {
		assertTrue("PT".equals(extractor.maxTimeliness(Arrays.asList("PT"))));
		assertTrue("NRT".equals(extractor.maxTimeliness(Arrays.asList("NRT"))));
		assertTrue("FAST24".equals(extractor.maxTimeliness(Arrays.asList("FAST24"))));

		assertTrue("PT".equals(extractor.maxTimeliness(Arrays.asList("NRT", "PT"))));
		assertTrue("PT".equals(extractor.maxTimeliness(Arrays.asList("PT", "NRT"))));
		assertTrue("PT".equals(extractor.maxTimeliness(Arrays.asList("FAST24", "PT"))));
		assertTrue("PT".equals(extractor.maxTimeliness(Arrays.asList("PT", "FAST24"))));
		assertTrue("NRT".equals(extractor.maxTimeliness(Arrays.asList("FAST24", "NRT"))));
		assertTrue("NRT".equals(extractor.maxTimeliness(Arrays.asList("NRT", "FAST24"))));

		assertTrue("PT".equals(extractor.maxTimeliness(Arrays.asList("FAST24", "NRT", "PT"))));
		assertTrue("PT".equals(extractor.maxTimeliness(Arrays.asList("FAST24", "PT", "NRT"))));
		assertTrue("PT".equals(extractor.maxTimeliness(Arrays.asList("NRT", "FAST24", "PT"))));
		assertTrue("PT".equals(extractor.maxTimeliness(Arrays.asList("PT", "FAST24", "NRT"))));
		assertTrue("PT".equals(extractor.maxTimeliness(Arrays.asList("NRT", "PT", "FAST24"))));
		assertTrue("PT".equals(extractor.maxTimeliness(Arrays.asList("PT", "NRT", "FAST24"))));
	}

	@Test(expected = AbstractCodedException.class)
	public void testProcessL0SegmentMissingFileFail() throws MetadataExtractionException, MetadataMalformedException {

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("B");
		descriptor.setProductName("S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE");
		descriptor.setRelativePath("S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE");
		descriptor.setSwathtype("IW");
		descriptor.setResolution("_");
		descriptor.setProductClass("S");
		descriptor.setProductType("IW_RAW__0S");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("021735");

		final File file = new File(testDir,
				"S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DD.SAFE/manifest.safe");

		extractor.processL0Segment(descriptor, file, ReportingFactory.NULL);
	}

	@Test
	public void testProcessL0ACNFile() {

		ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"missionDataTakeId\":137013,\"productFamily\":\"L0_ACN\",\"totalNumberOfSlice\":20,\"theoreticalSliceLength\":\"\",\"sliceCoordinates\":{\"orientation\":\"counterclockwise\",\"coordinates\":[[[90.3636,18.6541],[84.2062,49.0506],[80.8613,48.7621],[88.0584,18.3765],[90.3636,18.6541]]],\"type\":\"polygon\"},\"insertionTime\":\"2021-01-08T17:02:33.291000Z\",\"creationTime\":\"2021-01-08T17:02:33.291000Z\",\"polarisation\":\"DV\",\"sliceNumber\":1,\"absoluteStopOrbit\":19684,\"resolution\":\"_\",\"circulationFlag\":13,\"productName\":\"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE\",\"dataTakeId\":\"021735\",\"productConsolidation\":\"FULL\",\"absoluteStartOrbit\":19684,\"validityStopTime\":\"2017-12-13T12:19:47.264351Z\",\"instrumentConfigurationId\":6,\"relativeStopOrbit\":12,\"relativeStartOrbit\":12,\"startTime\":\"2017-12-13T12:11:23.682488Z\",\"stopTime\":\"2017-12-13T12:19:47.264351Z\",\"productType\":\"IW_RAW__0A\",\"productClass\":\"A\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":799670.769,\"url\":\"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE\",\"sliceOverlap\":\"\",\"startTimeANX\":296088.912,\"validityStartTime\":\"2017-12-13T12:11:23.682488Z\",\"processMode\":\"NOMINAL\"}");

		OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
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
		descriptor.setProductFamily(ProductFamily.L0_ACN);
		descriptor.setMode("FAST");

		File file = new File(testDir,
				"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE/manifest.safe");

		try {
			final ProductMetadata result = extractor.processProduct(descriptor, ProductFamily.L0_ACN, file);
			assertNotNull("JSON object should not be null", result);
//			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			assertTrue("JSON object value validityStartTime are not equals",
					expectedResult.get("missionId").toString().equals(result.get("missionId").toString()));

			assertTrue("JSON object value totalNumberOfSlice are not equals", expectedResult.get("totalNumberOfSlice")
					.toString().equals(result.get("totalNumberOfSlice").toString()));

		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}

		expectedResult = ProductMetadata.ofJson(
				"{\"missionDataTakeId\":146024,\"productFamily\":\"L0_SLICE\",\"totalNumberOfSlice\":1,\"theoreticalSliceLength\":\"\",\"sliceCoordinates\":{\"orientation\":\"counterclockwise\",\"coordinates\":[[[56.2016,-13.3062],[56.1801,-13.2193],[52.4759,-13.8154],[52.4961,-13.9029],[56.2016,-13.3062]]],\"type\":\"polygon\"},\"insertionTime\":\"2021-01-08T17:03:49.782000Z\",\"creationTime\":\"2021-01-08T17:03:49.782000Z\",\"polarisation\":\"DV\",\"sliceNumber\":1,\"absoluteStopOrbit\":20793,\"resolution\":\"_\",\"circulationFlag\":3,\"productName\":\"S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE\",\"dataTakeId\":\"023A68\",\"productConsolidation\":\"PARTIAL\",\"absoluteStartOrbit\":20793,\"validityStopTime\":\"2018-02-27T14:47:06.722008Z\",\"instrumentConfigurationId\":6,\"relativeStopOrbit\":71,\"relativeStartOrbit\":71,\"startTime\":\"2018-02-27T14:47:04.973656Z\",\"stopTime\":\"2018-02-27T14:47:06.722008Z\",\"productType\":\"EW_RAW__0S\",\"productClass\":\"C\",\"missionId\":\"S1\",\"swathtype\":\"EW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":5691467.842,\"url\":\"S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE\",\"sliceOverlap\":\"\",\"startTimeANX\":5689719.49,\"validityStartTime\":\"2018-02-27T14:47:04.973656Z\",\"processMode\":\"NOMINAL\"}");

		descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE");
		descriptor.setRelativePath("S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE");
		descriptor.setSwathtype("EW");
		descriptor.setResolution("_");
		descriptor.setProductClass("C");
		descriptor.setProductType("EW_RAW__0S");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("023A68");
		descriptor.setProductFamily(ProductFamily.L0_SLICE);
		descriptor.setMode("NRT");

		file = new File(testDir,
				"S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE/manifest.safe");

		try {
			final ProductMetadata result = extractor.processProduct(descriptor, ProductFamily.L0_SLICE, file);

			assertNotNull("JSON object should not be null", result);
//			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			assertTrue("JSON object value validityStartTime are not equals", expectedResult.get("productConsolidation")
					.toString().equals(result.get("productConsolidation").toString()));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}

		expectedResult = ProductMetadata.ofJson(
				"{\"missionDataTakeId\":146024,\"productFamily\":\"L0_SLICE\",\"totalNumberOfSlice\":1,\"theoreticalSliceLength\":\"\",\"sliceCoordinates\":{\"orientation\":\"counterclockwise\",\"coordinates\":[[[56.2016,-13.3062],[56.1801,-13.2193],[52.4759,-13.8154],[52.4961,-13.9029],[56.2016,-13.3062]]],\"type\":\"polygon\"},\"insertionTime\":\"2021-01-08T17:04:55.475000Z\",\"creationTime\":\"2021-01-08T17:04:55.475000Z\",\"polarisation\":\"DV\",\"sliceNumber\":1,\"absoluteStopOrbit\":20793,\"resolution\":\"_\",\"circulationFlag\":3,\"productName\":\"S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE\",\"dataTakeId\":\"023A68\",\"productConsolidation\":\"PARTIAL\",\"absoluteStartOrbit\":20793,\"validityStopTime\":\"2018-02-27T14:47:06.722008Z\",\"instrumentConfigurationId\":6,\"relativeStopOrbit\":71,\"relativeStartOrbit\":71,\"startTime\":\"2018-02-27T14:47:04.973656Z\",\"stopTime\":\"2018-02-27T14:47:06.722008Z\",\"productType\":\"EW_RAW__0C\",\"productClass\":\"C\",\"missionId\":\"S1\",\"swathtype\":\"EW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"B\",\"stopTimeANX\":5691467.842,\"url\":\"S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE\",\"sliceOverlap\":\"\",\"startTimeANX\":5689719.49,\"validityStartTime\":\"2018-02-27T14:47:04.973656Z\",\"processMode\":\"NOMINAL\"}");

		descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("B");
		descriptor.setProductName("S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE");
		descriptor.setRelativePath("S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE");
		descriptor.setSwathtype("EW");
		descriptor.setResolution("_");
		descriptor.setProductClass("C");
		descriptor.setProductType("EW_RAW__0C");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("023A68");
		descriptor.setProductFamily(ProductFamily.L0_SLICE);
		descriptor.setMode("NRT");

		file = new File(testDir,
				"S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE/manifest.safe");

		try {
			final ProductMetadata result = extractor.processProduct(descriptor, ProductFamily.L0_SLICE, file);

			assertNotNull("JSON object should not be null", result);
//			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			assertTrue("JSON object value totalNumberOfSlice are not equals",
					expectedResult.get("totalNumberOfSlice").toString().equals(result.get("totalNumberOfSlice").toString()));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}

	}

	@Test(expected = AbstractCodedException.class)
	public void testProcessL0ACNMissingFileFail() throws MetadataExtractionException, MetadataMalformedException {

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
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

		final File file = new File(testDir,
				"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B2.SAFE/manifest.safe");

		extractor.processProduct(descriptor, ProductFamily.L0_ACN, file);
	}

	@Test
	public void testProcessL1SlicesFile() {

		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"missionDataTakeId\":146025,\"sliceCoordinates\":{\"coordinates\":[[[48.27924,12.378114],[50.603844,12.829241],[50.958828,11.081389],[48.64994,10.625828],[48.27924,12.378114]]],\"type\":\"polygon\"},\"insertionTime\":\"2018-06-01T11:40:35\",\"creationTime\":\"2018-06-01T11:40:35\",\"polarisation\":\"DV\",\"sliceNumber\":1,\"absoluteStopOrbit\":20794,\"resolution\":\"_\",\"productName\":\"S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE\",\"dataTakeId\":\"023A69\",\"absoluteStartOrbit\":20794,\"validityStopTime\":\"2018-02-27T14:54:13.190581\",\"instrumentConfigurationId\":6,\"relativeStopOrbit\":72,\"relativeStartOrbit\":72,\"startTime\":\"2018-02-27T14:53:44.184986\",\"stopTime\":\"2018-02-27T14:54:13.190581\",\"productType\":\"IW_GRDH_0S\",\"productClass\":\"S\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":193284.4,\"url\":\"S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE\",\"startTimeANX\":164278.8,\"validityStartTime\":\"2018-02-27T14:53:44.184986\",\"productFamily\":\"L1_SLICE\",\"processMode\":\"NOMINAL\"}");

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE");
		descriptor.setRelativePath("S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE");
		descriptor.setSwathtype("IW");
		descriptor.setResolution("_");
		descriptor.setProductClass("S");
		descriptor.setProductType("IW_GRDH_1S");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("023A69");
		descriptor.setProductFamily(ProductFamily.L1_SLICE);

		final File file = new File(testDir,
				"S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE/manifest.safe");

		try {
			final ProductMetadata result = extractor.processProduct(descriptor, ProductFamily.L1_SLICE, file);
			assertNotNull("JSON object should not be null", result);
//			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			@SuppressWarnings("unchecked")
			Map<String,Object> sliceCoordinates = (Map<String,Object>)result.get("sliceCoordinates");
			assertTrue("polygon".equals(sliceCoordinates.get("type")));
			@SuppressWarnings("unchecked")
			List<List<List<Double>>> coordinates = (List<List<List<Double>>>)sliceCoordinates.get("coordinates");
			assertEquals(48.64994, coordinates.get(0).get(0).get(0));
			assertEquals(10.625828, coordinates.get(0).get(0).get(1));
			assertEquals(48.27924, coordinates.get(0).get(1).get(0));
			assertEquals(12.378114, coordinates.get(0).get(1).get(1));
			assertEquals(50.603844, coordinates.get(0).get(2).get(0));
			assertEquals(12.829241, coordinates.get(0).get(2).get(1));
			assertEquals(50.958828, coordinates.get(0).get(3).get(0));
			assertEquals(11.081389, coordinates.get(0).get(3).get(1));
			assertEquals(48.64994, coordinates.get(0).get(4).get(0));
			assertEquals(10.625828, coordinates.get(0).get(4).get(1));
			
			assertTrue("JSON object value validityStartTime are not equals",
					expectedResult.get("absoluteStopOrbit").toString().equals(result.get("absoluteStopOrbit").toString()));
			assertTrue("2018-02-27T14:53:44.184986Z".equals(result.get("validityStartTime")));
			assertTrue("2018-02-27T14:54:13.190581Z".equals(result.get("validityStopTime")));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test
	public void testProcessL1SlicesWVFile() {

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1B_WV_SLC__1SDV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("B");
		descriptor.setProductName("S1B_WV_SLC__1SDV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE");
		descriptor.setRelativePath("S1B_WV_SLC__1SDV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE");
		descriptor.setSwathtype("WV");
		descriptor.setResolution("_");
		descriptor.setProductClass("S");
		descriptor.setProductType("WV_SLC__1S");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("017EF8");
		descriptor.setProductFamily(ProductFamily.L1_SLICE);

		final File file = new File(testDir,
				"S1B_WV_SLC__1SDV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE/manifest.safe");

		try {
			final ProductMetadata result = extractor.processProduct(descriptor, ProductFamily.L1_SLICE, file);

			assertNotNull("JSON object should not be null", result);
			@SuppressWarnings("unchecked")
			Map<String,Object> sliceCoordinates = (Map<String,Object>)result.get("sliceCoordinates");
			assertTrue("Polygon".equals(sliceCoordinates.get("type")));
			@SuppressWarnings("unchecked")
			List<List<List<Double>>> coordinates = (List<List<List<Double>>>)sliceCoordinates.get("coordinates");
			assertEquals(63.622650, coordinates.get(0).get(0).get(0));
			assertEquals(-6.293599, coordinates.get(0).get(0).get(1));
			assertEquals(65.249229, coordinates.get(0).get(1).get(0));
			assertEquals(-5.011662, coordinates.get(0).get(1).get(1));
			assertEquals(84.809326, coordinates.get(0).get(2).get(0));
			assertEquals(-57.154243, coordinates.get(0).get(2).get(1));
			assertEquals(82.243843, coordinates.get(0).get(3).get(0));
			assertEquals(-58.707996, coordinates.get(0).get(3).get(1));
			assertEquals(63.622650, coordinates.get(0).get(4).get(0));
			assertEquals(-6.293599, coordinates.get(0).get(4).get(1));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test
	public void testProcessL1SlicesWVFile2() {

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1B_WV_SLC__1SSH_20170702T130912_20170702T133355_006309_00B17D_BC10.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("B");
		descriptor.setProductName("S1B_WV_SLC__1SSH_20170702T130912_20170702T133355_006309_00B17D_BC10.SAFE");
		descriptor.setRelativePath("S1B_WV_SLC__1SSH_20170702T130912_20170702T133355_006309_00B17D_BC10.SAFE");
		descriptor.setSwathtype("WV");
		descriptor.setResolution("_");
		descriptor.setProductClass("S");
		descriptor.setProductType("WV_SLC__1S");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("017EF8");
		descriptor.setProductFamily(ProductFamily.L1_SLICE);

		final File file = new File(testDir,
				"S1B_WV_SLC__1SSH_20170702T130912_20170702T133355_006309_00B17D_BC10.SAFE/manifest.safe");

		try {
			final ProductMetadata result = extractor.processProduct(descriptor, ProductFamily.L1_SLICE, file);

			assertNotNull("JSON object should not be null", result);
			@SuppressWarnings("unchecked")
			Map<String,Object> sliceCoordinates = (Map<String,Object>)result.get("sliceCoordinates");
			assertTrue("Polygon".equals(sliceCoordinates.get("type")));
			@SuppressWarnings("unchecked")
			List<List<List<Double>>> coordinates = (List<List<List<Double>>>)sliceCoordinates.get("coordinates");
			assertEquals(-162.062683, coordinates.get(0).get(0).get(0));
			assertEquals(-76.17926, coordinates.get(0).get(0).get(1));
			assertEquals(-169.470245, coordinates.get(0).get(1).get(0));
			assertEquals(-75.217514, coordinates.get(0).get(1).get(1));
			assertEquals(-110.519669, coordinates.get(0).get(2).get(0));
			assertEquals(8.143796, coordinates.get(0).get(2).get(1));
			assertEquals(-108.503227, coordinates.get(0).get(3).get(0));
			assertEquals(8.657177, coordinates.get(0).get(3).get(1));
			assertEquals(-162.062683, coordinates.get(0).get(4).get(0));
			assertEquals(-76.17926, coordinates.get(0).get(4).get(1));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test
	public void testProcessL2SlicesWVFile() {

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1B_WV_OCN__2SSH_20170702T130912_20170702T133355_006309_00B17D_3B01.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("B");
		descriptor.setProductName("S1B_WV_OCN__2SSH_20170702T130912_20170702T133355_006309_00B17D_3B01.SAFE");
		descriptor.setRelativePath("S1B_WV_OCN__2SSH_20170702T130912_20170702T133355_006309_00B17D_3B01.SAFE");
		descriptor.setSwathtype("WV");
		descriptor.setResolution("_");
		descriptor.setProductClass("S");
		descriptor.setProductType("WV_OCN__2S");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("017EF8");
		descriptor.setProductFamily(ProductFamily.L2_SLICE);

		final File file = new File(testDir,
				"S1B_WV_OCN__2SSH_20170702T130912_20170702T133355_006309_00B17D_3B01.SAFE/manifest.safe");

		try {
			final ProductMetadata result = extractor.processProduct(descriptor, ProductFamily.L2_SLICE, file);

			assertNotNull("JSON object should not be null", result);
			@SuppressWarnings("unchecked")
			Map<String,Object> sliceCoordinates = (Map<String,Object>)result.get("sliceCoordinates");
			assertTrue("Polygon".equals(sliceCoordinates.get("type")));
			@SuppressWarnings("unchecked")
			List<List<List<Double>>> coordinates = (List<List<List<Double>>>)sliceCoordinates.get("coordinates");
			assertEquals(-162.062119, coordinates.get(0).get(0).get(0));
			assertEquals(-76.179138, coordinates.get(0).get(0).get(1));
			assertEquals(-169.469604, coordinates.get(0).get(1).get(0));
			assertEquals(-75.217407, coordinates.get(0).get(1).get(1));
			assertEquals(-110.51963, coordinates.get(0).get(2).get(0));
			assertEquals(8.143985, coordinates.get(0).get(2).get(1));
			assertEquals(-108.503189, coordinates.get(0).get(3).get(0));
			assertEquals(8.657347, coordinates.get(0).get(3).get(1));
			assertEquals(-162.062119, coordinates.get(0).get(4).get(0));
			assertEquals(-76.179138, coordinates.get(0).get(4).get(1));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test
	public void testProcessL1SlicesWVFile3raw() {

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_WV_GRDM_1ASV_20180913T214338_20180913T214410_023685_0294F4_70D1.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("S1A_WV_GRDM_1ASV_20180913T214338_20180913T214410_023685_0294F4_70D1.SAFE");
		descriptor.setRelativePath("S1A_WV_GRDM_1ASV_20180913T214338_20180913T214410_023685_0294F4_70D1.SAFE");
		descriptor.setSwathtype("WV");
		descriptor.setResolution("M");
		descriptor.setProductClass("S");
		descriptor.setProductType("WV_GRDM_1S");
		descriptor.setPolarisation("SV");
		descriptor.setDataTakeId("0294F4");
		descriptor.setProductFamily(ProductFamily.L1_SLICE);

		final File file = new File(testDir,
				"S1A_WV_GRDM_1ASV_20180913T214338_20180913T214410_023685_0294F4_70D1.SAFE/manifest.safe");

		try {
			final ProductMetadata result = extractor.processProduct(descriptor, ProductFamily.L1_SLICE, file);

			assertNotNull("JSON object should not be null", result);
			@SuppressWarnings("unchecked")
			Map<String,Object> sliceCoordinates = (Map<String,Object>)result.get("sliceCoordinates");
			assertTrue("Polygon".equals(sliceCoordinates.get("type")));
			@SuppressWarnings("unchecked")
			List<List<List<Double>>> coordinates = (List<List<List<Double>>>)sliceCoordinates.get("coordinates");
			assertEquals(98.145752, coordinates.get(0).get(0).get(0));
			assertEquals(-63.219410, coordinates.get(0).get(0).get(1));
			assertEquals(97.84906, coordinates.get(0).get(1).get(0));
			assertEquals(-63.146168, coordinates.get(0).get(1).get(1));
			assertEquals(102.146751, coordinates.get(0).get(2).get(0));
			assertEquals(-62.931400, coordinates.get(0).get(2).get(1));
			assertEquals(100.111465, coordinates.get(0).get(3).get(0));
			assertEquals(-61.471767, coordinates.get(0).get(3).get(1));
			assertEquals(98.145752, coordinates.get(0).get(4).get(0));
			assertEquals(-63.219410, coordinates.get(0).get(4).get(1));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test(expected = AbstractCodedException.class)
	public void testProcessL1SlicesMissingFileFail() throws MetadataExtractionException, MetadataMalformedException {

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE");
		descriptor.setRelativePath("S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE");
		descriptor.setSwathtype("IW");
		descriptor.setResolution("_");
		descriptor.setProductClass("S");
		descriptor.setProductType("IW_GRDH_0S");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("023A69");

		final File file = new File(testDir,
				"S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B4.SAFE/manifest.safe");
		extractor.processProduct(descriptor, ProductFamily.L1_SLICE, file);
	}

	@Test
	public void testProcessL1AFile() {

		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"missionDataTakeId\":146025,\"sliceCoordinates\":{\"coordinates\":[[[47.968777,13.890088],[50.308678,14.337382],[50.603825,12.829331],[48.279221,12.378204],[47.968777,13.890088]]],\"type\":\"polygon\"},\"insertionTime\":\"2018-06-01T11:40:59\",\"creationTime\":\"2018-06-01T11:40:59\",\"polarisation\":\"DV\",\"sliceNumber\":2,\"absoluteStopOrbit\":20794,\"resolution\":\"_\",\"productName\":\"S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE\",\"dataTakeId\":\"023A69\",\"absoluteStartOrbit\":20794,\"validityStopTime\":\"2018-02-27T14:54:38.190463\",\"instrumentConfigurationId\":6,\"relativeStopOrbit\":72,\"relativeStartOrbit\":72,\"startTime\":\"2018-02-27T14:54:13.192073\",\"stopTime\":\"2018-02-27T14:54:38.190463\",\"productType\":\"IW_GRDH_0A\",\"productClass\":\"A\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":218284.2,\"url\":\"S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE\",\"startTimeANX\":193285.8,\"validityStartTime\":\"2018-02-27T14:54:13.192073\",\"productFamily\":\"L1_ACN\",\"processMode\":\"NOMINAL\"}");

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE");
		descriptor.setRelativePath("S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE");
		descriptor.setSwathtype("IW");
		descriptor.setResolution("_");
		descriptor.setProductClass("A");
		descriptor.setProductType("IW_GRDH_1A");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("023A69");
		descriptor.setProductFamily(ProductFamily.L1_ACN);

		final File file = new File(testDir,
				"S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE/manifest.safe");

		try {
			final ProductMetadata result = extractor.processProduct(descriptor, ProductFamily.L1_ACN, file);

			assertNotNull("JSON object should not be null", result);
//			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			assertTrue("JSON object value validityStartTime are not equals",
					expectedResult.get("missionId").toString().equals(result.get("missionId").toString()));
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test(expected = AbstractCodedException.class)
	public void testProcessL1AMissingFileFail() throws MetadataExtractionException, MetadataMalformedException {

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE");
		descriptor.setRelativePath("S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE");
		descriptor.setSwathtype("IW");
		descriptor.setResolution("_");
		descriptor.setProductClass("A");
		descriptor.setProductType("IW_GRDH_0A");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("023A69");

		final File file = new File(testDir,
				"S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632B.SAFE/manifest.safe");

		extractor.processProduct(descriptor, ProductFamily.L1_ACN, file);
	}

	@Test
	public void testProcessIIFFile() {
		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"startTime\":\"2004-07-02T23:59:15.906000Z\", \"stopTime\":\"2004-07-03T00:04:15.906000Z\", \"creationTime\":\"2015-04-24T16:05:30.000000Z\", \"ISIPProvider\":\"L0PP\", \"dumpStart\":\"2004-07-02T22:44:15.906000\", \"receivingStartTime\":\"2012-05-11T18:25:35.000499Z\", \"receivingStopTime\":\"2012-05-11T18:27:35.000499Z\", \"receivingGroundStation\":\"dummy-text\", \"granuleNumber\":16, \"granulePosition\":\"NONE\", \"qualityIndicator\":\"APPROVED\", \"NRT\":true,\"STC\":false,\"NTC\":false, \"productName\":\"S3A_SL_0_SLT__G_20040702T235915_20040703T000415_20150424T160530___________________WER_D_______.ISIP\", \"productClass\":\"SL\", \"productType\":\"SL_0_SLT__G\", \"missionId\":\"S3\", \"satelliteId\":\"A\", \"url\":\"S3A_SL_0_SLT__G_20040702T235915_20040703T000415_20150424T160530___________________WER_D_______.ISIP\", \"productFamily\":\"S3_GRANULES\", \"instanceId\":\"_________________\", \"generatingCentre\":\"WER\", \"classId\":\"D_______\", \"processMode\":\"NOMINAL\"}");

		final S3FileDescriptor descriptor = new S3FileDescriptor();
		descriptor.setProductType("SL_0_SLT__G");
		descriptor.setProductClass("SL");
		descriptor.setRelativePath(
				"S3A_SL_0_SLT__G_20040702T235915_20040703T000415_20150424T160530___________________WER_D_______.ISIP/S3A_SL_0_SLT__G_20040702T235915_20040703T000415_20150424T160530___________________WER_D________iif.xml");
		descriptor.setFilename(
				"S3A_SL_0_SLT__G_20040702T235915_20040703T000415_20150424T160530___________________WER_D________iif.xml");
		descriptor.setExtension(FileExtension.ISIP);
		descriptor.setProductName(
				"S3A_SL_0_SLT__G_20040702T235915_20040703T000415_20150424T160530___________________WER_D_______.ISIP");
		descriptor.setMissionId("S3");
		descriptor.setSatelliteId("A");
		descriptor.setKeyObjectStorage(
				"S3A_SL_0_SLT__G_20040702T235915_20040703T000415_20150424T160530___________________WER_D_______.ISIP");
		descriptor.setProductFamily(ProductFamily.S3_GRANULES);
		descriptor.setInstanceId("_________________");
		descriptor.setGeneratingCentre("WER");
		descriptor.setClassId("D_______");
		descriptor.setMode("NRT");

		final File file = new File(testDir,
				"S3A_SL_0_SLT__G_20040702T235915_20040703T000415_20150424T160530___________________WER_D_______.ISIP/S3A_SL_0_SLT__G_20040702T235915_20040703T000415_20150424T160530___________________WER_D________iif.xml");

		try {
			final ProductMetadata result = extractor.processIIFFile(descriptor, file);

			assertNotNull("JSON object should not be null", result);
//			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			
			Iterator<String> it = expectedResult.keys().iterator();
			while (it.hasNext()) {
				String key = it.next();
				assertEquals("JSON object value " + key + " are not equals", expectedResult.get(key), result.get(key));
			}
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test
	public void testProcessAuxXFDUFile() {
		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"validityStartTime\":\"2004-07-02T22:30:00.000000Z\", \"validityStopTime\":\"2004-07-04T04:21:58.000000Z\", \"creationTime\":\"2017-11-30T08:21:16.000000Z\", \"adfQualityCheck\":\"OPASSED\", \"baselineCollection\":\"___\", \"site\":\"dummy-text\", \"NRT\":true,  \"STC\":true,  \"NTC\":true , \"productName\":\"S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3\", \"productClass\":\"AX\", \"productType\":\"AX___BA__AX\", \"missionId\":\"S3\", \"satelliteId\":\"A\", \"url\":\"S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3\", \"productFamily\":\"S3_AUX\", \"instanceId\":\"_________________\", \"generatingCentre\":\"WER\", \"classId\":\"D_AL____\", \"processMode\":\"NOMINAL\"}");

		final S3FileDescriptor descriptor = new S3FileDescriptor();
		descriptor.setProductType("AX___BA__AX");
		descriptor.setProductClass("AX");
		descriptor.setRelativePath(
				"S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3/xfdumanifest.xml");
		descriptor.setFilename("xfdumanifest.xml");
		descriptor.setExtension(FileExtension.SEN3);
		descriptor.setProductName(
				"S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3");
		descriptor.setMissionId("S3");
		descriptor.setSatelliteId("A");
		descriptor.setKeyObjectStorage(
				"S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3");
		descriptor.setProductFamily(ProductFamily.S3_AUX);
		descriptor.setInstanceId("_________________");
		descriptor.setGeneratingCentre("WER");
		descriptor.setClassId("D_AL____");
		descriptor.setMode("NRT");

		final File file = new File(testDir,
				"S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3/xfdumanifest.xml");

		try {
			final ProductMetadata result = extractor.processAuxXFDUFile(descriptor, file);

			assertNotNull("JSON object should not be null", result);
//			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			
			Iterator<String> it = expectedResult.keys().iterator();
			while (it.hasNext()) {
				String key = it.next();
				assertEquals("JSON object value " + key + " are not equals", expectedResult.get(key), result.get(key));
			}
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test
	public void testProcessProductXFDUFile() {
		final ProductMetadata expectedResult = ProductMetadata.ofJson(
				"{\"startTime\":\"2004-07-03T00:30:00.906000Z\", \"stopTime\":\"2004-07-03T00:32:00.906000Z\", \"creationTime\":\"2016-02-04T07:09:33.000000Z\", \"baselineCollection\":\"NNN\", \"sliceCoordinates\":{\"orientation\":\"counterclockwise\",\"coordinates\":[[[-75.312935,-14.93704],[-75.312935,-11.387472],[-14.312935,-11.387472],[-14.312935,-14.387472],[-75.312935,-14.93704]]],\"type\":\"polygon\"}, \"site\":\"dummy-text\", \"absoluteStartOrbit\":9895, \"absoluteStopOrbit\":9895, \"relativeStartOrbit\":2, \"relativeStopOrbit\":2, \"receivingGroundStation\":\"dummy-text\", \"instrumentName\":\"OLCI\", \"procTime\":\"2016-02-04T07:09:49.000845Z\", \"granuleNumber\":1, \"granulePosition\":\"FIRST\", \"dumpStart\":\"2004-07-03T00:28:17.706000Z\", \"utcTime\":\"2004-07-03T00:00:00.906000Z\", \"utc1Time\":\"2004-07-03T01:41:00.906000Z\", \"processingLevel\":1, \"procVersion\":1.0, \"procName\":\"ACQ-WERUM\", \"qualityIndicator\":\"dummy-text\", \"NRT\":true,\"STC\":false,\"NTC\":false, \"productName\":\"S3B_OL_1_EFR____20040703T003000_20040703T003200_20160204T070933_DDDD_001_002_FFFF_WER_D_NR_NNN.SEN3\", \"productClass\":\"OL\", \"productType\":\"OL_1_EFR___\", \"missionId\":\"S3\", \"satelliteId\":\"B\", \"url\":\"S3B_OL_1_EFR____20040703T003000_20040703T003200_20160204T070933_DDDD_001_002_FFFF_WER_D_NR_NNN.SEN3\", \"productFamily\":\"S3_L1_NRT\", \"instanceId\":\"DDDD_001_002_FFFF\", \"generatingCentre\":\"WER\", \"classId\":\"D_NR_NNN\", \"processMode\":\"NOMINAL\"}");

		final S3FileDescriptor descriptor = new S3FileDescriptor();
		descriptor.setProductType("OL_1_EFR___");
		descriptor.setProductClass("OL");
		descriptor.setRelativePath(
				"S3B_OL_1_EFR____20040703T003000_20040703T003200_20160204T070933_DDDD_001_002_FFFF_WER_D_NR_NNN.SEN3/xfdumanifest.xml");
		descriptor.setFilename("xfdumanifest.xml");
		descriptor.setExtension(FileExtension.SEN3);
		descriptor.setProductName(
				"S3B_OL_1_EFR____20040703T003000_20040703T003200_20160204T070933_DDDD_001_002_FFFF_WER_D_NR_NNN.SEN3");
		descriptor.setMissionId("S3");
		descriptor.setSatelliteId("B");
		descriptor.setKeyObjectStorage(
				"S3B_OL_1_EFR____20040703T003000_20040703T003200_20160204T070933_DDDD_001_002_FFFF_WER_D_NR_NNN.SEN3");
		descriptor.setProductFamily(ProductFamily.S3_L1_NRT);
		descriptor.setInstanceId("DDDD_001_002_FFFF");
		descriptor.setGeneratingCentre("WER");
		descriptor.setClassId("D_NR_NNN");
		descriptor.setMode("NRT");

		final File file = new File(testDir,
				"S3B_OL_1_EFR____20040703T003000_20040703T003200_20160204T070933_DDDD_001_002_FFFF_WER_D_NR_NNN.SEN3/xfdumanifest.xml");

		try {
			final ProductMetadata result = extractor.processProductXFDUFile(descriptor, file);

			assertNotNull("JSON object should not be null", result);
//			assertEquals("JSON object are not equals", expectedResult.length(), result.length());
			Iterator<String> it = expectedResult.keys().iterator();
			while (it.hasNext()) {
				String key = it.next();
				// boundPolygon is a JSONobject, so we test it seperately
				if (!"sliceCoordinates".equals(key)) {
					assertTrue(expectedResult.get(key).equals(result.get(key)));
				}
			}

//			// test boundPolygon
//			for (int i = 0; i < expectedResult.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).length(); i++) {
//				assertEquals("JSON object value sliceCoordinates/coordinates/" + i + " are not equals",
//						expectedResult.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(i),
//						result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(i));
//			}
		} catch (final AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test
	public void testTotalNumberOfSliceEW() {

		String startTime = "2019-12-09T00:34:49.567000Z";
		String stopTime = "2019-12-09T00:42:06.379000Z";
		String sliceType = "EW";

		assertEquals(8, extractor.totalNumberOfSlice(startTime, stopTime, sliceType));
	}

	@Test
	public void testTotalNumberOfSliceEWSmallerThanSliceOverlap() {

		String startTime = "2019-12-09T00:34:49.567000Z";
		String stopTime = "2019-12-09T00:42:04.379000Z";
		String sliceType = "EW";

		assertEquals(7, extractor.totalNumberOfSlice(startTime, stopTime, sliceType));
	}

	@Test
	public void testTotalNumberOfSliceEWDuationLessThanSliceLength() {

		String startTime = "2019-12-09T00:34:00.000000Z";
		String stopTime = "2019-12-09T00:35:00.000000Z";
		String sliceType = "EW";

		assertEquals(1, extractor.totalNumberOfSlice(startTime, stopTime, sliceType));
	}

	@Test
	public void testTotalNumberOfSliceEWDuationOneSliceLength() {

		String startTime = "2019-12-09T00:34:00.000000Z";
		String stopTime = "2019-12-09T00:35:08.200000Z";
		String sliceType = "EW";

		assertEquals(1, extractor.totalNumberOfSlice(startTime, stopTime, sliceType));
	}

	@Test
	public void testTotalNumberOfSliceEWDurationOnlyOneSecond() {

		String startTime = "2019-12-09T00:34:49.379000Z";
		String stopTime = "2019-12-09T00:34:50.379000Z";
		String sliceType = "EW";

		assertEquals(1, extractor.totalNumberOfSlice(startTime, stopTime, sliceType));
	}

	@Test
	public void testTotalNumberOfSliceIW() {

		String startTime = "2019-12-09T00:34:49.567000Z";
		String stopTime = "2019-12-09T00:36:00.379000Z";
		String sliceType = "IW";

		assertEquals(3, extractor.totalNumberOfSlice(startTime, stopTime, sliceType));
	}

	@Test
	public void testTotalNumberOfSliceSM() {

		String startTime = "2019-12-09T00:34:49.567000Z";
		String stopTime = "2019-12-09T00:36:00.379000Z";
		String sliceType = "SM";

		assertEquals(3, extractor.totalNumberOfSlice(startTime, stopTime, sliceType));
	}

	@Test
	public void testTotalNumberOfSliceWV() {

		String startTime = "2019-12-09T00:34:49.567000Z";
		String stopTime = "2019-12-09T00:36:00.379000Z";
		String sliceType = "WV";

		assertEquals(1, extractor.totalNumberOfSlice(startTime, stopTime, sliceType));
	}
	
	@Test
	public void testConvertCoordinatesToClosedForm_whenNotClosedShallClose() {
		String input = "81.3179,-81.9895 56.5997,-97.7338 56.0243,-91.2164 79.6240,-62.0955";
		String expected = "81.3179,-81.9895 56.5997,-97.7338 56.0243,-91.2164 79.6240,-62.0955 81.3179,-81.9895";
		assertTrue(expected.equals(ExtractMetadata.convertCoordinatesToClosedForm(input)));
	}

	@Test
	public void testConvertCoordinatesToClosedForm_whenClosedShallNop() {
		String input = "81.3179,-81.9895 56.5997,-97.7338 56.0243,-91.2164 79.6240,-62.0955 81.3179,-81.9895";
		String expected = "81.3179,-81.9895 56.5997,-97.7338 56.0243,-91.2164 79.6240,-62.0955 81.3179,-81.9895";
		assertTrue(expected.equals(ExtractMetadata.convertCoordinatesToClosedForm(input)));
	}

	@Test
	public void testEnforceFieldTypes_WhenLongRequested_ShallReturnLong()
			throws MetadataMalformedException {
		@SuppressWarnings("serial")
		final ExtractMetadata uut = new ExtractMetadata(null, null, new HashMap<String, String>() {
			{
				put("key", "long");
			}
		}, null, null, null, null, null);
		assertEquals("ProductMetadata should enforce long when requested", Long.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", 1);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce long when requested", Long.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", 1L);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce long when requested", Long.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "1");
			}
		}).get("key").getClass());
	}

	@Test(expected = MetadataMalformedException.class)
	public void testEnforceFieldTypes_WhenLongRequested_ShallOmitIncompatibleEmptyString()
			throws MetadataMalformedException {
		@SuppressWarnings("serial")
		final ExtractMetadata uut = new ExtractMetadata(null, null, new HashMap<String, String>() {
			{
				put("key", "long");
			}
		}, null, null, null, null, null);
		uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "");
			}
		}).get("key");
	}

	@Test
	public void testEnforceFieldTypes_WhenDoubleRequested_ShallReturnDouble()
			throws MetadataMalformedException {
		@SuppressWarnings("serial")
		final ExtractMetadata uut = new ExtractMetadata(null, null, new HashMap<String, String>() {
			{
				put("key", "double");
			}
		}, null, null, null, null, null);
		assertEquals("ProductMetadata should enforce double when requested", Double.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", 1);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce double when requested", Double.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", 1L);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce double when requested", Double.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", 1.0);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce double when requested", Double.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "1");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce double when requested", Double.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "1.0");
			}
		}).get("key").getClass());
	}

	@Test(expected = MetadataMalformedException.class)
	public void testEnforceFieldTypes_WhenDoubleRequested_ShallOmitIncompatibleEmptyString()
			throws MetadataMalformedException {
		@SuppressWarnings("serial")
		final ExtractMetadata uut = new ExtractMetadata(null, null, new HashMap<String, String>() {
			{
				put("key", "double");
			}
		}, null, null, null, null, null);
		uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "");
			}
		}).get("key");
	}

	@Test
	public void testEnforceFieldTypes_WhenBooleanRequested_ShallReturnBoolean()
			throws MetadataMalformedException {
		@SuppressWarnings("serial")
		final ExtractMetadata uut = new ExtractMetadata(null, null, new HashMap<String, String>() {
			{
				put("key", "boolean");
			}
		}, null, null, null, null, null);
		assertEquals("ProductMetadata should enforce boolean when requested", Boolean.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", true);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce boolean when requested", Boolean.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", false);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce boolean when requested", Boolean.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "true");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce boolean when requested", Boolean.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "false");
			}
		}).get("key").getClass());
	}

	@Test(expected = MetadataMalformedException.class)
	public void testEnforceFieldTypes_WhenBooleanRequested_ShallOmitIncompatibleEmptyString()
			throws MetadataMalformedException {
		@SuppressWarnings("serial")
		final ExtractMetadata uut = new ExtractMetadata(null, null, new HashMap<String, String>() {
			{
				put("key", "boolean");
			}
		}, null, null, null, null, null);
		uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "");
			}
		}).get("key");
	}

	@Test
	public void testEnforceFieldTypes_WhenDateRequested_ShallReturnDateString()
			throws MetadataMalformedException {
		@SuppressWarnings("serial")
		final ExtractMetadata uut = new ExtractMetadata(null, null, new HashMap<String, String>() {
			{
				put("key", "date");
			}
		}, null, null, null, null, null);
		assertEquals("ProductMetadata should enforce datestring when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "2020-01-19T03:11:42.000000Z");
			}
		}).get("key").getClass());
	}

	@Test(expected = MetadataMalformedException.class)
	public void testEnforceFieldTypes_WhenDateRequested_ShallOmitIncompatibleEmptyString()
			throws MetadataMalformedException {
		@SuppressWarnings("serial")
		final ExtractMetadata uut = new ExtractMetadata(null, null, new HashMap<String, String>() {
			{
				put("key", "date");
			}
		}, null, null, null, null, null);
		uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "");
			}
		}).get("key");
	}

	@Test
	public void testEnforceFieldTypes_WhenStringRequested_ShallReturnString()
			throws MetadataMalformedException {
		@SuppressWarnings("serial")
		final ExtractMetadata uut = new ExtractMetadata(null, null, new HashMap<String, String>() {
			{
				put("key", "string");
			}
		}, null, null, null, null, null);
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", 1);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", 1L);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "1");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", 1.0);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "1.0");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", 1.1);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "1.1");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "1.1.1");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", true);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "true");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", false);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "false");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "foobar");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should enforce string when requested", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "2020-01-19T03:11:42.000000Z");
			}
		}).get("key").getClass());
	}

	@Test
	public void testEnforceFieldTypes_WhenNotConfigured_ShallPassThru()
			throws MetadataMalformedException {
		final ExtractMetadata uut = new ExtractMetadata(null, null, Collections.<String, String>emptyMap(), null, null,
				null, null, null);
		assertEquals("ProductMetadata should have deterministic types", Integer.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", 1);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should have deterministic types", Long.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", 1L);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should have deterministic types", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "1");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should have deterministic types", Double.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", 1.0);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should have deterministic types", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "1.0");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should have deterministic types", Double.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", 1.1);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should have deterministic types", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "1.1");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should have deterministic types", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "1.1.1");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should have deterministic types", Boolean.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", true);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should have deterministic types", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "true");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should have deterministic types", Boolean.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", false);
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should have deterministic types", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "false");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should have deterministic types", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "foobar");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should have deterministic types", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "");
			}
		}).get("key").getClass());
		assertEquals("ProductMetadata should have deterministic types", String.class, uut.enforceFieldTypes(new ProductMetadata() {
			{
				put("key", "2020-01-19T03:11:42.000000Z");
			}
		}).get("key").getClass());
	}

	@Test
	public void testTransformFromOpengis() {
		final ExtractMetadata uut = new ExtractMetadata(null, null, Collections.<String, String>emptyMap(), null, null,
				null, null, null);
		Map<String, Object> transformed = uut.transformFromOpengis("-14.937040 -75.312935");
		@SuppressWarnings("unchecked")
		List<List<Double>> exteriorRing = (List<List<Double>>)((List<List<List<Double>>>)transformed.get("coordinates")).get(0);
		assertTrue(exteriorRing.contains(List.of(-75.312935,-14.93704)));
		try {
			uut.transformFromOpengis("-14.937040 -75.312935 50");
			Assert.fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			// Expected
		}

		try {
			uut.transformFromOpengis("-14.937040");
			Assert.fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			// Expected
		}
	}

	@Test
	public void testProcessS3Coordinates() throws MetadataMalformedException {
		final ExtractMetadata uut = new ExtractMetadata(null, null, Collections.<String, String>emptyMap(), null, null,
				null, null, null);
		ProductMetadata metadtaa = uut.processS3Coordinates(new ProductMetadata() {
			{
				put("sliceCoordinates",
						"-14.937040 -75.312935 -11.387472 -75.312935 -11.387472 -14.312935 -14.387472 -14.312935 -14.937040 -75.312935");
			}
		});
		System.out.println(metadtaa.toString());
		assertTrue(metadtaa.toString().contains(
				"[[[-75.312935,-14.93704],[-75.312935,-11.387472],[-14.312935,-11.387472],[-14.312935,-14.387472],[-75.312935,-14.93704]]]"));
	}

	@Test
	public void testProcessS3Coordinates2() throws MetadataMalformedException {
		final ExtractMetadata uut = new ExtractMetadata(null, null, Collections.<String, String>emptyMap(), null, null,
				null, null, null);
		ProductMetadata metadata = uut.processS3Coordinates(new ProductMetadata() {
			{
				put("sliceCoordinates",
						"38.1771 119.085 37.5391 118.884 36.9008 118.686 36.2623 118.491 35.6235 118.297 34.9844 118.106 34.3451 117.918 38.1771 119.085");
			}
		});
		System.out.println(metadata.toString());
		assertTrue(metadata.toString().contains(
				"[[[119.085,38.1771],[118.884,37.5391],[118.686,36.9008],[118.491,36.2623],[118.297,35.6235],[118.106,34.9844],[117.918,34.3451],[119.085,38.1771]]]"));
	}
}
