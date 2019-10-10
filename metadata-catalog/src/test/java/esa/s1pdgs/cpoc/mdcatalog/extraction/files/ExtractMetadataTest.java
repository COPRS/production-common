/**
 * 
 */
package esa.s1pdgs.cpoc.mdcatalog.extraction.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

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

/**
 * @author Olivier BEX-CHAUVET
 */
public class ExtractMetadataTest {

    private ExtractMetadata extractor;

    XmlConverter xmlConverter;
    
	private static String LOCAL_DIRECTORY = ExtractMetadataTest.class.getResource("/").getFile().concat("../../test/workDir").toString();

	@Before
    public void init() {
		xmlConverter = new XmlConverter();
		Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
		jaxb2Marshaller.setPackagesToScan("esa.s1pdgs.cpoc.mdcatalog.extraction.model");
		Map<String, Object> map = new ConcurrentHashMap<String, Object>();
		map.put("jaxb.formatted.output", true);
		map.put("jaxb.encoding", "UTF-8");
		jaxb2Marshaller.setMarshallerProperties(map);
		xmlConverter.setMarshaller(jaxb2Marshaller);
		xmlConverter.setUnmarshaller(jaxb2Marshaller);
		
        // "EW:8.2F||IW:7.4F||SM:7.7F||WM:0.0F"
        Map<String, Float> typeOverlap = new HashMap<String, Float>();
        typeOverlap.put("EW", 8.2F);
        typeOverlap.put("IW", 7.4F);
        typeOverlap.put("SM", 7.7F);
        typeOverlap.put("WV", 0.0F);
        // "EW:60.0F||IW:25.0F||SM:25.0F||WM:0.0F"
        Map<String, Float> typeSliceLength = new HashMap<String, Float>();
        typeSliceLength.put("EW", 60.0F);
        typeSliceLength.put("IW", 25.0F);
        typeSliceLength.put("SM", 25.0F);
        typeSliceLength.put("WV", 0.0F);
        extractor = new ExtractMetadata(typeOverlap, typeSliceLength,
                "config/xsltDir/", xmlConverter, LOCAL_DIRECTORY);
    }

