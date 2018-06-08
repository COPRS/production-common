package fr.viveris.s1pdgs.ingestor.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.ingestor.files.model.FileDescriptor;
import fr.viveris.s1pdgs.ingestor.files.model.dto.KafkaConfigFileDto;
import fr.viveris.s1pdgs.ingestor.files.services.AuxiliaryFileDescriptorService;
import fr.viveris.s1pdgs.ingestor.files.services.ObsServices;
import fr.viveris.s1pdgs.ingestor.kafka.KafkaConfigFileProducer;

/**
 * 
 */
@Component
public class AuxiliaryFilesProcessor extends AbstractFileProcessor<KafkaConfigFileDto> {

	/**
	 * 
	 * @param obsService
	 * @param publisher
	 * @param extractor
	 */
	@Autowired
	public AuxiliaryFilesProcessor(@Qualifier("configFilesS3Services") final ObsServices obsService,
			final KafkaConfigFileProducer publisher, final AuxiliaryFileDescriptorService extractor) {
		super(obsService, publisher, extractor);
	}

	/**
	 * 
	 */
	@Override
	protected KafkaConfigFileDto buildDto(final FileDescriptor descriptor) {
		return new KafkaConfigFileDto(descriptor.getProductName(), descriptor.getProductName());
	}

}
