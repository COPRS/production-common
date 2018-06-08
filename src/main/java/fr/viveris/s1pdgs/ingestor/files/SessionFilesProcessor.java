package fr.viveris.s1pdgs.ingestor.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.ingestor.files.model.FileDescriptor;
import fr.viveris.s1pdgs.ingestor.files.model.dto.KafkaEdrsSessionDto;
import fr.viveris.s1pdgs.ingestor.files.services.EdrsSessionFileDescriptorService;
import fr.viveris.s1pdgs.ingestor.files.services.ObsServices;
import fr.viveris.s1pdgs.ingestor.kafka.KafkaSessionProducer;

/**
 * 
 */
@Component
public class SessionFilesProcessor extends AbstractFileProcessor<KafkaEdrsSessionDto> {

	/**
	 * 
	 * @param obsService
	 * @param publisher
	 * @param extractor
	 */
	@Autowired
	public SessionFilesProcessor(@Qualifier("sessionFilesS3Services") final ObsServices obsService,
			final KafkaSessionProducer publisher, final EdrsSessionFileDescriptorService extractor) {
		super(obsService, publisher, extractor);
	}

	/**
	 * 
	 */
	@Override
	protected KafkaEdrsSessionDto buildDto(final FileDescriptor descriptor) {
		return new KafkaEdrsSessionDto(descriptor.getKeyObjectStorage(), descriptor.getChannel(),
				descriptor.getProductType(), descriptor.getMissionId(), descriptor.getSatelliteId());
	}

}
