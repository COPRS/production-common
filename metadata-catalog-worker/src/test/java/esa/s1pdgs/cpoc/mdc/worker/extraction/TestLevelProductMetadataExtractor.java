package esa.s1pdgs.cpoc.mdc.worker.extraction;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import esa.s1pdgs.cpoc.mdc.worker.Utils;
import esa.s1pdgs.cpoc.mdc.worker.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.es.EsServices;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.ExtractMetadata;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.mdc.worker.status.AppStatusImpl;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class TestLevelProductMetadataExtractor {
	private static final String PATTERN = "^(S1|AS)(A|B)_(S[1-6]|IW|EW|WV|GP|HK|N[1-6]|EN|IM)_(SLC|GRD|OCN|RAW)(F|H|M|_)_(0|1|2)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$";

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
	 * MQI service
	 */
	@Mock
	private GenericMqiClient mqiService;

	/**
	 * 
	 */
	@Mock
	protected MetadataExtractorConfig extractorConfig;

	/**
	 * Application status
	 */
	@Mock
	protected AppStatusImpl appStatus;

	/**
	 * Extractor
	 */
	protected LevelProductMetadataExtractor extractor;
	/**
	 * Job to process
	 */
	//private GenericMessageDto<CatalogJob> inputMessageSafe;
    
    @Mock
    XmlConverter xmlConverter;
    
    
    private final File inputDir = new File("src/test/resources/workDir/");
    
    private final File testDir = FileUtils.createTmpDir();
    
	final Reporting reporting = ReportingUtils.newReportingBuilder()
			.newReporting("TestMetadataExtraction");

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

		doNothing().when(appStatus).setError(Mockito.any(), Mockito.anyString());
		doReturn(true).when(mqiService).ack(Mockito.any(), Mockito.any());

//		final GenericMessageDto<CatalogJob> inputMessageSafe = new GenericMessageDto<CatalogJob>(123, "",
//				Utils.newCatalogJob("S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE",
//						"S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE/manifest.safe", ProductFamily.L0_ACN, "NRT"));

		final FileDescriptorBuilder fileDescriptorBuilder = new FileDescriptorBuilder(
				testDir, 
				Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE)
		);
		
		final ExtractMetadata extract = new ExtractMetadata(
				extractorConfig.getTypeOverlap(), 
				extractorConfig.getTypeSliceLength(),
				extractorConfig.getPacketStoreType(),
				extractorConfig.getTimelinessPriorityFromHighToLow(),
				extractorConfig.getXsltDirectory(), 
				xmlConverter
		);		
		final MetadataBuilder mdBuilder = new MetadataBuilder(extract);

	    extractor = new LevelProductMetadataExtractor(
	    			esServices, 
	    			mdBuilder, 
	    			fileDescriptorBuilder, 
	    			testDir.getPath(), 
	    			new ProcessConfiguration(), 
	    			obsClient
	    );
	}

	@Test
	public void testExtractMetadataL0Slice() throws MetadataExtractionException, AbstractCodedException {
		final List<File> files = Arrays.asList(new File(testDir, 
				"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe")
		);
		final GenericMessageDto<CatalogJob> inputMessageSafe = new GenericMessageDto<CatalogJob>(
				123, 
				"",
				Utils.newCatalogJob(
						"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
						"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe",
						ProductFamily.L0_SLICE, 
						"NRT"
				)
		);
		doReturn(files).when(obsClient).download(Mockito.anyList(), Mockito.any());

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
		
		final CatalogJob job = new CatalogJob();
		job.setProductFamily(ProductFamily.L0_SLICE);

		final JSONObject expected = extractor.mdBuilder.buildOutputFileMetadata(descriptor, files.get(0), job);

		final JSONObject result = extractor.extract(reporting, inputMessageSafe);
		for (final String key : expected.keySet()) {
			if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) || "creationTime".equals(key))) {
				System.out.println("Key");
				assertEquals(expected.get(key), result.get(key));
			}
		}

		verify(obsClient, times(1)).download(Mockito.any(), Mockito.any());

	}

	@Test
	public void testExtractMetadataL0Acn() throws MetadataExtractionException, AbstractCodedException {

		final List<File> files = Arrays.asList(new File(testDir,
				"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE" + File.separator
				+ "manifest.safe"));

		final GenericMessageDto<CatalogJob> inputMessageSafe = new GenericMessageDto<CatalogJob>(
				123, "",
				Utils.newCatalogJob(
						"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
						"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe",
						ProductFamily.L0_ACN, 
						"NRT"
				)
		);
		doReturn(files).when(obsClient).download(Mockito.anyList(), Mockito.any());

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
		descriptor.setProductFamily(ProductFamily.L0_ACN);
		
		final CatalogJob job = new CatalogJob();
		job.setProductFamily(ProductFamily.L0_ACN);

		final JSONObject expected = extractor.mdBuilder.buildOutputFileMetadata(descriptor, files.get(0), job);

		final JSONObject result = extractor.extract(reporting, inputMessageSafe);
		for (final String key : expected.keySet()) {
			if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) || "creationTime".equals(key))) {
				assertEquals(expected.get(key), result.get(key));
			}
		}

		verify(obsClient, times(1)).download(Mockito.any(), Mockito.any());

	}

	@Test
	public void testExtractMetadataL1Slice() throws MetadataExtractionException, AbstractCodedException {

		final List<File> files = Arrays.asList(new File(testDir,
				"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE" + File.separator
				+ "manifest.safe"));

		final GenericMessageDto<CatalogJob> inputMessageSafe = new GenericMessageDto<CatalogJob>(123, "",
				Utils.newCatalogJob("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
						"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe",
						ProductFamily.L1_SLICE, "NRT"));

		doReturn(files).when(obsClient).download(Mockito.anyList(), Mockito.any());

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
		descriptor.setProductFamily(ProductFamily.L1_SLICE);
		
		final CatalogJob job = new CatalogJob();
		job.setProductFamily(ProductFamily.L1_SLICE);

		final JSONObject expected = extractor.mdBuilder.buildOutputFileMetadata(descriptor, files.get(0), job);

		final JSONObject result = extractor.extract(reporting, inputMessageSafe);
		for (final String key : expected.keySet()) {
			if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) || "creationTime".equals(key))) {
				assertEquals(expected.get(key), result.get(key));
			}
		}

		verify(obsClient, times(1)).download(Mockito.any(), Mockito.any());

	}

	@Test
	public void testExtractMetadataL1Acn() throws MetadataExtractionException, AbstractCodedException {

		final String l1acnName = "S1A_IW_GRDH_1ADV_20180227T145413_20180227T145438_020794_023A69_632A.SAFE";

		final List<File> files = Arrays.asList(new File(testDir.getAbsolutePath(), l1acnName + File.separator + "manifest.safe"));

		final GenericMessageDto<CatalogJob> inputMessageSafe = new GenericMessageDto<CatalogJob>(123, "",
				Utils.newCatalogJob(l1acnName, l1acnName + "/manifest.safe", ProductFamily.L1_ACN, "NRT"));

		doReturn(files).when(obsClient).download(Mockito.anyList(), Mockito.any());

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename(l1acnName);
		descriptor.setKeyObjectStorage(l1acnName);
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName(l1acnName);
		descriptor.setRelativePath(l1acnName);
		descriptor.setSwathtype("IW");
		descriptor.setResolution("H");
		descriptor.setProductClass("A");
		descriptor.setProductType("IW_GRDH_1A");
		descriptor.setPolarisation("DV");
		descriptor.setDataTakeId("023A69");
		descriptor.setProductFamily(ProductFamily.L1_ACN);
		
		final CatalogJob job = new CatalogJob();
		job.setProductFamily(ProductFamily.L1_ACN);

		final JSONObject expected = extractor.mdBuilder.buildOutputFileMetadata(descriptor, files.get(0), job);

		final JSONObject result = extractor.extract(reporting, inputMessageSafe);
		for (final String key : expected.keySet()) {
			if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) || "creationTime".equals(key))) {
				assertEquals(expected.get(key), result.get(key));
			}
		}

		verify(obsClient, times(1)).download(Mockito.any(), Mockito.any());

	}

	@Test
	public void testExtractMetadataL2Slice() throws AbstractCodedException {

		final String l2SliceName = "S1A_WV_OCN__2SSV_20190518T160559_20190518T161434_027284_0313A0_46F2.SAFE";

		final List<File> files = Arrays.asList(new File(testDir, l2SliceName + File.separator + "manifest.safe"));

		final GenericMessageDto<CatalogJob> inputMessageSafe = new GenericMessageDto<CatalogJob>(123, "",
				Utils.newCatalogJob(l2SliceName, l2SliceName+ "/manifest.safe", ProductFamily.L2_SLICE, "NRT"));

		doReturn(files).when(obsClient).download(Mockito.anyList(), Mockito.any());

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
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
		
		final CatalogJob job = new CatalogJob();
		job.setProductFamily(ProductFamily.L2_SLICE);

		final JSONObject expected = extractor.mdBuilder.buildOutputFileMetadata(descriptor, files.get(0), job);

		final JSONObject result = extractor.extract(reporting, inputMessageSafe);
		for (final String key : expected.keySet()) {
			if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) || "creationTime".equals(key))) {
				assertEquals(expected.get(key), result.get(key));
			}
		}

		verify(obsClient, times(1)).download(Mockito.any(), Mockito.any());

	}

	@Test
	public void testExtractMetadataL2Acn() throws AbstractCodedException {

		final String l2acnName = "S1A_WV_OCN__2ASV_20190518T160559_20190518T161434_027284_0313A0_2960.SAFE";

		final List<File> files = Arrays.asList(new File(testDir, l2acnName + File.separator + "manifest.safe"));

		final GenericMessageDto<CatalogJob> inputMessageSafe = new GenericMessageDto<CatalogJob>(123, "",
				Utils.newCatalogJob(l2acnName, l2acnName+ "/manifest.safe", ProductFamily.L2_ACN, "NRT"));

		doReturn(files).when(obsClient).download(Mockito.anyList(), Mockito.any());

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
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
		
		final CatalogJob job = new CatalogJob();
		job.setProductFamily(ProductFamily.L2_ACN);

		final JSONObject expected = extractor.mdBuilder.buildOutputFileMetadata(descriptor, files.get(0), job);

		final JSONObject result = extractor.extract(reporting, inputMessageSafe);
		for (final String key : expected.keySet()) {
			if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) || "creationTime".equals(key))) {
				assertEquals(expected.get(key), result.get(key));
			}
		}
		verify(obsClient, times(1)).download(Mockito.any(), Mockito.any());
	}

}
