package fr.viveris.s1pdgs.ingestor.files;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.ingestor.files.model.EdrsSessionFileType;
import fr.viveris.s1pdgs.ingestor.files.model.FileDescriptor;
import fr.viveris.s1pdgs.ingestor.files.model.dto.KafkaEdrsSessionDto;
import fr.viveris.s1pdgs.ingestor.files.services.EdrsSessionFileDescriptorService;
import fr.viveris.s1pdgs.ingestor.files.services.ObsServices;
import fr.viveris.s1pdgs.ingestor.kafka.KafkaSessionProducer;

public class EdrsSessionProcessorTest {

	/**
	 * Amazon S3 service for configuration files
	 */
	@Mock
	private ObsServices obsService;

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
