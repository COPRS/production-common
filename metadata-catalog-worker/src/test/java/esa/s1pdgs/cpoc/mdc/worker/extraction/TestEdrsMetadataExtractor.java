package esa.s1pdgs.cpoc.mdc.worker.extraction;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.util.regex.Pattern;

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
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.mdc.worker.Utils;
import esa.s1pdgs.cpoc.mdc.worker.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.es.EsServices;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.ExtractMetadata;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.mdc.worker.status.AppStatusImpl;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.LoggerReporting;

public class TestEdrsMetadataExtractor {
	
	private static final String PATTERN = "^(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)(\\w{4})\\.(xml|RAW))$";

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
    protected EdrsMetadataExtractor extractor;

    /**
     * Job to process
     */
    private GenericMessageDto<CatalogJob> inputMessage;
    

    private final File testDir = FileUtils.createTmpDir();
    
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

        doNothing().when(appStatus).setError(Mockito.anyString());
        doReturn(true).when(mqiService).ack(Mockito.any(), Mockito.any());

        inputMessage = new GenericMessageDto<CatalogJob>(123, "",
                Utils.newCatalogJob("123", "123/ch01/D_123_ch01_D.RAW", ProductFamily.EDRS_SESSION, null, 1,
                        EdrsSessionFileType.RAW, "S1", "A", "WILE", "123"));
        
		final FileDescriptorBuilder fileDescriptorBuilder = new FileDescriptorBuilder(
				testDir, 
				Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE)
		);
		
		final ExtractMetadata extract = new ExtractMetadata(
				extractorConfig.getTypeOverlap(), 
				extractorConfig.getTypeSliceLength(),
				extractorConfig.getXsltDirectory(), 
				xmlConverter
		);		
		final MetadataBuilder mdBuilder = new MetadataBuilder(extract);

        extractor = new EdrsMetadataExtractor(
    			esServices, 
    			mdBuilder, 
    			fileDescriptorBuilder, 
    			testDir.getPath(), 
    			new ProcessConfiguration(), 
    			obsClient
    	);
    }

    @Test
    public void testExtractMetadata() throws MetadataExtractionException, AbstractCodedException {
        final EdrsSessionFileDescriptor expectedDescriptor =
                new EdrsSessionFileDescriptor();
        
        expectedDescriptor.setFilename("D_123_ch01_D.RAW");
        expectedDescriptor.setRelativePath("123/ch01/D_123_ch01_D.RAW");
        expectedDescriptor.setProductName("D_123_ch01_D.RAW");
        expectedDescriptor.setExtension(FileExtension.RAW);
        expectedDescriptor.setEdrsSessionFileType(EdrsSessionFileType.RAW);
        expectedDescriptor.setMissionId("S1");
        expectedDescriptor.setSatelliteId("A");
        expectedDescriptor.setChannel(1);
        expectedDescriptor.setSessionIdentifier("123");
        expectedDescriptor.setStationCode("WILE");
        expectedDescriptor.setKeyObjectStorage("123/ch01/D_123_ch01_D.RAW");
        expectedDescriptor.setProductFamily(ProductFamily.EDRS_SESSION);
        
        final LoggerReporting.Factory reportingFactory = new LoggerReporting.Factory("TestMetadataExtraction");
        
        final JSONObject expected = extractor.mdBuilder.buildEdrsSessionFileRaw(expectedDescriptor);
        final JSONObject result = extractor.extract(reportingFactory, inputMessage);
        
        for (final String key: expected.keySet()) {
            if (!"insertionTime".equals(key)) {
                assertEquals(expected.get(key), result.get(key));
            }
        }

    }

}
