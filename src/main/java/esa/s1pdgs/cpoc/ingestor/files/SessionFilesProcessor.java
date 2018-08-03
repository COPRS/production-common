package esa.s1pdgs.cpoc.ingestor.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestor.files.model.FileDescriptor;
import esa.s1pdgs.cpoc.ingestor.files.model.ProductFamily;
import esa.s1pdgs.cpoc.ingestor.files.model.dto.KafkaEdrsSessionDto;
import esa.s1pdgs.cpoc.ingestor.files.services.EdrsSessionFileDescriptorService;
import esa.s1pdgs.cpoc.ingestor.kafka.KafkaSessionProducer;
import esa.s1pdgs.cpoc.ingestor.obs.ObsService;

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
