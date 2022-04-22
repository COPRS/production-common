package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.metadata.extraction.Utils;
import esa.s1pdgs.cpoc.metadata.extraction.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.ExtractMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.S2FileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class TestS2ProductMetadataExtractor {

	private static final String PATTERN = "^(S2)(A|B|_)_([A-Z0-9]{4})_(MSI_(L0_|L1A|L1B|L1C)_(GR|DS|TL|TC))_\\w{4}_(\\d{8}T\\d{6})(.*)$";

	@Mock
	private EsServices esServices;

	@Mock
	private ObsClient obsClient;

	@Mock
	private MetadataExtractorConfig extractorConfig;

	@Mock
	XmlConverter xmlConverter;

	private S2ProductMetadataExtractor extractor;

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

		doReturn("config/xsltDir/").when(extractorConfig).getXsltDirectory();

		final FileDescriptorBuilder fileDescriptorBuilder = new FileDescriptorBuilder(testDir,
				Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE));

		final ExtractMetadata extract = new ExtractMetadata(extractorConfig.getTypeOverlap(),
				extractorConfig.getTypeSliceLength(), Collections.<String, String>emptyMap(),
				extractorConfig.getPacketStoreTypes(), extractorConfig.getPacketstoreTypeTimelinesses(),
				extractorConfig.getTimelinessPriorityFromHighToLow(), extractorConfig.getXsltDirectory(), xmlConverter);
		final MetadataBuilder mdBuilder = new MetadataBuilder(extract);

		ProcessConfiguration processConfig = new ProcessConfiguration();
		Map<String, String> manifestMap = new HashMap<>();
		manifestMap.put("safe", "manifest.safe");
		manifestMap.put("inventory", "Inventory_Metadata.xml");
		processConfig.setManifestFilenames(manifestMap);

		extractor = new S2ProductMetadataExtractor(esServices, mdBuilder, fileDescriptorBuilder, testDir.getPath(),
				false, processConfig, obsClient);
	}

	@After
	public void cleanup() {
		FileUtils.delete(testDir.getPath());
	}

	@Test
	public void extract_S2_L0_DS_Metadata() throws AbstractCodedException {

		final String keyObs = "S2A_OPER_MSI_L0__DS_SGS__20200420T205828_S20200322T173347_N02.08";
		
		final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.S2)
				.newReporting("TestMetadataExtraction");

		// Prepare OBS returnValues
		final String inventoryMetadataPath = keyObs + File.separator + "Inventory_Metadata.xml";
		final List<File> inventoryMetadataFile = Arrays.asList(new File(testDir, inventoryMetadataPath));
		doReturn(inventoryMetadataFile).when(obsClient)
				.download(eq(Collections.singletonList(
						new ObsDownloadObject(ProductFamily.S2_L0_DS, inventoryMetadataPath, testDir.getPath()))),
						Mockito.any());

		final String safeMetadataPath = keyObs + File.separator + "manifest.safe";
		final List<File> safeMetadataFile = Arrays.asList(new File(testDir, safeMetadataPath));
		doReturn(safeMetadataFile).when(obsClient).download(
				eq(Collections.singletonList(
						new ObsDownloadObject(ProductFamily.S2_L0_DS, safeMetadataPath, testDir.getPath()))),
				Mockito.any());

		// Prepare message
		final CatalogJob message = Utils.newCatalogJob(keyObs, keyObs, ProductFamily.S2_L0_DS, "NRT");

		final S2FileDescriptor expectedDescriptor = new S2FileDescriptor();
		expectedDescriptor.setProductType("MSI_L0__DS");
		expectedDescriptor.setProductClass("OPER");
		expectedDescriptor.setRelativePath(keyObs);
		expectedDescriptor.setFilename(keyObs);
		expectedDescriptor.setProductName(keyObs);
		expectedDescriptor.setKeyObjectStorage(keyObs);
		expectedDescriptor.setMissionId("S2");
		expectedDescriptor.setSatelliteId("A");
		expectedDescriptor.setProductFamily(ProductFamily.S2_L0_DS);
		expectedDescriptor.setMode("NRT");

		final JSONObject expected = extractor.mdBuilder.buildS2ProductFileMetadata(expectedDescriptor,
				safeMetadataFile.get(0), inventoryMetadataFile.get(0), message);

		final JSONObject result = extractor.extract(reporting, message);

		@SuppressWarnings("unchecked")
		Iterator<String> it = (Iterator<String>) expected.keys();
		while (it.hasNext()) {
			String key = it.next();
			if (!"coordinates".equals(key)) {
				assertEquals(expected.get(key), result.get(key));
			}
		}
	}
	
	@Test
	public void extract_S2_L0_GR_Metadata() throws AbstractCodedException {

		final String keyObs = "S2A_OPER_MSI_L0__GR_SGS__20200420T205828_S20200322T173347_D01_N02.08";
		
		final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.S2)
				.newReporting("TestMetadataExtraction");

		// Prepare OBS returnValues
		final String inventoryMetadataPath = keyObs + File.separator + "Inventory_Metadata.xml";
		final List<File> inventoryMetadataFile = Arrays.asList(new File(testDir, inventoryMetadataPath));
		doReturn(inventoryMetadataFile).when(obsClient)
				.download(eq(Collections.singletonList(
						new ObsDownloadObject(ProductFamily.S2_L0_GR, inventoryMetadataPath, testDir.getPath()))),
						Mockito.any());

		final String safeMetadataPath = keyObs + File.separator + "manifest.safe";
		final List<File> safeMetadataFile = Arrays.asList(new File(testDir, safeMetadataPath));
		doReturn(safeMetadataFile).when(obsClient).download(
				eq(Collections.singletonList(
						new ObsDownloadObject(ProductFamily.S2_L0_GR, safeMetadataPath, testDir.getPath()))),
				Mockito.any());

		// Prepare message
		final CatalogJob message = Utils.newCatalogJob(keyObs, keyObs, ProductFamily.S2_L0_GR, "NRT");

		final S2FileDescriptor expectedDescriptor = new S2FileDescriptor();
		expectedDescriptor.setProductType("MSI_L0__GR");
		expectedDescriptor.setProductClass("OPER");
		expectedDescriptor.setRelativePath(keyObs);
		expectedDescriptor.setFilename(keyObs);
		expectedDescriptor.setProductName(keyObs);
		expectedDescriptor.setKeyObjectStorage(keyObs);
		expectedDescriptor.setMissionId("S2");
		expectedDescriptor.setSatelliteId("A");
		expectedDescriptor.setProductFamily(ProductFamily.S2_L0_GR);
		expectedDescriptor.setMode("NRT");

		final JSONObject expected = extractor.mdBuilder.buildS2ProductFileMetadata(expectedDescriptor,
				safeMetadataFile.get(0), inventoryMetadataFile.get(0), message);

		final JSONObject result = extractor.extract(reporting, message);
		
		@SuppressWarnings("unchecked")
		Iterator<String> it = (Iterator<String>) expected.keys();
		while (it.hasNext()) {
			String key = it.next();
			if (!"coordinates".equals(key)) {
				assertEquals(expected.get(key), result.get(key));
			}
		}
	}

}
