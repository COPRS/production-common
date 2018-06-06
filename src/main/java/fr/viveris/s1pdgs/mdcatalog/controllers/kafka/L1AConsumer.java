package fr.viveris.s1pdgs.mdcatalog.controllers.kafka;

import java.io.File;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.mdcatalog.config.MetadataExtractorConfig;
import fr.viveris.s1pdgs.mdcatalog.model.L1OutputFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaL1ADto;
import fr.viveris.s1pdgs.mdcatalog.model.exception.FilePathException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.IgnoredFileException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.MetadataExtractionException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;
import fr.viveris.s1pdgs.mdcatalog.services.files.FileDescriptorBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.files.MetadataBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.s3.L1AS3Services;

/**
 * KAFKA consumer. Consume on a topic defined in L1 Annotaions
 * 
 * @author Olivier Bex-Chauvet
 *
 */
@Service
public class L1AConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(L1AConsumer.class);

	/**
	 * Pattern for configuration files to extract data
	 */
	private final static String PATTERN_CONFIG = "^(S1A|S1B|ASA)_(S[1-6]|IW|EW|WM|N[1-6]|EN|IM)_(SLC|GRD|OCN)(F|H|M|_)_(1|2)(A|S)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$";

	/**
	 * Elasticsearch services
	 */
	private final EsServices esServices;

	/**
	 * Amazon S3 service for configuration files
	 */
	private final L1AS3Services l1AS3Services;

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
	public L1AConsumer(final EsServices esServices, final L1AS3Services l1AS3Services,
			@Value("${file.l1-acns.local-directory}") final String localDirectory,
			final MetadataExtractorConfig extractorConfig,
			@Value("${file.manifest-filename}") final String manifestFilename,
			@Value("${file.file-with-manifest-ext}") final String fileWithManifestExt) {
		this.localDirectory = localDirectory;
		this.fileDescriptorBuilder = new FileDescriptorBuilder(this.localDirectory,
				Pattern.compile(PATTERN_CONFIG, Pattern.CASE_INSENSITIVE));
		this.extractorConfig = extractorConfig;
		this.mdBuilder = new MetadataBuilder(this.extractorConfig);
		this.esServices = esServices;
		this.l1AS3Services = l1AS3Services;
		this.manifestFilename = manifestFilename;
		this.fileWithManifestExt = fileWithManifestExt;
	}

	/**
	 * Message listener container. Read a message
	 * 
	 * @param payload
	 */
	@KafkaListener(topics = "${kafka.topic.l1-acns}", groupId = "${kafka.group-id}", containerFactory = "l1AKafkaListenerContainerFactory")
	public void receive(KafkaL1ADto dto) {
		LOGGER.info("[MONITOR] [Step 0] [l1-acn] [productName {}] Starting metadata extraction", dto.getProductName());

		File metadataFile = null;
		// Create metadata
		try {
			// Build key object storage
			String keyObs = dto.getKeyObjectStorage();
			if (dto.getKeyObjectStorage().toLowerCase().endsWith(this.fileWithManifestExt.toLowerCase())) {
				keyObs += "/" + manifestFilename;
			}
			// Upload file
			if (l1AS3Services.exist(keyObs)) {

				// Upload file
				LOGGER.info("[MONITOR] [Step 1] [l1-acn] [productName {}] Downloading file {}", dto.getProductName(),
						keyObs);
				metadataFile = l1AS3Services.getFile(keyObs, this.localDirectory + keyObs);

				// Extract metadata from name
				LOGGER.info("[MONITOR] [Step 2] [l1-acn] [productName {}] Building file descriptor",
						dto.getProductName());
				L1OutputFileDescriptor l1AFileDescriptor = fileDescriptorBuilder
						.buildL1OutputFileDescriptor(metadataFile);

				// Build metadata from file and extracted
				LOGGER.info("[MONITOR] [Step 3] [l1-acn] [productName {}] Building metadata", dto.getProductName());
				JSONObject metadata = mdBuilder.buildL1SliceOutputFileMetadata(l1AFileDescriptor, metadataFile);

				// Publish metadata
				LOGGER.info("[MONITOR] [Step 4] [l1-acn] [productName {}] Publishing metadata", dto.getProductName());
				if (!esServices.isMetadataExist(metadata)) {
					esServices.createMetadata(metadata);
				}

			} else {
				throw new FilePathException(dto.getProductName(), dto.getKeyObjectStorage(),
						"No such L1 As in object storage");
			}
		} catch (ObjectStorageException | FilePathException | MetadataExtractionException | IgnoredFileException e1) {
			LOGGER.error("[MONITOR] [l1-acn] [productName {}] {}", dto.getProductName(), e1.getMessage());
		} catch (Exception e) {
			LOGGER.error("[MONITOR] [l1-acn] [productName {}] Exception occurred: {}", dto.getProductName(),
					e.getMessage());
		} finally {
			// Remove file
			if (metadataFile != null) {
				LOGGER.info("[MONITOR] [Step 5] [l1-acn] [productName {}] Removing downloaded file",
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
