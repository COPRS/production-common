package fr.viveris.s1pdgs.ingestor.services.file;

import java.io.File;

import fr.viveris.s1pdgs.ingestor.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.dto.KafkaMetadataDto;
import fr.viveris.s1pdgs.ingestor.model.exception.MetadataExtractionException;

/**
 * Class to build metadata for configuration and ERDS session files
 * 
 * @author Cyrielle Gailliard
 *
 */
public class MetadataBuilder {
	
	private ExtractMetadata extractor;
	
	public MetadataBuilder() {
		this(new ExtractMetadata());
	}
	
	public MetadataBuilder(ExtractMetadata extractor) {
		this.extractor = extractor;
	}

	/**
	 * Build metadata from configuration files
	 * 
	 * @param descriptor
	 * @param file
	 * @return
	 * @throws MetadataExtractionException
	 */
	public KafkaMetadataDto buildConfigFileMetadata(ConfigFileDescriptor descriptor, File file)
			throws MetadataExtractionException {
		KafkaMetadataDto metadata = new KafkaMetadataDto();
		metadata.setAction("CREATE");
		switch (descriptor.getExtension()) {
		case EOF:
			if (descriptor.getProductType().equals("AUX_RESORB")) {
				metadata.setMetadata(extractor.processEOFFileWithoutNamespace(descriptor, file).toString());
			} else {
				metadata.setMetadata(extractor.processEOFFile(descriptor, file).toString());
			}
			break;
		case SAFE:
			metadata.setMetadata(extractor.processSAFEFile(descriptor, file).toString());
			break;
		case XML:
			metadata.setMetadata(extractor.processXMLFile(descriptor, file).toString());
			break;
		default:
			throw new MetadataExtractionException(descriptor.getProductName(), new Exception("Invalid extension"));
		}
		return metadata;
	}

	/**
	 * Build metadata for ERDS session files
	 * 
	 * @param descriptor
	 * @param file
	 * @return
	 * @throws MetadataExtractionException
	 */
	public KafkaMetadataDto buildErdsSessionFileMetadata(ErdsSessionFileDescriptor descriptor, File file)
			throws MetadataExtractionException {
		KafkaMetadataDto metadata = new KafkaMetadataDto();
		metadata.setAction("CREATE");
		metadata.setMetadata(null);
		switch (descriptor.getProductType()) {
		case RAW:
			metadata.setMetadata(extractor.processRAWFile(descriptor).toString());
			break;
		case SESSION:
			metadata.setMetadata(extractor.processSESSIONFile(descriptor).toString());
			break;
		default:
			throw new MetadataExtractionException(descriptor.getProductName(), new Exception("Invalid extension"));
		}
		return metadata;
	}
}
