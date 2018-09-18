package esa.s1pdgs.cpoc.ingestor.files;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.ingestor.files.model.FileDescriptor;
import esa.s1pdgs.cpoc.ingestor.files.services.AuxiliaryFileDescriptorService;
import esa.s1pdgs.cpoc.ingestor.kafka.KafkaConfigFileProducer;
import esa.s1pdgs.cpoc.ingestor.obs.ObsService;
import esa.s1pdgs.cpoc.ingestor.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;

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
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        service = new AuxiliaryFilesProcessor(obsService, publisher, extractor, appStatus);
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
        AuxiliaryFileDto expected =
                new AuxiliaryFileDto("product-name", "product-name");

        assertEquals(expected, service.buildDto(desc));
    }
}
