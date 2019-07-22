package esa.s1pdgs.cpoc.ingestor.files;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestor.files.model.FileDescriptor;
import esa.s1pdgs.cpoc.ingestor.files.services.AuxiliaryFileDescriptorService;
import esa.s1pdgs.cpoc.ingestor.kafka.KafkaConfigFileProducer;
import esa.s1pdgs.cpoc.ingestor.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsService;

public class AuxiliaryFilesProcessorTest {

    /**
     * Amazon S3 service for configuration files
     */
    @Mock
    private ObsService obsService;

    /**
     * KAFKA producer on the topic "metadata"
     */
    @Mock
    private KafkaConfigFileProducer publisher;

    /**
     * Builder of file descriptors
     */
    @Mock
    private AuxiliaryFileDescriptorService extractor;
    
    /**
     * Application status
     */
    @Mock
    private AppStatus appStatus;

    /**
     * Service to test
     */
    private AuxiliaryFilesProcessor service;
    
    /**
     * 
     */
    private String backupDirectory = "/tmp";

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        service = new AuxiliaryFilesProcessor(obsService, publisher, extractor, appStatus, backupDirectory ,backupDirectory);
    }

    /**
     * Test the build of DTO object
     */
    @Test
    public void testBuildDto() {
        FileDescriptor desc = new FileDescriptor();
        desc.setProductName("product-name");
        desc.setKeyObjectStorage("key-obs");
        desc.setChannel(15);
        desc.setProductType(EdrsSessionFileType.RAW);
        desc.setMissionId("mission");
        desc.setSatelliteId("sat");
        ProductDto expected = new ProductDto("product-name", "product-name", ProductFamily.AUXILIARY_FILE);

        assertEquals(expected, service.buildDto(desc));
    }
}
