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

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.mdc.worker.Utils;
import esa.s1pdgs.cpoc.mdc.worker.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.mdc.worker.es.EsServices;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.mdc.worker.status.AppStatusImpl;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.report.LoggerReporting;

@Ignore
public class LevelSegmentsExtractorTest {

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

    
    private final File testDir = new File("src/test/resources/workDir");

    @Mock
    XmlConverter xmlConverter;
    
    /**
     * Initialization
     * 
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);

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

        extractor = null; /*new LevelSegmentsExtractor(esServices, obsClient,
                mqiService, appStatus, extractorConfig,
                testDir.getAbsolutePath()
                        + File.separator,
                "manifest.safe", errorAppender, config, ".safe", xmlConverter, 0, 0);*/
    }

    @SuppressWarnings("unchecked")
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

        doReturn(files).when(obsClient).download(Mockito.anyList());

        final OutputFileDescriptor descriptor = new OutputFileDescriptor();
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
        descriptor.setProductFamily(ProductFamily.L0_SEGMENT);
        descriptor.setMode("FAST");

        final JSONObject expected = extractor.mdBuilder
                .buildL0SegmentOutputFileMetadata(descriptor, files.get(0));
		final LoggerReporting.Factory reportingFactory = new LoggerReporting.Factory("TestMetadataExtraction");
        
        final JSONObject result = extractor.extract(reportingFactory, inputMessageSafe);
        for (final String key : expected.keySet()) {
            if (!("insertionTime".equals(key) || "segmentCoordinates".equals(key) || "creationTime".equals(key))) {
                assertEquals(expected.get(key), result.get(key));
            }
        }

        verify(obsClient, times(1)).download((List<ObsDownloadObject>) ArgumentMatchers.argThat(s -> ((List<ObsDownloadObject>) s).contains(
        		new ObsDownloadObject(ProductFamily.L0_SEGMENT,
        		"S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE/manifest.safe",
                extractor.localDirectory))));

    }

}
