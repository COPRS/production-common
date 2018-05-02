/**
 * 
 */
package fr.viveris.s1pdgs.mdcatalog.controllers.kafka;

import java.io.File;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.mdcatalog.config.MetadataExtractorConfig;
import fr.viveris.s1pdgs.mdcatalog.model.EdrsSessionFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaEdrsSessionDto;
import fr.viveris.s1pdgs.mdcatalog.model.exception.FilePathException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.IgnoredFileException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.MetadataExtractionException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;
import fr.viveris.s1pdgs.mdcatalog.services.files.FileDescriptorBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.files.MetadataBuilder;

/**
 * KAFKA consumer. Consume on a topic defined in configuration file
 * 
 * @author Olivier Bex-Chauvet
 *
 */
@Service
public class EdrsSessionFileConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(EdrsSessionFileConsumer.class);

	/**
	 * Pattern for ERDS session files to extract data
	 */
	private final static String PATTERN_SESSION = "^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$";

	/**
	 * Elasticsearch services
	 */
	private final EsServices esServices;

	/**
	 * Metadata builder
	 */
	private final MetadataBuilder mdBuilder;

	/**
	 * 
	 */
	private final MetadataExtractorConfig extractorConfig;

	/**
	 * Local directory for sessions files
	 */
	private final String localDirectory;

	/**
	 * Builder of file descriptors
	 */
	private final FileDescriptorBuilder fileDescriptorBuilder;

	public EdrsSessionFileConsumer(final EsServices esServices,
			@Value("${file.session-files.local-directory}") final String localDirectory,
			final MetadataExtractorConfig extractorConfig) {
		this.localDirectory = localDirectory;
		this.fileDescriptorBuilder = new FileDescriptorBuilder(this.localDirectory,
				Pattern.compile(PATTERN_SESSION, Pattern.CASE_INSENSITIVE));
		this.extractorConfig = extractorConfig;
		this.mdBuilder = new MetadataBuilder(this.extractorConfig);
		this.esServices = esServices;
	}

	/**
	 * Message listener container. Read a message
	 * 
	 * @param payload
	 */
	@KafkaListener(topics = "${kafka.topic.edrs-sessions}", groupId = "${kafka.group-id}", containerFactory = "edrsSessionsKafkaListenerContainerFactory")
	public void receive(KafkaEdrsSessionDto dto) {
		LOGGER.info("[MONITOR] [Step 0] [session] [obs {}] Starting metadata extraction", dto.getObjectStorageKey());

		try {
			// Extract metadata from name
			LOGGER.info("[MONITOR] [Step 2] [session] [obs {}] Extracting from filename", dto.getObjectStorageKey());
			EdrsSessionFileDescriptor edrsFileDescriptor = fileDescriptorBuilder
					.buildEdrsSessionFileDescriptor(new File(this.localDirectory + dto.getObjectStorageKey()));

			// Build metadata from file and extracted
			LOGGER.info("[MONITOR] [Step 3] [session] [obs {}] Extracting from file", dto.getObjectStorageKey());
			JSONObject metadata = mdBuilder.buildEdrsSessionFileMetadata(edrsFileDescriptor);

			// Publish metadata
			LOGGER.info("[MONITOR] [Step 4] [session] [obs {}] Publishing metadata", dto.getObjectStorageKey());
			if (!esServices.isMetadataExist(metadata)) {
				esServices.createMetadata(metadata);
			}

		} catch (ObjectStorageException | FilePathException | MetadataExtractionException | IgnoredFileException e1) {
			LOGGER.error("[MONITOR] [session] [obs {}] {}", dto.getObjectStorageKey(), e1.getMessage());
		} catch (Exception e) {
			LOGGER.error("[MONITOR] [session] [obs {}] Exception occurred: {}", dto.getObjectStorageKey(),
					e.getMessage());
		}
	}
}
