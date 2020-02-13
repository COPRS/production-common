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

public class TestLevelSegmentMetadataExtractor {
	private static final String PATTERN = "^(S1|AS)(A|B)_(S[1-6]|RF|GP|HK|IW|EW|WV|N[1-6]|EN|IM)_(SLC|GRD|OCN|RAW)(F|H|M|_)_(0)(A|C|N|S|_)(SH|__|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$";
			
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
    protected LevelSegmentMetadataExtractor extractor;

    /**
     * Job to process
     */
    private GenericMessageDto<CatalogJob> inputMessageSafe;

    
	final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("TestMetadataExtraction");
    
    private static final File inputDir = new File("src/test/resources/workDir/");
    
    private final File testDir = FileUtils.createTmpDir();

    @Mock
    XmlConverter xmlConverter;
    
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
        typeOverlap.put("WV", 0.0F);
        // "EW:60.0F||IW:25.0F||SM:25.0F||WM:0.0F"
        final Map<String, Float> typeSliceLength = new HashMap<String, Float>();
        typeSliceLength.put("EW", 60.0F);
        typeSliceLength.put("IW", 25.0F);
        typeSliceLength.put("SM", 25.0F);
        typeSliceLength.put("WV", 0.0F);
        doReturn("config/xsltDir/").when(extractorConfig).getXsltDirectory();
        doReturn(typeOverlap).when(extractorConfig).getTypeOverlap();
        doReturn(typeSliceLength).when(extractorConfig).getTypeSliceLength();

        doNothing().when(appStatus).setError(Mockito.any(), Mockito.anyString());
        doReturn(true).when(mqiService).ack(Mockito.any(), Mockito.any());

        inputMessageSafe = new GenericMessageDto<CatalogJob>(123, "",
                Utils.newCatalogJob(
                        "S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE",
                        "S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE",
                        ProductFamily.L0_SEGMENT, "NRT"));
        
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

	    extractor = new LevelSegmentMetadataExtractor(
	    			esServices, 
	    			mdBuilder, 
	    			fileDescriptorBuilder, 
	    			testDir.getPath(), 
	    			new ProcessConfiguration(), 
	    			obsClient
	    );
    }

	@Test
    public void testExtractMetadataL0Segment()
            throws MetadataExtractionException, AbstractCodedException {

        final List<File> files = Arrays.asList(new File(testDir, 
                "S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE"+ File.separator + "manifest.safe"));

        inputMessageSafe = new GenericMessageDto<CatalogJob>(123, "",
                Utils.newCatalogJob(
                        "S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE",
                        "S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE",
                        ProductFamily.L0_SEGMENT, "FAST"));

        doReturn(files).when(obsClient).download(Mockito.anyList(), Mockito.any());

        final OutputFileDescriptor descriptor = new OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE/manifest.safe");
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
        descriptor.setProductFamily(ProductFamily.L0_SEGMENT);
        descriptor.setMode("FAST");

        final JSONObject expected = extractor.mdBuilder
                .buildL0SegmentOutputFileMetadata(descriptor, files.get(0));

        final JSONObject result = extractor.extract(reporting, inputMessageSafe);
        for (final String key : expected.keySet()) {
            if (!("insertionTime".equals(key) || "segmentCoordinates".equals(key) || "creationTime".equals(key))) {
                assertEquals(expected.get(key), result.get(key));
            }
        }
        verify(obsClient, times(1)).download(Mockito.any(), Mockito.any());
    }

}
