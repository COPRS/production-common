package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONException;
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
import esa.s1pdgs.cpoc.metadata.extraction.Utils;
import esa.s1pdgs.cpoc.metadata.extraction.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.ExtractMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.AuxDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class TestAuxMetadataExtractor {

	private static final String PATTERN = "^([0-9a-z][0-9a-z]){1}([0-9a-z_]){1}(_(OPER|TEST))?_(AMH_ERRMAT|AMV_ERRMAT|AM__ERRMAT|AUX_CAL|AUX_ICE|AUX_INS|AUX_ITC|AUX_OBMEMC|AUX_PP1|AUX_PP2|AUX_POEORB|AUX_PREORB|AUX_RESORB|AUX_SCF|AUX_SCS|AUX_TEC|AUX_TRO|AUX_WAV|AUX_WND|MPL_ORBPRE|MPL_ORBRES|MPL_ORBSCT|MSK_EW_SLC|MSK__LAND_|MSK_OCEAN_|MSK_OVRPAS)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$";

	/**
	 * Elasticsearch services
	 */
	@Mock
	protected EsServices esServices;

	/**
	 * Elasticsearch services
	 */
	@Mock
	protected ObsClient obsClient;

	/**
	 * 
	 */
	@Mock
	protected MetadataExtractorConfig extractorConfig;

	/**
	 * Extractor
	 */
	protected AuxMetadataExtractor extractor;

	@Mock
	XmlConverter xmlConverter;

	private static final File inputDir = new File("src/test/resources/workDir/");

	private final File testDir = FileUtils.createTmpDir();

	/**
	 * Initialization
	 * 
	 * @throws AbstractCodedException
	 */
	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		Utils.copyFolder(inputDir.toPath(), testDir.toPath());

		// "EW:8.2F||IW:7.4F||SM:7.7F||WM:0.0F"
		final Map<String, Float> typeOverlap = new HashMap<String, Float>();
		typeOverlap.put("EW", 8.2F);
		typeOverlap.put("IW", 7.4F);
		typeOverlap.put("SM", 7.7F);
		typeOverlap.put("WM", 0.0F);
		// "EW:60.0F||IW:25.0F||SM:25.0F||WM:0.0F"
		final Map<String, Float> typeSliceLength = new HashMap<String, Float>();
		typeSliceLength.put("EW", 60.0F);
		typeSliceLength.put("IW", 25.0F);
		typeSliceLength.put("SM", 25.0F);
		typeSliceLength.put("WM", 0.0F);
		doReturn("config/xsltDir/").when(extractorConfig).getXsltDirectory();
		doReturn(typeOverlap).when(extractorConfig).getTypeOverlap();
		doReturn(typeSliceLength).when(extractorConfig).getTypeSliceLength();

		Map<String, String> packetStoreTypes = new HashMap<>();
		doReturn(packetStoreTypes).when(extractorConfig).getPacketStoreTypes();

		Map<String, String> packetStoreTypeTimelinesses = new HashMap<>();
		doReturn(packetStoreTypeTimelinesses).when(extractorConfig).getPacketstoreTypeTimelinesses();

		final List<String> timelinessPriorityFromHighToLow = Arrays.asList("PT", "NRT", "FAST24");
		doReturn(timelinessPriorityFromHighToLow).when(extractorConfig).getTimelinessPriorityFromHighToLow();

		final ExtractMetadata extract = new ExtractMetadata(extractorConfig.getTypeOverlap(),
				extractorConfig.getTypeSliceLength(), Collections.<String, String>emptyMap(),
				extractorConfig.getPacketStoreTypes(), extractorConfig.getPacketstoreTypeTimelinesses(),
				extractorConfig.getTimelinessPriorityFromHighToLow(), extractorConfig.getXsltDirectory(), xmlConverter);
		final MetadataBuilder mdBuilder = new MetadataBuilder(extract);

		final FileDescriptorBuilder fileDescriptorBuilder = new FileDescriptorBuilder(
				new File(testDir.getAbsolutePath()), Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE));
		extractor = new AuxMetadataExtractor(esServices, mdBuilder, fileDescriptorBuilder, testDir.getAbsolutePath(),
				new ProcessConfiguration(), obsClient);
	}

	@Test
	public void testExtractMetadataAuxOBMEMC() throws MetadataExtractionException, AbstractCodedException, JSONException {
		final String fileName = "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml";
		final CatalogJob inputMessageAux = Utils.newCatalogJob("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
				"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml", ProductFamily.AUXILIARY_FILE);
		testExtractMetadata(inputMessageAux, fileName, fileName, FileExtension.XML, "S1", "A", "OPER", "AUX_OBMEMC");
	}

	@Test
	public void testExtractMetadataAuxWAV() throws AbstractCodedException, JSONException {

		final String fileName = "S1__AUX_WAV_V20110801T000000_G20111026T141850.SAFE";

		final CatalogJob inputMessageAuxWAV = Utils.newCatalogJob(fileName, fileName, ProductFamily.AUXILIARY_FILE);

		testExtractMetadata(inputMessageAuxWAV, fileName, fileName + File.separator + "manifest.safe",
				FileExtension.SAFE, "S1", "_", null, "AUX_WAV");
	}

	@Test
	public void testExtractMetadataAuxICE() throws AbstractCodedException, JSONException {

		final String fileName = "S1__AUX_ICE_V20160501T120000_G20160502T043607.SAFE";
		final CatalogJob inputMessageAuxICE = Utils.newCatalogJob(fileName, fileName, ProductFamily.AUXILIARY_FILE);

		testExtractMetadata(inputMessageAuxICE, fileName, fileName + File.separator + "manifest.safe",
				FileExtension.SAFE, "S1", "_", null, "AUX_ICE");
	}

	@Test
	public void testExtractMetadataAuxWND() throws AbstractCodedException, JSONException {

		final String fileName = "S1__AUX_WND_V20160423T120000_G20160422T060059.SAFE";

		final CatalogJob inputMessageAuxWND = Utils.newCatalogJob(fileName, fileName, ProductFamily.AUXILIARY_FILE);

		testExtractMetadata(inputMessageAuxWND, fileName, fileName + File.separator + "manifest.safe",
				FileExtension.SAFE, "S1", "_", null, "AUX_WND");

	}

	@Test
	public void testExtractMetadataAuxPP2() throws AbstractCodedException, JSONException {

		final String fileName = "S1A_AUX_PP2_V20171017T080000_G20171013T101254.SAFE";

		final CatalogJob inputMessageAuxWND = Utils.newCatalogJob(fileName, fileName, ProductFamily.AUXILIARY_FILE);

		testExtractMetadata(inputMessageAuxWND, fileName, fileName + File.separator + "manifest.safe",
				FileExtension.SAFE, "S1", "A", null, "AUX_PP2");

	}
	
	@Test
	public void testExtractMetadataAuxTec() throws AbstractCodedException {
		final String fileName = "S1__AUX_TEC_V20190805T000000_20190805T235959_G20200210T102615.SAFE";
		final CatalogJob inputMessage = Utils.newCatalogJob(fileName, fileName, ProductFamily.AUXILIARY_FILE);
		
		JSONObject result = testExtractMetadata(inputMessage, fileName, fileName + File.separator + "manifest.safe",
				FileExtension.SAFE, "S1", "_", null, "AUX_TEC");
		assertEquals("2019-08-05T00:00:00.000000Z", result.get("validityStartTime"));
		assertEquals("2019-08-05T23:59:59.000000Z", result.get("validityStopTime"));
		assertEquals("2020-02-10T10:26:15.000000Z", result.get("creationTime"));
	}
	
	@Test
	public void testExtractMetadataAuxTro() throws AbstractCodedException {
		final String fileName = "S1__AUX_TRO_V20190805T180000_20190805T235959_G20200518T084534.SAFE";
		final CatalogJob inputMessage = Utils.newCatalogJob(fileName, fileName, ProductFamily.AUXILIARY_FILE);
		
		JSONObject result = testExtractMetadata(inputMessage, fileName, fileName + File.separator + "manifest.safe",
				FileExtension.SAFE, "S1", "_", null, "AUX_TRO");
		assertEquals("2019-08-05T18:00:00.000000Z", result.get("validityStartTime"));
		assertEquals("2019-08-05T23:59:59.000000Z", result.get("validityStopTime"));
		assertEquals("2020-05-18T08:45:34.000000Z", result.get("creationTime"));
	}	

	private void testExtractMetadata(final CatalogJob inputMessage, final String productFileName,
			final String metadataFile, final FileExtension fileExtension, final String missionId,
			final String satelliteId, final String productClass, final String productType)
			throws AbstractCodedException, JSONException {
		final List<File> files = Arrays.asList(new File(testDir, metadataFile));

		final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.valueOf(missionId))
				.newReporting("TestMetadataExtraction");

		doReturn(files).when(obsClient).download(Mockito.anyList(), Mockito.any());

		final AuxDescriptor expectedDescriptor = new AuxDescriptor();
		expectedDescriptor.setExtension(fileExtension);
		expectedDescriptor.setFilename(productFileName);
		expectedDescriptor.setKeyObjectStorage(productFileName);
		expectedDescriptor.setMissionId(missionId);
		expectedDescriptor.setSatelliteId(satelliteId);
		expectedDescriptor.setProductClass(productClass);
		expectedDescriptor.setProductName(productFileName);
		expectedDescriptor.setProductType(productType);
		expectedDescriptor.setProductFamily(ProductFamily.AUXILIARY_FILE);
		expectedDescriptor.setRelativePath(productFileName);

		final JSONObject expected = extractor.mdBuilder.buildConfigFileMetadata(expectedDescriptor, files.get(0));
		final JSONObject result = extractor.extract(reporting, inputMessage);
		
		@SuppressWarnings("unchecked")
		Iterator<String> it = (Iterator<String>) expected.keys();
		while (it.hasNext()) {
			String key = it.next();
			if (!"insertionTime".equals(key)) {
				assertEquals(expected.get(key), result.get(key));
			}
		}

		// verify(obsClient, times(1)).download(Mockito.any());
		return result;
	}
}
