package fr.viveris.s1pdgs.ingestor.services.file;

import java.io.File;

import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.ingestor.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.dto.KafkaMetadataDto;

/**
 * Class to build metadata for configuration and ERDS session files
 * 
 * @author Cyrielle Gailliard
 *
 */
@Service
public class MetadataBuilder {

	/**
	 * Build metadata from configuration files
	 * 
	 * @param descriptor
	 * @param file
	 * @return
	 */
	// TODO (throw an exceptionif error)
	public KafkaMetadataDto buildConfigFileMetadata(ConfigFileDescriptor descriptor, File file) {
		String info = String.format(
				"{'productName': %s, 'productClass': %s, 'productType': %s, 'missionId': %s, 'satelliteId': %s, 'keyObjectStorage': %s}",
				descriptor.getProductName(), descriptor.getProductClass(), descriptor.getProductType(), descriptor.getMissionId(), descriptor.getSatelliteId(),
				descriptor.getKeyObjectStorage());
		
		KafkaMetadataDto metadata = new KafkaMetadataDto();
		metadata.setAction("CREATE");
		metadata.setMetadata(info);
		return metadata;
	}

	/**
	 * Build metadata for ERDS session files
	 * 
	 * @param descriptor
	 * @param file
	 * @return
	 */
	// TODO (throw an exceptionif error)
	public KafkaMetadataDto buildErdsSessionFileMetadata(ErdsSessionFileDescriptor descriptor, File file) {
		String info = String.format(
				"{ 'sessionIdentifier': %s, 'productName': %s, 'productType': %s, 'channel': %d, 'missionId': %s, 'satelliteId': %s, 'keyObjectStorage': %s}",
				descriptor.getSessionIdentifier(), descriptor.getProductName(), descriptor.getProductType(),
				descriptor.getChannel(), descriptor.getMissionId(), descriptor.getSatelliteId(),
				descriptor.getKeyObjectStorage());
		KafkaMetadataDto metadata = new KafkaMetadataDto();
		metadata.setAction("CREATE");
		metadata.setMetadata(info);
		return metadata;
	}
}
