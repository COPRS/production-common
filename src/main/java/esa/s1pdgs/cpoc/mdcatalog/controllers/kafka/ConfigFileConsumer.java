/**
 * 
 */
package esa.s1pdgs.cpoc.mdcatalog.controllers.kafka;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.mdcatalog.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.mdcatalog.model.ConfigFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.model.dto.KafkaConfigFileDto;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.ResumeDetails;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.mdcatalog.services.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.services.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdcatalog.services.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdcatalog.services.s3.ObsService;

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
	private static final Logger LOGGER = LogManager.getLogger(ConfigFileConsumer.class);

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
	private final ObsService obsService;

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
	private final String topicName;

	/**
	 * 
	 * @param esServices
	 * @param configFilesS3Services
	 * @param localDirectory
	 * @param extractorConfig
	 * @param manifestFilename
	 * @param fileWithManifestExt
	 * @param topicName
	 */
	@Autowired
	public ConfigFileConsumer(final EsServices esServices, final ObsService obsService,
			@Value("${file.auxiliary-files.local-directory}") final String localDirectory,
			final MetadataExtractorConfig extractorConfig,
			@Value("${file.manifest-filename}") final String manifestFilename,
			@Value("${file.file-with-manifest-ext}") final String fileWithManifestExt,
			@Value("${kafka.topic.auxiliary-files}") final String topicName) {
		this.localDirectory = localDirectory;
		this.fileDescriptorBuilder = new FileDescriptorBuilder(this.localDirectory,
				Pattern.compile(PATTERN_CONFIG, Pattern.CASE_INSENSITIVE));
		this.extractorConfig = extractorConfig;
		this.mdBuilder = new MetadataBuilder(this.extractorConfig);
		this.esServices = esServices;
		this.obsService = obsService;
		this.manifestFilename = manifestFilename;
		this.fileWithManifestExt = fileWithManifestExt;
		this.topicName = topicName;
	}

	/**
	 * Internal constructor
	 * 
	 * @param esServices
	 * @param configFilesS3Services
	 * @param localDirectory
	 * @param extractorConfig
	 * @param fileDescriptorBuilder
	 * @param metadataBuilder
	 * @param manifestFilename
	 * @param fileWithManifestExt
	 * @param topicName
	 */
	protected ConfigFileConsumer(final EsServices esServices, final ObsService obsService, final String localDirectory,
			final MetadataExtractorConfig extractorConfig, final FileDescriptorBuilder fileDescriptorBuilder,
			final MetadataBuilder metadataBuilder, final String manifestFilename, final String fileWithManifestExt,
			final String topicName) {
		this.localDirectory = localDirectory;
		this.fileDescriptorBuilder = fileDescriptorBuilder;
		this.extractorConfig = extractorConfig;
		this.mdBuilder = metadataBuilder;
		this.esServices = esServices;
		this.obsService = obsService;
		this.manifestFilename = manifestFilename;
		this.fileWithManifestExt = fileWithManifestExt;
		this.topicName = topicName;
	}

	/**
	 * Message listener container. Read a message
	 * 
	 * @param payload
	 */
	@KafkaListener(topics = "${kafka.topic.auxiliary-files}", groupId = "${kafka.group-id}")
	public void receive(KafkaConfigFileDto dto) {
		LOGGER.info("[MONITOR] [Step 0] [auxiliary] [productName {}] Starting metadata extraction",
				dto.getProductName());

		File metadataFile = null;
		int step = 0;
		// Create metadata
		try {
			step++;
			// Build key object storage
			String keyObs = dto.getKeyObjectStorage();
			if (dto.getKeyObjectStorage().toLowerCase().endsWith(this.fileWithManifestExt.toLowerCase())) {
				keyObs += "/" + manifestFilename;
			}
			// Upload file
			LOGGER.info("[MONITOR] [step 1] [auxiliary] [productName {}] Downloading file {}", dto.getProductName(),
					keyObs);
			metadataFile = obsService.downloadFile(ProductFamily.AUXILIARY_FILE, keyObs, this.localDirectory);

			// Extract metadata from name
			step++;
			LOGGER.info("[MONITOR] [step 2] [auxiliary] [productName {}] Extracting from filename",
					dto.getProductName());
			ConfigFileDescriptor configFileDescriptor = fileDescriptorBuilder.buildConfigFileDescriptor(metadataFile);

			// Build metadata from file and extracted
			step++;
			LOGGER.info("[MONITOR] [step 3] [auxiliary] [productName {}] Extracting from file", dto.getProductName());
			JSONObject metadata = mdBuilder.buildConfigFileMetadata(configFileDescriptor, metadataFile);

			// Publish metadata
			step++;
			LOGGER.info("[MONITOR] [step 4] [auxiliary] [productName {}] Publishing metadata", dto.getProductName());
			if (!esServices.isMetadataExist(metadata)) {
				esServices.createMetadata(metadata);
			}

		} catch (AbstractCodedException e1) {
			LOGGER.error("[MONITOR] [step {}] [auxiliary] [productName {}] [code {}] [resuming {}] {}", step,
					dto.getProductName(), e1.getCode().getCode(), new ResumeDetails(topicName, dto),
					e1.getLogMessage());
		} catch (Exception e) {
			LOGGER.error("[MONITOR] [step {}] [auxiliary] [productName {}] [code {}] [resuming {}] [msg {}]", step,
					dto.getProductName(), ErrorCode.INTERNAL_ERROR.getCode(), new ResumeDetails(topicName, dto),
					e.getMessage());
		} finally {
			String log = "[MONITOR] [step 5] [auxiliary] [productName " + dto.getProductName()
					+ "] Removing downloaded file";
			this.deleteFile(metadataFile, log);
		}
		LOGGER.info("[MONITOR] [step 0] [auxiliary] [productName {}] End", dto.getProductName());
	}

	protected void deleteFile(File metadataFile, String log) {
		if (metadataFile != null) {
			LOGGER.info(log);
			File parent = metadataFile.getParentFile();
			metadataFile.delete();
			// Remove upper directory if needed
			if (!this.localDirectory.endsWith(parent.getName() + "/")) {
				parent.delete();
			}
		}
	}

}
