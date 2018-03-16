/**
 * 
 */
package fr.viveris.s1pdgs.mdcatalog.controllers.kafka;

import java.io.File;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.mdcatalog.config.MetadataExtractorConfig;
import fr.viveris.s1pdgs.mdcatalog.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaConfigFileDto;
import fr.viveris.s1pdgs.mdcatalog.model.exception.FilePathException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.IgnoredFileException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.MetadataExtractionException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;
import fr.viveris.s1pdgs.mdcatalog.services.files.FileDescriptorBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.files.MetadataBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.s3.ConfigFilesS3Services;

/**
 * KAFKA consumer. Consume on a topic defined in configuration file
 * 
 * @author Olivier Bex-Chauvet
 *
 */
@Service
public class ConfigFileConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFileConsumer.class);

	/**
	 * Pattern for configuration files to extract data
	 */
	private final static String PATTERN_CONFIG = "^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$";

	/**
	 * Elasticsearch services
	 */
	private final EsServices esServices;

	/**
	 * Amazon S3 service for configuration files
	 */
	private final ConfigFilesS3Services configFilesS3Services;

	/**
	 * Metadata builder
	 */
	private final MetadataBuilder mdBuilder;

	/**
	 * Local directory for configurations files
	 */
	private final String localDirectory;

	/**
	 * 
	 */
	private final MetadataExtractorConfig extractorConfig;

	/**
	 * Builder of file descriptors
	 */
	private final FileDescriptorBuilder fileDescriptorBuilder;

	/**
	 * Manifest filename
	 */
	private final String manifestFilename;
	private final String fileWithManifestExt;

	@Autowired
	public ConfigFileConsumer(final EsServices esServices, final ConfigFilesS3Services configFilesS3Services,
			@Value("${file.auxiliary-files.local-directory}") final String localDirectory,
			final MetadataExtractorConfig extractorConfig,
			@Value("${file.manifest-filename}") final String manifestFilename,
			@Value("${file.file-with-manifest-ext}") final String fileWithManifestExt) {
		this.localDirectory = localDirectory;
		this.fileDescriptorBuilder = new FileDescriptorBuilder(this.localDirectory,
				Pattern.compile(PATTERN_CONFIG, Pattern.CASE_INSENSITIVE));
		this.extractorConfig = extractorConfig;
		this.mdBuilder = new MetadataBuilder(this.extractorConfig);
		this.esServices = esServices;
		this.configFilesS3Services = configFilesS3Services;
		this.manifestFilename = manifestFilename;
		this.fileWithManifestExt = fileWithManifestExt;
	}

	/**
	 * Message listener container. Read a message
	 * 
	 * @param payload
	 */
	@KafkaListener(topics = "${kafka.topic.auxiliary-files}", groupId = "${kafka.group-id}")
	public void receive(KafkaConfigFileDto dto) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[receive] Consume message {}", dto);
		}
		File metadataFile = null;
		// Create metadata
		try {
			// Build key object storage
			String keyObs = dto.getKeyObjectStorage();
			if (dto.getKeyObjectStorage().toLowerCase().endsWith(this.fileWithManifestExt.toLowerCase())) {
				keyObs += "/" + manifestFilename;
			}
			if (configFilesS3Services.exist(keyObs)) {

				// Upload file
				metadataFile = configFilesS3Services.getFile(keyObs, this.localDirectory + keyObs);

				// Extract metadata from name
				ConfigFileDescriptor configFileDescriptor = fileDescriptorBuilder
						.buildConfigFileDescriptor(metadataFile);

				// Build metadata from file and extracted
				JSONObject metadata = mdBuilder.buildConfigFileMetadata(configFileDescriptor, metadataFile);

				// Publish metadata
				if (!esServices.isMetadataExist(metadata)) {
					esServices.createMetadata(metadata);
				}
				LOGGER.info("[productName {}] Metadata created", dto.getProductName());
			} else {
				throw new FilePathException(dto.getProductName(), dto.getKeyObjectStorage(),
						"No such L0 ACNs in object storage");
			}
		} catch (ObjectStorageException | FilePathException | MetadataExtractionException | IgnoredFileException e1) {
			LOGGER.error("[productName {}] {}", dto.getProductName(), e1.getMessage());
		} catch (Exception e) {
			LOGGER.error("[productName {}] Exception occurred: {}", dto.getProductName(), e.getMessage());
		} finally {
			// Remove file
			if (metadataFile != null) {
				File parent = metadataFile.getParentFile();
				metadataFile.delete();
				// Remove upper directory if needed
				if (!this.localDirectory.endsWith(parent.getName() + "/")) {
					parent.delete();
				}
			}
		}
	}

}
