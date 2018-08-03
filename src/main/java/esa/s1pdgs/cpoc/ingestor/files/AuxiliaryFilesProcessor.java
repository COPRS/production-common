package esa.s1pdgs.cpoc.ingestor.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestor.files.model.FileDescriptor;
import esa.s1pdgs.cpoc.ingestor.files.model.ProductFamily;
import esa.s1pdgs.cpoc.ingestor.files.model.dto.KafkaConfigFileDto;
import esa.s1pdgs.cpoc.ingestor.files.services.AuxiliaryFileDescriptorService;
import esa.s1pdgs.cpoc.ingestor.kafka.KafkaConfigFileProducer;
import esa.s1pdgs.cpoc.ingestor.obs.ObsService;

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
	public AuxiliaryFilesProcessor(final ObsService obsService, final KafkaConfigFileProducer publisher,
			final AuxiliaryFileDescriptorService extractor) {
		super(obsService, publisher, extractor, ProductFamily.AUXILIARY_FILE);
	}

	/**
	 * 
	 */
	@Override
	protected KafkaConfigFileDto buildDto(final FileDescriptor descriptor) {
		return new KafkaConfigFileDto(descriptor.getProductName(), descriptor.getProductName());
	}

}
