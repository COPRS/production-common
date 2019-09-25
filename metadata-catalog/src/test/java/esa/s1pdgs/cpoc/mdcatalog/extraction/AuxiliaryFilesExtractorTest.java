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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
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
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.ConfigFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.report.LoggerReporting;

public class AuxiliaryFilesExtractorTest {

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
    protected AppStatus appStatus;

    /**
     * Extractor
     */
    protected AuxiliaryFilesExtractor extractor;

    /**
     * Job to process
     */
    private GenericMessageDto<ProductDto> inputMessage;

    /**
     * Job to process
     */
    private GenericMessageDto<ProductDto> inputMessageSafe;

    /**
     * Job to process
     */
    private GenericMessageDto<ProductDto> inputMessageAux;
    
    private final ErrorRepoAppender errorAppender = ErrorRepoAppender.NULL;
    
    @Mock
    XmlConverter xmlConverter;
    
    private final File testDir = new File("src/test/resources/workDir/");

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
        doReturn(true).when(mqiService).ack(Mockito.any(), Mockito.any());

        inputMessage = new GenericMessageDto<ProductDto>(123, "",
                new ProductDto("product-name", "key-obs", ProductFamily.AUXILIARY_FILE));

        inputMessageSafe = new GenericMessageDto<ProductDto>(123, "",
                new ProductDto(
                        "S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE",
                        "S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE", ProductFamily.AUXILIARY_FILE));

        inputMessageAux = new GenericMessageDto<ProductDto>(123, "",
                new ProductDto(
                        "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
                        "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml", ProductFamily.AUXILIARY_FILE));

        extractor = new AuxiliaryFilesExtractor(esServices, obsClient,
                mqiService, appStatus, extractorConfig,
                testDir.getAbsolutePath()
                        + File.separator,
                "manifest.safe", errorAppender, new ProcessConfiguration(),".safe", xmlConverter);
    }

    @Test
    public void testGetKeyObs() {
        assertEquals("key-obs", extractor.getKeyObs(inputMessage));
        assertEquals(
                "S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE/manifest.safe",
                extractor.getKeyObs(inputMessageSafe));
    }

    @Test
    public void testExtractProductName() {
        assertEquals("product-name",
                extractor.extractProductNameFromDto(inputMessage.getBody()));
    }

    @Test
    public void testCleanProcessing() throws IOException {
        final File newWorkDir = FileUtils.createTmpDir();

        (new File(newWorkDir,"S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE")).mkdirs();
        (new File(newWorkDir,"S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE/manifest.safe")).createNewFile();
        (new File(newWorkDir,"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml")).createNewFile();

        extractor = new AuxiliaryFilesExtractor(esServices, obsClient,
                mqiService, appStatus, extractorConfig, newWorkDir.getAbsolutePath(),
                "manifest.safe", errorAppender, new ProcessConfiguration(),".safe", xmlConverter);
        assertTrue(new File(newWorkDir,"S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE").exists());
        assertTrue(new File(newWorkDir,"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml").exists());

        extractor.cleanProcessing(inputMessage);
        assertTrue(new File(newWorkDir,"S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE").exists());
        assertTrue(new File(newWorkDir,"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml").exists());

        extractor.cleanProcessing(inputMessageSafe);
        assertFalse(new File(newWorkDir,"S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE").exists());
        assertTrue(new File(newWorkDir,"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml").exists());

        extractor.cleanProcessing(inputMessageAux);
        assertFalse(new File(newWorkDir,"S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE").exists());
        assertFalse(new File(newWorkDir,"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml").exists());
    }
    
    @Test
	public void testExtractMetadataAuxOBMEMC() throws MetadataExtractionException, AbstractCodedException {
		String fileName = "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml";

		testExtractMetadata(inputMessageAux, fileName, fileName, FileExtension.XML, "S1", "A", "OPER", "AUX_OBMEMC");
	}

	@Test
	public void testExtractMetadataAuxWAV() throws AbstractCodedException {

		String fileName = "S1__AUX_WAV_V20110801T000000_G20111026T141850.SAFE";

		GenericMessageDto<ProductDto> inputMessageAuxWAV = new GenericMessageDto<ProductDto>(123, "",
				new ProductDto(fileName, fileName, ProductFamily.AUXILIARY_FILE));

		testExtractMetadata(inputMessageAuxWAV, fileName, fileName + File.separator + "manifest.safe",
				FileExtension.SAFE, "S1", "_", null, "AUX_WAV");
	}

	@Test
	public void testExtractMetadataAuxICE() throws AbstractCodedException {

		String fileName = "S1__AUX_ICE_V20160501T120000_G20160502T043607.SAFE";
		GenericMessageDto<ProductDto> inputMessageAuxICE = new GenericMessageDto<ProductDto>(123, "",
				new ProductDto(fileName, fileName, ProductFamily.AUXILIARY_FILE));

		testExtractMetadata(inputMessageAuxICE, fileName, fileName + File.separator + "manifest.safe",
				FileExtension.SAFE, "S1", "_", null, "AUX_ICE");
	}

	@Test
	public void testExtractMetadataAuxWND() throws AbstractCodedException {

		String fileName = "S1__AUX_WND_V20160423T120000_G20160422T060059.SAFE";

		GenericMessageDto<ProductDto> inputMessageAuxWND = new GenericMessageDto<ProductDto>(123, "",
				new ProductDto(fileName, fileName, ProductFamily.AUXILIARY_FILE));

		testExtractMetadata(inputMessageAuxWND, fileName, fileName + File.separator + "manifest.safe",
				FileExtension.SAFE, "S1", "_", null, "AUX_WND");

	}
	
	@Test
	public void testExtractMetadataAuxPP2() throws AbstractCodedException {

		String fileName = "S1A_AUX_PP2_V20171017T080000_G20171013T101254.SAFE";

		GenericMessageDto<ProductDto> inputMessageAuxWND = new GenericMessageDto<ProductDto>(123, "",
				new ProductDto(fileName, fileName, ProductFamily.AUXILIARY_FILE));

		testExtractMetadata(inputMessageAuxWND, fileName, fileName + File.separator + "manifest.safe",
				FileExtension.SAFE, "S1", "A", null, "AUX_PP2");

	}

	@SuppressWarnings("unchecked")
	private void testExtractMetadata(GenericMessageDto<ProductDto> inputMessage, String productFileName,
			String metadataFile, FileExtension fileExtension, String missionId, String satelliteId, String productClass,
			String productType) throws AbstractCodedException {
		List<File> files = Arrays.asList(new File(testDir,metadataFile));
		
		final LoggerReporting.Factory reportingFactory = new LoggerReporting.Factory("TestMetadataExtraction");

		doReturn(files).when(obsClient).download(Mockito.anyList());

		ConfigFileDescriptor expectedDescriptor = new ConfigFileDescriptor();
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

		JSONObject expected = extractor.mdBuilder.buildConfigFileMetadata(expectedDescriptor, files.get(0));
		JSONObject result = extractor.extractMetadata(reportingFactory, inputMessage);
		for (String key : expected.keySet()) {
			if (!"insertionTime".equals(key)) {
				assertEquals(expected.get(key), result.get(key));
			}
		}

		verify(obsClient, times(1)).download((List<ObsDownloadObject>) ArgumentMatchers.argThat(s -> ((List<ObsDownloadObject>) s).contains(
				new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, metadataFile, extractor.localDirectory))));
	}

}
