package esa.s1pdgs.cpoc.ingestor.files;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.ingestor.files.SessionFilesProcessor;
import esa.s1pdgs.cpoc.ingestor.files.model.EdrsSessionFileType;
import esa.s1pdgs.cpoc.ingestor.files.model.FileDescriptor;
import esa.s1pdgs.cpoc.ingestor.files.model.dto.KafkaEdrsSessionDto;
import esa.s1pdgs.cpoc.ingestor.files.services.EdrsSessionFileDescriptorService;
import esa.s1pdgs.cpoc.ingestor.kafka.KafkaSessionProducer;
import esa.s1pdgs.cpoc.ingestor.obs.ObsService;

public class EdrsSessionProcessorTest {

	/**
	 * Amazon S3 service for configuration files
	 */
	@Mock
	private ObsService obsService;

	/**
	 * KAFKA producer on the topic "metadata"
	 */
	@Mock
	private KafkaSessionProducer publisher;

	/**
	 * Builder of file descriptors
	 */
	@Mock
	private EdrsSessionFileDescriptorService extractor;

	/**
	 * Service to test
	 */
	private SessionFilesProcessor service;

	/**
	 * Initialization
	 */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		service = new SessionFilesProcessor(obsService, publisher, extractor);
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
		KafkaEdrsSessionDto expected = new KafkaEdrsSessionDto("key-obs", 15, EdrsSessionFileType.RAW, "mission",
				"sat");

		assertEquals(expected, service.buildDto(desc));
	}
}