    @Test
    public void testProcessXMLFile() {

        JSONObject expectedResult = new JSONObject(
                "{\"validityStopTime\":\"9999-12-31T23:59:59.000000Z\",\"productClass\":\"OPER\",\"missionid\":\"S1\",\"creationTime\":\"2014-02-12T12:28:19.000000Z\",\"insertionTime\":\"2018-05-31T14:34:17.000000Z\",\"satelliteid\":\"A\",\"validityStartTime\":\"2014-02-01T00:00:00.000000Z\",\"productName\":\"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml\",\"url\":\"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml\",\"productType\":\"AUX_OBMEMC\",\"productFamily\":\"AUXILIARY_FILE\"}");

        ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
        descriptor.setExtension(FileExtension.XML);
        descriptor.setFilename("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        descriptor.setKeyObjectStorage(
                "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductClass("OPER");
        descriptor
                .setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        descriptor.setProductType("AUX_OBMEMC");
        descriptor.setRelativePath(
                "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        descriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);
        File file = new File(
                "test/workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

        try {
            JSONObject result = extractor.processXMLFile(descriptor, file);

            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("JSON object value validityStartTime are not equals",
                    expectedResult.get("validityStartTime").toString(),
                    result.get("validityStartTime").toString());
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }

        expectedResult = new JSONObject(
                "{\"validityStopTime\":\"2099-12-31T23:59:59.000000Z\",\"productClass\":\"OPER\",\"missionId\":\"S1\",\"creationTime\":\"2014-02-12T12:28:19.000000Z\",\"insertionTime\":\"2018-06-04T09:38:40.000000Z\",\"satelliteId\":\"B\",\"validityStartTime\":\"2014-02-01T00:00:00.000000Z\",\"productName\":\"S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml\",\"productType\":\"AUX_OBMEMC\",\"url\":\"S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml\",\"productFamily\":\"AUXILIARY_FILE\"}");

        descriptor = new ConfigFileDescriptor();
        descriptor.setExtension(FileExtension.XML);
        descriptor.setFilename("S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml");
        descriptor.setKeyObjectStorage(
                "S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("B");
        descriptor.setProductClass("OPER");
        descriptor
                .setProductName("S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml");
        descriptor.setProductType("AUX_OBMEMC");
        descriptor.setRelativePath(
                "S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml");
        descriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);

        file = new File(
                "test/workDir/S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml");

        try {
            JSONObject result = extractor.processXMLFile(descriptor, file);

            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("JSON object value validityStartTime are not equals",
                    expectedResult.get("validityStartTime").toString(),
                    result.get("validityStartTime").toString());
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test(expected = AbstractCodedException.class)
    public void testProcessXMLMissingFileFail()
            throws MetadataExtractionException, MetadataMalformedException {

        ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
        descriptor.setExtension(FileExtension.XML);
        descriptor.setFilename("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        descriptor.setKeyObjectStorage(
                "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductClass("OPER");
        descriptor
                .setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        descriptor.setProductType("AUX_OBMEMC");
        descriptor.setRelativePath(
                "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        descriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);
        File file = new File(
                "test/workDir/S1A_OPER_OUX_OBMEMC_PDMC_20140201T000000.xml");

        extractor.processXMLFile(descriptor, file);
    }

    @Test
    public void testProcessEOFFile() {

        JSONObject expectedResult = new JSONObject(
                "{\"validityStopTime\":\"2017-12-15T20:03:09.000000Z\",\"productClass\":\"OPER\",\"missionid\":\"S1\",\"creationTime\":\"2017-12-08T20:02:13.000000Z\",\"insertionTime\":\"2018-05-31T14:34:17.000000Z\",\"satelliteid\":\"A\",\"validityStartTime\":\"2017-12-08T20:03:09.000000Z\",\"version\":\"0001\",\"productName\":\"S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF\",\"url\":\"S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF\",\"productType\":\"MPL_ORBPRE\",\"productFamily\":\"AUXILIARY_FILE\"}");

        ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
        descriptor.setExtension(FileExtension.EOF);
        descriptor.setFilename(
                "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
        descriptor.setKeyObjectStorage(
                "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductClass("OPER");
        descriptor.setProductName(
                "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
        descriptor.setProductType("MPL_ORBPRE");
        descriptor.setRelativePath(
                "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
        descriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);

        File file = new File(
                "test/workDir/S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");

        try {
            JSONObject result = extractor.processEOFFile(descriptor, file);

            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("JSON object value validityStartTime are not equals",
                    expectedResult.getString("validityStartTime"),
                    result.getString("validityStartTime"));
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }
    
    @Test
    public void testProcessLandMskFile() {

        JSONObject expectedResult = new JSONObject(
                "{\"validityStopTime\":\"9999-12-31T00:00:00.000000Z\",\"productClass\":\"OPER\",\"missionid\":\"S1\",\"creationTime\":\"2019-07-11T11:33:00.000000Z\",\"insertionTime\":\"2018-05-31T14:34:17.000000Z\",\"satelliteid\":\"A\",\"validityStartTime\":\"2014-04-03T21:02:00.000000Z\",\"version\":\"0001\",\"productName\":\"S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF\",\"url\":\"S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF\",\"productType\":\"MSK__LAND_\",\"productFamily\":\"AUXILIARY_FILE\"}");

        ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
        descriptor.setExtension(FileExtension.EOF);
        descriptor.setFilename(
                "S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
        descriptor.setKeyObjectStorage(
                "S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("_");
        descriptor.setProductClass("OPER");
        descriptor.setProductName(
                "S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
        descriptor.setProductType("MSK__LAND_");
        descriptor.setRelativePath(
                "S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
        descriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);

        File file = new File(
                "test/workDir/S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");

        try {
            JSONObject result = extractor.processEOFFile(descriptor, file);

            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("JSON object value validityStartTime are not equals",
                    expectedResult.getString("validityStartTime"),
                    result.getString("validityStartTime"));
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test(expected = AbstractCodedException.class)
    public void testProcessEOFMissingFileFail()
            throws MetadataExtractionException, MetadataMalformedException {

        ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
        descriptor.setExtension(FileExtension.EOF);
        descriptor.setFilename(
                "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
        descriptor.setKeyObjectStorage(
                "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductClass("OPER");
        descriptor.setProductName(
                "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
        descriptor.setProductType("MPL_ORBPRE");
        descriptor.setRelativePath(
                "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");

        File file = new File(
                "test/workDir/S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0003.EOF");

        extractor.processEOFFile(descriptor, file);
    }

    @Test
    public void testProcessEOFFileWithoutFile() {

        JSONObject expectedResult = new JSONObject(
                "{\"validityStopTime\":\"2017-12-13T13:45:07.000000Z\",\"productClass\":\"OPER\",\"missionid\":\"S1\",\"creationTime\":\"2017-12-13T14:38:38.000000Z\",\"insertionTime\":\"2018-05-31T14:34:18.000000Z\",\"satelliteid\":\"A\",\"validityStartTime\":\"2017-12-13T10:27:37.000000Z\",\"version\":\"0001\",\"productType\":\"AUX_RESORB\",\"productName\":\"S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF\",\"url\":\"S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF\",\"productFamily\":\"AUXILIARY_FILE\"}");

        ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
        descriptor.setExtension(FileExtension.EOF);
        descriptor.setFilename(
                "S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");
        descriptor.setKeyObjectStorage(
                "S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductClass("OPER");
        descriptor.setProductName(
                "S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");
        descriptor.setProductType("AUX_RESORB");
        descriptor.setRelativePath(
                "S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");
        descriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);

        File file = new File(
                "test/workDir/S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");

        try {
            JSONObject result =
                    extractor.processEOFFileWithoutNamespace(descriptor, file);

            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("JSON object value validityStartTime are not equals",
                    expectedResult.get("validityStartTime").toString(),
                    result.get("validityStartTime").toString());
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test(expected = AbstractCodedException.class)
    public void testProcessEOFFileWithoutMissingFileFail()
            throws MetadataExtractionException, MetadataMalformedException {

        ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
        descriptor.setExtension(FileExtension.EOF);
        descriptor.setFilename(
                "S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");
        descriptor.setKeyObjectStorage(
                "S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductClass("OPER");
        descriptor.setProductName(
                "S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");
        descriptor.setProductType("AUX_RESORB");
        descriptor.setRelativePath(
                "S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF");

        File file = new File(
                "test/workDir/S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134508.EOF");

        extractor.processEOFFileWithoutNamespace(descriptor, file);
    }

    @Test
    public void testProcessSAFEFile() throws MetadataExtractionException {

        JSONObject expectedResult = new JSONObject(
                "{\"validityStopTime\":\"9999-12-31T23:59:59.000000Z\",\"site\":\"CLS-Brest\",\"missionid\":\"S1\",\"creationTime\":\"2017-10-13T10:12:00.000000Z\",\"insertionTime\":\"2018-05-31T14:34:17.000000Z\",\"satelliteid\":\"A\",\"instrumentConfigurationId\":\"6\",\"validityStartTime\":\"2017-10-17T08:00:00.000000Z\",\"productName\":\"S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE\",\"productType\":\"AUX_CAL\",\"url\":\"S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE\",\"productFamily\":\"AUXILIARY_FILE\"}");

        ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductClass("OPER");
        descriptor.setProductName(
                "S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE");
        descriptor.setProductType("AUX_CAL");
        descriptor.setRelativePath(
                "S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE");
        descriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);

        File file = new File(
                "test/workDir/S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE/manifest.safe");

       try {
            JSONObject result = extractor.processSAFEFile(descriptor, file);

            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("JSON object value validityStartTime are not equals",
                    expectedResult.get("validityStartTime").toString(),
                    result.get("validityStartTime").toString());
       } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
       }
    }

    @Test(expected = AbstractCodedException.class)
    public void testProcessSAFEFileFail() throws MetadataExtractionException, MetadataMalformedException {

        ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductClass("OPER");
        descriptor.setProductName(
                "S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE");
        descriptor.setProductType("AUX_CAL");
        descriptor.setRelativePath(
                "S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE");

        File file = new File(
                "test/workDir/S1A_AUX_CAL_V20171017T080000_G20171013T101201.SAFE");

        extractor.processSAFEFile(descriptor, file);
    }

    @Test
    public void testProcessRAWFile() {

        JSONObject expectedResult = new JSONObject(
                "{\"insertionTime\":\"2018-02-07T13:26:12\",\"missionId\":\"S1\",\"sessionId\":\"707000180\",\"productName\":\"DCS_02_L20171109175634707000180_ch1_DSDB_00001.raw\",\"satelliteId\":\"A\",\"productType\":\"RAW\",\"url\":\"SESSION1/DCS_02_SESSION1_ch1_DSIB.xml\",\"productFamily\":\"EDRS_SESSION\"}");

        EdrsSessionFileDescriptor descriptor = new EdrsSessionFileDescriptor();
        descriptor.setExtension(FileExtension.RAW);
        descriptor.setFilename(
                "DCS_02_L20171109175634707000180_ch1_DSDB_00001.raw");
        descriptor.setKeyObjectStorage(
                "S1A/707000180/ch01/DCS_02_L20171109175634707000180_ch1_DSDB_00001.raw");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName(
                "DCS_02_L20171109175634707000180_ch1_DSDB_00001.raw");
        descriptor.setEdrsSessionFileType(EdrsSessionFileType.RAW);
        descriptor.setRelativePath(
                "S1A/707000180/ch01/DCS_02_L20171109175634707000180_ch1_DSDB_00001.raw");
        descriptor.setChannel(1);
        descriptor.setSessionIdentifier("707000180");
        descriptor.setProductFamily(ProductFamily.EDRS_SESSION);

        try {
            JSONObject result = extractor.processRAWFile(descriptor);

            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("JSON object value validityStartTime are not equals",
                    expectedResult.get("missionId").toString(),
                    result.get("missionId").toString());
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test
    public void testProcessSESSIONFile() {
        JSONObject expectedResult = new JSONObject(
                "{\"insertionTime\":\"2018-02-07T13:26:12\",\"missionId\":\"S1\",\"sessionId\":\"SESSION1\",\"timeStart\":\"2017-12-13T14:59:48Z\",\"timeStop\":\"2017-12-13T15:17:25Z\",\"rawNames\":[],\"productName\":\"DCS_02_SESSION1_ch1_DSIB.xml\",\"satelliteId\":\"A\",\"productType\":\"SESSION\",\"url\":\"SESSION1/DCS_02_SESSION1_ch1_DSIB.xml\",\"productFamily\":\"EDRS_SESSION\"}");

        EdrsSessionFileDescriptor descriptor = new EdrsSessionFileDescriptor();
        descriptor.setExtension(FileExtension.XML);
        descriptor.setFilename("DCS_02_SESSION1_ch1_DSIB.xml");
        descriptor.setKeyObjectStorage(
                "S1A/SESSION1/ch01/DCS_02_SESSION1_ch1_DSIB.xml");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName("DCS_02_SESSION1_ch1_DSIB.xml");
        descriptor.setEdrsSessionFileType(EdrsSessionFileType.SESSION);
        descriptor.setRelativePath(
                "S1A/SESSION1/ch01/DCS_02_SESSION1_ch1_DSIB.xml");
        descriptor.setChannel(1);
        descriptor.setSessionIdentifier("SESSION1");
        descriptor.setProductFamily(ProductFamily.EDRS_SESSION);
        descriptor.setSessionIdentifier("sessionId");
        try {
            JSONObject result = extractor.processSESSIONFile(descriptor);
            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("JSON object value validityStartTime are not equals",
                    expectedResult.get("missionId").toString(),
                    result.get("missionId").toString());
        } catch (AbstractCodedException fe) {
        	fe.printStackTrace();
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test
    public void testProcessL0SlicesFileIW() {

        JSONObject expectedResult = new JSONObject(
                "{\"missionDataTakeId\":137013,\"theoreticalSliceLength\":25,\"sliceCoordinates\":{\"coordinates\":[[[86.8273,36.7787],[86.4312,38.7338],[83.6235,38.4629],[84.0935,36.5091],[86.8273,36.7787]]],\"type\":\"polygon\"},\"insertionTime\":\"2018-05-31T14:34:18\",\"creationTime\":\"2018-05-31T15:33:43\",\"polarisation\":\"DV\",\"sliceNumber\":13,\"absoluteStopOrbit\":19684,\"resolution\":\"_\",\"circulationFlag\":13,\"productName\":\"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe\",\"dataTakeId\":\"021735\",\"productConsolidation\":\"SLICE\",\"absoluteStartOrbit\":19684,\"validityStopTime\":\"2017-12-13T12:16:56.085136Z\",\"instrumentConfigurationId\":6,\"relativeStopOrbit\":12,\"relativeStartOrbit\":12,\"startTime\":\"2017-12-13T12:16:23.685188Z\",\"stopTime\":\"2017-12-13T12:16:56.085136Z\",\"productType\":\"IW_RAW__0S\",\"productClass\":\"S\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":628491.556,\"url\":\"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE\",\"sliceOverlap\":7.4,\"startTimeANX\":\"596091.6080\",\"validityStartTime\":\"2017-12-13T12:16:23.685188Z\",\"processMode\":\"FAST\",\"productFamily\":\"L0_SLICE\"}");

        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setRelativePath(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setSwathtype("IW");
        descriptor.setResolution("_");
        descriptor.setProductClass("S");
        descriptor.setProductType("IW_RAW__0S");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("021735");
        descriptor.setProductFamily(ProductFamily.L0_SLICE);
        descriptor.setMode("FAST");

        File file = new File(
                "test/workDir/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");

        try {
            JSONObject result = extractor.processProduct(descriptor, ProductFamily.L0_SLICE, file);
            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("JSON object value validityStartTime are not equals",
                    expectedResult.getString("productConsolidation"),
                    result.getString("productConsolidation"));
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test
    public void testProcessL0SlicesFileWV() {
        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName(
                "S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE");
        descriptor.setRelativePath(
                "S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE");
        descriptor.setSwathtype("WV");
        descriptor.setResolution("_");
        descriptor.setProductClass("S");
        descriptor.setProductType("WV_RAW__0S");
        descriptor.setPolarisation("SV");
        descriptor.setDataTakeId("0294F4");
        descriptor.setProductFamily(ProductFamily.L0_SLICE);
        descriptor.setMode("FAST");

        File file = new File(
                "test/workDir/S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE/manifest.safe");

        try {
            JSONObject result = extractor.processProduct(descriptor, ProductFamily.L0_SLICE, file);
            
            assertNotNull("JSON object should not be null", result);
            assertEquals("linestring", result.getJSONObject("sliceCoordinates").getString("type"));
            assertEquals(new JSONArray("[108.5909,-62.2900]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").get(0).toString());
            assertEquals(new JSONArray("[105.8055,-65.5655]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").get(1).toString());
            assertEquals(1, result.getInt("sliceNumber"));
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test(expected = AbstractCodedException.class)
    public void testProcessL0SlicesMissingFileFail()
            throws MetadataExtractionException, MetadataMalformedException {

        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setRelativePath(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setSwathtype("IW");
        descriptor.setResolution("_");
        descriptor.setProductClass("S");
        descriptor.setProductType("IW_RAW__0S");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("021735");

        File file = new File(
                "test/workDir/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DA.SAFE/manifest.safe");

        extractor.processProduct(descriptor, ProductFamily.L0_SLICE, file);
    }

    @Test
    public void testProcessL0SegmentFile() {

        JSONObject expectedResult = new JSONObject(
                "{\"missionDataTakeId\":72627,\"productFamily\":\"L0_SEGMENT\",\"insertionTime\":\"2018-10-15T11:44:03.000000Z\",\"creationTime\":\"2018-10-15T11:44:03.000000Z\",\"polarisation\":\"DV\",\"absoluteStopOrbit\":9809,\"resolution\":\"_\",\"circulationFlag\":7,\"productName\":\"S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE\",\"dataTakeId\":\"021735\",\"productConsolidation\":\"FULL\",\"absoluteStartOrbit\":9809,\"validityStopTime\":\"2018-02-27T12:53:00.422905Z\",\"instrumentConfigurationId\":1,\"relativeStopOrbit\":158,\"relativeStartOrbit\":158,\"startTime\":\"2018-02-27T12:51:14.794304Z\",\"stopTime\":\"2018-02-27T12:53:00.422905Z\",\"productType\":\"IW_RAW__0S\",\"productClass\":\"S\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"DESCENDING\",\"satelliteId\":\"B\",\"stopTimeANX\":1849446.881,\"url\":\"S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE\",\"startTimeANX\":1743818.281,\"validityStartTime\":\"2018-02-27T12:51:14.794304Z\",\"segmentCoordinates\":{\"coordinates\":[[[-94.8783,73.8984],[-98.2395,67.6029],[-88.9623,66.8368],[-82.486,72.8925],[-94.8783,73.8984]]],\"type\":\"polygon\"},\"processMode\":\"FAST\"}");

        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("B");
        descriptor.setProductName(
                "S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE");
        descriptor.setRelativePath(
                "S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE");
        descriptor.setSwathtype("IW");
        descriptor.setResolution("_");
        descriptor.setProductClass("S");
        descriptor.setProductType("IW_RAW__0S");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("021735");
        descriptor.setProductFamily(ProductFamily.L0_SEGMENT);
        descriptor.setMode("FAST");

        File file = new File(
                "test/workDir/S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE/manifest.safe");

        try {
            JSONObject result = extractor.processL0Segment(descriptor, file);
            
            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("JSON object value validityStartTime are not equals",
                    expectedResult.getString("productConsolidation"),
                    result.getString("productConsolidation"));
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test(expected = AbstractCodedException.class)
    public void testProcessL0SegmentMissingFileFail()
            throws MetadataExtractionException, MetadataMalformedException {

        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("B");
        descriptor.setProductName(
                "S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE");
        descriptor.setRelativePath(
                "S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE");
        descriptor.setSwathtype("IW");
        descriptor.setResolution("_");
        descriptor.setProductClass("S");
        descriptor.setProductType("IW_RAW__0S");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("021735");

        File file = new File(
                "test/workDir/S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DD.SAFE/manifest.safe");

        extractor.processL0Segment(descriptor, file);
    }

    @Test
    public void testProcessL0ACNFile() {

        JSONObject expectedResult = new JSONObject(
                "{\"missionDataTakeId\":137013,\"totalNumberOfSlice\":20,\"sliceCoordinates\":{\"coordinates\":[[[90.3636,18.6541],[84.2062,49.0506],[80.8613,48.7621],[88.0584,18.3765],[90.3636,18.6541]]],\"type\":\"polygon\"},\"insertionTime\":\"2018-05-30T14:27:43\",\"creationTime\":\"2018-05-31T15:43:33\",\"polarisation\":\"DV\",\"absoluteStopOrbit\":19684,\"resolution\":\"_\",\"circulationFlag\":13,\"productName\":\"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE\",\"dataTakeId\":\"021735\",\"productConsolidation\":\"FULL\",\"absoluteStartOrbit\":19684,\"validityStopTime\":\"2017-12-13T12:19:47.264351Z\",\"instrumentConfigurationId\":6,\"relativeStopOrbit\":12,\"relativeStartOrbit\":12,\"startTime\":\"2017-12-13T12:11:23.682488Z\",\"stopTime\":\"2017-12-13T12:19:47.264351Z\",\"productType\":\"IW_RAW__0A\",\"productClass\":\"A\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":799670.769,\"url\":\"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE/manifest.safe\",\"startTimeANX\":296088.912,\"validityStartTime\":\"2017-12-13T12:11:23.682488Z\",\"sliceNumber\":\"\",\"sliceOverlap\":\"\",\"theoreticalSliceLength\":\"\",\"processMode\":\"FAST\",\"productFamily\":\"L0_ACN\"}");

        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName(
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE");
        descriptor.setRelativePath(
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE");
        descriptor.setSwathtype("IW");
        descriptor.setResolution("_");
        descriptor.setProductClass("A");
        descriptor.setProductType("IW_RAW__0A");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("021735");
        descriptor.setProductFamily(ProductFamily.L0_ACN);
        descriptor.setMode("FAST");

        File file = new File(
                "test/workDir/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE/manifest.safe");

        try {
            JSONObject result = extractor.processProduct(descriptor,ProductFamily.L0_ACN, file);
            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("JSON object value validityStartTime are not equals",
                    expectedResult.get("missionId").toString(),
                    result.get("missionId").toString());

            assertEquals("JSON object value totalNumberOfSlice are not equals",
                    expectedResult.get("totalNumberOfSlice").toString(),
                    result.get("totalNumberOfSlice").toString());

        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }

        expectedResult = new JSONObject(
                "{\"missionDataTakeId\":146024,\"productFamily\":\"L0_SLICE\",\"totalNumberOfSlice\":1,\"theoreticalSliceLength\":\"\",\"sliceCoordinates\":{\"orientation\":\"counterclockwise\",\"coordinates\":[[[56.2016,-13.3062],[56.1801,-13.2193],[52.4759,-13.8154],[52.4961,-13.9029],[56.2016,-13.3062]]],\"type\":\"polygon\"},\"insertionTime\":\"2019-07-31T14:12:50.250000Z\",\"creationTime\":\"2019-07-31T14:12:50.250000Z\",\"polarisation\":\"DV\",\"sliceNumber\":1,\"absoluteStopOrbit\":20793,\"resolution\":\"_\",\"circulationFlag\":3,\"productName\":\"S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE\",\"dataTakeId\":\"023A68\",\"productConsolidation\":\"PARTIAL\",\"absoluteStartOrbit\":20793,\"validityStopTime\":\"2018-02-27T14:47:06.722008Z\",\"instrumentConfigurationId\":6,\"relativeStopOrbit\":71,\"relativeStartOrbit\":71,\"startTime\":\"2018-02-27T14:47:04.973656Z\",\"stopTime\":\"2018-02-27T14:47:06.722008Z\",\"productType\":\"EW_RAW__0C\",\"productClass\":\"C\",\"missionId\":\"S1\",\"swathtype\":\"EW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"B\",\"stopTimeANX\":5691467.842,\"url\":\"S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE\",\"sliceOverlap\":\"\",\"startTimeANX\":5689719.49,\"validityStartTime\":\"2018-02-27T14:47:04.973656Z\",\"processMode\":\"NRT\"}");

        descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName(
                "S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE");
        descriptor.setRelativePath(
                "S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE");
        descriptor.setSwathtype("EW");
        descriptor.setResolution("_");
        descriptor.setProductClass("C");
        descriptor.setProductType("EW_RAW__0S");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("023A68");
        descriptor.setProductFamily(ProductFamily.L0_SLICE);
        descriptor.setMode("NRT");

        file = new File(
                "test/workDir/S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE/manifest.safe");

        try {
            JSONObject result = extractor.processProduct(descriptor, ProductFamily.L0_SLICE, file);

            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("JSON object value validityStartTime are not equals",
                    expectedResult.get("productConsolidation").toString(),
                    result.get("productConsolidation").toString());
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }

        expectedResult = new JSONObject(
                "{\"missionDataTakeId\":146024,\"productFamily\":\"L0_SLICE\",\"totalNumberOfSlice\":1,\"theoreticalSliceLength\":\"\",\"sliceCoordinates\":{\"orientation\":\"counterclockwise\",\"coordinates\":[[[56.2016,-13.3062],[56.1801,-13.2193],[52.4759,-13.8154],[52.4961,-13.9029],[56.2016,-13.3062]]],\"type\":\"polygon\"},\"insertionTime\":\"2019-07-31T14:13:49.988000Z\",\"creationTime\":\"2019-07-31T14:13:49.988000Z\",\"polarisation\":\"DV\",\"sliceNumber\":1,\"absoluteStopOrbit\":20793,\"resolution\":\"_\",\"circulationFlag\":3,\"productName\":\"S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE\",\"dataTakeId\":\"023A68\",\"productConsolidation\":\"PARTIAL\",\"absoluteStartOrbit\":20793,\"validityStopTime\":\"2018-02-27T14:47:06.722008Z\",\"instrumentConfigurationId\":6,\"relativeStopOrbit\":71,\"relativeStartOrbit\":71,\"startTime\":\"2018-02-27T14:47:04.973656Z\",\"stopTime\":\"2018-02-27T14:47:06.722008Z\",\"productType\":\"EW_RAW__0C\",\"productClass\":\"C\",\"missionId\":\"S1\",\"swathtype\":\"EW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"B\",\"stopTimeANX\":5691467.842,\"url\":\"S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE\",\"sliceOverlap\":\"\",\"startTimeANX\":5689719.49,\"validityStartTime\":\"2018-02-27T14:47:04.973656Z\",\"processMode\":\"NRT\"}");

        descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("B");
        descriptor.setProductName(
                "S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE");
        descriptor.setRelativePath(
                "S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE");
        descriptor.setSwathtype("EW");
        descriptor.setResolution("_");
        descriptor.setProductClass("C");
        descriptor.setProductType("EW_RAW__0C");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("023A68");
        descriptor.setProductFamily(ProductFamily.L0_SLICE);
        descriptor.setMode("NRT");

        file = new File(
                "test/workDir/S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE/manifest.safe");

        try {
            JSONObject result = extractor.processProduct(descriptor, ProductFamily.L0_SLICE, file);
            
            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("JSON object value totalNumberOfSlice are not equals",
                    expectedResult.get("totalNumberOfSlice").toString(),
                    result.get("totalNumberOfSlice").toString());
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }

    }

    @Test(expected = AbstractCodedException.class)
    public void testProcessL0ACNMissingFileFail()
            throws MetadataExtractionException, MetadataMalformedException {

        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName(
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE");
        descriptor.setRelativePath(
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE");
        descriptor.setSwathtype("IW");
        descriptor.setResolution("_");
        descriptor.setProductClass("A");
        descriptor.setProductType("IW_RAW__0A");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("021735");

        File file = new File(
                "test/workDir/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B2.SAFE/manifest.safe");

        extractor.processProduct(descriptor, ProductFamily.L0_ACN, file);
    }

    @Test
    public void testProcessL1SlicesFile() {

        JSONObject expectedResult = new JSONObject(
                "{\"missionDataTakeId\":146025,\"sliceCoordinates\":{\"coordinates\":[[[48.27924,12.378114],[50.603844,12.829241],[50.958828,11.081389],[48.64994,10.625828],[48.27924,12.378114]]],\"type\":\"polygon\"},\"insertionTime\":\"2018-06-01T11:40:35\",\"creationTime\":\"2018-06-01T11:40:35\",\"polarisation\":\"DV\",\"sliceNumber\":1,\"absoluteStopOrbit\":20794,\"resolution\":\"_\",\"productName\":\"S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE\",\"dataTakeId\":\"023A69\",\"absoluteStartOrbit\":20794,\"validityStopTime\":\"2018-02-27T14:54:13.190581\",\"instrumentConfigurationId\":6,\"relativeStopOrbit\":72,\"relativeStartOrbit\":72,\"startTime\":\"2018-02-27T14:53:44.184986\",\"stopTime\":\"2018-02-27T14:54:13.190581\",\"productType\":\"IW_GRDH_0S\",\"productClass\":\"S\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":193284.4,\"url\":\"S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE\",\"startTimeANX\":164278.8,\"validityStartTime\":\"2018-02-27T14:53:44.184986\",\"productFamily\":\"L1_SLICE\"}");

        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName(
                "S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE");
        descriptor.setRelativePath(
                "S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE");
        descriptor.setSwathtype("IW");
        descriptor.setResolution("_");
        descriptor.setProductClass("S");
        descriptor.setProductType("IW_GRDH_1S");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("023A69");
        descriptor.setProductFamily(ProductFamily.L1_SLICE);

        File file = new File(
                "test/workDir/S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE/manifest.safe");

        try {
            JSONObject result = extractor.processProduct(descriptor, ProductFamily.L1_SLICE, file);
            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("polygon", result.getJSONObject("sliceCoordinates").getString("type"));
            assertEquals(new JSONArray("[48.64994,10.625828]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(0).toString());
            assertEquals(new JSONArray("[48.27924,12.378114]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(1).toString());
            assertEquals(new JSONArray("[50.603844,12.829241]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(2).toString());
            assertEquals(new JSONArray("[50.958828,11.081389]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(3).toString());
            assertEquals(new JSONArray("[48.64994,10.625828]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(4).toString());
            assertEquals("JSON object value validityStartTime are not equals",
                    expectedResult.get("absoluteStopOrbit").toString(),
                    result.get("absoluteStopOrbit").toString());
            assertEquals("2018-02-27T14:53:44.184986Z", result.get("validityStartTime"));
            assertEquals("2018-02-27T14:54:13.190581Z", result.get("validityStopTime"));
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test
    public void testProcessL1SlicesWVFile() {
        
        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1B_WV_SLC__1SDV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("B");
        descriptor.setProductName(
                "S1B_WV_SLC__1SDV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE");
        descriptor.setRelativePath(
                "S1B_WV_SLC__1SDV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE");
        descriptor.setSwathtype("WV");
        descriptor.setResolution("_");
        descriptor.setProductClass("S");
        descriptor.setProductType("WV_SLC__1S");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("017EF8");
        descriptor.setProductFamily(ProductFamily.L1_SLICE);

        File file = new File(
                "test/workDir/S1B_WV_SLC__1SDV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE/manifest.safe");

        try {
            JSONObject result = extractor.processProduct(descriptor, ProductFamily.L1_SLICE, file);

            assertNotNull("JSON object should not be null", result);
            assertEquals("Polygon", result.getJSONObject("sliceCoordinates").getString("type"));
            assertEquals(new JSONArray("[63.622650,-6.293599]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(0).toString());
            assertEquals(new JSONArray("[65.249229,-5.011662]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(1).toString());
            assertEquals(new JSONArray("[84.809326,-57.154243]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(2).toString());
            assertEquals(new JSONArray("[82.243843,-58.707996]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(3).toString());
            assertEquals(new JSONArray("[63.622650,-6.293599]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(4).toString());
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }
    
    @Test
    public void testProcessL1SlicesWVFile2() {
        
        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1B_WV_SLC__1SSH_20170702T130912_20170702T133355_006309_00B17D_BC10.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("B");
        descriptor.setProductName(
                "S1B_WV_SLC__1SSH_20170702T130912_20170702T133355_006309_00B17D_BC10.SAFE");
        descriptor.setRelativePath(
                "S1B_WV_SLC__1SSH_20170702T130912_20170702T133355_006309_00B17D_BC10.SAFE");
        descriptor.setSwathtype("WV");
        descriptor.setResolution("_");
        descriptor.setProductClass("S");
        descriptor.setProductType("WV_SLC__1S");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("017EF8");
        descriptor.setProductFamily(ProductFamily.L1_SLICE);

        File file = new File(
                "test/workDir/S1B_WV_SLC__1SSH_20170702T130912_20170702T133355_006309_00B17D_BC10.SAFE/manifest.safe");

        try {
            JSONObject result = extractor.processProduct(descriptor, ProductFamily.L1_SLICE, file);

            assertNotNull("JSON object should not be null", result);
            assertEquals("Polygon", result.getJSONObject("sliceCoordinates").getString("type"));
            assertEquals(new JSONArray("[-162.062683,-76.17926]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(0).toString());
            assertEquals(new JSONArray("[-169.470245,-75.217514]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(1).toString());
            assertEquals(new JSONArray("[-110.519669,8.143796]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(2).toString());
            assertEquals(new JSONArray("[-108.503227,8.657177]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(3).toString());
            assertEquals(new JSONArray("[-162.062683,-76.17926]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(4).toString());
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }
    
    @Test
    public void testProcessL2SlicesWVFile() {
        
        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1B_WV_OCN__2SSH_20170702T130912_20170702T133355_006309_00B17D_3B01.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("B");
        descriptor.setProductName(
                "S1B_WV_OCN__2SSH_20170702T130912_20170702T133355_006309_00B17D_3B01.SAFE");
        descriptor.setRelativePath(
                "S1B_WV_OCN__2SSH_20170702T130912_20170702T133355_006309_00B17D_3B01.SAFE");
        descriptor.setSwathtype("WV");
        descriptor.setResolution("_");
        descriptor.setProductClass("S");
        descriptor.setProductType("WV_OCN__2S");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("017EF8");
        descriptor.setProductFamily(ProductFamily.L2_SLICE);

        File file = new File(
                "test/workDir/S1B_WV_OCN__2SSH_20170702T130912_20170702T133355_006309_00B17D_3B01.SAFE/manifest.safe");

        try {
            JSONObject result = extractor.processProduct(descriptor, ProductFamily.L2_SLICE, file);

            assertNotNull("JSON object should not be null", result);
            assertEquals("Polygon", result.getJSONObject("sliceCoordinates").getString("type"));
            assertEquals(new JSONArray("[-162.062119,-76.179138]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(0).toString());
            assertEquals(new JSONArray("[-169.469604,-75.217407]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(1).toString());
            assertEquals(new JSONArray("[-110.51963,8.143985]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(2).toString());
            assertEquals(new JSONArray("[-108.503189,8.657347]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(3).toString());
            assertEquals(new JSONArray("[-162.062119,-76.179138]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(4).toString());
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test
    public void testProcessL1SlicesWVFile3raw() {
        
        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_WV_GRDM_1ASV_20180913T214338_20180913T214410_023685_0294F4_70D1.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName(
                "S1A_WV_GRDM_1ASV_20180913T214338_20180913T214410_023685_0294F4_70D1.SAFE");
        descriptor.setRelativePath(
                "S1A_WV_GRDM_1ASV_20180913T214338_20180913T214410_023685_0294F4_70D1.SAFE");
        descriptor.setSwathtype("WV");
        descriptor.setResolution("M");
        descriptor.setProductClass("S");
        descriptor.setProductType("WV_GRDM_1S");
        descriptor.setPolarisation("SV");
        descriptor.setDataTakeId("0294F4");
        descriptor.setProductFamily(ProductFamily.L1_SLICE);

        File file = new File(
                "test/workDir/S1A_WV_GRDM_1ASV_20180913T214338_20180913T214410_023685_0294F4_70D1.SAFE/manifest.safe");

        try {
            JSONObject result = extractor.processProduct(descriptor, ProductFamily.L1_SLICE, file);

            assertNotNull("JSON object should not be null", result);
            assertEquals("Polygon", result.getJSONObject("sliceCoordinates").getString("type"));
            assertEquals(new JSONArray("[98.145752,-63.219410]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(0).toString());
            assertEquals(new JSONArray("[97.84906,-63.146168]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(1).toString());
            assertEquals(new JSONArray("[102.146751,-62.931400]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(2).toString());
            assertEquals(new JSONArray("[100.111465,-61.471767]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(3).toString());
            assertEquals(new JSONArray("[98.145752,-63.219410]").toString(), result.getJSONObject("sliceCoordinates").getJSONArray("coordinates").getJSONArray(0).get(4).toString());
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test(expected = AbstractCodedException.class)
    public void testProcessL1SlicesMissingFileFail()
            throws MetadataExtractionException, MetadataMalformedException {

        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName(
                "S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE");
        descriptor.setRelativePath(
                "S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B5.SAFE");
        descriptor.setSwathtype("IW");
        descriptor.setResolution("_");
        descriptor.setProductClass("S");
        descriptor.setProductType("IW_GRDH_0S");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("023A69");

        File file = new File(
                "test/workDir/S1A_IW_GRDH_1SDV_20180227T145344_20180227T145413_020794_023A69_C0B4.SAFE/manifest.safe");
        extractor.processProduct(descriptor, ProductFamily.L1_SLICE, file);
    }

    @Test
    public void testProcessL1AFile() {

        JSONObject expectedResult = new JSONObject(
                "{\"missionDataTakeId\":146025,\"sliceCoordinates\":{\"coordinates\":[[[47.968777,13.890088],[50.308678,14.337382],[50.603825,12.829331],[48.279221,12.378204],[47.968777,13.890088]]],\"type\":\"polygon\"},\"insertionTime\":\"2018-06-01T11:40:59\",\"creationTime\":\"2018-06-01T11:40:59\",\"polarisation\":\"DV\",\"sliceNumber\":2,\"absoluteStopOrbit\":20794,\"resolution\":\"_\",\"productName\":\"S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE\",\"dataTakeId\":\"023A69\",\"absoluteStartOrbit\":20794,\"validityStopTime\":\"2018-02-27T14:54:38.190463\",\"instrumentConfigurationId\":6,\"relativeStopOrbit\":72,\"relativeStartOrbit\":72,\"startTime\":\"2018-02-27T14:54:13.192073\",\"stopTime\":\"2018-02-27T14:54:38.190463\",\"productType\":\"IW_GRDH_0A\",\"productClass\":\"A\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"A\",\"stopTimeANX\":218284.2,\"url\":\"S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE\",\"startTimeANX\":193285.8,\"validityStartTime\":\"2018-02-27T14:54:13.192073\",\"productFamily\":\"L1_ACN\"}");

        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName(
                "S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE");
        descriptor.setRelativePath(
                "S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE");
        descriptor.setSwathtype("IW");
        descriptor.setResolution("_");
        descriptor.setProductClass("A");
        descriptor.setProductType("IW_GRDH_1A");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("023A69");
        descriptor.setProductFamily(ProductFamily.L1_ACN);

        File file = new File(
                "test/workDir/S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE/manifest.safe");

        try {
            JSONObject result = extractor.processProduct(descriptor, ProductFamily.L1_ACN, file);

            assertNotNull("JSON object should not be null", result);
            assertEquals("JSON object are not equals", expectedResult.length(),
                    result.length());
            assertEquals("JSON object value validityStartTime are not equals",
                    expectedResult.get("missionId").toString(),
                    result.get("missionId").toString());
        } catch (AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test(expected = AbstractCodedException.class)
    public void testProcessL1AMissingFileFail()
            throws MetadataExtractionException, MetadataMalformedException {

        OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName(
                "S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE");
        descriptor.setRelativePath(
                "S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE");
        descriptor.setSwathtype("IW");
        descriptor.setResolution("_");
        descriptor.setProductClass("A");
        descriptor.setProductType("IW_GRDH_0A");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("023A69");

        File file = new File(
                "test/workDir/S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632B.SAFE/manifest.safe");

        extractor.processProduct(descriptor, ProductFamily.L1_ACN, file);
    }

}
