package fr.viveris.s1pdgs.mdcatalog.controllers.kafka;

import java.io.File;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;

import fr.viveris.s1pdgs.mdcatalog.config.MetadataExtractorConfig;
import fr.viveris.s1pdgs.mdcatalog.model.L1OutputFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaL1SliceDto;
import fr.viveris.s1pdgs.mdcatalog.model.exception.FilePathException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.IgnoredFileException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.MetadataExtractionException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;
import fr.viveris.s1pdgs.mdcatalog.services.files.FileDescriptorBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.files.MetadataBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.s3.L1SlicesS3Services;

/**
 * KAFKA consumer. Consume on a topic defined in L1 slices
 * 
 * @author Olivier Bex-Chauvet
 *
 */
public class L1SlicesConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(L1SlicesConsumer.class);

	/**
	 * Pattern for configuration files to extract data
	 */
	private final static String PATTERN_CONFIG = "^(S1A|S1B|ASA)_(S[1-6]|IW|EW|WM|N[1-6]|EN|IM)_(SLC|GRD|OCN)(F|H|M|_)_(1|2)(A|S)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\\\w{1,}\\\\.SAFE(/.*)?$";

	/**
	 * Elasticsearch services
	 */
	private final EsServices esServices;

	/**
	 * Amazon S3 service for configuration files
	 */
	private final L1SlicesS3Services l1SlicesS3Services;

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

	@Autowired
	public L1SlicesConsumer(final EsServices esServices, final L1SlicesS3Services l1SlicesS3Services,
			@Value("${file.l1-slices.local-directory}") final String localDirectory,
			final MetadataExtractorConfig extractorConfig) {
		this.localDirectory = localDirectory;
		this.fileDescriptorBuilder = new FileDescriptorBuilder(this.localDirectory,
				Pattern.compile(PATTERN_CONFIG, Pattern.CASE_INSENSITIVE));
		this.extractorConfig = extractorConfig;
		this.mdBuilder = new MetadataBuilder(this.extractorConfig);
		this.esServices = esServices;
		this.l1SlicesS3Services = l1SlicesS3Services;
	}

	/**
	 * Message listener container. Read a message
	 * 
	 * @param payload
	 */
	@KafkaListener(topics = "${kafka.topic.l1-slices}", groupId = "${kafka.group-id}")
	public void receive(KafkaL1SliceDto dto) {
		LOGGER.info("[MONITOR] [Step 0] [l1-slice] [productName {}] Starting metadata extraction",
				dto.getProductName());

		File metadataFile = null;
		// Create metadata
		try {
			if (l1SlicesS3Services.exist(dto.getKeyObjectStorage())) {

				// Upload file
				LOGGER.info("[MONITOR] [Step 1] [l1-slice] [productName {}] Downloading file {}", dto.getProductName(),
						dto.getKeyObjectStorage());
				metadataFile = l1SlicesS3Services.getFile(dto.getKeyObjectStorage(),
						this.localDirectory + dto.getKeyObjectStorage());

				// Extract metadata from name
				LOGGER.info("[MONITOR] [Step 2] [l1-slice] [productName {}] Building file descriptor",
						dto.getProductName());
				L1OutputFileDescriptor l1SlicesFileDescriptor = fileDescriptorBuilder
						.buildL1OutputFileDescriptor(metadataFile);

				// Build metadata from file and extracted
				LOGGER.info("[MONITOR] [Step 3] [l1-slice] [productName {}] Building metadata", dto.getProductName());
				JSONObject metadata = mdBuilder.buildL1SliceOutputFileMetadata(l1SlicesFileDescriptor, metadataFile);

				// Publish metadata
				LOGGER.info("[MONITOR] [Step 4] [l1-slice] [productName {}] Publishing metadata", dto.getProductName());
				if (!esServices.isMetadataExist(metadata)) {
					esServices.createMetadata(metadata);
				}

			} else {
				throw new FilePathException(dto.getProductName(), dto.getKeyObjectStorage(),
						"No such L1 Slices in object storage");
			}
		} catch (ObjectStorageException | FilePathException | MetadataExtractionException | IgnoredFileException e1) {
			LOGGER.error("[MONITOR] [l1-slice] [productName {}] {}", dto.getProductName(), e1.getMessage());
		} catch (Exception e) {
			LOGGER.error("[MONITOR] [l1-slice] [productName {}] Exception occurred: {}", dto.getProductName(),
					e.getMessage());
		} finally {
			// Remove file
			if (metadataFile != null) {
				LOGGER.info("[MONITOR] [Step 5] [l1-slice] [productName {}] Removing downloaded file",
						dto.getProductName());
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
