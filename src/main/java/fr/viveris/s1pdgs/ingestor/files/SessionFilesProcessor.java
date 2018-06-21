package fr.viveris.s1pdgs.ingestor.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.ingestor.files.model.FileDescriptor;
import fr.viveris.s1pdgs.ingestor.files.model.ProductFamily;
import fr.viveris.s1pdgs.ingestor.files.model.dto.KafkaEdrsSessionDto;
import fr.viveris.s1pdgs.ingestor.files.services.EdrsSessionFileDescriptorService;
import fr.viveris.s1pdgs.ingestor.kafka.KafkaSessionProducer;
import fr.viveris.s1pdgs.ingestor.obs.ObsService;

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
	public SessionFilesProcessor(final ObsService obsService, final KafkaSessionProducer publisher,
			final EdrsSessionFileDescriptorService extractor) {
		super(obsService, publisher, extractor, ProductFamily.EDRS_SESSION);
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
