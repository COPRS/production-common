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
import fr.viveris.s1pdgs.mdcatalog.model.L0OutputFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaL0AcnDto;
import fr.viveris.s1pdgs.mdcatalog.model.exception.FilePathException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.IgnoredFileException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.MetadataExtractionException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;
import fr.viveris.s1pdgs.mdcatalog.services.files.FileDescriptorBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.files.MetadataBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.s3.L0AcnsS3Services;

@Service
public class L0AcnsConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(L0AcnsConsumer.class);

	/**
	 * Pattern for L0 output files
	 */
	private static final String PATTERN_L0_OUTPUT = "^([0-9a-z]{2})([0-9a-z]){1}_(S[1-6]|IW|EW|WM|N[1-6]|EN|Z[1-6]|ZE|ZI|ZW|RF|GP|HK)_(RAW)(_)_(0)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV|__)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.SAFE(/.*)?$";

	/**
	 * Amazon S3 service for configuration files
	 */
	private final L0AcnsS3Services l0AcnsS3Services;

	/**
	 * Elasticsearch services
	 */
	private final EsServices esServices;

	/**
	 * Builder of file descriptors
	 */
	private final FileDescriptorBuilder fileDescriptorBuilder;

	/**
	 * Metadata builder
	 */
	private final MetadataBuilder mdBuilder;

	/**
	 * 
	 */
	private final MetadataExtractorConfig extractorConfig;

	/**
	 * Local directory to upload files
	 */
	private final String localDirectory;

	/**
	 * Manifest filename
	 */
	private final String manifestFilename;
	private final String fileWithManifestExt;

	@Autowired
	public L0AcnsConsumer(final EsServices esServices, final L0AcnsS3Services l0AcnsS3Services,
			@Value("${file.l0-acns.local-directory}") final String localDirectory,
			final MetadataExtractorConfig extractorConfig,
			@Value("${file.manifest-filename}") final String manifestFilename,
			@Value("${file.file-with-manifest-ext}") final String fileWithManifestExt) {
		this.localDirectory = localDirectory;
		this.fileDescriptorBuilder = new FileDescriptorBuilder(this.localDirectory,
				Pattern.compile(PATTERN_L0_OUTPUT, Pattern.CASE_INSENSITIVE));
		this.extractorConfig = extractorConfig;
		this.mdBuilder = new MetadataBuilder(this.extractorConfig);
		this.esServices = esServices;
		this.l0AcnsS3Services = l0AcnsS3Services;
		this.manifestFilename = manifestFilename;
		this.fileWithManifestExt = fileWithManifestExt;
	}

	@KafkaListener(topics = "${kafka.topic.l0-acns}", groupId = "${kafka.group-id}", containerFactory = "l0AcnsKafkaListenerContainerFactory")
	public void receive(KafkaL0AcnDto dto) {
		LOGGER.info("[MONITOR] [Step 0] [l0-acn] [productName {}] Starting metadata extraction", dto.getProductName());

		File metadataFile = null;
		try {
			// Build key object storage
			String keyObs = dto.getKeyObjectStorage();
			if (dto.getKeyObjectStorage().toLowerCase().endsWith(this.fileWithManifestExt.toLowerCase())) {
				keyObs += "/" + manifestFilename;
			}
			// Upload file
			if (l0AcnsS3Services.exist(keyObs)) {
				// Upload file
				LOGGER.info("[MONITOR] [Step 1] [l0-acn] [productName {}] Downloading file {}", dto.getProductName(),
						keyObs);
				metadataFile = this.l0AcnsS3Services.getFile(keyObs, this.localDirectory + keyObs);

				// Extract metadata from name
				LOGGER.info("[MONITOR] [Step 2] [l0-acn] [productName {}] Building file descriptor",
						dto.getProductName());
				L0OutputFileDescriptor descriptor = this.fileDescriptorBuilder
						.buildL0OutputFileDescriptor(metadataFile);

				// Build metadata from file and extracted
				LOGGER.info("[MONITOR] [Step 3] [l0-acn] [productName {}] Building metadata", dto.getProductName());
				JSONObject metadata = mdBuilder.buildL0AcnOutputFileMetadata(descriptor, metadataFile);

				// Publish metadata
				LOGGER.info("[MONITOR] [Step 4] [l0-acn] [productName {}] Publishing metadata", dto.getProductName());
				if (!esServices.isMetadataExist(metadata)) {
					esServices.createMetadata(metadata);
				}
			} else {
				throw new FilePathException(dto.getProductName(), keyObs, "No such L0 ACNs in object storage");
			}

		} catch (ObjectStorageException | FilePathException | MetadataExtractionException | IgnoredFileException e1) {
			LOGGER.error("[MONITOR] [l0-acn] [productName {}] {}", dto.getProductName(), e1.getMessage());
		} catch (Exception e) {
			LOGGER.error("[MONITOR] [l0-acn] [productName {}] Exception occurred: {}", dto.getProductName(),
					e.getMessage());
		} finally {
			// Remove file
			if (metadataFile != null) {
				LOGGER.info("[MONITOR] [Step 5] [l0-acn] [productName {}] Removing downloaded file",
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
