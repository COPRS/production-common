package esa.s1pdgs.cpoc.ingestor.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestor.files.model.FileDescriptor;
import esa.s1pdgs.cpoc.ingestor.files.services.EdrsSessionFileDescriptorService;
import esa.s1pdgs.cpoc.ingestor.kafka.KafkaSessionProducer;
import esa.s1pdgs.cpoc.ingestor.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

/**
 * 
 */
@Component
public class SessionFilesProcessor extends AbstractFileProcessor<EdrsSessionDto> {

	/**
	 * 
	 * @param obsClient
	 * @param publisher
	 * @param extractor
	 */
	@Autowired
	public SessionFilesProcessor(final ObsClient obsClient, final KafkaSessionProducer publisher,
			final EdrsSessionFileDescriptorService extractor,final AppStatus appStatus,
			@Value("${file.session-files.local-directory}") final String pickupDirectory,
			@Value("${file.backup-directory}") final String backupDirectory) {
		super(obsClient, publisher, extractor, ProductFamily.EDRS_SESSION, appStatus, pickupDirectory, backupDirectory);
	}

	/**
	 * 
	 */
	@Override
	protected EdrsSessionDto buildDto(final FileDescriptor descriptor) {
		return new EdrsSessionDto(descriptor.getKeyObjectStorage(), descriptor.getChannel(),
				descriptor.getProductType(), descriptor.getMissionId(), descriptor.getSatelliteId(), descriptor.getStationCode(), descriptor.getSessionId());
	}

}
